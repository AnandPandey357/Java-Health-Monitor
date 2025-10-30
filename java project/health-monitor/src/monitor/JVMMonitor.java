package monitor;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.lang.management.GarbageCollectorMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * JVMMonitor class monitors Java Virtual Machine metrics
 * This class collects memory usage, thread information, and runtime details
 */
public class JVMMonitor {
    
    // MXBeans are used to access JVM management information
    private final MemoryMXBean memoryBean;      // For memory statistics
    private final RuntimeMXBean runtimeBean;    // For runtime information
    private final ThreadMXBean threadBean;      // For thread statistics
    private final List<GarbageCollectorMXBean> gcBeans; // For garbage collection stats
    
    /**
     * Constructor initializes all the MXBeans needed for monitoring
     * MXBeans provide access to JVM internal metrics
     */
    public JVMMonitor() {
        // Initialize memory management bean to get heap and non-heap memory info
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        
        // Initialize runtime bean to get JVM uptime and other runtime info
        this.runtimeBean = ManagementFactory.getRuntimeMXBean();
        
        // Initialize thread bean to get thread count and CPU time
        this.threadBean = ManagementFactory.getThreadMXBean();
        
        // Initialize garbage collector beans to get GC statistics
        this.gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    }
    
    /**
     * Collects comprehensive JVM metrics and returns them as a Map
     * This method gathers memory, thread, runtime, and GC information
     * @return Map containing all JVM metrics with descriptive keys
     */
    public Map<String, Object> collectMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Collect memory metrics - heap and non-heap memory usage
        collectMemoryMetrics(metrics);
        
        // Collect thread metrics - active threads and CPU usage
        collectThreadMetrics(metrics);
        
        // Collect runtime metrics - uptime and JVM details
        collectRuntimeMetrics(metrics);
        
        // Collect garbage collection metrics - GC count and time
        collectGarbageCollectionMetrics(metrics);
        
