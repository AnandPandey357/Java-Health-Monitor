package monitor;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * HealthChecker class monitors the health of external applications and services
 * It performs HTTP health checks and TCP port connectivity checks
 */
public class HealthChecker {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthChecker.class);
    
    // HTTP client for making health check requests
    private final HttpClient httpClient;
    
    // Thread pool for parallel health checks
    private final ExecutorService executorService;
    
    // Configuration constants
    private static final int DEFAULT_TIMEOUT_MS = 5000;  // 5 seconds timeout
    private static final int DEFAULT_THREADS = 10;       // Thread pool size
    
    /**
     * Constructor initializes HTTP client and thread pool
     * HTTP client is configured with timeouts for reliable health checks
     */
    public HealthChecker() {
        // Configure HTTP client with timeouts to prevent hanging requests
        RequestConfig config = RequestConfig.custom()
                .setConnectionRequestTimeout(DEFAULT_TIMEOUT_MS)  // Timeout for getting connection from pool
                .setConnectTimeout(DEFAULT_TIMEOUT_MS)            // Timeout for establishing connection
                .setSocketTimeout(DEFAULT_TIMEOUT_MS)             // Timeout for reading data
                .build();
        
        // Build HTTP client with configuration
        this.httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(config)
                .build();
        
        // Create thread pool for parallel health checks
        this.executorService = Executors.newFixedThreadPool(DEFAULT_THREADS);
        
        logger.info("HealthChecker initialized with {} second timeout", DEFAULT_TIMEOUT_MS / 1000);
    }
    
    /**
     * Represents a service endpoint to be monitored
     * Contains URL, expected status code, and timeout configuration
     */
    public static class ServiceEndpoint {
        private final String name;           // Friendly name for the service
        private final String url;            // URL to check
        private final int expectedStatus;    // Expected HTTP status code (default 200)
        private final int timeoutMs;         // Timeout for this specific check
        
        public ServiceEndpoint(String name, String url) {
            this(name, url, 200, DEFAULT_TIMEOUT_MS);
        }
        
        public ServiceEndpoint(String name, String url, int expectedStatus) {
            this(name, url, expectedStatus, DEFAULT_TIMEOUT_MS);
        }
        
        public ServiceEndpoint(String name, String url, int expectedStatus, int timeoutMs) {
            this.name = name;
            this.url = url;
            this.expectedStatus = expectedStatus;
            this.timeoutMs = timeoutMs;
        }
        
        // Getter methods for accessing endpoint properties
        public String getName() { return name; }
        public String getUrl() { return url; }
        public int getExpectedStatus() { return expectedStatus; }
        public int getTimeoutMs() { return timeoutMs; }
    }
    
    /**
     * Represents the result of a health check
     * Contains status, response time, and any error information
     */
    public static class HealthCheckResult {
        private final String serviceName;
        private final String url;
        private final boolean isHealthy;
        private final long responseTimeMs;
        private final int statusCode;
        private final String errorMessage;
        private final long timestamp;
        
        public HealthCheckResult(String serviceName, String url, boolean isHealthy, 
                               long responseTimeMs, int statusCode, String errorMessage) {
            this.serviceName = serviceName;
            this.url = url;
            this.isHealthy = isHealthy;
            this.responseTimeMs = responseTimeMs;
            this.statusCode = statusCode;
            this.errorMessage = errorMessage;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getter methods for accessing result properties
        public String getServiceName() { return serviceName; }
        public String getUrl() { return url; }
        public boolean isHealthy() { return isHealthy; }
        public long getResponseTimeMs() { return responseTimeMs; }
        public int getStatusCode() { return statusCode; }
        public String getErrorMessage() { return errorMessage; }
        public long getTimestamp() { return timestamp; }
        
        /**
         * Converts the health check result to a Map for JSON serialization
         * @return Map representation of the health check result
         */
        public Map<String, Object> toMap() {
            Map<String, Object> result = new HashMap<>();
            result.put("service_name", serviceName);
            result.put("url", url);
            result.put("is_healthy", isHealthy);
            result.put("response_time_ms", responseTimeMs);
            result.put("status_code", statusCode);
            result.put("error_message", errorMessage);
            result.put("timestamp", timestamp);
            result.put("check_time", new java.util.Date(timestamp).toString());
            return result;
        }
    }
    
    /**
     * Performs a single HTTP health check on the specified endpoint
     * Measures response time and validates status code
     * @param endpoint The service endpoint to check
     * @return HealthCheckResult containing the check results
     */
    public HealthCheckResult checkHealth(ServiceEndpoint endpoint) {
        long startTime = System.currentTimeMillis();
        
        try {
            logger.debug("Checking health for service: {} at {}", endpoint.getName(), endpoint.getUrl());
            
            // Create HTTP GET request
            HttpGet request = new HttpGet(endpoint.getUrl());
            
            // Add standard headers to identify our health checker
            request.addHeader("User-Agent", "Java-Health-Monitor/1.0");
            request.addHeader("Accept", "application/json,text/plain,*/*");
            
            // Execute the request
            HttpResponse response = httpClient.execute(request);
            
            // Calculate response time
            long responseTime = System.currentTimeMillis() - startTime;
            
            // Get status code
            int statusCode = response.getStatusLine().getStatusCode();
            
            // Check if status code matches expected
            boolean isHealthy = (statusCode == endpoint.getExpectedStatus());
            
            // Get response body for debugging (first 1000 characters)
            String responseBody = "";
            if (response.getEntity() != null) {
                responseBody = EntityUtils.toString(response.getEntity());
                if (responseBody.length() > 1000) {
                    responseBody = responseBody.substring(0, 1000) + "...";
                }
            }
            
            String errorMessage = null;
            if (!isHealthy) {
                errorMessage = String.format("Expected status %d but got %d. Response: %s", 
                    endpoint.getExpectedStatus(), statusCode, responseBody);
            }
            
            logger.debug("Health check completed for {}: status={}, time={}ms", 
                endpoint.getName(), statusCode, responseTime);
            
            return new HealthCheckResult(endpoint.getName(), endpoint.getUrl(), 
                isHealthy, responseTime, statusCode, errorMessage);
            
        } catch (SocketTimeoutException e) {
            // Handle timeout specifically
            long responseTime = System.currentTimeMillis() - startTime;
            logger.warn("Timeout checking health for service: {} - {}", endpoint.getName(), e.getMessage());
            return new HealthCheckResult(endpoint.getName(), endpoint.getUrl(), 
                false, responseTime, -1, "Timeout: " + e.getMessage());
            
        } catch (IOException e) {
            // Handle other IO exceptions (connection refused, DNS issues, etc.)
            long responseTime = System.currentTimeMillis() - startTime;
            logger.error("Error checking health for service: {} - {}", endpoint.getName(), e.getMessage());
            return new HealthCheckResult(endpoint.getName(), endpoint.getUrl(), 
                false, responseTime, -1, "Connection error: " + e.getMessage());
            
        } catch (Exception e) {
            // Handle any unexpected exceptions
            long responseTime = System.currentTimeMillis() - startTime;
            logger.error("Unexpected error checking health for service: {}", endpoint.getName(), e);
            return new HealthCheckResult(endpoint.getName(), endpoint.getUrl(), 
                false, responseTime, -1, "Unexpected error: " + e.getMessage());
        }
    }
    
    /**
     * Performs health checks on multiple endpoints in parallel
     * Uses CompletableFuture for asynchronous execution
     * @param endpoints List of service endpoints to check
     * @return List of health check results
     */
    public List<HealthCheckResult> checkMultipleServices(List<ServiceEndpoint> endpoints) {
        logger.info("Starting health checks for {} services", endpoints.size());
        
        // Create list to store CompletableFuture objects
        List<CompletableFuture<HealthCheckResult>> futures = new ArrayList<>();
        
        // Submit health checks to thread pool
        for (ServiceEndpoint endpoint : endpoints) {
            CompletableFuture<HealthCheckResult> future = CompletableFuture.supplyAsync(
                () -> checkHealth(endpoint), executorService);
            futures.add(future);
        }
        
        // Collect all results
        List<HealthCheckResult> results = new ArrayList<>();
        for (CompletableFuture<HealthCheckResult> future : futures) {
            try {
                // Wait for each health check to complete (with timeout)
                HealthCheckResult result = future.get(DEFAULT_TIMEOUT_MS * 2, TimeUnit.MILLISECONDS);
                results.add(result);
            } catch (Exception e) {
                logger.error("Error getting health check result", e);
                // Create a failed result for the timeout case
                results.add(new HealthCheckResult("Unknown", "Unknown", false, 
                    DEFAULT_TIMEOUT_MS * 2, -1, "Future timeout: " + e.getMessage()));
            }
        }
        
        logger.info("Completed health checks for {} services", results.size());
        return results;
    }
    
    /**
     * Checks if a TCP port is open and accessible
     * Useful for checking database connections, message queues, etc.
     * @param host Hostname or IP address
     * @param port Port number to check
     * @param timeoutMs Timeout in milliseconds
     * @return true if port is accessible, false otherwise
     */
    public boolean checkTcpPort(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            long startTime = System.currentTimeMillis();
            
            // Attempt to connect to the host and port
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            
            long responseTime = System.currentTimeMillis() - startTime;
            logger.debug("TCP port check successful for {}:{} in {}ms", host, port, responseTime);
            
            return true;
        } catch (IOException e) {
            logger.warn("TCP port check failed for {}:{} - {}", host, port, e.getMessage());
            return false;
        }
    }
    
    /**
     * Creates a summary of health check results
     * Provides overall statistics and identifies unhealthy services
     * @param results List of health check results
     * @return Map containing summary statistics
     */
    public Map<String, Object> createHealthSummary(List<HealthCheckResult> results) {
        Map<String, Object> summary = new HashMap<>();
        
        int totalServices = results.size();
        int healthyServices = 0;
        int unhealthyServices = 0;
        long totalResponseTime = 0;
        long maxResponseTime = 0;
        long minResponseTime = Long.MAX_VALUE;
        
        List<String> unhealthyServiceNames = new ArrayList<>();
        
        // Analyze each result
        for (HealthCheckResult result : results) {
            if (result.isHealthy()) {
                healthyServices++;
            } else {
                unhealthyServices++;
                unhealthyServiceNames.add(result.getServiceName());
            }
            
            long responseTime = result.getResponseTimeMs();
            totalResponseTime += responseTime;
            maxResponseTime = Math.max(maxResponseTime, responseTime);
            minResponseTime = Math.min(minResponseTime, responseTime);
        }
        
        // Calculate average response time
        double avgResponseTime = totalServices > 0 ? (double) totalResponseTime / totalServices : 0;
        
        // Build summary map
        summary.put("total_services", totalServices);
        summary.put("healthy_services", healthyServices);
        summary.put("unhealthy_services", unhealthyServices);
        summary.put("health_percentage", totalServices > 0 ? (double) healthyServices / totalServices * 100 : 0);
        summary.put("avg_response_time_ms", avgResponseTime);
        summary.put("max_response_time_ms", maxResponseTime == 0 ? 0 : maxResponseTime);
        summary.put("min_response_time_ms", minResponseTime == Long.MAX_VALUE ? 0 : minResponseTime);
        summary.put("unhealthy_services", unhealthyServiceNames);
        summary.put("overall_status", unhealthyServices == 0 ? "HEALTHY" : "UNHEALTHY");
        summary.put("check_timestamp", System.currentTimeMillis());
        
        return summary;
    }
    
    /**
     * Prints health check results in a formatted way
     * @param results List of health check results to display
     */
    public void printHealthResults(List<HealthCheckResult> results) {
        System.out.println("=== HEALTH CHECK RESULTS ===");
        System.out.println("Timestamp: " + new java.util.Date());
        System.out.println();
        
        for (HealthCheckResult result : results) {
            String status = result.isHealthy() ? "✓ HEALTHY" : "✗ UNHEALTHY";
            String statusColor = result.isHealthy() ? "" : ""; // Could add ANSI colors here
            
            System.out.printf("Service: %s%n", result.getServiceName());
            System.out.printf("  URL: %s%n", result.getUrl());
            System.out.printf("  Status: %s%s%n", statusColor, status);
            System.out.printf("  Response Time: %d ms%n", result.getResponseTimeMs());
            System.out.printf("  HTTP Status: %d%n", result.getStatusCode());
            
            if (!result.isHealthy() && result.getErrorMessage() != null) {
                System.out.printf("  Error: %s%n", result.getErrorMessage());
            }
            System.out.println();
        }
        
        // Print summary
        Map<String, Object> summary = createHealthSummary(results);
        System.out.println("=== SUMMARY ===");
        System.out.printf("Overall Status: %s%n", summary.get("overall_status"));
        System.out.printf("Healthy Services: %d/%d (%.1f%%)%n", 
            summary.get("healthy_services"), summary.get("total_services"), summary.get("health_percentage"));
        System.out.printf("Average Response Time: %.1f ms%n", summary.get("avg_response_time_ms"));
        System.out.println("================");
    }
    
    /**
     * Cleanup method to shutdown the executor service
     * Should be called when the HealthChecker is no longer needed
     */
    public void shutdown() {
        try {
            logger.info("Shutting down HealthChecker...");
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
    
    /**
     * Main method for testing the HealthChecker class
     * Demonstrates basic usage with common web services
     */
    public static void main(String[] args) {
        HealthChecker checker = new HealthChecker();
        
        try {
            // Create list of test endpoints
            List<ServiceEndpoint> endpoints = new ArrayList<>();
            endpoints.add(new ServiceEndpoint("Google", "https://www.google.com"));
            endpoints.add(new ServiceEndpoint("GitHub", "https://api.github.com"));
            endpoints.add(new ServiceEndpoint("JSONPlaceholder", "https://jsonplaceholder.typicode.com/posts/1"));
            endpoints.add(new ServiceEndpoint("Local Service", "http://localhost:8080/health")); // This will likely fail
            
            // Perform health checks
            List<HealthCheckResult> results = checker.checkMultipleServices(endpoints);
            
            // Print results
            checker.printHealthResults(results);
            
            // Demonstrate TCP port checking
            System.out.println("\n=== TCP PORT CHECKS ===");
            boolean googlePortOpen = checker.checkTcpPort("www.google.com", 80, 3000);
            System.out.println("Google port 80: " + (googlePortOpen ? "OPEN" : "CLOSED"));
            
            boolean localPortOpen = checker.checkTcpPort("localhost", 8080, 1000);
            System.out.println("Local port 8080: " + (localPortOpen ? "OPEN" : "CLOSED"));
            
        } finally {
            // Always shutdown the checker to clean up resources
            checker.shutdown();
        }
    }
}