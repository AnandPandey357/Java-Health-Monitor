package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

/**
 * ConfigurationDialog - Settings dialog for the Health Monitor application
 * This dialog allows users to configure monitoring preferences, intervals, and alerts
 */
public class ConfigurationDialog extends JDialog {
    
    // Preferences storage
    private static final String PREF_NODE = "HealthMonitorConfig";
    private Preferences prefs;
    
    // Configuration fields
    private JSpinner monitoringIntervalSpinner;
    private JSpinner alertThresholdMemorySpinner;
    private JSpinner alertThresholdCpuSpinner;
    private JCheckBox enableSoundAlertsCheckBox;
    private JCheckBox enablePopupAlertsCheckBox;
    private JCheckBox enableSystemTrayCheckBox;
    private JCheckBox autoStartMonitoringCheckBox;
    private JSpinner serviceCheckIntervalSpinner;
    private JSpinner serviceTimeoutSpinner;
    private JComboBox<String> logLevelComboBox;
    private JTextField exportDirectoryField;
    private JButton browseExportDirButton;
    private JCheckBox autoExportReportsCheckBox;
    private JSpinner maxReportsToKeepSpinner;
    
    // Color scheme options
    private JComboBox<String> themeComboBox;
    private JCheckBox darkModeCheckBox;
    
    // Network settings
    private JTextField proxyHostField;
    private JSpinner proxyPortSpinner;
    private JCheckBox useProxyCheckBox;
    
    // Buttons
    private JButton okButton;
    private JButton cancelButton;
    private JButton resetDefaultsButton;
    private JButton applyButton;
    
    // Parent reference
    private HealthMonitorGUI parent;
    
    // Colors
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color WARNING_COLOR = new Color(241, 196, 15);
    private static final Color DANGER_COLOR = new Color(231, 76, 60);
    
    /**
     * Constructor creates the configuration dialog
     */
    public ConfigurationDialog(HealthMonitorGUI parent) {
        super(parent, "Health Monitor Configuration", true);
        this.parent = parent;
        this.prefs = Preferences.userRoot().node(PREF_NODE);
        
        initializeComponents();
        layoutComponents();
        setupEventListeners();
        loadSettings();
        
        setSize(500, 600);
        setLocationRelativeTo(parent);
        setResizable(false);
    }
    
