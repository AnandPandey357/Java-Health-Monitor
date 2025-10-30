# Java Application Health Monitor

A comprehensive monitoring system for Java applications that tracks health status, memory usage, and response times. This tool helps you keep track of your applications' performance and quickly identify issues.

## 🎯 Features

- **JVM Monitoring**: Memory usage, thread count, garbage collection metrics, and runtime information
- **Health Checking**: HTTP endpoint monitoring for external services and APIs
- **Report Generation**: JSON, HTML, and CSV reports for analysis
- **Real-time Monitoring**: Continuous monitoring with configurable intervals
- **Interactive Mode**: Command-line interface for real-time control
- **Historical Data**: Tracks metrics over time with statistical analysis
- **Deployment Scripts**: Automated deployment and service management
- **Configurable**: Easy-to-configure service monitoring via text files

## 📋 Prerequisites

- **Java 11 or later** (OpenJDK or Oracle JDK)
- **Maven 3.6+** (for building from source)
- **Network access** (for monitoring external services)

### Checking Prerequisites

```bash
# Check Java version
java -version

# Check Maven version
mvn -version
```

## 🚀 Quick Start

### 1. Clone or Download the Project

```bash
# Navigate to your desired directory
cd /path/to/your/projects

# If you have the project files, ensure the structure looks like this:
health-monitor/
├── src/
│   ├── monitor/
│   │   ├── JVMMonitor.java
│   │   ├── HealthChecker.java
│   │   └── MetricsCollector.java
│   └── utils/
│       └── ReportGenerator.java
├── scripts/
│   ├── start_monitor.sh
│   └── deploy.sh
├── pom.xml
└── README.md
```

### 2. Build the Project

```bash
cd health-monitor

# Build using Maven
mvn clean package

# This creates target/java-health-monitor-1.0.0-shaded.jar
```

### 3. Run the Monitor

#### Option A: Interactive Mode (Recommended for first-time users)

```bash
java -jar target/java-health-monitor-1.0.0-shaded.jar
```

#### Option B: Single Metrics Collection

```bash
java -jar target/java-health-monitor-1.0.0-shaded.jar --once
```

#### Option C: Continuous Monitoring

```bash
java -jar target/java-health-monitor-1.0.0-shaded.jar --continuous
```

#### Option D: Generate Report

```bash
java -jar target/java-health-monitor-1.0.0-shaded.jar --report my-report.json
```

## 📖 Detailed Usage Guide

### Understanding the Components

#### 1. JVMMonitor.java
**Purpose**: Monitors Java Virtual Machine metrics
- **Memory Usage**: Heap and non-heap memory consumption
- **Thread Information**: Active threads, peak thread count, daemon threads
- **Runtime Details**: JVM uptime, version, and system properties
- **Garbage Collection**: GC statistics and performance metrics

**Key Methods**:
```java
// Collect all JVM metrics
Map<String, Object> metrics = jvmMonitor.collectMetrics();

// Print formatted metrics to console
jvmMonitor.printMetrics();
```

#### 2. HealthChecker.java
**Purpose**: Monitors external service health via HTTP requests
- **HTTP Health Checks**: GET requests to service endpoints
- **Response Time Measurement**: Tracks how long services take to respond
- **Status Code Validation**: Ensures services return expected HTTP codes
- **Parallel Checking**: Monitors multiple services simultaneously
- **TCP Port Checking**: Verifies if ports are accessible

**Key Methods**:
```java
// Create a service endpoint to monitor
ServiceEndpoint endpoint = new ServiceEndpoint("My API", "http://localhost:8080/health");

// Perform health check
HealthCheckResult result = healthChecker.checkHealth(endpoint);

// Check multiple services in parallel
List<HealthCheckResult> results = healthChecker.checkMultipleServices(endpoints);
```

#### 3. MetricsCollector.java
**Purpose**: Orchestrates all monitoring activities
- **Data Aggregation**: Combines JVM and health check data
- **Historical Tracking**: Stores metrics over time
- **Statistical Analysis**: Calculates trends and averages
- **Interactive Interface**: Provides command-line control
- **Continuous Monitoring**: Runs periodic collection cycles

**Key Methods**:
```java
// Add a service to monitor
collector.addServiceToMonitor("My Service", "http://localhost:8080/health");

// Start continuous monitoring
collector.startContinuousMonitoring();

// Generate comprehensive report
collector.generateReport("report.json");
```

#### 4. ReportGenerator.java
**Purpose**: Creates various types of reports from monitoring data
- **JSON Reports**: Machine-readable format for integration
- **HTML Summaries**: Human-readable reports for browsers
- **CSV Exports**: Spreadsheet-compatible data exports
- **Report Metadata**: Includes generation time and system info

