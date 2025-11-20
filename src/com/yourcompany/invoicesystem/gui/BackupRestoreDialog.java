package com.yourcompany.invoicesystem.gui;

import com.yourcompany.invoicesystem.util.DatabaseBackup;
import com.yourcompany.invoicesystem.util.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.File;

/**
 * Backup and Restore Dialog
 * Manages database backup and restore operations
 */
public class BackupRestoreDialog extends JDialog {
    
    private JList<String> backupList;
    private DefaultListModel<String> listModel;
    private JButton createBackupButton;
    private JButton restoreButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JTextArea statusArea;
    
    public BackupRestoreDialog(Frame parent) {
        super(parent, "Database Backup & Restore", true);
        initComponents();
        loadBackups();
        setSize(700, 600);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Title Panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("Database Backup & Restore Management");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        titlePanel.add(titleLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // Center Panel - Split between list and status
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.6);
        
        // Backup List Panel
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(BorderFactory.createTitledBorder("Available Backups"));
        
        listModel = new DefaultListModel<>();
        backupList = new JList<>(listModel);
        backupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        backupList.addListSelectionListener(e -> updateButtonStates());
        JScrollPane scrollPane = new JScrollPane(backupList);
        listPanel.add(scrollPane, BorderLayout.CENTER);
        
        splitPane.setTopComponent(listPanel);
        
        // Status Panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status"));
        
        statusArea = new JTextArea();
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusArea.setText("Ready. Select a backup or create a new one.");
        JScrollPane statusScroll = new JScrollPane(statusArea);
        statusPanel.add(statusScroll, BorderLayout.CENTER);
        
        splitPane.setBottomComponent(statusPanel);
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new BorderLayout());
        
        // Action buttons on the left
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        createBackupButton = new JButton("Create Backup");
        createBackupButton.addActionListener(e -> createBackup());
        actionPanel.add(createBackupButton);
        
        restoreButton = new JButton("Restore Selected");
        restoreButton.setEnabled(false);
        restoreButton.addActionListener(e -> restoreBackup());
        actionPanel.add(restoreButton);
        
        deleteButton = new JButton("Delete Selected");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> deleteBackup());
        actionPanel.add(deleteButton);
        
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadBackups());
        actionPanel.add(refreshButton);
        
        buttonPanel.add(actionPanel, BorderLayout.WEST);
        
        // Close button on the right
        JPanel closePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        closePanel.add(closeButton);
        
        buttonPanel.add(closePanel, BorderLayout.EAST);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadBackups() {
        listModel.clear();
        String[] backups = DatabaseBackup.listBackups();
        
        if (backups.length == 0) {
            statusArea.setText("No backups found.\nCreate your first backup to get started.");
        } else {
            for (String backup : backups) {
                listModel.addElement(backup);
            }
            statusArea.setText("Found " + backups.length + " backup(s).");
        }
        
        updateButtonStates();
    }
    
    private void updateButtonStates() {
        boolean hasSelection = backupList.getSelectedIndex() != -1;
        restoreButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
    }
    
    private void createBackup() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Create a new database backup?\nThis may take a few moments.",
                "Confirm Backup", JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        createBackupButton.setEnabled(false);
        statusArea.setText("Creating backup...\nPlease wait...");
        
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return DatabaseBackup.createBackup();
            }
            
            @Override
            protected void done() {
                try {
                    String backupPath = get();
                    File file = new File(backupPath);
                    double sizeKB = file.length() / 1024.0;
                    
                    statusArea.setText("Backup created successfully!\n" +
                                     "File: " + file.getName() + "\n" +
                                     "Size: " + String.format("%.2f KB", sizeKB) + "\n" +
                                     "Location: " + file.getAbsolutePath());
                    
                    loadBackups();
                    
                    JOptionPane.showMessageDialog(BackupRestoreDialog.this,
                            "Backup created successfully!\n" + file.getName(),
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                } catch (Exception e) {
                    Logger.error("Error creating backup: " + e.getMessage(), e);
                    statusArea.setText("ERROR: Failed to create backup\n" + e.getMessage());
                    JOptionPane.showMessageDialog(BackupRestoreDialog.this,
                            "Error creating backup:\n" + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    createBackupButton.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    private void restoreBackup() {
        String selectedBackup = backupList.getSelectedValue();
        if (selectedBackup == null) {
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
                "WARNING: Restore will REPLACE all current database data!\n\n" +
                "Restore from: " + selectedBackup + "\n\n" +
                "Are you absolutely sure?",
                "Confirm Restore", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Second confirmation
        confirm = JOptionPane.showConfirmDialog(this,
                "This is your LAST CHANCE to cancel.\n\n" +
                "All current data will be LOST!\n\n" +
                "Proceed with restore?",
                "Final Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        restoreButton.setEnabled(false);
        statusArea.setText("Restoring database...\nPlease wait...\nDO NOT close this window!");
        
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                String backupPath = "backups" + File.separator + selectedBackup;
                DatabaseBackup.restoreBackup(backupPath);
                return null;
            }
            
            @Override
            protected void done() {
                try {
                    get();
                    statusArea.setText("Database restored successfully!\n" +
                                     "From: " + selectedBackup + "\n\n" +
                                     "Please restart the application for changes to take full effect.");
                    
                    JOptionPane.showMessageDialog(BackupRestoreDialog.this,
                            "Database restored successfully!\n\n" +
                            "Please restart the application.",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                } catch (Exception e) {
                    Logger.error("Error restoring backup: " + e.getMessage(), e);
                    statusArea.setText("ERROR: Failed to restore backup\n" + e.getMessage());
                    JOptionPane.showMessageDialog(BackupRestoreDialog.this,
                            "Error restoring backup:\n" + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    restoreButton.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    private void deleteBackup() {
        String selectedBackup = backupList.getSelectedValue();
        if (selectedBackup == null) {
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete backup file?\n\n" + selectedBackup,
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        try {
            File backupFile = new File("backups" + File.separator + selectedBackup);
            if (backupFile.delete()) {
                statusArea.setText("Deleted: " + selectedBackup);
                Logger.info("Deleted backup: " + selectedBackup);
                loadBackups();
                JOptionPane.showMessageDialog(this, "Backup deleted successfully",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                throw new Exception("Failed to delete file");
            }
        } catch (Exception e) {
            Logger.error("Error deleting backup: " + e.getMessage(), e);
            statusArea.setText("ERROR: Failed to delete backup\n" + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error deleting backup:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