    /**
     * Initializes all GUI components with default values
     */
    private void initializeComponents() {
        // Monitoring settings
        monitoringIntervalSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 300, 1));
        alertThresholdMemorySpinner = new JSpinner(new SpinnerNumberModel(85, 10, 99, 5));
        alertThresholdCpuSpinner = new JSpinner(new SpinnerNumberModel(80, 10, 99, 5));
        
        // Alert settings
        enableSoundAlertsCheckBox = new JCheckBox("Enable sound alerts");
        enablePopupAlertsCheckBox = new JCheckBox("Enable popup alerts");
        enableSystemTrayCheckBox = new JCheckBox("Enable system tray");
        autoStartMonitoringCheckBox = new JCheckBox("Auto-start monitoring on launch");
        
        // Service monitoring settings
        serviceCheckIntervalSpinner = new JSpinner(new SpinnerNumberModel(30, 10, 3600, 10));
        serviceTimeoutSpinner = new JSpinner(new SpinnerNumberModel(5000, 1000, 30000, 1000));
        
        // Logging settings
        logLevelComboBox = new JComboBox<>(new String[]{"DEBUG", "INFO", "WARN", "ERROR"});
        logLevelComboBox.setSelectedItem("INFO");
        
        // Export settings
        exportDirectoryField = new JTextField(System.getProperty("user.home"));
        browseExportDirButton = new JButton("Browse...");
        autoExportReportsCheckBox = new JCheckBox("Auto-export daily reports");
        maxReportsToKeepSpinner = new JSpinner(new SpinnerNumberModel(30, 5, 365, 5));
        
        // Theme settings
        themeComboBox = new JComboBox<>(new String[]{"Default", "Blue", "Green", "Dark"});
        darkModeCheckBox = new JCheckBox("Enable dark mode");
        
        // Network settings
        proxyHostField = new JTextField();
        proxyPortSpinner = new JSpinner(new SpinnerNumberModel(8080, 1, 65535, 1));
        useProxyCheckBox = new JCheckBox("Use proxy server");
        
        // Buttons
        okButton = new JButton("OK");
        okButton.setBackground(SUCCESS_COLOR);
        okButton.setForeground(Color.WHITE);
        okButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        cancelButton = new JButton("Cancel");
        cancelButton.setBackground(DANGER_COLOR);
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        resetDefaultsButton = new JButton("Reset Defaults");
        resetDefaultsButton.setBackground(WARNING_COLOR);
        resetDefaultsButton.setForeground(Color.WHITE);
        resetDefaultsButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        applyButton = new JButton("Apply");
        applyButton.setBackground(PRIMARY_COLOR);
        applyButton.setForeground(Color.WHITE);
        applyButton.setFont(new Font("Arial", Font.BOLD, 12));
    }
    
    /**
     * Creates the layout for all components
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());
        
        // Create tabbed pane for organized settings
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // Monitoring tab
        tabbedPane.addTab("⚡ Monitoring", createMonitoringPanel());
        
        // Alerts tab
        tabbedPane.addTab("🔔 Alerts", createAlertsPanel());
        
        // Services tab
        tabbedPane.addTab("🔍 Services", createServicesPanel());
        
        // Appearance tab
        tabbedPane.addTab("🎨 Appearance", createAppearancePanel());
        
        // Advanced tab
        tabbedPane.addTab("⚙️ Advanced", createAdvancedPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(resetDefaultsButton);
        buttonPanel.add(applyButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * Creates the monitoring settings panel
     */
    private JPanel createMonitoringPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Monitoring interval
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Monitoring Interval (seconds):"), gbc);
        gbc.gridx = 1;
        panel.add(monitoringIntervalSpinner, gbc);
        
        // Memory threshold
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Memory Alert Threshold (%):"), gbc);
        gbc.gridx = 1;
        panel.add(alertThresholdMemorySpinner, gbc);
        
        // CPU threshold
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("CPU Alert Threshold (%):"), gbc);
        gbc.gridx = 1;
        panel.add(alertThresholdCpuSpinner, gbc);
        
        // Auto-start checkbox
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        panel.add(autoStartMonitoringCheckBox, gbc);
        
        // Description
        gbc.gridy = 4;
        JTextArea description = new JTextArea(
            "Configure how often the monitor collects JVM metrics and the thresholds " +
            "for triggering alerts. Lower intervals provide more real-time data but may " +
            "consume more system resources."
        );
        description.setWrapStyleWord(true);
        description.setLineWrap(true);
        description.setEditable(false);
        description.setOpaque(false);
        description.setFont(new Font("Arial", Font.ITALIC, 11));
        panel.add(description, gbc);
        
        return panel;
    }
    
    /**
     * Creates the alerts settings panel
     */
    private JPanel createAlertsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Alert checkboxes
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(enableSoundAlertsCheckBox, gbc);
        
        gbc.gridy = 1;
        panel.add(enablePopupAlertsCheckBox, gbc);
        
        gbc.gridy = 2;
        panel.add(enableSystemTrayCheckBox, gbc);
        
        // Description
        gbc.gridy = 3;
        JTextArea description = new JTextArea(
            "Configure how the application notifies you about important events and " +
            "threshold breaches. Sound alerts will play a notification sound, popup " +
            "alerts show dialog boxes, and system tray enables minimizing to the " +
            "system notification area."
        );
        description.setWrapStyleWord(true);
        description.setLineWrap(true);
        description.setEditable(false);
        description.setOpaque(false);
        description.setFont(new Font("Arial", Font.ITALIC, 11));
        panel.add(description, gbc);
        
        return panel;
    }
    
    /**
     * Creates the services settings panel
     */
    private JPanel createServicesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Service check interval
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Service Check Interval (seconds):"), gbc);
        gbc.gridx = 1;
        panel.add(serviceCheckIntervalSpinner, gbc);
        
        // Service timeout
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Service Timeout (milliseconds):"), gbc);
        gbc.gridx = 1;
        panel.add(serviceTimeoutSpinner, gbc);
        
        // Description
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        JTextArea description = new JTextArea(
            "Configure how often external services are checked for health and how long " +
            "to wait for a response before considering the service unavailable. Higher " +
            "check intervals reduce network traffic but may delay detection of outages."
        );
        description.setWrapStyleWord(true);
        description.setLineWrap(true);
        description.setEditable(false);
        description.setOpaque(false);
        description.setFont(new Font("Arial", Font.ITALIC, 11));
        panel.add(description, gbc);
        
        return panel;
    }
    
    /**
     * Creates the appearance settings panel
     */
    private JPanel createAppearancePanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Theme selection
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Color Theme:"), gbc);
        gbc.gridx = 1;
        panel.add(themeComboBox, gbc);
        
        // Dark mode checkbox
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        panel.add(darkModeCheckBox, gbc);
        
        // Description
        gbc.gridy = 2;
        JTextArea description = new JTextArea(
            "Customize the visual appearance of the Health Monitor. Choose from different " +
            "color themes and enable dark mode for better viewing in low-light conditions. " +
            "Changes will take effect after restarting the application."
        );
        description.setWrapStyleWord(true);
        description.setLineWrap(true);
        description.setEditable(false);
        description.setOpaque(false);
        description.setFont(new Font("Arial", Font.ITALIC, 11));
        panel.add(description, gbc);
        
        return panel;
    }
    
    /**
     * Creates the advanced settings panel
     */
    private JPanel createAdvancedPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Logging level
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Log Level:"), gbc);
        gbc.gridx = 1;
        panel.add(logLevelComboBox, gbc);
        
        // Export directory
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Export Directory:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(exportDirectoryField, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE;
        panel.add(browseExportDirButton, gbc);
        
        // Auto-export checkbox
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        panel.add(autoExportReportsCheckBox, gbc);
        
        // Max reports to keep
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        panel.add(new JLabel("Max Reports to Keep:"), gbc);
        gbc.gridx = 1;
        panel.add(maxReportsToKeepSpinner, gbc);
        
        // Proxy settings
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 3;
        panel.add(useProxyCheckBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        panel.add(new JLabel("Proxy Host:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(proxyHostField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 6; gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Proxy Port:"), gbc);
        gbc.gridx = 1;
        panel.add(proxyPortSpinner, gbc);
        
        return panel;
    }
    
    /**
     * Sets up event listeners for all components
     */
    private void setupEventListeners() {
        // OK button - save and close
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSettings();
                dispose();
            }
        });
        
        // Cancel button - close without saving
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        // Apply button - save without closing
        applyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSettings();
                JOptionPane.showMessageDialog(ConfigurationDialog.this, 
                    "Settings have been saved successfully!", 
                    "Settings Applied", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        // Reset defaults button
        resetDefaultsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int option = JOptionPane.showConfirmDialog(ConfigurationDialog.this,
                    "Are you sure you want to reset all settings to default values?",
                    "Reset to Defaults",
                    JOptionPane.YES_NO_OPTION);
                
                if (option == JOptionPane.YES_OPTION) {
                    resetToDefaults();
                }
            }
        });
        
        // Browse export directory button
        browseExportDirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.setCurrentDirectory(new java.io.File(exportDirectoryField.getText()));
                
                int result = fileChooser.showOpenDialog(ConfigurationDialog.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    exportDirectoryField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        
        // Use proxy checkbox - enable/disable proxy fields
        useProxyCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean enabled = useProxyCheckBox.isSelected();
                proxyHostField.setEnabled(enabled);
                proxyPortSpinner.setEnabled(enabled);
            }
        });
    }
    
    /**
     * Loads settings from preferences
     */
    private void loadSettings() {
        // Monitoring settings
        monitoringIntervalSpinner.setValue(prefs.getInt("monitoringInterval", 5));
        alertThresholdMemorySpinner.setValue(prefs.getInt("alertThresholdMemory", 85));
        alertThresholdCpuSpinner.setValue(prefs.getInt("alertThresholdCpu", 80));
        
        // Alert settings
        enableSoundAlertsCheckBox.setSelected(prefs.getBoolean("enableSoundAlerts", true));
        enablePopupAlertsCheckBox.setSelected(prefs.getBoolean("enablePopupAlerts", true));
        enableSystemTrayCheckBox.setSelected(prefs.getBoolean("enableSystemTray", false));
        autoStartMonitoringCheckBox.setSelected(prefs.getBoolean("autoStartMonitoring", false));
        
        // Service settings
        serviceCheckIntervalSpinner.setValue(prefs.getInt("serviceCheckInterval", 30));
        serviceTimeoutSpinner.setValue(prefs.getInt("serviceTimeout", 5000));
        
        // Logging settings
        logLevelComboBox.setSelectedItem(prefs.get("logLevel", "INFO"));
        
        // Export settings
        exportDirectoryField.setText(prefs.get("exportDirectory", System.getProperty("user.home")));
        autoExportReportsCheckBox.setSelected(prefs.getBoolean("autoExportReports", false));
        maxReportsToKeepSpinner.setValue(prefs.getInt("maxReportsToKeep", 30));
        
        // Appearance settings
        themeComboBox.setSelectedItem(prefs.get("theme", "Default"));
        darkModeCheckBox.setSelected(prefs.getBoolean("darkMode", false));
        
        // Network settings
        useProxyCheckBox.setSelected(prefs.getBoolean("useProxy", false));
        proxyHostField.setText(prefs.get("proxyHost", ""));
        proxyPortSpinner.setValue(prefs.getInt("proxyPort", 8080));
        
        // Update proxy field states
        boolean useProxy = useProxyCheckBox.isSelected();
        proxyHostField.setEnabled(useProxy);
        proxyPortSpinner.setEnabled(useProxy);
    }
    
    /**
     * Saves settings to preferences
     */
    private void saveSettings() {
        // Monitoring settings
        prefs.putInt("monitoringInterval", (Integer) monitoringIntervalSpinner.getValue());
        prefs.putInt("alertThresholdMemory", (Integer) alertThresholdMemorySpinner.getValue());
        prefs.putInt("alertThresholdCpu", (Integer) alertThresholdCpuSpinner.getValue());
        
        // Alert settings
        prefs.putBoolean("enableSoundAlerts", enableSoundAlertsCheckBox.isSelected());
        prefs.putBoolean("enablePopupAlerts", enablePopupAlertsCheckBox.isSelected());
        prefs.putBoolean("enableSystemTray", enableSystemTrayCheckBox.isSelected());
        prefs.putBoolean("autoStartMonitoring", autoStartMonitoringCheckBox.isSelected());
        
        // Service settings
        prefs.putInt("serviceCheckInterval", (Integer) serviceCheckIntervalSpinner.getValue());
        prefs.putInt("serviceTimeout", (Integer) serviceTimeoutSpinner.getValue());
        
        // Logging settings
        prefs.put("logLevel", (String) logLevelComboBox.getSelectedItem());
        
        // Export settings
        prefs.put("exportDirectory", exportDirectoryField.getText());
        prefs.putBoolean("autoExportReports", autoExportReportsCheckBox.isSelected());
        prefs.putInt("maxReportsToKeep", (Integer) maxReportsToKeepSpinner.getValue());
        
        // Appearance settings
        prefs.put("theme", (String) themeComboBox.getSelectedItem());
        prefs.putBoolean("darkMode", darkModeCheckBox.isSelected());
        
        // Network settings
        prefs.putBoolean("useProxy", useProxyCheckBox.isSelected());
        prefs.put("proxyHost", proxyHostField.getText());
        prefs.putInt("proxyPort", (Integer) proxyPortSpinner.getValue());
        
        // Notify parent of setting changes
        if (parent != null) {
            parent.applyConfigurationSettings();
        }
    }
    
    /**
     * Resets all settings to default values
     */
    private void resetToDefaults() {
        // Monitoring settings
        monitoringIntervalSpinner.setValue(5);
        alertThresholdMemorySpinner.setValue(85);
        alertThresholdCpuSpinner.setValue(80);
        
        // Alert settings
        enableSoundAlertsCheckBox.setSelected(true);
        enablePopupAlertsCheckBox.setSelected(true);
        enableSystemTrayCheckBox.setSelected(false);
        autoStartMonitoringCheckBox.setSelected(false);
        
        // Service settings
        serviceCheckIntervalSpinner.setValue(30);
        serviceTimeoutSpinner.setValue(5000);
        
        // Logging settings
        logLevelComboBox.setSelectedItem("INFO");
        
        // Export settings
        exportDirectoryField.setText(System.getProperty("user.home"));
        autoExportReportsCheckBox.setSelected(false);
        maxReportsToKeepSpinner.setValue(30);
        
        // Appearance settings
        themeComboBox.setSelectedItem("Default");
        darkModeCheckBox.setSelected(false);
        
        // Network settings
        useProxyCheckBox.setSelected(false);
        proxyHostField.setText("");
        proxyPortSpinner.setValue(8080);
        
        // Update proxy field states
        proxyHostField.setEnabled(false);
        proxyPortSpinner.setEnabled(false);
    }
    
    /**
     * Gets the monitoring interval setting
     */
    public static int getMonitoringInterval() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        return prefs.getInt("monitoringInterval", 5);
    }
    
    /**
     * Gets whether auto-start monitoring is enabled
     */
    public static boolean isAutoStartMonitoring() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        return prefs.getBoolean("autoStartMonitoring", false);
    }
    
    /**
     * Gets the memory alert threshold
     */
    public static int getMemoryAlertThreshold() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        return prefs.getInt("alertThresholdMemory", 85);
    }
    
    /**
     * Gets whether system tray is enabled
     */
    public static boolean isSystemTrayEnabled() {
        Preferences prefs = Preferences.userRoot().node(PREF_NODE);
        return prefs.getBoolean("enableSystemTray", false);
    }
}