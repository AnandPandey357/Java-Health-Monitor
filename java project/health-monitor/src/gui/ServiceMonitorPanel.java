package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * ServiceMonitorPanel - Panel for managing and monitoring external service health
 * This panel allows users to add, remove, and monitor external service endpoints
 */
public class ServiceMonitorPanel extends JPanel {
    
    // Service data structure
    public static class ServiceEndpoint {
        private String name;
        private String url;
        private int expectedStatus;
        private boolean isHealthy;
        private long responseTime;
        private String lastError;
        private long lastChecked;
        
        public ServiceEndpoint(String name, String url, int expectedStatus) {
            this.name = name;
            this.url = url;
            this.expectedStatus = expectedStatus;
            this.isHealthy = false;
            this.responseTime = 0;
            this.lastError = "";
            this.lastChecked = 0;
        }
        
        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public int getExpectedStatus() { return expectedStatus; }
        public void setExpectedStatus(int expectedStatus) { this.expectedStatus = expectedStatus; }
        public boolean isHealthy() { return isHealthy; }
        public void setHealthy(boolean healthy) { isHealthy = healthy; }
        public long getResponseTime() { return responseTime; }
        public void setResponseTime(long responseTime) { this.responseTime = responseTime; }
        public String getLastError() { return lastError; }
        public void setLastError(String lastError) { this.lastError = lastError; }
        public long getLastChecked() { return lastChecked; }
        public void setLastChecked(long lastChecked) { this.lastChecked = lastChecked; }
    }
    
    // GUI Components
    private JTable servicesTable;
    private DefaultTableModel tableModel;
    private JButton addServiceBtn;
    private JButton removeServiceBtn;
    private JButton editServiceBtn;
    private JButton checkAllBtn;
    private JButton checkSelectedBtn;
    private JLabel statusSummaryLabel;
    private JProgressBar overallHealthBar;
    
    // Data
    private List<ServiceEndpoint> services;
    
    // Colors
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    
    /**
     * Constructor initializes the service monitor panel
     */
    public ServiceMonitorPanel() {
        this.services = new ArrayList<>();
        
        // Add some default services for demonstration
        addDefaultServices();
        
        // Initialize GUI components
        initializeComponents();
        setupLayout();
        setupEventListeners();
        
        // Initial table update
        updateTable();
        updateStatusSummary();
    }
    
    /**
     * Adds some default services for demonstration purposes
     */
    private void addDefaultServices() {
        services.add(new ServiceEndpoint("Google", "https://www.google.com", 200));
        services.add(new ServiceEndpoint("GitHub API", "https://api.github.com", 200));
        services.add(new ServiceEndpoint("JSONPlaceholder", "https://jsonplaceholder.typicode.com/posts/1", 200));
        services.add(new ServiceEndpoint("Local Service", "http://localhost:8080/health", 200));
    }
    
    /**
     * Initializes all GUI components
     */
    private void initializeComponents() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        // Create table model and table
        createTable();
        
        // Create control buttons
        createControlButtons();
        
