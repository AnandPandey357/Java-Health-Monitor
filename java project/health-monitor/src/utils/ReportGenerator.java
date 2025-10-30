package utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * ReportGenerator creates various types of reports from monitoring data
 * It can generate JSON reports, HTML summaries, and CSV exports
 */
public class ReportGenerator {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportGenerator.class);
    
    // Jackson ObjectMapper for JSON serialization
    private final ObjectMapper objectMapper;
    
    // Date formatter for timestamps in reports
    private final SimpleDateFormat dateFormatter;
    
    /**
     * Constructor initializes the JSON mapper and date formatter
     * JSON mapper is configured for pretty printing and proper date handling
     */
    public ReportGenerator() {
        // Configure ObjectMapper for JSON generation
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);  // Pretty print JSON
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // Use readable dates
        
        // Configure date formatter for consistent timestamp formatting
        this.dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        logger.info("ReportGenerator initialized");
    }
    
    /**
     * Generates a comprehensive JSON report containing all monitoring data
     * This is the main report generation method
     * @param data Map containing all the monitoring data to include in the report
     * @param outputPath Path where the JSON report should be saved
     * @throws IOException If file writing fails
     */
    public void generateJsonReport(Map<String, Object> data, String outputPath) throws IOException {
        logger.info("Generating JSON report to: {}", outputPath);
        
        // Enhance the data with report metadata
        Map<String, Object> enhancedData = new HashMap<>(data);
        addReportMetadata(enhancedData);
        
        // Ensure output directory exists
        Path outputDir = Paths.get(outputPath).getParent();
        if (outputDir != null && !Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
            logger.debug("Created output directory: {}", outputDir);
        }
        
        try {
            // Write JSON data to file
            objectMapper.writeValue(new File(outputPath), enhancedData);
            
            // Calculate file size for logging
            long fileSize = Files.size(Paths.get(outputPath));
            logger.info("JSON report generated successfully: {} ({} bytes)", outputPath, fileSize);
            
        } catch (IOException e) {
            logger.error("Failed to generate JSON report: {}", e.getMessage(), e);
            throw new IOException("Failed to generate JSON report: " + e.getMessage(), e);
        }
    }
    
    /**
     * Adds metadata to the report for better tracking and identification
     * @param data The data map to enhance with metadata
     */
    private void addReportMetadata(Map<String, Object> data) {
        Map<String, Object> metadata = new HashMap<>();
        
        // Add report generation information
        metadata.put("generator", "Java Application Health Monitor");
        metadata.put("generator_version", "1.0.0");
        metadata.put("report_format", "JSON");
        metadata.put("generated_timestamp", System.currentTimeMillis());
        metadata.put("generated_time_readable", dateFormatter.format(new Date()));
        
        // Add system information
        metadata.put("java_version", System.getProperty("java.version"));
        metadata.put("os_name", System.getProperty("os.name"));
        metadata.put("os_version", System.getProperty("os.version"));
        metadata.put("user_name", System.getProperty("user.name"));
        
        // Add JVM information
        Runtime runtime = Runtime.getRuntime();
        metadata.put("available_processors", runtime.availableProcessors());
        metadata.put("max_memory_mb", runtime.maxMemory() / (1024 * 1024));
        
        data.put("report_metadata", metadata);
        logger.debug("Added report metadata");
    }
    
    /**
     * Generates a simple HTML summary report for easy viewing in browsers
     * This creates a human-readable version of the monitoring data
     * @param data Map containing monitoring data
     * @param outputPath Path where the HTML report should be saved
     * @throws IOException If file writing fails
     */
    public void generateHtmlSummary(Map<String, Object> data, String outputPath) throws IOException {
        logger.info("Generating HTML summary to: {}", outputPath);
        
        // Ensure output directory exists
        Path outputDir = Paths.get(outputPath).getParent();
        if (outputDir != null && !Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        
        try (FileWriter writer = new FileWriter(outputPath)) {
            // Write HTML header
            writeHtmlHeader(writer);
            
            // Write report content
            writeHtmlContent(writer, data);
            
            // Write HTML footer
            writeHtmlFooter(writer);
            
            logger.info("HTML summary generated successfully: {}", outputPath);
            
        } catch (IOException e) {
            logger.error("Failed to generate HTML summary: {}", e.getMessage(), e);
            throw new IOException("Failed to generate HTML summary: " + e.getMessage(), e);
        }
    }
    
    /**
     * Writes the HTML header with CSS styling
     * @param writer FileWriter to write HTML content
     * @throws IOException If writing fails
     */
    private void writeHtmlHeader(FileWriter writer) throws IOException {
        writer.write("<!DOCTYPE html>\n");
        writer.write("<html lang=\"en\">\n");
        writer.write("<head>\n");
        writer.write("    <meta charset=\"UTF-8\">\n");
        writer.write("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        writer.write("    <title>Java Application Health Monitor Report</title>\n");
        writer.write("    <style>\n");
        writer.write("        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n");
        writer.write("        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n");
        writer.write("        h1 { color: #333; border-bottom: 3px solid #007acc; padding-bottom: 10px; }\n");
        writer.write("        h2 { color: #555; margin-top: 30px; }\n");
        writer.write("        .metric-box { background: #f8f9fa; padding: 15px; margin: 10px 0; border-left: 4px solid #007acc; }\n");
        writer.write("        .healthy { color: #28a745; font-weight: bold; }\n");
        writer.write("        .unhealthy { color: #dc3545; font-weight: bold; }\n");
        writer.write("        .timestamp { color: #666; font-size: 0.9em; }\n");
        writer.write("        table { width: 100%; border-collapse: collapse; margin: 10px 0; }\n");
        writer.write("        th, td { padding: 8px; text-align: left; border-bottom: 1px solid #ddd; }\n");
        writer.write("        th { background-color: #f2f2f2; }\n");
        writer.write("        .status-ok { background-color: #d4edda; }\n");
        writer.write("        .status-error { background-color: #f8d7da; }\n");
        writer.write("    </style>\n");
        writer.write("</head>\n");
        writer.write("<body>\n");
        writer.write("    <div class=\"container\">\n");
        writer.write("        <h1>Java Application Health Monitor Report</h1>\n");
        writer.write("        <p class=\"timestamp\">Generated: " + dateFormatter.format(new Date()) + "</p>\n");
    }
    
    /**
     * Writes the main HTML content with monitoring data
     * @param writer FileWriter to write HTML content
     * @param data Map containing monitoring data
     * @throws IOException If writing fails
     */
    @SuppressWarnings("unchecked")
    private void writeHtmlContent(FileWriter writer, Map<String, Object> data) throws IOException {
        // Write current metrics section
        if (data.containsKey("current_metrics")) {
            Map<String, Object> currentMetrics = (Map<String, Object>) data.get("current_metrics");
            writeCurrentMetricsSection(writer, currentMetrics);
        }
        
        // Write historical data summary
        if (data.containsKey("summary_statistics")) {
            Map<String, Object> summaryStats = (Map<String, Object>) data.get("summary_statistics");
            writeSummaryStatistics(writer, summaryStats);
        }
        
        // Write monitoring configuration
        writeMonitoringConfiguration(writer, data);
    }
    
    /**
     * Writes current metrics section in HTML
     * @param writer FileWriter to write HTML content
     * @param currentMetrics Current metrics data
     * @throws IOException If writing fails
     */
    @SuppressWarnings("unchecked")
    private void writeCurrentMetricsSection(FileWriter writer, Map<String, Object> currentMetrics) throws IOException {
        writer.write("        <h2>Current System Status</h2>\n");
        
        // JVM Metrics
        if (currentMetrics.containsKey("jvm_metrics")) {
            Map<String, Object> jvmMetrics = (Map<String, Object>) currentMetrics.get("jvm_metrics");
            writer.write("        <div class=\"metric-box\">\n");
            writer.write("            <h3>JVM Metrics</h3>\n");
            writer.write("            <p>Heap Usage: " + jvmMetrics.get("heap_used_mb") + " MB / " + 
                        jvmMetrics.get("heap_max_mb") + " MB (" + 
                        String.format("%.1f", (Double) jvmMetrics.get("heap_usage_percentage")) + "%)</p>\n");
            writer.write("            <p>Thread Count: " + jvmMetrics.get("current_thread_count") + "</p>\n");
            writer.write("            <p>Uptime: " + jvmMetrics.get("uptime_formatted") + "</p>\n");
            writer.write("        </div>\n");
        }
        
        // Health Check Results
        if (currentMetrics.containsKey("health_summary")) {
            Map<String, Object> healthSummary = (Map<String, Object>) currentMetrics.get("health_summary");
            writer.write("        <div class=\"metric-box\">\n");
            writer.write("            <h3>Service Health Summary</h3>\n");
            
            String overallStatus = (String) healthSummary.get("overall_status");
            String statusClass = "HEALTHY".equals(overallStatus) ? "healthy" : "unhealthy";
            
            writer.write("            <p>Overall Status: <span class=\"" + statusClass + "\">" + overallStatus + "</span></p>\n");
            writer.write("            <p>Healthy Services: " + healthSummary.get("healthy_services") + 
                        " / " + healthSummary.get("total_services") + "</p>\n");
            
            if (healthSummary.containsKey("avg_response_time_ms")) {
                writer.write("            <p>Average Response Time: " + 
                           String.format("%.1f", (Double) healthSummary.get("avg_response_time_ms")) + " ms</p>\n");
            }
            writer.write("        </div>\n");
        }
    }
    
    /**
     * Writes summary statistics section in HTML
     * @param writer FileWriter to write HTML content
     * @param summaryStats Summary statistics data
     * @throws IOException If writing fails
     */
    @SuppressWarnings("unchecked")
    private void writeSummaryStatistics(FileWriter writer, Map<String, Object> summaryStats) throws IOException {
        writer.write("        <h2>Historical Summary</h2>\n");
        
        if (summaryStats.containsKey("no_data")) {
            writer.write("        <p>No historical data available.</p>\n");
            return;
        }
        
        writer.write("        <div class=\"metric-box\">\n");
        writer.write("            <h3>Data Collection Summary</h3>\n");
        writer.write("            <p>Data Points Analyzed: " + summaryStats.get("data_points_analyzed") + "</p>\n");
        writer.write("            <p>Time Range: " + String.format("%.1f", (Double) summaryStats.get("time_range_minutes")) + " minutes</p>\n");
        writer.write("        </div>\n");
        
        // Heap usage statistics
        if (summaryStats.containsKey("heap_usage_statistics")) {
            Map<String, Object> heapStats = (Map<String, Object>) summaryStats.get("heap_usage_statistics");
            writer.write("        <div class=\"metric-box\">\n");
            writer.write("            <h3>Heap Usage Statistics</h3>\n");
            writer.write("            <p>Average Usage: " + String.format("%.1f", (Double) heapStats.get("average_heap_usage_percentage")) + "%</p>\n");
            writer.write("            <p>Peak Usage: " + String.format("%.1f", (Double) heapStats.get("max_heap_usage_percentage")) + "%</p>\n");
            writer.write("            <p>Minimum Usage: " + String.format("%.1f", (Double) heapStats.get("min_heap_usage_percentage")) + "%</p>\n");
            writer.write("        </div>\n");
        }
        
        // Health check statistics
        if (summaryStats.containsKey("health_check_statistics")) {
            Map<String, Object> healthStats = (Map<String, Object>) summaryStats.get("health_check_statistics");
            writer.write("        <div class=\"metric-box\">\n");
            writer.write("            <h3>Health Check Statistics</h3>\n");
            writer.write("            <p>Total Health Checks: " + healthStats.get("total_health_checks") + "</p>\n");
            writer.write("            <p>Successful Checks: " + healthStats.get("successful_health_checks") + "</p>\n");
            writer.write("            <p>Success Rate: " + String.format("%.1f", (Double) healthStats.get("health_success_rate_percentage")) + "%</p>\n");
            writer.write("        </div>\n");
        }
    }
    
    /**
     * Writes monitoring configuration section in HTML
     * @param writer FileWriter to write HTML content
     * @param data Full monitoring data
     * @throws IOException If writing fails
     */
    private void writeMonitoringConfiguration(FileWriter writer, Map<String, Object> data) throws IOException {
        writer.write("        <h2>Monitoring Configuration</h2>\n");
        writer.write("        <div class=\"metric-box\">\n");
        writer.write("            <h3>Settings</h3>\n");
        
        if (data.containsKey("monitoring_interval_seconds")) {
            writer.write("            <p>Monitoring Interval: " + data.get("monitoring_interval_seconds") + " seconds</p>\n");
        }
        
        if (data.containsKey("historical_data_count")) {
            writer.write("            <p>Historical Records: " + data.get("historical_data_count") + "</p>\n");
        }
        
        writer.write("        </div>\n");
    }
    
    /**
     * Writes the HTML footer
     * @param writer FileWriter to write HTML content
     * @throws IOException If writing fails
     */
    private void writeHtmlFooter(FileWriter writer) throws IOException {
        writer.write("        <hr>\n");
        writer.write("        <p class=\"timestamp\">Report generated by Java Application Health Monitor v1.0.0</p>\n");
        writer.write("    </div>\n");
        writer.write("</body>\n");
        writer.write("</html>\n");
    }
    
    /**
     * Generates a CSV export of metrics data for spreadsheet analysis
     * Useful for importing data into Excel or other analysis tools
     * @param data Map containing monitoring data
     * @param outputPath Path where the CSV file should be saved
     * @throws IOException If file writing fails
     */
    @SuppressWarnings("unchecked")
    public void generateCsvExport(Map<String, Object> data, String outputPath) throws IOException {
        logger.info("Generating CSV export to: {}", outputPath);
        
        // Ensure output directory exists
        Path outputDir = Paths.get(outputPath).getParent();
        if (outputDir != null && !Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        
        try (FileWriter writer = new FileWriter(outputPath)) {
            // Write CSV header
            writer.write("Timestamp,Heap_Used_MB,Heap_Max_MB,Heap_Usage_Percent,Thread_Count,Uptime_MS,Healthy_Services,Total_Services,Health_Percentage\n");
            
            // Write historical data if available
            if (data.containsKey("historical_data")) {
                java.util.List<Map<String, Object>> historicalData = 
                    (java.util.List<Map<String, Object>>) data.get("historical_data");
                
                for (Map<String, Object> record : historicalData) {
                    writeCsvRecord(writer, record);
                }
            }
            
            logger.info("CSV export generated successfully: {}", outputPath);
            
        } catch (IOException e) {
            logger.error("Failed to generate CSV export: {}", e.getMessage(), e);
            throw new IOException("Failed to generate CSV export: " + e.getMessage(), e);
        }
    }
    
    /**
     * Writes a single CSV record from monitoring data
     * @param writer FileWriter to write CSV content
     * @param record Single monitoring record
     * @throws IOException If writing fails
     */
    @SuppressWarnings("unchecked")
    private void writeCsvRecord(FileWriter writer, Map<String, Object> record) throws IOException {
        StringBuilder csvLine = new StringBuilder();
        
        // Extract timestamp
        Long timestamp = (Long) record.get("collection_timestamp");
        csvLine.append(timestamp != null ? timestamp : "");
        csvLine.append(",");
        
        // Extract JVM metrics
        Map<String, Object> jvmMetrics = (Map<String, Object>) record.get("jvm_metrics");
        if (jvmMetrics != null) {
            csvLine.append(getValueOrEmpty(jvmMetrics, "heap_used_mb")).append(",");
            csvLine.append(getValueOrEmpty(jvmMetrics, "heap_max_mb")).append(",");
            csvLine.append(getValueOrEmpty(jvmMetrics, "heap_usage_percentage")).append(",");
            csvLine.append(getValueOrEmpty(jvmMetrics, "current_thread_count")).append(",");
            csvLine.append(getValueOrEmpty(jvmMetrics, "uptime_ms")).append(",");
        } else {
            csvLine.append(",,,,,");
        }
        
        // Extract health metrics
        Map<String, Object> healthSummary = (Map<String, Object>) record.get("health_summary");
        if (healthSummary != null) {
            csvLine.append(getValueOrEmpty(healthSummary, "healthy_services")).append(",");
            csvLine.append(getValueOrEmpty(healthSummary, "total_services")).append(",");
            csvLine.append(getValueOrEmpty(healthSummary, "health_percentage"));
        } else {
            csvLine.append(",,");
        }
        
        csvLine.append("\n");
        writer.write(csvLine.toString());
    }
    
    /**
     * Helper method to safely extract values from maps for CSV generation
     * @param map Source map
     * @param key Key to extract
     * @return String representation of value or empty string if not found
     */
    private String getValueOrEmpty(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }
    
    /**
     * Validates if the output path is writable
     * @param outputPath Path to validate
     * @throws IOException If path is not writable
     */
    public void validateOutputPath(String outputPath) throws IOException {
        Path path = Paths.get(outputPath);
        Path parentDir = path.getParent();
        
        // Check if parent directory exists or can be created
        if (parentDir != null && !Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                throw new IOException("Cannot create output directory: " + parentDir, e);
            }
        }
        
        // Check if file can be written (test by creating a temporary file)
        try {
            Files.createFile(path);
            Files.delete(path);
        } catch (IOException e) {
            if (!Files.exists(path)) {
                throw new IOException("Cannot write to output path: " + outputPath, e);
            }
            // File already exists, which is okay for reports
        }
    }
    
    /**
     * Main method for testing the ReportGenerator class
     * Demonstrates report generation with sample data
     */
    public static void main(String[] args) {
        ReportGenerator generator = new ReportGenerator();
        
        try {
            // Create sample monitoring data
            Map<String, Object> sampleData = createSampleData();
            
            // Generate different types of reports
            String basePath = "sample-reports/health-monitor-";
            long timestamp = System.currentTimeMillis();
            
            // JSON report
            generator.generateJsonReport(sampleData, basePath + timestamp + ".json");
            
            // HTML summary
            generator.generateHtmlSummary(sampleData, basePath + timestamp + ".html");
            
            // CSV export
            generator.generateCsvExport(sampleData, basePath + timestamp + ".csv");
            
            System.out.println("Sample reports generated successfully!");
            System.out.println("Check the 'sample-reports' directory for output files.");
            
        } catch (IOException e) {
            System.err.println("Error generating sample reports: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates sample monitoring data for testing and demonstration
     * @return Map containing sample monitoring data
     */
    private static Map<String, Object> createSampleData() {
        Map<String, Object> data = new HashMap<>();
        
        // Sample current metrics
        Map<String, Object> currentMetrics = new HashMap<>();
        
        // Sample JVM metrics
        Map<String, Object> jvmMetrics = new HashMap<>();
        jvmMetrics.put("heap_used_mb", 256L);
        jvmMetrics.put("heap_max_mb", 1024L);
        jvmMetrics.put("heap_usage_percentage", 25.0);
        jvmMetrics.put("current_thread_count", 12);
        jvmMetrics.put("uptime_ms", 3600000L);
        jvmMetrics.put("uptime_formatted", "1 hours, 0 minutes, 0 seconds");
        currentMetrics.put("jvm_metrics", jvmMetrics);
        
        // Sample health summary
        Map<String, Object> healthSummary = new HashMap<>();
        healthSummary.put("total_services", 3);
        healthSummary.put("healthy_services", 2);
        healthSummary.put("unhealthy_services", 1);
        healthSummary.put("health_percentage", 66.7);
        healthSummary.put("overall_status", "UNHEALTHY");
        healthSummary.put("avg_response_time_ms", 250.0);
        currentMetrics.put("health_summary", healthSummary);
        
        data.put("current_metrics", currentMetrics);
        data.put("monitoring_interval_seconds", 30);
        data.put("historical_data_count", 50);
        
        return data;
    }
}