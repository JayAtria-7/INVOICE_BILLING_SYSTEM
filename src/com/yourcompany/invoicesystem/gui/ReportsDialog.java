/**
 * Author: Jay Prakash Kumar
 * Copyright (c) 2025
 * Licensed under MIT License
 */

package com.yourcompany.invoicesystem.gui;

import com.yourcompany.invoicesystem.util.ReportGenerator;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class ReportsDialog extends JDialog {
    
    private ReportGenerator reportGenerator;
    
    private JTextArea reportTextArea;
    private JComboBox<String> reportTypeCombo;
    private JSpinner startDateSpinner;
    private JSpinner endDateSpinner;
    private JButton generateButton;
    private JButton closeButton;
    
    public ReportsDialog(JFrame parent) {
        super(parent, "Reports", true);
        
        reportGenerator = new ReportGenerator();
        
        initComponents();
        
        setSize(900, 700);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Top panel with controls
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Report type
        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("Report Type:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        String[] reportTypes = {"Sales Report", "Inventory Report", "Revenue Analysis"};
        reportTypeCombo = new JComboBox<>(reportTypes);
        reportTypeCombo.addActionListener(e -> updateDateFieldsVisibility());
        controlPanel.add(reportTypeCombo, gbc);
        
        // Start date
        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        controlPanel.add(new JLabel("Start Date:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        SpinnerDateModel startModel = new SpinnerDateModel();
        startDateSpinner = new JSpinner(startModel);
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startDateSpinner, "dd-MMM-yyyy");
        startDateSpinner.setEditor(startEditor);
        startDateSpinner.setValue(java.sql.Date.valueOf(thirtyDaysAgo));
        controlPanel.add(startDateSpinner, gbc);
        
        // End date
        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        controlPanel.add(new JLabel("End Date:"), gbc);
        
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        SpinnerDateModel endModel = new SpinnerDateModel();
        endDateSpinner = new JSpinner(endModel);
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endDateSpinner, "dd-MMM-yyyy");
        endDateSpinner.setEditor(endEditor);
        endDateSpinner.setValue(java.sql.Date.valueOf(LocalDate.now()));
        controlPanel.add(endDateSpinner, gbc);
        
        // Generate button
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        generateButton = new JButton("Generate Report");
        generateButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        generateButton.addActionListener(e -> generateReport());
        controlPanel.add(generateButton, gbc);
        
        add(controlPanel, BorderLayout.NORTH);
        
        // Center panel with report text area
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 10, 15));
        
        reportTextArea = new JTextArea();
        reportTextArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        reportTextArea.setEditable(false);
        reportTextArea.setLineWrap(false);
        
        JScrollPane scrollPane = new JScrollPane(reportTextArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Report Output"));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 15, 15, 15));
        
        JButton exportButton = new JButton("Export to File");
        exportButton.addActionListener(e -> exportReport());
        
        closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(exportButton);
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        updateDateFieldsVisibility();
    }
    
    private void updateDateFieldsVisibility() {
        String selectedReport = (String) reportTypeCombo.getSelectedItem();
        boolean needsDates = !"Inventory Report".equals(selectedReport);
        
        startDateSpinner.setEnabled(needsDates);
        endDateSpinner.setEnabled(needsDates);
    }
    
    private void generateReport() {
        String reportType = (String) reportTypeCombo.getSelectedItem();
        String report = "";
        
        try {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            
            if ("Sales Report".equals(reportType)) {
                LocalDate startDate = convertToLocalDate((java.util.Date) startDateSpinner.getValue());
                LocalDate endDate = convertToLocalDate((java.util.Date) endDateSpinner.getValue());
                
                if (startDate.isAfter(endDate)) {
                    JOptionPane.showMessageDialog(this,
                        "Start date cannot be after end date",
                        "Invalid Date Range",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                report = reportGenerator.generateSalesReport(startDate, endDate);
                
            } else if ("Inventory Report".equals(reportType)) {
                report = reportGenerator.generateInventoryReport();
                
            } else if ("Revenue Analysis".equals(reportType)) {
                LocalDate startDate = convertToLocalDate((java.util.Date) startDateSpinner.getValue());
                LocalDate endDate = convertToLocalDate((java.util.Date) endDateSpinner.getValue());
                
                if (startDate.isAfter(endDate)) {
                    JOptionPane.showMessageDialog(this,
                        "Start date cannot be after end date",
                        "Invalid Date Range",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                report = reportGenerator.generateRevenueAnalysisReport(startDate, endDate);
            }
            
            reportTextArea.setText(report);
            reportTextArea.setCaretPosition(0);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error generating report: " + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    private void exportReport() {
        String content = reportTextArea.getText();
        if (content.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No report to export. Please generate a report first.",
                "No Content",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Report");
        fileChooser.setSelectedFile(new java.io.File("report_" + 
            LocalDate.now().toString() + ".txt"));
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                java.io.File file = fileChooser.getSelectedFile();
                java.nio.file.Files.write(file.toPath(), content.getBytes());
                JOptionPane.showMessageDialog(this,
                    "Report exported successfully to:\n" + file.getAbsolutePath(),
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error exporting report: " + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private LocalDate convertToLocalDate(java.util.Date date) {
        return new java.sql.Date(date.getTime()).toLocalDate();
    }
}