        // Create status summary
        createStatusSummary();
    }
    
    /**
     * Creates the services table
     */
    private void createTable() {
        // Define table columns
        String[] columnNames = {
            "Service Name", "URL", "Expected Status", "Status", "Response Time (ms)", "Last Checked", "Error"
        };
        
        // Create table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        // Create table
        servicesTable = new JTable(tableModel);
        servicesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        servicesTable.setRowHeight(25);
        servicesTable.getTableHeader().setReorderingAllowed(false);
        
        // Set column widths
        servicesTable.getColumnModel().getColumn(0).setPreferredWidth(150); // Service Name
        servicesTable.getColumnModel().getColumn(1).setPreferredWidth(250); // URL
        servicesTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Expected Status
        servicesTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Status
        servicesTable.getColumnModel().getColumn(4).setPreferredWidth(120); // Response Time
        servicesTable.getColumnModel().getColumn(5).setPreferredWidth(150); // Last Checked
        servicesTable.getColumnModel().getColumn(6).setPreferredWidth(200); // Error
        
        // Custom cell renderer for status column
        servicesTable.getColumnModel().getColumn(3).setCellRenderer(new StatusCellRenderer());
    }
    
    /**
     * Custom cell renderer for the status column
     */
    private class StatusCellRenderer extends JLabel implements TableCellRenderer {
        
        public StatusCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(CENTER);
            setFont(new Font("Arial", Font.BOLD, 12));
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            String status = (String) value;
            
            if ("HEALTHY".equals(status)) {
                setBackground(SUCCESS_COLOR);
                setForeground(Color.WHITE);
                setText("✓ HEALTHY");
            } else if ("UNHEALTHY".equals(status)) {
                setBackground(DANGER_COLOR);
                setForeground(Color.WHITE);
                setText("✗ UNHEALTHY");
            } else {
                setBackground(WARNING_COLOR);
                setForeground(Color.WHITE);
                setText("? UNKNOWN");
            }
            
            if (isSelected) {
                setBackground(table.getSelectionBackground());
                setForeground(table.getSelectionForeground());
            }
            
            return this;
        }
    }
    
    /**
     * Creates control buttons
     */
    private void createControlButtons() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);
        
        // Add Service button
        addServiceBtn = new JButton("➕ Add Service");
        addServiceBtn.setBackground(SUCCESS_COLOR);
        addServiceBtn.setForeground(Color.WHITE);
        addServiceBtn.setFont(new Font("Arial", Font.BOLD, 12));
        addServiceBtn.setFocusPainted(false);
        
        // Remove Service button
        removeServiceBtn = new JButton("➖ Remove Service");
        removeServiceBtn.setBackground(DANGER_COLOR);
        removeServiceBtn.setForeground(Color.WHITE);
        removeServiceBtn.setFont(new Font("Arial", Font.BOLD, 12));
        removeServiceBtn.setFocusPainted(false);
        
        // Edit Service button
        editServiceBtn = new JButton("✏️ Edit Service");
        editServiceBtn.setBackground(PRIMARY_COLOR);
        editServiceBtn.setForeground(Color.WHITE);
        editServiceBtn.setFont(new Font("Arial", Font.BOLD, 12));
        editServiceBtn.setFocusPainted(false);
        
        // Check All button
        checkAllBtn = new JButton("🔍 Check All Services");
        checkAllBtn.setBackground(WARNING_COLOR);
        checkAllBtn.setForeground(Color.WHITE);
        checkAllBtn.setFont(new Font("Arial", Font.BOLD, 12));
        checkAllBtn.setFocusPainted(false);
        
        // Check Selected button
        checkSelectedBtn = new JButton("🔍 Check Selected");
        checkSelectedBtn.setBackground(PRIMARY_COLOR);
        checkSelectedBtn.setForeground(Color.WHITE);
        checkSelectedBtn.setFont(new Font("Arial", Font.BOLD, 12));
        checkSelectedBtn.setFocusPainted(false);
        
        buttonPanel.add(addServiceBtn);
        buttonPanel.add(removeServiceBtn);
        buttonPanel.add(editServiceBtn);
        buttonPanel.add(new JSeparator(SwingConstants.VERTICAL));
        buttonPanel.add(checkAllBtn);
        buttonPanel.add(checkSelectedBtn);
        
        add(buttonPanel, BorderLayout.NORTH);
    }
    
    /**
     * Creates status summary panel
     */
    private void createStatusSummary() {
        JPanel summaryPanel = new JPanel(new BorderLayout());
        summaryPanel.setBackground(Color.WHITE);
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Service Health Summary"));
        
        statusSummaryLabel = new JLabel("0 services configured");
        statusSummaryLabel.setFont(new Font("Arial", Font.BOLD, 14));
        
        overallHealthBar = new JProgressBar(0, 100);
        overallHealthBar.setStringPainted(true);
        overallHealthBar.setString("No data");
        overallHealthBar.setForeground(SUCCESS_COLOR);
        
        summaryPanel.add(statusSummaryLabel, BorderLayout.NORTH);
        summaryPanel.add(overallHealthBar, BorderLayout.CENTER);
        
        add(summaryPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Sets up the main layout
     */
    private void setupLayout() {
        // Add table with scroll pane to center
        JScrollPane scrollPane = new JScrollPane(servicesTable);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Sets up event listeners for all buttons
     */
    private void setupEventListeners() {
        // Add service button
        addServiceBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddServiceDialog();
            }
        });
        
        // Remove service button
        removeServiceBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeSelectedService();
            }
        });
        
        // Edit service button
        editServiceBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editSelectedService();
            }
        });
        
        // Check all button
        checkAllBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkAllServices();
            }
        });
        
        // Check selected button
        checkSelectedBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkSelectedService();
            }
        });
    }
    
    /**
     * Shows dialog to add a new service
     */
    private void showAddServiceDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add New Service", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Service name field
        gbc.gridx = 0; gbc.gridy = 0;
        dialog.add(new JLabel("Service Name:"), gbc);
        gbc.gridx = 1;
        JTextField nameField = new JTextField(20);
        dialog.add(nameField, gbc);
        
        // URL field
        gbc.gridx = 0; gbc.gridy = 1;
        dialog.add(new JLabel("URL:"), gbc);
        gbc.gridx = 1;
        JTextField urlField = new JTextField(20);
        dialog.add(urlField, gbc);
        
        // Expected status field
        gbc.gridx = 0; gbc.gridy = 2;
        dialog.add(new JLabel("Expected Status:"), gbc);
        gbc.gridx = 1;
        JSpinner statusSpinner = new JSpinner(new SpinnerNumberModel(200, 100, 599, 1));
        dialog.add(statusSpinner, gbc);
        
        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("Add");
        JButton cancelButton = new JButton("Cancel");
        
        okButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String url = urlField.getText().trim();
            int status = (Integer) statusSpinner.getValue();
            
            if (!name.isEmpty() && !url.isEmpty()) {
                services.add(new ServiceEndpoint(name, url, status));
                updateTable();
                updateStatusSummary();
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        dialog.add(buttonPanel, gbc);
        
        dialog.setVisible(true);
    }
    
    /**
     * Removes the selected service
     */
    private void removeSelectedService() {
        int selectedRow = servicesTable.getSelectedRow();
        if (selectedRow >= 0) {
            int option = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to remove this service?",
                "Confirm Removal",
                JOptionPane.YES_NO_OPTION
            );
            
            if (option == JOptionPane.YES_OPTION) {
                services.remove(selectedRow);
                updateTable();
                updateStatusSummary();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a service to remove.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Edits the selected service
     */
    private void editSelectedService() {
        int selectedRow = servicesTable.getSelectedRow();
        if (selectedRow >= 0) {
            ServiceEndpoint service = services.get(selectedRow);
            
            // Create edit dialog (similar to add dialog but pre-filled)
            JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Edit Service", true);
            dialog.setLayout(new GridBagLayout());
            dialog.setSize(400, 250);
            dialog.setLocationRelativeTo(this);
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // Pre-fill fields with current values
            gbc.gridx = 0; gbc.gridy = 0;
            dialog.add(new JLabel("Service Name:"), gbc);
            gbc.gridx = 1;
            JTextField nameField = new JTextField(service.getName(), 20);
            dialog.add(nameField, gbc);
            
            gbc.gridx = 0; gbc.gridy = 1;
            dialog.add(new JLabel("URL:"), gbc);
            gbc.gridx = 1;
            JTextField urlField = new JTextField(service.getUrl(), 20);
            dialog.add(urlField, gbc);
            
            gbc.gridx = 0; gbc.gridy = 2;
            dialog.add(new JLabel("Expected Status:"), gbc);
            gbc.gridx = 1;
            JSpinner statusSpinner = new JSpinner(new SpinnerNumberModel(service.getExpectedStatus(), 100, 599, 1));
            dialog.add(statusSpinner, gbc);
            
            // Buttons
            JPanel buttonPanel = new JPanel();
            JButton okButton = new JButton("Update");
            JButton cancelButton = new JButton("Cancel");
            
            okButton.addActionListener(e -> {
                String name = nameField.getText().trim();
                String url = urlField.getText().trim();
                int status = (Integer) statusSpinner.getValue();
                
                if (!name.isEmpty() && !url.isEmpty()) {
                    service.setName(name);
                    service.setUrl(url);
                    service.setExpectedStatus(status);
                    updateTable();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Please fill in all fields.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                }
            });
            
            cancelButton.addActionListener(e -> dialog.dispose());
            
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);
            
            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
            dialog.add(buttonPanel, gbc);
            
            dialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a service to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Checks health of all services
     */
    private void checkAllServices() {
        checkAllBtn.setEnabled(false);
        checkAllBtn.setText("⏳ Checking...");
        
        // Use SwingWorker for background health checks
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (ServiceEndpoint service : services) {
                    checkServiceHealth(service);
                }
                return null;
            }
            
            @Override
            protected void done() {
                checkAllBtn.setEnabled(true);
                checkAllBtn.setText("🔍 Check All Services");
                updateTable();
                updateStatusSummary();
            }
        };
        
        worker.execute();
    }
    
    /**
     * Checks health of selected service
     */
    private void checkSelectedService() {
        int selectedRow = servicesTable.getSelectedRow();
        if (selectedRow >= 0) {
            ServiceEndpoint service = services.get(selectedRow);
            
            checkSelectedBtn.setEnabled(false);
            checkSelectedBtn.setText("⏳ Checking...");
            
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    checkServiceHealth(service);
                    return null;
                }
                
                @Override
                protected void done() {
                    checkSelectedBtn.setEnabled(true);
                    checkSelectedBtn.setText("🔍 Check Selected");
                    updateTable();
                    updateStatusSummary();
                }
            };
            
            worker.execute();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a service to check.", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Performs health check on a single service
     */
    private void checkServiceHealth(ServiceEndpoint service) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Simple URL connection test (in a real implementation, you'd use the HealthChecker class)
            URL url = new URL(service.getUrl());
            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestMethod("GET");
            
            int responseCode = connection.getResponseCode();
            long responseTime = System.currentTimeMillis() - startTime;
            
            service.setResponseTime(responseTime);
            service.setLastChecked(System.currentTimeMillis());
            
            if (responseCode == service.getExpectedStatus()) {
                service.setHealthy(true);
                service.setLastError("");
            } else {
                service.setHealthy(false);
                service.setLastError("Expected status " + service.getExpectedStatus() + " but got " + responseCode);
            }
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            service.setResponseTime(responseTime);
            service.setLastChecked(System.currentTimeMillis());
            service.setHealthy(false);
            service.setLastError(e.getMessage());
        }
    }
    
    /**
     * Updates the services table with current data
     */
    private void updateTable() {
        // Clear existing rows
        tableModel.setRowCount(0);
        
        // Add current services
        for (ServiceEndpoint service : services) {
            String status = service.getLastChecked() == 0 ? "UNKNOWN" : 
                           (service.isHealthy() ? "HEALTHY" : "UNHEALTHY");
            
            String lastChecked = service.getLastChecked() == 0 ? "Never" :
                               new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date(service.getLastChecked()));
            
            String responseTime = service.getLastChecked() == 0 ? "-" : String.valueOf(service.getResponseTime());
            
            Object[] row = {
                service.getName(),
                service.getUrl(),
                service.getExpectedStatus(),
                status,
                responseTime,
                lastChecked,
                service.getLastError()
            };
            
            tableModel.addRow(row);
        }
    }
    
    /**
     * Updates the status summary
     */
    private void updateStatusSummary() {
        int totalServices = services.size();
        int healthyServices = 0;
        int checkedServices = 0;
        
        for (ServiceEndpoint service : services) {
            if (service.getLastChecked() > 0) {
                checkedServices++;
                if (service.isHealthy()) {
                    healthyServices++;
                }
            }
        }
        
        if (totalServices == 0) {
            statusSummaryLabel.setText("No services configured");
            overallHealthBar.setValue(0);
            overallHealthBar.setString("No data");
        } else if (checkedServices == 0) {
            statusSummaryLabel.setText(totalServices + " services configured (not checked)");
            overallHealthBar.setValue(0);
            overallHealthBar.setString("Not checked");
        } else {
            double healthPercentage = (double) healthyServices / checkedServices * 100;
            statusSummaryLabel.setText(String.format("%d/%d services healthy (%.1f%%)", 
                                                   healthyServices, checkedServices, healthPercentage));
            overallHealthBar.setValue((int) healthPercentage);
            overallHealthBar.setString(String.format("%.1f%% healthy", healthPercentage));
            
            // Update color based on health percentage
            if (healthPercentage >= 80) {
                overallHealthBar.setForeground(SUCCESS_COLOR);
            } else if (healthPercentage >= 60) {
                overallHealthBar.setForeground(WARNING_COLOR);
            } else {
                overallHealthBar.setForeground(DANGER_COLOR);
            }
        }
    }
    
    /**
     * Gets the list of configured services
     */
    public List<ServiceEndpoint> getServices() {
        return services;
    }
    
    /**
     * Performs health checks on all services (called from parent GUI)
     */
    public void performHealthChecks() {
        checkAllServices();
    }
}