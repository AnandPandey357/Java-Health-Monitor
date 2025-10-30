package gui;

import monitor.SimpleJVMMonitor;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * HealthMonitorGUI - Main graphical user interface for the Java Health Monitor
 * This class provides a comprehensive Swing-based GUI for monitoring JVM metrics
 * and managing health checks in real-time
 */
public class HealthMonitorGUI extends JFrame {
    
    // Core monitoring components
    private SimpleJVMMonitor jvmMonitor;
    private ScheduledExecutorService scheduler;
    
    // GUI Components - Main panels
    private JPanel mainPanel;
    private JTabbedPane tabbedPane;
    private JPanel dashboardPanel;
    private ServiceMonitorPanel servicesPanel;
    private ReportsPanel reportsPanel;
    
    // GUI Components - Dashboard elements
    private JLabel heapUsageLabel;
    private JProgressBar heapUsageBar;
    private JLabel threadCountLabel;
    private JLabel uptimeLabel;
    private JLabel gcCountLabel;
    private JTextArea metricsTextArea;
    private JScrollPane metricsScrollPane;
    
    // GUI Components - Control elements
    private JButton startMonitoringBtn;
    private JButton stopMonitoringBtn;
    private JButton refreshBtn;
    private JButton exportReportBtn;
    private JSpinner intervalSpinner;
    
    // GUI Components - Status elements
    private JLabel statusLabel;
    private JProgressBar activityIndicator;
    
    // Monitoring configuration
    private boolean isMonitoring = false;
    private int monitoringInterval = 30; // seconds
    
    // Colors for UI theming
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color BACKGROUND_COLOR = new Color(248, 249, 250);
    
    /**
     * Constructor initializes the GUI and sets up all components
     */
    public HealthMonitorGUI() {
        // Initialize the JVM monitor
        this.jvmMonitor = new SimpleJVMMonitor();
        
        // Initialize the scheduler for periodic monitoring
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // Set up the main window
        initializeMainWindow();
        
        // Create menu bar
        createMenuBar();
        
        // Create and configure all GUI components
        createComponents();
        
        // Set up the layout and arrange components
        setupLayout();
        
        // Add event listeners
        setupEventListeners();
        
        // Perform initial metrics collection
        updateMetrics();
        
        // Set window properties and make visible
        finalizeWindow();
    }
    
    /**
     * Initializes the main window properties
     */
    private void initializeMainWindow() {
        setTitle("Java Application Health Monitor - GUI Dashboard");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Custom close handling
        setSize(1000, 700);
        setMinimumSize(new Dimension(800, 600));
        
        // Center the window on screen
        setLocationRelativeTo(null);
        
        // Set application icon (if available)
        try {
            // You can add an icon here if you have one
            // setIconImage(Toolkit.getDefaultToolkit().getImage("icon.png"));
        } catch (Exception e) {
            // Icon not available, continue without it
        }
        
        // Set background color
        getContentPane().setBackground(BACKGROUND_COLOR);
    }
    
    /**
     * Creates all GUI components
     */
    private void createComponents() {
        // Create main panel
        mainPanel = new JPanel();
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Create tabbed pane for different sections
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Create individual panels
        createDashboardPanel();
        createServicesPanel();
        createReportsPanel();
        
        // Add panels to tabbed pane
        tabbedPane.addTab("📊 Dashboard", dashboardPanel);
        tabbedPane.addTab("🔍 Services", servicesPanel);
        tabbedPane.addTab("📋 Reports", reportsPanel);
        
        // Create control panel
        createControlPanel();
        
        // Create status bar
        createStatusBar();
    }
    
    /**
     * Creates the main dashboard panel with real-time metrics
     */
    private void createDashboardPanel() {
        dashboardPanel = new JPanel(new BorderLayout());
        dashboardPanel.setBackground(BACKGROUND_COLOR);
        
        // Create metrics overview panel (top section)
        JPanel overviewPanel = createMetricsOverviewPanel();
        
        // Create detailed metrics panel (center section)
        JPanel detailsPanel = createMetricsDetailsPanel();
        
        // Add panels to dashboard
        dashboardPanel.add(overviewPanel, BorderLayout.NORTH);
        dashboardPanel.add(detailsPanel, BorderLayout.CENTER);
    }
    
