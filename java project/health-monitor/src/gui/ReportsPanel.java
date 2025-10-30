package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * ReportsPanel - Panel for viewing and exporting monitoring reports
 * This panel displays historical data, trends, and allows exporting reports
 */
public class ReportsPanel extends JPanel {
    
    // Report data structure
    public static class MonitoringReport {
        private Date timestamp;
        private String reportType;
        private Map<String, Object> data;
        private String summary;
        
        public MonitoringReport(String reportType, Map<String, Object> data, String summary) {
            this.timestamp = new Date();
            this.reportType = reportType;
            this.data = data;
            this.summary = summary;
        }
        
        // Getters and setters
        public Date getTimestamp() { return timestamp; }
        public String getReportType() { return reportType; }
        public Map<String, Object> getData() { return data; }
        public String getSummary() { return summary; }
    }
    
    // GUI Components
    private JTable reportsTable;
    private DefaultTableModel tableModel;
    private JComboBox<String> reportTypeFilter;
    private JSpinner fromDateSpinner;
    private JSpinner toDateSpinner;
    private JButton filterButton;
    private JButton generateReportButton;
    private JButton exportButton;
    private JButton viewDetailsButton;
    private JTextArea reportPreview;
    private JLabel reportCountLabel;
    
    // Chart components
    private JPanel chartPanel;
    private JTabbedPane chartTabs;
    
    // Data
    private List<MonitoringReport> allReports;
    private List<MonitoringReport> filteredReports;
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color INFO_COLOR = new Color(155, 89, 182);
    
    /**
     * Constructor initializes the reports panel
     */
    public ReportsPanel() {
        this.allReports = new ArrayList<>();
        this.filteredReports = new ArrayList<>();
        
        // Generate some sample reports for demonstration
        generateSampleReports();
        
        // Initialize GUI components
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        // Initial data load
        refreshTable();
        updateReportCount();
    }
    
    /**
     * Generates sample reports for demonstration
     */
    private void generateSampleReports() {
        Random random = new Random();
        Calendar cal = Calendar.getInstance();
        
        // Generate reports for the last 7 days
        for (int day = 7; day >= 0; day--) {
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH, -day);
            
            // JVM Memory Reports
            Map<String, Object> memoryData = new HashMap<>();
            memoryData.put("heapUsed", 150 + random.nextInt(100));
            memoryData.put("heapMax", 512);
            memoryData.put("nonHeapUsed", 45 + random.nextInt(20));
            memoryData.put("gcCount", 15 + random.nextInt(10));
            
            MonitoringReport memoryReport = new MonitoringReport("JVM Memory", memoryData,
                String.format("Heap usage: %dMB/%dMB (%.1f%%)", 
                    (Integer)memoryData.get("heapUsed"), 
                    (Integer)memoryData.get("heapMax"),
                    ((Integer)memoryData.get("heapUsed")).doubleValue() / ((Integer)memoryData.get("heapMax")) * 100));
            memoryReport.timestamp = cal.getTime();
            allReports.add(memoryReport);
            
            // Service Health Reports
            Map<String, Object> serviceData = new HashMap<>();
            serviceData.put("totalServices", 4);
            serviceData.put("healthyServices", 3 + random.nextInt(2));
            serviceData.put("averageResponseTime", 200 + random.nextInt(300));
            serviceData.put("failedChecks", random.nextInt(3));
            
            MonitoringReport serviceReport = new MonitoringReport("Service Health", serviceData,
                String.format("%d/%d services healthy, avg response: %dms", 
                    (Integer)serviceData.get("healthyServices"),
                    (Integer)serviceData.get("totalServices"),
                    (Integer)serviceData.get("averageResponseTime")));
            serviceReport.timestamp = cal.getTime();
            allReports.add(serviceReport);
            
            // System Performance Reports
            Map<String, Object> systemData = new HashMap<>();
            systemData.put("cpuUsage", 30.0 + random.nextDouble() * 40);
            systemData.put("threadCount", 8 + random.nextInt(5));
            systemData.put("classesLoaded", 1200 + random.nextInt(300));
            
            MonitoringReport systemReport = new MonitoringReport("System Performance", systemData,
                String.format("CPU: %.1f%%, Threads: %d, Classes: %d", 
                    (Double)systemData.get("cpuUsage"),
                    (Integer)systemData.get("threadCount"),
                    (Integer)systemData.get("classesLoaded")));
            systemReport.timestamp = cal.getTime();
            allReports.add(systemReport);
        }
        
