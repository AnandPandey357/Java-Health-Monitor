package monitor;

import utils.ReportGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * MetricsCollector is the main orchestrator class that coordinates all monitoring activities
 * It combines JVM monitoring, health checking, and report generation into a unified system
 */
public class MetricsCollector {
    
    private static final Logger logger = LoggerFactory.getLogger(MetricsCollector.class);
    
    // Component instances for different types of monitoring
    private final JVMMonitor jvmMonitor;           // Monitors JVM metrics
    private final HealthChecker healthChecker;     // Monitors external service health
    private final ReportGenerator reportGenerator; // Generates reports
    
    // Scheduled executor for periodic monitoring
    private final ScheduledExecutorService scheduler;
    
    // Configuration
    private final List<HealthChecker.ServiceEndpoint> monitoredServices;
    private final int monitoringIntervalSeconds;
    
    // Data storage for historical metrics
    private final List<Map<String, Object>> historicalData;
    private final int maxHistorySize;
    
    /**
     * Constructor initializes all monitoring components
     * @param monitoringIntervalSeconds How often to collect metrics (in seconds)
     * @param maxHistorySize Maximum number of historical records to keep
     */
    public MetricsCollector(int monitoringIntervalSeconds, int maxHistorySize) {
        this.monitoringIntervalSeconds = monitoringIntervalSeconds;
        this.maxHistorySize = maxHistorySize;
        
        // Initialize monitoring components
        this.jvmMonitor = new JVMMonitor();
        this.healthChecker = new HealthChecker();
        this.reportGenerator = new ReportGenerator();
        
        // Initialize scheduler with a single thread (enough for our periodic tasks)
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "MetricsCollector-Scheduler");
            t.setDaemon(true);  // Don't prevent JVM shutdown
            return t;
        });
        
        // Initialize data structures
        this.monitoredServices = new ArrayList<>();
        this.historicalData = new ArrayList<>();
        
        logger.info("MetricsCollector initialized with {}s interval, max history: {}", 
            monitoringIntervalSeconds, maxHistorySize);
    }
    
    /**
     * Default constructor with sensible defaults
     * 30-second monitoring interval, keeping last 100 records
     */
    public MetricsCollector() {
        this(30, 100);  // Default: 30 seconds interval, 100 records history
    }
    
    /**
     * Adds a service endpoint to be monitored
     * @param name Friendly name for the service
     * @param url URL to monitor
     */
    public void addServiceToMonitor(String name, String url) {
        monitoredServices.add(new HealthChecker.ServiceEndpoint(name, url));
        logger.info("Added service to monitor: {} -> {}", name, url);
    }
    
    /**
     * Adds a service endpoint with custom expected status code
     * @param name Friendly name for the service
     * @param url URL to monitor
     * @param expectedStatus Expected HTTP status code (e.g., 200, 201, 204)
     */
    public void addServiceToMonitor(String name, String url, int expectedStatus) {
        monitoredServices.add(new HealthChecker.ServiceEndpoint(name, url, expectedStatus));
        logger.info("Added service to monitor: {} -> {} (expecting status {})", name, url, expectedStatus);
    }
    
    /**
     * Collects all metrics from JVM and external services
     * This is the main data collection method
     * @return Map containing all collected metrics
     */
    public Map<String, Object> collectAllMetrics() {
        long startTime = System.currentTimeMillis();
        Map<String, Object> allMetrics = new HashMap<>();
        
        try {
            // Add timestamp for this collection cycle
            allMetrics.put("collection_timestamp", startTime);
            allMetrics.put("collection_time", new java.util.Date(startTime).toString());
            
            // Collect JVM metrics
            logger.debug("Collecting JVM metrics...");
            Map<String, Object> jvmMetrics = jvmMonitor.collectMetrics();
            allMetrics.put("jvm_metrics", jvmMetrics);
            
            // Collect health check metrics if services are configured
            if (!monitoredServices.isEmpty()) {
                logger.debug("Collecting health check metrics for {} services...", monitoredServices.size());
                List<HealthChecker.HealthCheckResult> healthResults = 
                    healthChecker.checkMultipleServices(monitoredServices);
                
                // Convert health results to maps for JSON serialization
                List<Map<String, Object>> healthMaps = new ArrayList<>();
                for (HealthChecker.HealthCheckResult result : healthResults) {
                    healthMaps.add(result.toMap());
                }
                allMetrics.put("health_checks", healthMaps);
                
                // Add health summary
                Map<String, Object> healthSummary = healthChecker.createHealthSummary(healthResults);
                allMetrics.put("health_summary", healthSummary);
            } else {
                allMetrics.put("health_checks", new ArrayList<>());
                allMetrics.put("health_summary", createEmptyHealthSummary());
            }
            
            // Add collection performance metrics
            long collectionTime = System.currentTimeMillis() - startTime;
            allMetrics.put("collection_time_ms", collectionTime);
            allMetrics.put("monitor_status", "active");
            
            logger.debug("Metrics collection completed in {}ms", collectionTime);
            
        } catch (Exception e) {
            logger.error("Error during metrics collection", e);
            allMetrics.put("monitor_status", "error");
            allMetrics.put("error_message", e.getMessage());
        }
        
        return allMetrics;
    }
    
    /**
     * Creates an empty health summary when no services are monitored
     * @return Empty health summary map
     */
    private Map<String, Object> createEmptyHealthSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("total_services", 0);
        summary.put("healthy_services", 0);
        summary.put("unhealthy_services", 0);
        summary.put("health_percentage", 100.0);
        summary.put("overall_status", "NO_SERVICES_CONFIGURED");
        return summary;
    }
    
    /**
     * Starts continuous monitoring with the configured interval
     * Metrics are collected periodically and stored in memory
     */
    public void startContinuousMonitoring() {
        logger.info("Starting continuous monitoring every {} seconds", monitoringIntervalSeconds);
        
        // Schedule the monitoring task to run at fixed intervals
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Collect current metrics
                Map<String, Object> metrics = collectAllMetrics();
                
                // Add to historical data
                addToHistory(metrics);
                
                // Log basic health status
                logCurrentStatus(metrics);
                
            } catch (Exception e) {
                logger.error("Error in monitoring cycle", e);
            }
        }, 0, monitoringIntervalSeconds, TimeUnit.SECONDS);  // Start immediately, then repeat
        
        logger.info("Continuous monitoring started");
    }
    
    /**
     * Adds metrics to historical data, maintaining size limit
     * @param metrics Current metrics to add to history
     */
    private void addToHistory(Map<String, Object> metrics) {
        synchronized (historicalData) {
            historicalData.add(metrics);
            
            // Remove oldest entries if we exceed max size
            while (historicalData.size() > maxHistorySize) {
                historicalData.remove(0);
            }
        }
        
        logger.debug("Added metrics to history. Current history size: {}", historicalData.size());
    }
    
    /**
     * Logs current system status for monitoring
     * @param metrics Current metrics map
     */
    @SuppressWarnings("unchecked")
    private void logCurrentStatus(Map<String, Object> metrics) {
        try {
            // Log JVM memory status
            Map<String, Object> jvmMetrics = (Map<String, Object>) metrics.get("jvm_metrics");
            if (jvmMetrics != null) {
                double heapUsage = (Double) jvmMetrics.get("heap_usage_percentage");
                logger.info("JVM Heap Usage: {:.1f}%", heapUsage);
            }
            
            // Log health check status
            Map<String, Object> healthSummary = (Map<String, Object>) metrics.get("health_summary");
            if (healthSummary != null) {
                String overallStatus = (String) healthSummary.get("overall_status");
                int totalServices = (Integer) healthSummary.get("total_services");
                int healthyServices = (Integer) healthSummary.get("healthy_services");
                
                if (totalServices > 0) {
                    logger.info("Health Status: {} ({}/{} services healthy)", 
                        overallStatus, healthyServices, totalServices);
                }
            }
            
        } catch (Exception e) {
            logger.warn("Error logging current status", e);
        }
    }
    
    /**
     * Generates and saves a comprehensive report of current and historical metrics
     * @param outputPath Path where the report should be saved
     * @throws IOException If report generation fails
     */
    public void generateReport(String outputPath) throws IOException {
        logger.info("Generating comprehensive report...");
        
        // Collect current metrics
        Map<String, Object> currentMetrics = collectAllMetrics();
        
        // Create report data structure
        Map<String, Object> reportData = new HashMap<>();
        reportData.put("report_generated_at", System.currentTimeMillis());
        reportData.put("report_generated_time", new java.util.Date().toString());
        reportData.put("monitoring_interval_seconds", monitoringIntervalSeconds);
        reportData.put("current_metrics", currentMetrics);
        
        // Add historical data
        synchronized (historicalData) {
            reportData.put("historical_data", new ArrayList<>(historicalData));
            reportData.put("historical_data_count", historicalData.size());
        }
        
        // Add summary statistics
        reportData.put("summary_statistics", calculateSummaryStatistics());
        
        // Generate the report
        reportGenerator.generateJsonReport(reportData, outputPath);
        logger.info("Report generated successfully: {}", outputPath);
    }
    
    /**
     * Calculates summary statistics from historical data
     * @return Map containing statistical summaries
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> calculateSummaryStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        synchronized (historicalData) {
            if (historicalData.isEmpty()) {
                stats.put("no_data", true);
                return stats;
            }
            
            // Calculate JVM memory statistics
            List<Double> heapUsages = new ArrayList<>();
            List<Long> responseTimes = new ArrayList<>();
            int totalHealthChecks = 0;
            int successfulHealthChecks = 0;
            
            for (Map<String, Object> record : historicalData) {
                // Extract heap usage
                Map<String, Object> jvmMetrics = (Map<String, Object>) record.get("jvm_metrics");
                if (jvmMetrics != null && jvmMetrics.containsKey("heap_usage_percentage")) {
                    heapUsages.add((Double) jvmMetrics.get("heap_usage_percentage"));
                }
                
                // Extract health check data
                Map<String, Object> healthSummary = (Map<String, Object>) record.get("health_summary");
                if (healthSummary != null) {
                    Integer total = (Integer) healthSummary.get("total_services");
                    Integer healthy = (Integer) healthSummary.get("healthy_services");
                    if (total != null && healthy != null) {
                        totalHealthChecks += total;
                        successfulHealthChecks += healthy;
                    }
                }
            }
            
            // Calculate heap usage statistics
            if (!heapUsages.isEmpty()) {
                double avgHeapUsage = heapUsages.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
                double maxHeapUsage = heapUsages.stream().mapToDouble(Double::doubleValue).max().orElse(0.0);
                double minHeapUsage = heapUsages.stream().mapToDouble(Double::doubleValue).min().orElse(0.0);
                
                Map<String, Object> heapStats = new HashMap<>();
                heapStats.put("average_heap_usage_percentage", avgHeapUsage);
                heapStats.put("max_heap_usage_percentage", maxHeapUsage);
                heapStats.put("min_heap_usage_percentage", minHeapUsage);
                heapStats.put("sample_count", heapUsages.size());
                
                stats.put("heap_usage_statistics", heapStats);
            }
            
            // Calculate health check statistics
            if (totalHealthChecks > 0) {
                double healthSuccessRate = (double) successfulHealthChecks / totalHealthChecks * 100;
                
                Map<String, Object> healthStats = new HashMap<>();
                healthStats.put("total_health_checks", totalHealthChecks);
                healthStats.put("successful_health_checks", successfulHealthChecks);
                healthStats.put("health_success_rate_percentage", healthSuccessRate);
                
                stats.put("health_check_statistics", healthStats);
            }
            
            stats.put("data_points_analyzed", historicalData.size());
            stats.put("time_range_minutes", historicalData.size() * monitoringIntervalSeconds / 60.0);
        }
        
        return stats;
    }
    
    /**
     * Stops continuous monitoring and cleans up resources
     */
    public void stopMonitoring() {
        logger.info("Stopping monitoring...");
        
        try {
            scheduler.shutdown();
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            
            healthChecker.shutdown();
            
            logger.info("Monitoring stopped successfully");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            logger.warn("Monitoring shutdown interrupted", e);
        }
    }
    
    /**
     * Displays current metrics in a formatted way
     */
    public void displayCurrentMetrics() {
        Map<String, Object> metrics = collectAllMetrics();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("JAVA APPLICATION HEALTH MONITOR - CURRENT STATUS");
        System.out.println("=".repeat(60));
        System.out.println("Timestamp: " + metrics.get("collection_time"));
        System.out.println();
        
        // Display JVM metrics
        jvmMonitor.printMetrics();
        
        // Display health check results if available
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> healthChecks = (List<Map<String, Object>>) metrics.get("health_checks");
        if (healthChecks != null && !healthChecks.isEmpty()) {
            System.out.println("\n=== EXTERNAL SERVICE HEALTH ===");
            for (Map<String, Object> check : healthChecks) {
                String serviceName = (String) check.get("service_name");
                Boolean isHealthy = (Boolean) check.get("is_healthy");
                Long responseTime = (Long) check.get("response_time_ms");
                String status = Boolean.TRUE.equals(isHealthy) ? "✓ HEALTHY" : "✗ UNHEALTHY";
                
                System.out.printf("%-20s: %s (%d ms)%n", serviceName, status, responseTime);
            }
        }
        
        System.out.println("=".repeat(60));
    }
    
    /**
     * Interactive command-line interface for the monitoring system
     */
    public void runInteractiveMode() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        
        System.out.println("Java Application Health Monitor - Interactive Mode");
        System.out.println("Type 'help' for available commands");
        
        while (running) {
            System.out.print("\nmonitor> ");
            String input = scanner.nextLine().trim().toLowerCase();
            
            switch (input) {
                case "help":
                    printHelp();
                    break;
                case "status":
                    displayCurrentMetrics();
                    break;
                case "start":
                    startContinuousMonitoring();
                    System.out.println("Continuous monitoring started");
                    break;
                case "stop":
                    stopMonitoring();
                    System.out.println("Monitoring stopped");
                    break;
                case "report":
                    try {
                        String reportPath = "health-monitor-report-" + System.currentTimeMillis() + ".json";
                        generateReport(reportPath);
                        System.out.println("Report generated: " + reportPath);
                    } catch (IOException e) {
                        System.err.println("Error generating report: " + e.getMessage());
                    }
                    break;
                case "services":
                    listMonitoredServices();
                    break;
                case "add":
                    addServiceInteractive(scanner);
                    break;
                case "exit":
                case "quit":
                    running = false;
                    break;
                default:
                    System.out.println("Unknown command: " + input + ". Type 'help' for available commands.");
            }
        }
        
        stopMonitoring();
        scanner.close();
    }
    
    /**
     * Prints help information for interactive mode
     */
    private void printHelp() {
        System.out.println("\nAvailable commands:");
        System.out.println("  help     - Show this help message");
        System.out.println("  status   - Display current metrics");
        System.out.println("  start    - Start continuous monitoring");
        System.out.println("  stop     - Stop continuous monitoring");
        System.out.println("  report   - Generate JSON report");
        System.out.println("  services - List monitored services");
        System.out.println("  add      - Add a service to monitor");
        System.out.println("  exit     - Exit the application");
    }
    
    /**
     * Lists currently monitored services
     */
    private void listMonitoredServices() {
        if (monitoredServices.isEmpty()) {
            System.out.println("No services are currently being monitored.");
            System.out.println("Use 'add' command to add services.");
        } else {
            System.out.println("\nMonitored Services:");
            for (int i = 0; i < monitoredServices.size(); i++) {
                HealthChecker.ServiceEndpoint service = monitoredServices.get(i);
                System.out.printf("%d. %s -> %s%n", i + 1, service.getName(), service.getUrl());
            }
        }
    }
    
    /**
     * Interactive service addition
     * @param scanner Scanner for user input
     */
    private void addServiceInteractive(Scanner scanner) {
        System.out.print("Enter service name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Enter service URL: ");
        String url = scanner.nextLine().trim();
        
        if (!name.isEmpty() && !url.isEmpty()) {
            addServiceToMonitor(name, url);
            System.out.println("Service added successfully!");
        } else {
            System.out.println("Invalid input. Both name and URL are required.");
        }
    }
    
    /**
     * Main method - entry point of the application
     * Supports different running modes based on command line arguments
     */
    public static void main(String[] args) {
        MetricsCollector collector = new MetricsCollector();
        
        // Add some default services for demonstration
        collector.addServiceToMonitor("Google", "https://www.google.com");
        collector.addServiceToMonitor("GitHub API", "https://api.github.com");
        collector.addServiceToMonitor("JSONPlaceholder", "https://jsonplaceholder.typicode.com/posts/1");
        
        try {
            if (args.length == 0) {
                // No arguments - run interactive mode
                collector.runInteractiveMode();
                
            } else if (args[0].equals("--once")) {
                // Single metrics collection
                collector.displayCurrentMetrics();
                
            } else if (args[0].equals("--continuous")) {
                // Continuous monitoring mode
                collector.startContinuousMonitoring();
                
                // Keep running until interrupted
                System.out.println("Monitoring started. Press Ctrl+C to stop.");
                Runtime.getRuntime().addShutdownHook(new Thread(collector::stopMonitoring));
                
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException e) {
                    System.out.println("Monitoring interrupted");
                }
                
            } else if (args[0].equals("--report")) {
                // Generate report and exit
                String reportPath = args.length > 1 ? args[1] : "health-monitor-report.json";
                collector.generateReport(reportPath);
                System.out.println("Report generated: " + reportPath);
                
            } else {
                System.out.println("Usage:");
                System.out.println("  java MetricsCollector                    - Interactive mode");
                System.out.println("  java MetricsCollector --once             - Single metrics collection");
                System.out.println("  java MetricsCollector --continuous       - Continuous monitoring");
                System.out.println("  java MetricsCollector --report [path]    - Generate report");
            }
            
        } catch (Exception e) {
            logger.error("Application error", e);
            e.printStackTrace();
        } finally {
            collector.stopMonitoring();
        }
    }
}