    /**
     * Creates the metrics overview panel with key indicators
     */
    private JPanel createMetricsOverviewPanel() {
        JPanel overviewPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        overviewPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "System Overview",
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.BOLD, 14),
            PRIMARY_COLOR
        ));
        overviewPanel.setBackground(BACKGROUND_COLOR);
        
        // Heap Usage Panel
        JPanel heapPanel = createMetricPanel("Heap Memory Usage", PRIMARY_COLOR);
        heapUsageLabel = new JLabel("0 MB / 0 MB (0.0%)", SwingConstants.CENTER);
        heapUsageLabel.setFont(new Font("Arial", Font.BOLD, 14));
        heapUsageBar = new JProgressBar(0, 100);
        heapUsageBar.setStringPainted(true);
        heapUsageBar.setForeground(SUCCESS_COLOR);
        heapPanel.add(heapUsageLabel, BorderLayout.CENTER);
        heapPanel.add(heapUsageBar, BorderLayout.SOUTH);
        
        // Thread Count Panel
        JPanel threadPanel = createMetricPanel("Active Threads", PRIMARY_COLOR);
        threadCountLabel = new JLabel("0 threads", SwingConstants.CENTER);
        threadCountLabel.setFont(new Font("Arial", Font.BOLD, 16));
        threadPanel.add(threadCountLabel, BorderLayout.CENTER);
        
        // Uptime Panel
        JPanel uptimePanel = createMetricPanel("JVM Uptime", PRIMARY_COLOR);
        uptimeLabel = new JLabel("0 minutes", SwingConstants.CENTER);
        uptimeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        uptimePanel.add(uptimeLabel, BorderLayout.CENTER);
        
        // Garbage Collection Panel
        JPanel gcPanel = createMetricPanel("Garbage Collections", PRIMARY_COLOR);
        gcCountLabel = new JLabel("0 collections", SwingConstants.CENTER);
        gcCountLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gcPanel.add(gcCountLabel, BorderLayout.CENTER);
        
        // Add all panels to overview
        overviewPanel.add(heapPanel);
        overviewPanel.add(threadPanel);
        overviewPanel.add(uptimePanel);
        overviewPanel.add(gcPanel);
        
        return overviewPanel;
    }
    
    /**
     * Creates a styled metric panel
     */
    private JPanel createMetricPanel(String title, Color borderColor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(borderColor, 1),
            title,
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.PLAIN, 12),
            borderColor
        ));
        panel.setBackground(Color.WHITE);
        return panel;
    }
    
    /**
     * Creates the detailed metrics panel with full information
     */
    private JPanel createMetricsDetailsPanel() {
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 2),
            "Detailed Metrics",
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            new Font("Arial", Font.BOLD, 14),
            PRIMARY_COLOR
        ));
        detailsPanel.setBackground(BACKGROUND_COLOR);
        
        // Create text area for detailed metrics
        metricsTextArea = new JTextArea();
        metricsTextArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        metricsTextArea.setEditable(false);
        metricsTextArea.setBackground(Color.WHITE);
        metricsTextArea.setMargin(new Insets(10, 10, 10, 10));
        
        // Add scroll pane
        metricsScrollPane = new JScrollPane(metricsTextArea);
        metricsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        metricsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        detailsPanel.add(metricsScrollPane, BorderLayout.CENTER);
        
        return detailsPanel;
    }
    
    /**
     * Creates the services monitoring panel
     */
    private void createServicesPanel() {
        servicesPanel = new ServiceMonitorPanel();
    }
    
    /**
     * Creates the reports panel
     */
    private void createReportsPanel() {
        reportsPanel = new ReportsPanel();
    }
    
    /**
     * Creates the control panel with monitoring controls
     */
    private void createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        controlPanel.setBackground(BACKGROUND_COLOR);
        controlPanel.setBorder(BorderFactory.createTitledBorder("Monitoring Controls"));
        
        // Start monitoring button
        startMonitoringBtn = new JButton("▶ Start Monitoring");
        startMonitoringBtn.setBackground(SUCCESS_COLOR);
        startMonitoringBtn.setForeground(Color.WHITE);
        startMonitoringBtn.setFont(new Font("Arial", Font.BOLD, 12));
        startMonitoringBtn.setFocusPainted(false);
        
        // Stop monitoring button
        stopMonitoringBtn = new JButton("⏹ Stop Monitoring");
        stopMonitoringBtn.setBackground(DANGER_COLOR);
        stopMonitoringBtn.setForeground(Color.WHITE);
        stopMonitoringBtn.setFont(new Font("Arial", Font.BOLD, 12));
        stopMonitoringBtn.setFocusPainted(false);
        stopMonitoringBtn.setEnabled(false);
        
        // Refresh button
        refreshBtn = new JButton("🔄 Refresh Now");
        refreshBtn.setBackground(PRIMARY_COLOR);
        refreshBtn.setForeground(Color.WHITE);
        refreshBtn.setFont(new Font("Arial", Font.BOLD, 12));
        refreshBtn.setFocusPainted(false);
        
        // Export report button
        exportReportBtn = new JButton("📄 Export Report");
        exportReportBtn.setBackground(WARNING_COLOR);
        exportReportBtn.setForeground(Color.WHITE);
        exportReportBtn.setFont(new Font("Arial", Font.BOLD, 12));
        exportReportBtn.setFocusPainted(false);
        
        // Monitoring interval spinner
        JLabel intervalLabel = new JLabel("Interval (seconds):");
        intervalLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        intervalSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 300, 5));
        intervalSpinner.setFont(new Font("Arial", Font.PLAIN, 12));
        
        // Activity indicator
        activityIndicator = new JProgressBar();
        activityIndicator.setIndeterminate(false);
        activityIndicator.setStringPainted(true);
        activityIndicator.setString("Ready");
        activityIndicator.setPreferredSize(new Dimension(150, 25));
        
        // Add components to control panel
        controlPanel.add(startMonitoringBtn);
        controlPanel.add(stopMonitoringBtn);
        controlPanel.add(new JSeparator(SwingConstants.VERTICAL));
        controlPanel.add(refreshBtn);
        controlPanel.add(exportReportBtn);
        controlPanel.add(new JSeparator(SwingConstants.VERTICAL));
        controlPanel.add(intervalLabel);
        controlPanel.add(intervalSpinner);
        controlPanel.add(new JSeparator(SwingConstants.VERTICAL));
        controlPanel.add(activityIndicator);
        
        // Add control panel to main panel
        mainPanel.add(controlPanel, BorderLayout.NORTH);
    }
    
    /**
     * Creates the status bar at the bottom of the window
     */
    private void createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(BACKGROUND_COLOR);
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        
        statusLabel = new JLabel("Ready - Java Health Monitor GUI v1.0");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        
        JLabel timestampLabel = new JLabel("Last Update: " + new java.util.Date());
        timestampLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        timestampLabel.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(timestampLabel, BorderLayout.EAST);
        
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Sets up the main layout and adds components
     */
    private void setupLayout() {
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    /**
     * Sets up event listeners for all interactive components
     */
    private void setupEventListeners() {
        // Start monitoring button
        startMonitoringBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startMonitoring();
            }
        });
        
        // Stop monitoring button
        stopMonitoringBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopMonitoring();
            }
        });
        
        // Refresh button
        refreshBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMetrics();
            }
        });
        
        // Export report button
        exportReportBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportReport();
            }
        });
        
        // Interval spinner
        intervalSpinner.addChangeListener(e -> {
            monitoringInterval = (Integer) intervalSpinner.getValue();
            if (isMonitoring) {
                // Restart monitoring with new interval
                stopMonitoring();
                startMonitoring();
            }
        });
        
        // Window close event
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleWindowClose();
            }
        });
    }
    
    /**
     * Starts continuous monitoring
     */
    private void startMonitoring() {
        if (isMonitoring) return;
        
        isMonitoring = true;
        startMonitoringBtn.setEnabled(false);
        stopMonitoringBtn.setEnabled(true);
        activityIndicator.setIndeterminate(true);
        activityIndicator.setString("Monitoring...");
        statusLabel.setText("Monitoring started - Interval: " + monitoringInterval + " seconds");
        
        // Schedule periodic monitoring
        scheduler.scheduleAtFixedRate(
            this::updateMetrics,
            0,
            monitoringInterval,
            TimeUnit.SECONDS
        );
    }
    
    /**
     * Stops continuous monitoring
     */
    private void stopMonitoring() {
        if (!isMonitoring) return;
        
        isMonitoring = false;
        startMonitoringBtn.setEnabled(true);
        stopMonitoringBtn.setEnabled(false);
        activityIndicator.setIndeterminate(false);
        activityIndicator.setString("Stopped");
        statusLabel.setText("Monitoring stopped");
        
        // Cancel scheduled monitoring
        scheduler.shutdown();
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }
    
    /**
     * Updates all metrics displays with current data
     */
    private void updateMetrics() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Collect current metrics
                Map<String, Object> metrics = jvmMonitor.collectMetrics();
                
                // Update overview panels
                updateOverviewPanels(metrics);
                
                // Update detailed metrics text
                updateDetailedMetrics(metrics);
                
                // Update timestamp in status bar
                JPanel statusPanel = (JPanel) mainPanel.getComponent(1);
                JLabel timestampLabel = (JLabel) ((JPanel) statusPanel).getComponent(1);
                timestampLabel.setText("Last Update: " + new java.util.Date());
                
            } catch (Exception e) {
                statusLabel.setText("Error updating metrics: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Updates the overview panels with current metrics
     */
    private void updateOverviewPanels(Map<String, Object> metrics) {
        // Update heap usage
        Long heapUsed = (Long) metrics.get("heap_used_mb");
        Long heapMax = (Long) metrics.get("heap_max_mb");
        Double heapPercentage = (Double) metrics.get("heap_usage_percentage");
        
        if (heapUsed != null && heapMax != null && heapPercentage != null) {
            heapUsageLabel.setText(String.format("%d MB / %d MB (%.1f%%)", heapUsed, heapMax, heapPercentage));
            heapUsageBar.setValue((int) Math.round(heapPercentage));
            
            // Update color based on usage
            if (heapPercentage > 80) {
                heapUsageBar.setForeground(DANGER_COLOR);
            } else if (heapPercentage > 60) {
                heapUsageBar.setForeground(WARNING_COLOR);
            } else {
                heapUsageBar.setForeground(SUCCESS_COLOR);
            }
        }
        
        // Update thread count
        Integer threadCount = (Integer) metrics.get("current_thread_count");
        if (threadCount != null) {
            threadCountLabel.setText(threadCount + " threads");
        }
        
        // Update uptime
        String uptimeFormatted = (String) metrics.get("uptime_formatted");
        if (uptimeFormatted != null) {
            uptimeLabel.setText(uptimeFormatted);
        }
        
        // Update GC count
        Long gcCount = (Long) metrics.get("total_gc_collections");
        if (gcCount != null) {
            gcCountLabel.setText(gcCount + " collections");
        }
    }
    
    /**
     * Updates the detailed metrics text area
     */
    private void updateDetailedMetrics(Map<String, Object> metrics) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== JAVA APPLICATION HEALTH MONITOR - DETAILED METRICS ===\n");
        sb.append("Timestamp: ").append(new java.util.Date()).append("\n\n");
        
        // Memory Metrics
        sb.append("MEMORY METRICS:\n");
        sb.append("  Heap Used: ").append(metrics.get("heap_used_mb")).append(" MB\n");
        sb.append("  Heap Max: ").append(metrics.get("heap_max_mb")).append(" MB\n");
        sb.append("  Heap Committed: ").append(metrics.get("heap_committed_mb")).append(" MB\n");
        sb.append("  Heap Usage: ").append(String.format("%.2f", metrics.get("heap_usage_percentage"))).append("%\n");
        sb.append("  Non-Heap Used: ").append(metrics.get("non_heap_used_mb")).append(" MB\n");
        sb.append("  Total Memory: ").append(metrics.get("total_memory_mb")).append(" MB\n");
        sb.append("  Free Memory: ").append(metrics.get("free_memory_mb")).append(" MB\n");
        sb.append("  Used Memory: ").append(metrics.get("used_memory_mb")).append(" MB\n\n");
        
        // Thread Metrics
        sb.append("THREAD METRICS:\n");
        sb.append("  Current Threads: ").append(metrics.get("current_thread_count")).append("\n");
        sb.append("  Peak Threads: ").append(metrics.get("peak_thread_count")).append("\n");
        sb.append("  Daemon Threads: ").append(metrics.get("daemon_thread_count")).append("\n");
        sb.append("  Total Started Threads: ").append(metrics.get("total_started_threads")).append("\n");
        if (metrics.containsKey("current_thread_cpu_time_ms")) {
            sb.append("  Current Thread CPU Time: ").append(metrics.get("current_thread_cpu_time_ms")).append(" ms\n");
        }
        sb.append("\n");
        
        // Runtime Metrics
        sb.append("RUNTIME METRICS:\n");
        sb.append("  Uptime: ").append(metrics.get("uptime_formatted")).append("\n");
        sb.append("  Uptime (ms): ").append(metrics.get("uptime_ms")).append("\n");
        sb.append("  JVM Name: ").append(metrics.get("jvm_name")).append("\n");
        sb.append("  JVM Version: ").append(metrics.get("jvm_version")).append("\n");
        sb.append("  JVM Vendor: ").append(metrics.get("jvm_vendor")).append("\n");
        sb.append("  Java Version: ").append(metrics.get("java_version")).append("\n");
        sb.append("  OS Name: ").append(metrics.get("os_name")).append("\n");
        sb.append("  OS Architecture: ").append(metrics.get("os_arch")).append("\n");
        sb.append("  Available Processors: ").append(metrics.get("available_processors")).append("\n\n");
        
        // Garbage Collection Metrics
        sb.append("GARBAGE COLLECTION METRICS:\n");
        sb.append("  Total Collections: ").append(metrics.get("total_gc_collections")).append("\n");
        sb.append("  Total GC Time: ").append(metrics.get("total_gc_time_ms")).append(" ms\n");
        if (metrics.containsKey("avg_gc_time_ms")) {
            sb.append("  Average GC Time: ").append(String.format("%.2f", metrics.get("avg_gc_time_ms"))).append(" ms\n");
        }
        
        metricsTextArea.setText(sb.toString());
        metricsTextArea.setCaretPosition(0); // Scroll to top
    }
    
    /**
     * Exports current metrics to a report file
     */
    private void exportReport() {
        try {
            Map<String, Object> metrics = jvmMonitor.collectMetrics();
            
            // Create filename with timestamp
            String timestamp = String.valueOf(System.currentTimeMillis());
            String filename = "health-monitor-gui-report-" + timestamp + ".txt";
            
            // Write report to file
            java.io.FileWriter writer = new java.io.FileWriter(filename);
            writer.write(metricsTextArea.getText());
            writer.close();
            
            // Show success message
            JOptionPane.showMessageDialog(
                this,
                "Report exported successfully to:\n" + filename,
                "Export Successful",
                JOptionPane.INFORMATION_MESSAGE
            );
            
            statusLabel.setText("Report exported: " + filename);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                this,
                "Error exporting report:\n" + e.getMessage(),
                "Export Error",
                JOptionPane.ERROR_MESSAGE
            );
            statusLabel.setText("Export failed: " + e.getMessage());
        }
    }
    
    /**
     * Handles window close event
     */
    private void handleWindowClose() {
        int option = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to exit the Health Monitor?",
            "Confirm Exit",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            // Stop monitoring if running
            if (isMonitoring) {
                stopMonitoring();
            }
            
            // Shutdown scheduler
            scheduler.shutdown();
            
            // Exit application
            System.exit(0);
        }
    }
    
    /**
     * Finalizes window setup and makes it visible
     */
    /**
     * Creates the menu bar with File, Tools, and Help menus
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('F');
        
        JMenuItem exportMenuItem = new JMenuItem("Export Report...");
        exportMenuItem.setMnemonic('E');
        exportMenuItem.addActionListener(e -> exportReport());
        
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.setMnemonic('x');
        exitMenuItem.addActionListener(e -> {
            int option = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to exit the Health Monitor?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION
            );
            if (option == JOptionPane.YES_OPTION) {
                stopMonitoring();
                dispose();
                System.exit(0);
            }
        });
        
        fileMenu.add(exportMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        
        // Tools Menu
        JMenu toolsMenu = new JMenu("Tools");
        toolsMenu.setMnemonic('T');
        
        JMenuItem configMenuItem = new JMenuItem("Configuration...");
        configMenuItem.setMnemonic('C');
        configMenuItem.addActionListener(e -> showConfigurationDialog());
        
        JMenuItem refreshMenuItem = new JMenuItem("Refresh All");
        refreshMenuItem.setMnemonic('R');
        refreshMenuItem.addActionListener(e -> refreshAllData());
        
        JMenuItem gcMenuItem = new JMenuItem("Force Garbage Collection");
        gcMenuItem.setMnemonic('G');
        gcMenuItem.addActionListener(e -> {
            System.gc();
            updateMetrics();
            statusLabel.setText("Garbage collection triggered");
        });
        
        toolsMenu.add(configMenuItem);
        toolsMenu.addSeparator();
        toolsMenu.add(refreshMenuItem);
        toolsMenu.add(gcMenuItem);
        
        // View Menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic('V');
        
        JMenuItem fullScreenMenuItem = new JMenuItem("Toggle Full Screen");
        fullScreenMenuItem.setMnemonic('F');
        fullScreenMenuItem.addActionListener(e -> toggleFullScreen());
        
        JMenuItem alwaysOnTopMenuItem = new JMenuItem("Always on Top");
        alwaysOnTopMenuItem.setMnemonic('A');
        alwaysOnTopMenuItem.addActionListener(e -> {
            boolean current = isAlwaysOnTop();
            setAlwaysOnTop(!current);
            statusLabel.setText("Always on top: " + !current);
        });
        
        viewMenu.add(fullScreenMenuItem);
        viewMenu.add(alwaysOnTopMenuItem);
        
        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('H');
        
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.setMnemonic('A');
        aboutMenuItem.addActionListener(e -> showAboutDialog());
        
        JMenuItem userGuideMenuItem = new JMenuItem("User Guide");
        userGuideMenuItem.setMnemonic('U');
        userGuideMenuItem.addActionListener(e -> showUserGuide());
        
        helpMenu.add(userGuideMenuItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutMenuItem);
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(toolsMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Shows the configuration dialog
     */
    private void showConfigurationDialog() {
        ConfigurationDialog dialog = new ConfigurationDialog(this);
        dialog.setVisible(true);
    }
    
    /**
     * Applies configuration settings from the dialog
     */
    public void applyConfigurationSettings() {
        // Update monitoring interval if needed
        int newInterval = ConfigurationDialog.getMonitoringInterval();
        if (scheduler != null && !scheduler.isShutdown()) {
            // Restart scheduler with new interval
            stopMonitoring();
            startMonitoring();
        }
        
        statusLabel.setText("Configuration updated successfully");
    }
    
    /**
     * Refreshes all data in all panels
     */
    private void refreshAllData() {
        updateMetrics();
        if (servicesPanel != null) {
            servicesPanel.performHealthChecks();
        }
        statusLabel.setText("All data refreshed");
    }
    
    /**
     * Toggles full screen mode
     */
    private void toggleFullScreen() {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (device.getFullScreenWindow() == this) {
            device.setFullScreenWindow(null);
            statusLabel.setText("Exited full screen mode");
        } else {
            device.setFullScreenWindow(this);
            statusLabel.setText("Entered full screen mode");
        }
    }
    
    /**
     * Shows the about dialog
     */
    private void showAboutDialog() {
        String aboutText = 
            "<html><center><h2>Java Application Health Monitor</h2>" +
            "<p><b>Version:</b> 2.0</p>" +
            "<p><b>Author:</b> Health Monitor Development Team</p>" +
            "<p><b>Description:</b> Comprehensive JVM and service health monitoring tool</p>" +
            "<br>" +
            "<p><b>Features:</b></p>" +
            "<ul style='text-align: left;'>" +
            "<li>Real-time JVM metrics monitoring</li>" +
            "<li>External service health checks</li>" +
            "<li>Comprehensive reporting and analytics</li>" +
            "<li>Configurable alerts and notifications</li>" +
            "<li>Export capabilities for reports</li>" +
            "</ul>" +
            "<br>" +
            "<p>Built with Java Swing for cross-platform compatibility</p>" +
            "</center></html>";
            
        JOptionPane.showMessageDialog(this, aboutText, "About Health Monitor", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Shows the user guide
     */
    private void showUserGuide() {
        String guideText = 
            "<html><h2>Health Monitor User Guide</h2>" +
            "<h3>Dashboard Tab:</h3>" +
            "<ul>" +
            "<li>View real-time JVM memory, thread, and GC metrics</li>" +
            "<li>Use Start/Stop buttons to control monitoring</li>" +
            "<li>Adjust monitoring interval with the spinner</li>" +
            "</ul>" +
            "<h3>Services Tab:</h3>" +
            "<ul>" +
            "<li>Add external service endpoints to monitor</li>" +
            "<li>View service health status and response times</li>" +
            "<li>Check services individually or all at once</li>" +
            "</ul>" +
            "<h3>Reports Tab:</h3>" +
            "<ul>" +
            "<li>View historical monitoring data</li>" +
            "<li>Filter reports by type and date range</li>" +
            "<li>Export reports to text files</li>" +
            "</ul>" +
            "<h3>Configuration:</h3>" +
            "<ul>" +
            "<li>Access Tools > Configuration to customize settings</li>" +
            "<li>Set monitoring intervals and alert thresholds</li>" +
            "<li>Configure appearance and advanced options</li>" +
            "</ul>" +
            "</html>";
            
        JDialog guideDialog = new JDialog(this, "User Guide", true);
        JTextPane textPane = new JTextPane();
        textPane.setContentType("text/html");
        textPane.setText(guideText);
        textPane.setEditable(false);
        textPane.setCaretPosition(0);
        
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        
        guideDialog.add(scrollPane);
        guideDialog.pack();
        guideDialog.setLocationRelativeTo(this);
        guideDialog.setVisible(true);
    }
    
    /**
     * Finalizes the window setup and makes it visible
     */
    private void finalizeWindow() {
        pack();
        setSize(1000, 700); // Override pack size
        setVisible(true);
    }
    
    /**
     * Main method to launch the GUI application
     */
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel if system L&F is not available
        }
        
        // Create and show GUI on EDT
        SwingUtilities.invokeLater(() -> {
            System.out.println("Starting Java Health Monitor GUI...");
            new HealthMonitorGUI();
        });
    }
}