        // Sort reports by timestamp (newest first)
        allReports.sort((r1, r2) -> r2.getTimestamp().compareTo(r1.getTimestamp()));
    }
    
    /**
     * Initializes all GUI components
     */
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Create filter panel
        createFilterPanel();
        
        // Create main content (split pane)
        createMainContent();
        
        // Create charts panel
        createChartsPanel();
    }
    
    /**
     * Creates the filter panel
     */
    private void createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(Color.WHITE);
        filterPanel.setBorder(BorderFactory.createTitledBorder("Report Filters"));
        
        // Report type filter
        filterPanel.add(new JLabel("Report Type:"));
        reportTypeFilter = new JComboBox<>(new String[]{
            "All Reports", "JVM Memory", "Service Health", "System Performance"
        });
        filterPanel.add(reportTypeFilter);
        
        // Date range filters
        filterPanel.add(Box.createHorizontalStrut(20));
        filterPanel.add(new JLabel("From:"));
        
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -7);
        fromDateSpinner = new JSpinner(new SpinnerDateModel(cal.getTime(), null, null, Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor fromEditor = new JSpinner.DateEditor(fromDateSpinner, "yyyy-MM-dd");
        fromDateSpinner.setEditor(fromEditor);
        filterPanel.add(fromDateSpinner);
        
        filterPanel.add(new JLabel("To:"));
        toDateSpinner = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor toEditor = new JSpinner.DateEditor(toDateSpinner, "yyyy-MM-dd");
        toDateSpinner.setEditor(toEditor);
        filterPanel.add(toDateSpinner);
        
        // Filter button
        filterButton = new JButton("🔍 Apply Filter");
        filterButton.setBackground(PRIMARY_COLOR);
        filterButton.setForeground(Color.WHITE);
        filterButton.setFont(new Font("Arial", Font.BOLD, 12));
        filterButton.setFocusPainted(false);
        filterPanel.add(filterButton);
        
        // Generate report button
        generateReportButton = new JButton("📊 Generate Report");
        generateReportButton.setBackground(SUCCESS_COLOR);
        generateReportButton.setForeground(Color.WHITE);
        generateReportButton.setFont(new Font("Arial", Font.BOLD, 12));
        generateReportButton.setFocusPainted(false);
        filterPanel.add(generateReportButton);
        
        // Export button
        exportButton = new JButton("💾 Export");
        exportButton.setBackground(INFO_COLOR);
        exportButton.setForeground(Color.WHITE);
        exportButton.setFont(new Font("Arial", Font.BOLD, 12));
        exportButton.setFocusPainted(false);
        filterPanel.add(exportButton);
        
        add(filterPanel, BorderLayout.NORTH);
    }
    
    /**
     * Creates the main content area with split pane
     */
    private void createMainContent() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(600);
        
        // Left side: Reports table
        createReportsTable();
        JScrollPane tableScrollPane = new JScrollPane(reportsTable);
        tableScrollPane.setPreferredSize(new Dimension(600, 300));
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(tableScrollPane, BorderLayout.CENTER);
        
        // Report count label
        reportCountLabel = new JLabel("0 reports found");
        reportCountLabel.setFont(new Font("Arial", Font.BOLD, 12));
        reportCountLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        leftPanel.add(reportCountLabel, BorderLayout.SOUTH);
        
        // View details button
        JPanel tableButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        viewDetailsButton = new JButton("📋 View Details");
        viewDetailsButton.setBackground(PRIMARY_COLOR);
        viewDetailsButton.setForeground(Color.WHITE);
        viewDetailsButton.setFont(new Font("Arial", Font.BOLD, 12));
        viewDetailsButton.setFocusPainted(false);
        tableButtonPanel.add(viewDetailsButton);
        leftPanel.add(tableButtonPanel, BorderLayout.NORTH);
        
        splitPane.setLeftComponent(leftPanel);
        
        // Right side: Report preview
        createReportPreview();
        splitPane.setRightComponent(new JScrollPane(reportPreview));
        
        add(splitPane, BorderLayout.CENTER);
    }
    
    /**
     * Creates the reports table
     */
    private void createReportsTable() {
        String[] columnNames = {"Timestamp", "Type", "Summary", "Status"};
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        reportsTable = new JTable(tableModel);
        reportsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        reportsTable.setRowHeight(25);
        reportsTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        reportsTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Timestamp
        reportsTable.getColumnModel().getColumn(1).setPreferredWidth(120); // Type
        reportsTable.getColumnModel().getColumn(2).setPreferredWidth(250); // Summary
        reportsTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Status
        
        // Custom cell renderer for status column
        reportsTable.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());
        
        // Selection listener for preview
        reportsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateReportPreview();
            }
        });
    }
    
    /**
     * Custom cell renderer for status column
     */
    private class StatusCellRenderer extends JLabel implements TableCellRenderer {
        
        public StatusCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setFont(new Font("Arial", Font.BOLD, 10));
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            String status = (String) value;
            
            // Color coding based on report type and data
            if ("NORMAL".equals(status)) {
                setBackground(SUCCESS_COLOR);
                setForeground(Color.WHITE);
                setText("✓ NORMAL");
            } else if ("WARNING".equals(status)) {
                setBackground(WARNING_COLOR);
                setForeground(Color.WHITE);
                setText("⚠ WARNING");
            } else if ("CRITICAL".equals(status)) {
                setBackground(DANGER_COLOR);
                setForeground(Color.WHITE);
                setText("✗ CRITICAL");
            } else {
                setBackground(INFO_COLOR);
                setForeground(Color.WHITE);
                setText("ℹ INFO");
            }
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            }
            
            return this;
        }
    }
    
    /**
     * Creates the report preview area
     */
    private void createReportPreview() {
        reportPreview = new JTextArea();
        reportPreview.setFont(new Font("Consolas", Font.PLAIN, 12));
        reportPreview.setEditable(false);
        reportPreview.setBackground(new Color(248, 248, 248));
        reportPreview.setBorder(BorderFactory.createTitledBorder("Report Details"));
        reportPreview.setText("Select a report to view details...");
    }
    
    /**
     * Creates the charts panel
     */
    private void createChartsPanel() {
        chartTabs = new JTabbedPane();
        
        // Memory usage chart
        JPanel memoryChartPanel = createMemoryChart();
        chartTabs.addTab("Memory Usage", memoryChartPanel);
        
        // Service health chart
        JPanel serviceChartPanel = createServiceChart();
        chartTabs.addTab("Service Health", serviceChartPanel);
        
        // Performance chart
        JPanel performanceChartPanel = createPerformanceChart();
        chartTabs.addTab("Performance", performanceChartPanel);
        
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.add(chartTabs, BorderLayout.CENTER);
        chartPanel.setPreferredSize(new Dimension(0, 200));
        
        add(chartPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates a simple memory usage chart (text-based)
     */
    private JPanel createMemoryChart() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JTextArea chartText = new JTextArea();
        chartText.setFont(new Font("Consolas", Font.PLAIN, 11));
        chartText.setEditable(false);
        chartText.setBackground(Color.WHITE);
        
        StringBuilder chart = new StringBuilder();
        chart.append("Memory Usage Trend (Last 7 Days)\n");
        chart.append("=====================================\n\n");
        
        // Simple ASCII chart
        chart.append("Heap Usage (MB):\n");
        chart.append("300 |                    ██\n");
        chart.append("250 |          ██        ██\n");
        chart.append("200 |    ██    ██    ██  ██\n");
        chart.append("150 |██  ██    ██    ██  ██\n");
        chart.append("100 |██  ██    ██    ██  ██\n");
        chart.append(" 50 |██  ██    ██    ██  ██\n");
        chart.append("  0 +--+--+--+--+--+--+--+\n");
        chart.append("     D-7 D-6 D-5 D-4 D-3 D-2 D-1\n\n");
        
        chart.append("Legend: ██ = Used Memory\n");
        chart.append("Average Usage: 180MB\n");
        chart.append("Peak Usage: 245MB\n");
        chart.append("GC Events: 89 total\n");
        
        chartText.setText(chart.toString());
        panel.add(new JScrollPane(chartText), BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates a simple service health chart
     */
    private JPanel createServiceChart() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JTextArea chartText = new JTextArea();
        chartText.setFont(new Font("Consolas", Font.PLAIN, 11));
        chartText.setEditable(false);
        chartText.setBackground(Color.WHITE);
        
        StringBuilder chart = new StringBuilder();
        chart.append("Service Health Overview\n");
        chart.append("======================\n\n");
        
        chart.append("Service Status:\n");
        chart.append("✓ Google API          [HEALTHY]   Response: 145ms\n");
        chart.append("✓ GitHub API          [HEALTHY]   Response: 230ms\n");
        chart.append("✓ JSONPlaceholder     [HEALTHY]   Response: 189ms\n");
        chart.append("✗ Local Service       [DOWN]      Connection refused\n\n");
        
        chart.append("Health Summary:\n");
        chart.append("- Total Services: 4\n");
        chart.append("- Healthy: 3 (75%)\n");
        chart.append("- Average Response: 188ms\n");
        chart.append("- Uptime (24h): 94.2%\n\n");
        
        chart.append("Response Time Trend:\n");
        chart.append("500ms |    ✗\n");
        chart.append("400ms |\n");
        chart.append("300ms |        ○\n");
        chart.append("200ms |○   ○   ○   ○\n");
        chart.append("100ms |○   ○   ○   ○\n");
        chart.append("  0ms +--+--+--+--+--+\n");
        chart.append("      12h 9h 6h 3h now\n");
        
        chartText.setText(chart.toString());
        panel.add(new JScrollPane(chartText), BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Creates a simple performance chart
     */
    private JPanel createPerformanceChart() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        JTextArea chartText = new JTextArea();
        chartText.setFont(new Font("Consolas", Font.PLAIN, 11));
        chartText.setEditable(false);
        chartText.setBackground(Color.WHITE);
        
        StringBuilder chart = new StringBuilder();
        chart.append("System Performance Metrics\n");
        chart.append("==========================\n\n");
        
        chart.append("CPU Usage (%):\n");
        chart.append("100 |\n");
        chart.append(" 80 |\n");
        chart.append(" 60 |    ██\n");
        chart.append(" 40 |██  ██    ██\n");
        chart.append(" 20 |██  ██  ████  ██\n");
        chart.append("  0 +--+--+--+--+--+\n");
        chart.append("     D-4 D-3 D-2 D-1 Now\n\n");
        
        chart.append("Thread Count:\n");
        chart.append("Current: 12 threads\n");
        chart.append("Average: 10 threads\n");
        chart.append("Peak: 15 threads\n\n");
        
        chart.append("Classes Loaded:\n");
        chart.append("Current: 1,487 classes\n");
        chart.append("Total Loaded: 1,487\n");
        chart.append("Unloaded: 0\n\n");
        
        chart.append("JVM Uptime: 2h 34m 17s\n");
        
        chartText.setText(chart.toString());
        panel.add(new JScrollPane(chartText), BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Sets up the main layout
     */
    private void setupLayout() {
        // Layout is already set up in createMainContent()
    }
    
    /**
     * Sets up event listeners
     */
    private void setupEventListeners() {
        // Filter button
        filterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                applyFilters();
            }
        });
        
        // Generate report button
        generateReportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateNewReport();
            }
        });
        
        // Export button
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportReports();
            }
        });
        
        // View details button
        viewDetailsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                viewReportDetails();
            }
        });
    }
    
    /**
     * Applies filters to the reports list
     */
    private void applyFilters() {
        String selectedType = (String) reportTypeFilter.getSelectedItem();
        Date fromDate = (Date) fromDateSpinner.getValue();
        Date toDate = (Date) toDateSpinner.getValue();
        
        filteredReports.clear();
        
        for (MonitoringReport report : allReports) {
            // Type filter
            if (!"All Reports".equals(selectedType) && !selectedType.equals(report.getReportType())) {
                continue;
            }
            
            // Date filter
            if (report.getTimestamp().before(fromDate) || report.getTimestamp().after(toDate)) {
                continue;
            }
            
            filteredReports.add(report);
        }
        
        refreshTable();
        updateReportCount();
    }
    
    /**
     * Generates a new report (simulation)
     */
    private void generateNewReport() {
        generateReportButton.setEnabled(false);
        generateReportButton.setText("⏳ Generating...");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep(2000); // Simulate report generation
                
                // Create a new current report
                Map<String, Object> currentData = new HashMap<>();
                currentData.put("heapUsed", 156);
                currentData.put("heapMax", 512);
                currentData.put("threadCount", 12);
                currentData.put("gcCount", 23);
                
                MonitoringReport newReport = new MonitoringReport("Current Status", currentData,
                    "Real-time system snapshot: Heap 156MB/512MB, 12 threads, 23 GC events");
                
                allReports.add(0, newReport);
                return null;
            }
            
            @Override
            protected void done() {
                generateReportButton.setEnabled(true);
                generateReportButton.setText("📊 Generate Report");
                applyFilters(); // Refresh with current filters
                JOptionPane.showMessageDialog(ReportsPanel.this, 
                    "New report generated successfully!", 
                    "Report Generated", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        };
        
        worker.execute();
    }
    
    /**
     * Exports reports to a file
     */
    private void exportReports() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Reports");
        fileChooser.setSelectedFile(new File("health_monitor_reports.txt"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            try (FileWriter writer = new FileWriter(file)) {
                writer.write("Health Monitor Reports Export\n");
                writer.write("Generated: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "\n");
                writer.write("=====================================\n\n");
                
                for (MonitoringReport report : filteredReports) {
                    writer.write("Report Type: " + report.getReportType() + "\n");
                    writer.write("Timestamp: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(report.getTimestamp()) + "\n");
                    writer.write("Summary: " + report.getSummary() + "\n");
                    writer.write("Data: " + report.getData().toString() + "\n");
                    writer.write("---\n\n");
                }
                
                JOptionPane.showMessageDialog(this, 
                    "Reports exported successfully to:\n" + file.getAbsolutePath(), 
                    "Export Complete", 
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error exporting reports:\n" + e.getMessage(), 
                    "Export Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Views detailed information about selected report
     */
    private void viewReportDetails() {
        int selectedRow = reportsTable.getSelectedRow();
        if (selectedRow >= 0) {
            MonitoringReport report = filteredReports.get(selectedRow);
            
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Report Details", true);
            dialog.setSize(500, 400);
            dialog.setLocationRelativeTo(this);
            
            JTextArea detailsArea = new JTextArea();
            detailsArea.setFont(new Font("Consolas", Font.PLAIN, 12));
            detailsArea.setEditable(false);
            
            StringBuilder details = new StringBuilder();
            details.append("Report Details\n");
            details.append("==============\n\n");
            details.append("Type: ").append(report.getReportType()).append("\n");
            details.append("Timestamp: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(report.getTimestamp())).append("\n");
            details.append("Summary: ").append(report.getSummary()).append("\n\n");
            details.append("Detailed Data:\n");
            details.append("--------------\n");
            
            for (Map.Entry<String, Object> entry : report.getData().entrySet()) {
                details.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            
            detailsArea.setText(details.toString());
            
            JScrollPane scrollPane = new JScrollPane(detailsArea);
            dialog.add(scrollPane, BorderLayout.CENTER);
            
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dialog.dispose());
            
            JPanel buttonPanel = new JPanel();
            buttonPanel.add(closeButton);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a report to view details.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Updates the report preview area
     */
    private void updateReportPreview() {
        int selectedRow = reportsTable.getSelectedRow();
        if (selectedRow >= 0) {
            MonitoringReport report = filteredReports.get(selectedRow);
            
            StringBuilder preview = new StringBuilder();
            preview.append("Report Preview\n");
            preview.append("==============\n\n");
            preview.append("Type: ").append(report.getReportType()).append("\n");
            preview.append("Generated: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(report.getTimestamp())).append("\n\n");
            preview.append("Summary:\n").append(report.getSummary()).append("\n\n");
            preview.append("Key Metrics:\n");
            
            for (Map.Entry<String, Object> entry : report.getData().entrySet()) {
                preview.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
            
            reportPreview.setText(preview.toString());
        } else {
            reportPreview.setText("Select a report to view details...");
        }
    }
    
    /**
     * Refreshes the reports table
     */
    private void refreshTable() {
        tableModel.setRowCount(0);
        
        List<MonitoringReport> reportsToShow = filteredReports.isEmpty() ? allReports : filteredReports;
        
        for (MonitoringReport report : reportsToShow) {
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(report.getTimestamp());
            String status = determineReportStatus(report);
            
            Object[] row = {
                timestamp,
                report.getReportType(),
                report.getSummary(),
                status
            };
            
            tableModel.addRow(row);
        }
    }
    
    /**
     * Determines the status of a report based on its data
     */
    private String determineReportStatus(MonitoringReport report) {
        Map<String, Object> data = report.getData();
        
        if ("JVM Memory".equals(report.getReportType())) {
            Integer heapUsed = (Integer) data.get("heapUsed");
            Integer heapMax = (Integer) data.get("heapMax");
            if (heapUsed != null && heapMax != null) {
                double usage = (double) heapUsed / heapMax;
                if (usage > 0.9) return "CRITICAL";
                if (usage > 0.7) return "WARNING";
                return "NORMAL";
            }
        } else if ("Service Health".equals(report.getReportType())) {
            Integer healthy = (Integer) data.get("healthyServices");
            Integer total = (Integer) data.get("totalServices");
            if (healthy != null && total != null) {
                double ratio = (double) healthy / total;
                if (ratio < 0.5) return "CRITICAL";
                if (ratio < 0.8) return "WARNING";
                return "NORMAL";
            }
        }
        
        return "INFO";
    }
    
    /**
     * Updates the report count label
     */
    private void updateReportCount() {
        List<MonitoringReport> reportsToShow = filteredReports.isEmpty() ? allReports : filteredReports;
        reportCountLabel.setText(reportsToShow.size() + " reports found");
    }
    
    /**
     * Adds a new report (called from parent GUI)
     */
    public void addReport(MonitoringReport report) {
        allReports.add(0, report);
        applyFilters();
    }
    
    /**
     * Gets all reports
     */
    public List<MonitoringReport> getAllReports() {
        return allReports;
    }
}