**Key Methods**:
```java
// Generate JSON report
reportGenerator.generateJsonReport(data, "report.json");

// Generate HTML summary
reportGenerator.generateHtmlSummary(data, "summary.html");

// Generate CSV export
reportGenerator.generateCsvExport(data, "data.csv");
```

### Interactive Mode Commands

When you run the application without arguments, it starts in interactive mode:

```
monitor> help
Available commands:
  help     - Show this help message
  status   - Display current metrics
  start    - Start continuous monitoring
  stop     - Stop continuous monitoring
  report   - Generate JSON report
  services - List monitored services
  add      - Add a service to monitor
  exit     - Exit the application
```

### Configuring Services to Monitor

Create a `monitored-services.txt` file in your project directory:

```txt
# Monitored Services Configuration
# Format: SERVICE_NAME,URL,EXPECTED_STATUS_CODE

# Production services
Production API,https://api.mycompany.com/health,200
User Service,https://users.mycompany.com/status,200
Payment Gateway,https://payments.mycompany.com/ping,200

# Development services
Dev API,http://localhost:8080/actuator/health,200
Test Database,http://localhost:5432/health,200
```

## 🔧 Advanced Configuration

### Environment Variables

Set these environment variables to customize behavior:

```bash
# Set monitoring interval (seconds)
export MONITORING_INTERVAL=60

# Set log level
export LOG_LEVEL=DEBUG

# Set Java heap size
export JAVA_OPTS="-Xmx512m -Xms256m"
```

### JVM Tuning for Production

```bash
java -Xmx1g -Xms512m -XX:+UseG1GC -XX:+UseStringDeduplication \
     -jar target/java-health-monitor-1.0.0-shaded.jar --continuous
```

### Custom Logging Configuration

Create a `logging.properties` file:

```properties
# Logging Configuration
org.slf4j.simpleLogger.defaultLogLevel=INFO
org.slf4j.simpleLogger.log.monitor=DEBUG
org.slf4j.simpleLogger.showDateTime=true
org.slf4j.simpleLogger.dateTimeFormat=yyyy-MM-dd HH:mm:ss
```

## 🐧 Linux Deployment

### Using the Deployment Script

The project includes automated deployment scripts for Linux systems:

```bash
# Make scripts executable
chmod +x scripts/*.sh

# Install as a system service
sudo scripts/deploy.sh install

# Start the service
sudo systemctl start health-monitor

# Check service status
sudo systemctl status health-monitor

# View logs
sudo journalctl -u health-monitor -f
```

### Manual Deployment Steps

1. **Create deployment directory**:
```bash
sudo mkdir -p /opt/health-monitor
sudo mkdir -p /var/log/health-monitor
```

2. **Create system user**:
```bash
sudo useradd --system --shell /bin/false health-monitor
```

3. **Copy files**:
```bash
sudo cp target/java-health-monitor-1.0.0-shaded.jar /opt/health-monitor/
sudo cp scripts/start_monitor.sh /opt/health-monitor/
sudo chmod +x /opt/health-monitor/start_monitor.sh
```

4. **Set ownership**:
```bash
sudo chown -R health-monitor:health-monitor /opt/health-monitor
sudo chown -R health-monitor:health-monitor /var/log/health-monitor
```

5. **Create systemd service**:
```bash
sudo nano /etc/systemd/system/health-monitor.service
```

```ini
[Unit]
Description=Java Application Health Monitor
After=network.target

[Service]
Type=forking
User=health-monitor
WorkingDirectory=/opt/health-monitor
ExecStart=/opt/health-monitor/start_monitor.sh -m continuous -d
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

6. **Enable and start service**:
```bash
sudo systemctl daemon-reload
sudo systemctl enable health-monitor
sudo systemctl start health-monitor
```

## 🪟 Windows Deployment

### Running as a Windows Service

1. **Build the project**:
```cmd
mvn clean package
```

2. **Create a Windows batch file** (`start-monitor.bat`):
```batch
@echo off
cd /d "C:\path\to\health-monitor"
java -jar target\java-health-monitor-1.0.0-shaded.jar --continuous
```

3. **Use NSSM (Non-Sucking Service Manager)** to create a Windows service:
```cmd
nssm install HealthMonitor "C:\path\to\health-monitor\start-monitor.bat"
nssm start HealthMonitor
```

### PowerShell Deployment Script

```powershell
# PowerShell deployment script for Windows
$deployPath = "C:\HealthMonitor"
$serviceName = "JavaHealthMonitor"

# Create deployment directory
New-Item -ItemType Directory -Force -Path $deployPath

# Copy JAR file
Copy-Item "target\java-health-monitor-1.0.0-shaded.jar" -Destination $deployPath

# Create startup script
@"
@echo off
cd /d "$deployPath"
java -jar java-health-monitor-1.0.0-shaded.jar --continuous
"@ | Out-File -FilePath "$deployPath\start.bat" -Encoding ASCII