        return metrics;
    }
    
    /**
     * Collects memory-related metrics including heap and non-heap memory
     * Heap memory is where objects are stored, non-heap is for class metadata
     * @param metrics Map to store the collected metrics
     */
    private void collectMemoryMetrics(Map<String, Object> metrics) {
        // Get heap memory usage (where Java objects are stored)
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        long heapCommitted = memoryBean.getHeapMemoryUsage().getCommitted();
        
        // Get non-heap memory usage (method area, code cache, etc.)
        long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();
        long nonHeapMax = memoryBean.getNonHeapMemoryUsage().getMax();
        long nonHeapCommitted = memoryBean.getNonHeapMemoryUsage().getCommitted();
        
        // Convert bytes to megabytes for easier reading
        metrics.put("heap_used_mb", heapUsed / (1024 * 1024));
        metrics.put("heap_max_mb", heapMax / (1024 * 1024));
        metrics.put("heap_committed_mb", heapCommitted / (1024 * 1024));
        metrics.put("heap_usage_percentage", (double) heapUsed / heapMax * 100);
        
        metrics.put("non_heap_used_mb", nonHeapUsed / (1024 * 1024));
        metrics.put("non_heap_max_mb", nonHeapMax == -1 ? "unlimited" : nonHeapMax / (1024 * 1024));
        metrics.put("non_heap_committed_mb", nonHeapCommitted / (1024 * 1024));
        
        // Get total system memory using Runtime class
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        metrics.put("total_memory_mb", totalMemory / (1024 * 1024));
        metrics.put("free_memory_mb", freeMemory / (1024 * 1024));
        metrics.put("used_memory_mb", usedMemory / (1024 * 1024));
    }
    
    /**
     * Collects thread-related metrics including count and CPU time
     * Threads are the execution units in Java applications
     * @param metrics Map to store the collected metrics
     */
    private void collectThreadMetrics(Map<String, Object> metrics) {
        // Get current thread count (number of active threads)
        int threadCount = threadBean.getThreadCount();
        
        // Get peak thread count (maximum threads at any point)
        int peakThreadCount = threadBean.getPeakThreadCount();
        
        // Get daemon thread count (background threads)
        int daemonThreadCount = threadBean.getDaemonThreadCount();
        
        // Get total started thread count since JVM start
        long totalStartedThreadCount = threadBean.getTotalStartedThreadCount();
        
        metrics.put("current_thread_count", threadCount);
        metrics.put("peak_thread_count", peakThreadCount);
        metrics.put("daemon_thread_count", daemonThreadCount);
        metrics.put("total_started_threads", totalStartedThreadCount);
        
        // CPU time is only available if supported by the platform
        if (threadBean.isCurrentThreadCpuTimeSupported()) {
            long currentThreadCpuTime = threadBean.getCurrentThreadCpuTime();
            metrics.put("current_thread_cpu_time_ms", currentThreadCpuTime / 1_000_000);
        }
    }
    
    /**
     * Collects runtime metrics including uptime and JVM information
     * Runtime metrics provide information about the JVM instance
     * @param metrics Map to store the collected metrics
     */
    private void collectRuntimeMetrics(Map<String, Object> metrics) {
        // Get JVM uptime in milliseconds
        long uptime = runtimeBean.getUptime();
        
        // Convert uptime to human-readable format
        long hours = uptime / (1000 * 60 * 60);
        long minutes = (uptime % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (uptime % (1000 * 60)) / 1000;
        
        metrics.put("uptime_ms", uptime);
        metrics.put("uptime_formatted", String.format("%d hours, %d minutes, %d seconds", hours, minutes, seconds));
        
        // Get JVM information
        metrics.put("jvm_name", runtimeBean.getVmName());
        metrics.put("jvm_version", runtimeBean.getVmVersion());
        metrics.put("jvm_vendor", runtimeBean.getVmVendor());
        
        // Get system properties
        metrics.put("java_version", System.getProperty("java.version"));
        metrics.put("os_name", System.getProperty("os.name"));
        metrics.put("os_arch", System.getProperty("os.arch"));
        
        // Get available processors
        metrics.put("available_processors", Runtime.getRuntime().availableProcessors());
    }
    
    /**
     * Collects garbage collection metrics for performance monitoring
     * GC metrics help identify memory management performance issues
     * @param metrics Map to store the collected metrics
     */
    private void collectGarbageCollectionMetrics(Map<String, Object> metrics) {
        long totalGcCollections = 0;
        long totalGcTime = 0;
        
        // Iterate through all garbage collectors
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            String gcName = gcBean.getName();
            long collectionCount = gcBean.getCollectionCount();
            long collectionTime = gcBean.getCollectionTime();
            
            // Store individual GC stats
            metrics.put("gc_" + gcName.toLowerCase().replace(" ", "_") + "_collections", collectionCount);
            metrics.put("gc_" + gcName.toLowerCase().replace(" ", "_") + "_time_ms", collectionTime);
            
            // Accumulate totals
            if (collectionCount > 0) {
                totalGcCollections += collectionCount;
                totalGcTime += collectionTime;
            }
        }
        
        // Store total GC statistics
        metrics.put("total_gc_collections", totalGcCollections);
        metrics.put("total_gc_time_ms", totalGcTime);
        
        // Calculate average GC time per collection
        if (totalGcCollections > 0) {
            metrics.put("avg_gc_time_ms", (double) totalGcTime / totalGcCollections);
        }
    }
    
    /**
     * Prints all collected metrics in a formatted way
     * This method displays the metrics in a human-readable format
     */
    public void printMetrics() {
        Map<String, Object> metrics = collectMetrics();
        
        System.out.println("=== JVM HEALTH MONITOR REPORT ===");
        System.out.println("Timestamp: " + new java.util.Date());
        System.out.println();
        
        // Print memory metrics
        System.out.println("MEMORY METRICS:");
        System.out.println("  Heap Used: " + metrics.get("heap_used_mb") + " MB");
        System.out.println("  Heap Max: " + metrics.get("heap_max_mb") + " MB");
        System.out.println("  Heap Usage: " + String.format("%.2f", metrics.get("heap_usage_percentage")) + "%");
        System.out.println("  Non-Heap Used: " + metrics.get("non_heap_used_mb") + " MB");
        System.out.println();
        
        // Print thread metrics
        System.out.println("THREAD METRICS:");
        System.out.println("  Current Threads: " + metrics.get("current_thread_count"));
        System.out.println("  Peak Threads: " + metrics.get("peak_thread_count"));
        System.out.println("  Daemon Threads: " + metrics.get("daemon_thread_count"));
        System.out.println();
        
        // Print runtime metrics
        System.out.println("RUNTIME METRICS:");
        System.out.println("  Uptime: " + metrics.get("uptime_formatted"));
        System.out.println("  JVM: " + metrics.get("jvm_name") + " " + metrics.get("jvm_version"));
        System.out.println("  Available Processors: " + metrics.get("available_processors"));
        System.out.println();
        
        // Print GC metrics
        System.out.println("GARBAGE COLLECTION METRICS:");
        System.out.println("  Total Collections: " + metrics.get("total_gc_collections"));
        System.out.println("  Total GC Time: " + metrics.get("total_gc_time_ms") + " ms");
        if (metrics.containsKey("avg_gc_time_ms")) {
            System.out.println("  Average GC Time: " + String.format("%.2f", metrics.get("avg_gc_time_ms")) + " ms");
        }
        System.out.println("=====================================");
    }
    
    /**
     * Main method for testing the JVMMonitor class
     * This allows running the monitor as a standalone application
     */
    public static void main(String[] args) {
        // Create an instance of JVMMonitor
        JVMMonitor monitor = new JVMMonitor();
        
        // Print current metrics
        monitor.printMetrics();
        
        // If arguments are provided, run continuous monitoring
        if (args.length > 0) {
            try {
                int intervalSeconds = Integer.parseInt(args[0]);
                System.out.println("\nStarting continuous monitoring every " + intervalSeconds + " seconds...");
                System.out.println("Press Ctrl+C to stop");
                
                // Run monitoring loop
                while (true) {
                    Thread.sleep(intervalSeconds * 1000);
                    System.out.println("\n" + "=".repeat(50));
                    monitor.printMetrics();
                }
            } catch (NumberFormatException e) {
                System.err.println("Invalid interval provided. Please provide a number in seconds.");
            } catch (InterruptedException e) {
                System.out.println("\nMonitoring stopped.");
            }
        }
    }
}