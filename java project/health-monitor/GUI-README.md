# Java Health Monitor - GUI Version

## 🎨 GUI Overview

The Health Monitor GUI provides a comprehensive, user-friendly interface for monitoring your Java application's health metrics in real-time.

## ✨ Features

### 1. **Dashboard Tab**
- **Real-time Metrics Overview**: Visual display of key metrics
  - Heap Usage (with progress bar)
  - Thread Count
  - JVM Uptime
  - Garbage Collection Statistics
- **Detailed Metrics Panel**: Comprehensive text view of all JVM metrics
- **Auto-refresh**: Configurable monitoring intervals

### 2. **Service Monitor Tab**
- Monitor external service endpoints
- Add/remove services dynamically
- Track response times
- Health status indicators
- Automatic health checks

### 3. **Reports Tab**
- View historical monitoring data
- Generate detailed reports
- Export reports in various formats
- Filter reports by date and type

### 4. **Control Panel**
- **Start/Stop Monitoring**: Toggle continuous monitoring
- **Refresh**: Manually update all metrics
- **Export Report**: Save current metrics to file
- **Interval Control**: Adjust monitoring frequency

## 🚀 How to Run

### Option 1: Using Batch File (Windows)
```batch
.\run-gui.bat
```

### Option 2: Using PowerShell
```powershell
.\run-gui.ps1
```

### Option 3: Manual Compilation and Execution
```batch
# Compile
javac -d target\classes -encoding UTF-8 src\monitor\SimpleJVMMonitor.java
javac -d target\classes -encoding UTF-8 -cp target\classes src\gui\*.java

# Run
java -cp target\classes gui.HealthMonitorGUI
```

## 📊 GUI Components

### Main Window
- **Menu Bar**: File, Tools, View, Help menus
- **Tabbed Interface**: Organized into functional areas
- **Status Bar**: Shows current monitoring status
- **Activity Indicator**: Visual feedback during operations

### Monitoring Controls
1. **Start Monitoring**: Begins continuous metric collection
2. **Stop Monitoring**: Pauses monitoring
3. **Refresh**: Updates all metrics immediately
4. **Export Report**: Saves current metrics to timestamped file

### Configuration Dialog
Access via: `Tools → Configuration` or `Ctrl+,`

Configure:
- Monitoring interval (seconds)
- Alert thresholds (memory, CPU)
- Alert preferences (sound, popup)
- Service check intervals
- Export settings
- Theme and appearance

## 🎯 Key Metrics Displayed

### Memory Metrics
- **Heap Used**: Current heap memory usage in MB
- **Heap Max**: Maximum heap memory available
- **Heap Usage %**: Percentage of heap memory in use
- **Non-Heap Used**: Memory used outside heap
- **Committed Memory**: Memory guaranteed to JVM

### Thread Metrics
- **Current Threads**: Active threads count
- **Peak Threads**: Maximum concurrent threads
- **Daemon Threads**: Background threads count
- **Total Started**: All threads created since JVM start

### Runtime Metrics
- **Uptime**: How long JVM has been running
- **JVM Name & Version**: Java Virtual Machine details
- **Available Processors**: CPU cores available
- **OS Information**: Operating system details

### Garbage Collection Metrics
- **Total Collections**: GC cycles performed
- **Total GC Time**: Time spent in garbage collection
- **Average GC Time**: Mean time per collection
- **Individual GC Stats**: Per-collector metrics

## 🛠️ Menu Options

### File Menu
- **Export Report** (Ctrl+E): Save current metrics
- **Exit** (Ctrl+Q): Close application

### Tools Menu
- **Configuration** (Ctrl+,): Open settings dialog
- **Refresh All**: Update all panels
- **Clear Data**: Reset monitoring data

### View Menu
- **Dashboard**: Switch to dashboard view
- **Services**: View service monitoring
- **Reports**: Access reports panel
- **Fullscreen** (F11): Toggle fullscreen mode

### Help Menu
- **User Guide**: Display help documentation
- **About**: Application information

## 🎨 Color Coding

The GUI uses intuitive color coding:

- **Blue (Primary)**: Normal operations, info
- **Green (Success)**: Healthy status, successful operations
- **Yellow (Warning)**: Approaching thresholds
- **Red (Danger)**: Critical issues, errors

## 📝 Export Reports

Reports are automatically saved with timestamps:
- Format: `health-monitor-gui-report-[timestamp].txt`
- Location: Current working directory
- Contents: Complete metrics snapshot

## ⚙️ Advanced Features

### Continuous Monitoring
- Set custom intervals (1-300 seconds)
- Runs in background thread
- Non-blocking UI updates
- Automatic metric collection

### Service Health Checks
- Add multiple service endpoints
- HTTP/HTTPS support
- Configurable timeout periods
- Response time tracking
- Success/failure indicators

### Historical Data
- Tracks metrics over time
- Statistical analysis
- Trend visualization
- Export capabilities

## 🔧 Integration with SimpleJVMMonitor

The GUI is fully integrated with `SimpleJVMMonitor` class:

```java
// The GUI creates an instance of SimpleJVMMonitor
private SimpleJVMMonitor jvmMonitor = new SimpleJVMMonitor();

// Collects metrics on demand
Map<String, Object> metrics = jvmMonitor.collectMetrics();

// Updates GUI components with real-time data
updateMetricsDisplay(metrics);
```

### Seamless Integration Features:
1. **Direct API Access**: GUI calls SimpleJVMMonitor methods directly
2. **Real-time Updates**: Metrics refreshed automatically
3. **Thread-safe Operations**: Monitoring runs on separate thread
4. **Resource Efficient**: Minimal overhead on monitored application

## 💡 Tips for Best Use

1. **Start with 30-second intervals** for general monitoring
2. **Use 5-10 seconds** for intensive monitoring/debugging
3. **Enable alerts** for proactive issue detection
4. **Export reports regularly** for historical analysis
5. **Monitor services** that your application depends on
6. **Check GC metrics** to identify memory issues

## 🐛 Troubleshooting

### GUI doesn't start
- Ensure Java 11+ is installed: `java -version`
- Check all Java files compiled successfully
- Verify no other GUI applications blocking display

### Metrics not updating
- Check if monitoring is started (green button)
- Verify monitoring interval is reasonable
- Click "Refresh" to force update

### High memory usage in GUI
- Reduce monitoring frequency
- Clear historical data periodically
- Close unused tabs

## 📚 Related Files

- `HealthMonitorGUI.java` - Main GUI application
- `SimpleJVMMonitor.java` - Core monitoring engine
- `ServiceMonitorPanel.java` - Service monitoring UI
- `ReportsPanel.java` - Reports and analytics UI
- `ConfigurationDialog.java` - Settings management
- `run-gui.bat` - Windows launcher
- `run-gui.ps1` - PowerShell launcher

## 🎓 Learning Resources

The GUI demonstrates several Java Swing concepts:
- Event-driven programming
- Layout managers
- Threading with SwingUtilities
- Model-View-Controller pattern
- Custom components
- Menu systems

Explore the source code to learn more about GUI development in Java!

---

**Enjoy monitoring your Java applications with style!** 🚀