# Install as Windows service (requires NSSM)
if (Get-Command nssm -ErrorAction SilentlyContinue) {
    nssm install $serviceName "$deployPath\start.bat"
    nssm start $serviceName
    Write-Host "Service installed and started successfully"
} else {
    Write-Host "NSSM not found. Please install NSSM to run as a Windows service"
}
```

## 📊 Understanding the Reports

### JSON Report Structure

```json
{
  "report_metadata": {
    "generator": "Java Application Health Monitor",
    "generator_version": "1.0.0",
    "generated_time_readable": "2024-01-15 14:30:00"
  },
  "current_metrics": {
    "jvm_metrics": {
      "heap_used_mb": 256,
      "heap_max_mb": 1024,
      "heap_usage_percentage": 25.0,
      "current_thread_count": 12,
      "uptime_formatted": "2 hours, 15 minutes, 30 seconds"
    },
    "health_summary": {
      "total_services": 3,
      "healthy_services": 2,
      "unhealthy_services": 1,
      "health_percentage": 66.7,
      "overall_status": "UNHEALTHY"
    }
  },
  "summary_statistics": {
    "heap_usage_statistics": {
      "average_heap_usage_percentage": 23.5,
      "max_heap_usage_percentage": 45.2,
      "min_heap_usage_percentage": 15.1
    }
  }
}
```

### Key Metrics Explained

#### JVM Metrics
- **heap_used_mb**: Current heap memory usage in megabytes
- **heap_max_mb**: Maximum heap memory available
- **heap_usage_percentage**: Percentage of heap memory used
- **current_thread_count**: Number of active threads
- **uptime_ms**: JVM uptime in milliseconds

#### Health Check Metrics
- **is_healthy**: Boolean indicating if service is responding correctly
- **response_time_ms**: Time taken for the service to respond
- **status_code**: HTTP status code returned by the service
- **error_message**: Description of any errors encountered

## 🔍 Troubleshooting

### Common Issues and Solutions

#### 1. "Java not found" Error
**Problem**: Java is not installed or not in PATH
**Solution**:
```bash
# Check if Java is installed
java -version

# If not installed, install Java 11 or later
# Ubuntu/Debian:
sudo apt update && sudo apt install openjdk-11-jdk

# CentOS/RHEL:
sudo yum install java-11-openjdk-devel

# Set JAVA_HOME if needed
export JAVA_HOME=/usr/lib/jvm/java-11-openjdk
```

#### 2. "Maven not found" Error
**Problem**: Maven is not installed
**Solution**:
```bash
# Install Maven
# Ubuntu/Debian:
sudo apt install maven

# CentOS/RHEL:
sudo yum install maven

# Or download manually from https://maven.apache.org/
```

#### 3. "Connection refused" for Health Checks
**Problem**: Services being monitored are not accessible
**Solution**:
- Verify the URLs are correct
- Check if services are running
- Verify network connectivity
- Check firewall settings

#### 4. High Memory Usage
**Problem**: Monitor consuming too much memory
**Solution**:
```bash
# Reduce JVM heap size
java -Xmx256m -jar target/java-health-monitor-1.0.0-shaded.jar

# Reduce history size (modify MetricsCollector constructor)
# Change: new MetricsCollector(30, 100)
# To: new MetricsCollector(30, 50)
```

#### 5. "Permission denied" for Scripts
**Problem**: Shell scripts are not executable
**Solution**:
```bash
chmod +x scripts/*.sh
```

### Debug Mode

Enable debug logging for troubleshooting:

```bash
java -Dorg.slf4j.simpleLogger.defaultLogLevel=DEBUG \
     -jar target/java-health-monitor-1.0.0-shaded.jar
```

## 🧪 Testing the Application

### Unit Testing Individual Components

Test each component separately:

```bash
# Test JVMMonitor
java -cp target/java-health-monitor-1.0.0-shaded.jar monitor.JVMMonitor

# Test HealthChecker with sample URLs
java -cp target/java-health-monitor-1.0.0-shaded.jar monitor.HealthChecker

# Test ReportGenerator with sample data
java -cp target/java-health-monitor-1.0.0-shaded.jar utils.ReportGenerator
```

### Integration Testing

Create a test services file (`test-services.txt`):

```txt
# Test Services - Safe to monitor
Google,https://www.google.com,200
GitHub API,https://api.github.com,200
HTTPBin,https://httpbin.org/status/200,200
```

Run integration test:

```bash
java -jar target/java-health-monitor-1.0.0-shaded.jar --once
```

### Load Testing

Monitor the monitor itself:

```bash
# Run continuous monitoring and observe memory usage
java -Xmx128m -jar target/java-health-monitor-1.0.0-shaded.jar --continuous &

# In another terminal, monitor the monitor
top -p $(pgrep -f health-monitor)
```

## 🔧 Customization and Extension

### Adding Custom Metrics

Extend the `JVMMonitor` class to add custom metrics:

```java
public class CustomJVMMonitor extends JVMMonitor {
    public Map<String, Object> collectCustomMetrics() {
        Map<String, Object> metrics = super.collectMetrics();
        
        // Add custom metrics
        metrics.put("custom_cpu_usage", getCpuUsage());
        metrics.put("custom_disk_space", getDiskSpace());
        
        return metrics;
    }
    
    private double getCpuUsage() {
        // Implementation for CPU usage
        return 0.0;
    }
    
    private long getDiskSpace() {
        // Implementation for disk space
        return 0L;
    }
}
```

### Adding Custom Health Checks

Extend the `HealthChecker` class for custom protocols:

```java
public class CustomHealthChecker extends HealthChecker {
    
    public boolean checkDatabaseHealth(String jdbcUrl) {
        // Implementation for database health check
        try (Connection conn = DriverManager.getConnection(jdbcUrl)) {
            return conn.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
    
    public boolean checkMessageQueueHealth(String mqUrl) {
        // Implementation for message queue health check
        return true; // Placeholder
    }
}
```

### Custom Report Formats

Add new report formats to `ReportGenerator`:

```java
public class CustomReportGenerator extends ReportGenerator {
    
    public void generateXmlReport(Map<String, Object> data, String outputPath) {
        // Implementation for XML report generation
    }
    
    public void generatePdfReport(Map<String, Object> data, String outputPath) {
        // Implementation for PDF report generation
    }
}
```

## 📈 Performance Considerations

### Memory Usage Optimization

1. **Limit Historical Data**: Reduce the `maxHistorySize` parameter
2. **Adjust Monitoring Interval**: Increase interval for less frequent checks
3. **Service Selection**: Monitor only critical services

### Network Optimization

1. **Timeout Configuration**: Set appropriate timeouts for health checks
2. **Parallel Execution**: Use thread pools efficiently
3. **Connection Pooling**: Reuse HTTP connections when possible

### CPU Usage Optimization

1. **Efficient Algorithms**: Use streaming for large datasets
2. **Background Processing**: Offload heavy computations
3. **Caching**: Cache frequently accessed data

## 🔒 Security Considerations

### Network Security

1. **HTTPS Usage**: Monitor services over HTTPS when possible
2. **Authentication**: Add authentication headers for secured endpoints
3. **Network Isolation**: Run monitor in isolated network segments

### Access Control

1. **User Permissions**: Run with minimal required permissions
2. **File Permissions**: Secure configuration and log files
3. **Service Isolation**: Use dedicated system user for the service

### Data Protection

1. **Sensitive Data**: Avoid logging sensitive information
2. **Report Security**: Secure report storage and transmission
3. **Configuration Security**: Protect configuration files

## 📝 License and Contributing

This project is for educational and demonstration purposes. Feel free to:

- Use the code in your own projects
- Modify and extend the functionality
- Share improvements with others
- Use as a learning resource

### Contributing Guidelines

1. **Code Style**: Follow Java naming conventions
2. **Documentation**: Comment your code thoroughly
3. **Testing**: Test new features before submission
4. **Error Handling**: Include proper exception handling

## 📞 Support and Resources

### Getting Help

1. **Documentation**: Read this README thoroughly
2. **Code Comments**: Review the detailed comments in source files
3. **Java Documentation**: Refer to Oracle Java documentation
4. **Maven Documentation**: Check Maven documentation for build issues

### Useful Resources

- [Java Management Extensions (JMX) Tutorial](https://docs.oracle.com/javase/tutorial/jmx/)
- [Apache HttpClient Documentation](https://hc.apache.org/httpcomponents-client-4.5.x/)
- [Jackson JSON Processing](https://github.com/FasterXML/jackson)
- [SLF4J Logging Framework](http://www.slf4j.org/)

## 🚀 What You've Learned

By working with this project, you've gained experience with:

1. **Java Application Architecture**: Organizing code into logical packages and classes
2. **JVM Monitoring**: Understanding how to access and monitor JVM internals
3. **HTTP Client Programming**: Making HTTP requests and handling responses
4. **JSON Processing**: Serializing and deserializing data
5. **Concurrent Programming**: Using thread pools and async processing
6. **File I/O Operations**: Reading configuration files and writing reports
7. **Error Handling**: Implementing robust error handling strategies
8. **Logging**: Using logging frameworks effectively
9. **Build Tools**: Working with Maven for dependency management
10. **System Integration**: Creating services and deployment scripts

This health monitor demonstrates real-world application development patterns and provides a solid foundation for building monitoring and observability tools.

---

**Happy Monitoring! 🎉**