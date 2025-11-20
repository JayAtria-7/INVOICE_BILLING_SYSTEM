package com.yourcompany.invoicesystem.gui;

import com.yourcompany.invoicesystem.dao.InvoiceDAO;
import com.yourcompany.invoicesystem.dao.InvoiceItemDAO;
import com.yourcompany.invoicesystem.dao.ProductDAO;
import com.yourcompany.invoicesystem.model.Invoice;
import com.yourcompany.invoicesystem.model.InvoiceItem;
import com.yourcompany.invoicesystem.util.DBUtil;
import com.yourcompany.invoicesystem.util.Logger;
import com.yourcompany.invoicesystem.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Payment Dialog for processing invoice payments
 * Supports multiple payment methods: Cash, Card, Split Payment
 */
public class PaymentDialog extends JDialog {
    
    private Invoice invoice;
    private List<InvoiceItem> invoiceItems;
    private double totalAmount;
    private boolean paymentSuccessful = false;
    
    // UI Components
    private JLabel totalLabel;
    private JComboBox<String> paymentMethodCombo;
    private JTextField amountField;
    private JTextField cashReceivedField;
    private JLabel changeLabel;
    private JButton addPaymentButton;
    private JButton completeButton;
    private JButton cancelButton;
    private JTextArea paymentSummaryArea;
    
    // Payment tracking
    private Map<String, Double> payments = new HashMap<>();
    private double totalPaid = 0.0;
    private List<Integer> paymentMethodIds = new ArrayList<>();
    
    public PaymentDialog(Frame parent, Invoice invoice, List<InvoiceItem> items) {
        super(parent, "Process Payment", true);
        this.invoice = invoice;
        this.invoiceItems = items;
        this.totalAmount = invoice.getTotalAmount().doubleValue();
        
        initComponents();
        loadPaymentMethods();
        updateDisplay();
        
        setSize(600, 700);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Invoice Info Panel
        JPanel infoPanel = createInvoiceInfoPanel();
        mainPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Payment Input Panel
        JPanel paymentPanel = createPaymentInputPanel();
        mainPanel.add(paymentPanel, BorderLayout.CENTER);
        
        // Payment Summary Panel
        JPanel summaryPanel = createPaymentSummaryPanel();
        mainPanel.add(summaryPanel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        completeButton = new JButton("Complete Payment");
        completeButton.setEnabled(false);
        completeButton.addActionListener(e -> completePayment());
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(completeButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createInvoiceInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Invoice Information"));
        
        panel.add(new JLabel("Invoice ID:"));
        panel.add(new JLabel(String.valueOf(invoice.getInvoiceID())));
        
        panel.add(new JLabel("Total Amount:"));
        totalLabel = new JLabel(String.format("€%.2f", totalAmount));
        totalLabel.setFont(totalLabel.getFont().deriveFont(Font.BOLD, 16f));
        totalLabel.setForeground(new Color(0, 100, 0));
        panel.add(totalLabel);
        
        panel.add(new JLabel("Amount Due:"));
        JLabel dueLabel = new JLabel(String.format("€%.2f", totalAmount));
        dueLabel.setFont(dueLabel.getFont().deriveFont(Font.BOLD, 16f));
        dueLabel.setForeground(Color.RED);
        panel.add(dueLabel);
        
        return panel;
    }
    
    private JPanel createPaymentInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Payment Method"));
        
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Payment Method
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Payment Method:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        paymentMethodCombo = new JComboBox<>();
        paymentMethodCombo.addActionListener(e -> handlePaymentMethodChange());
        inputPanel.add(paymentMethodCombo, gbc);
        
        // Amount
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        inputPanel.add(new JLabel("Amount:"), gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        amountField = new JTextField(String.format("%.2f", totalAmount));
        inputPanel.add(amountField, gbc);
        
        // Cash Received (only for cash payments)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        JLabel cashLabel = new JLabel("Cash Received:");
        cashLabel.setVisible(false);
        inputPanel.add(cashLabel, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        cashReceivedField = new JTextField();
        cashReceivedField.setVisible(false);
        cashReceivedField.addActionListener(e -> calculateChange());
        inputPanel.add(cashReceivedField, gbc);
        
        // Change
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        JLabel changeLabelText = new JLabel("Change:");
        changeLabelText.setVisible(false);
        inputPanel.add(changeLabelText, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1.0;
        changeLabel = new JLabel("€0.00");
        changeLabel.setFont(changeLabel.getFont().deriveFont(Font.BOLD, 14f));
        changeLabel.setForeground(new Color(0, 100, 0));
        changeLabel.setVisible(false);
        inputPanel.add(changeLabel, gbc);
        
        // Add Payment Button
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        addPaymentButton = new JButton("Add Payment");
        addPaymentButton.addActionListener(e -> addPayment());
        inputPanel.add(addPaymentButton, gbc);
        
        panel.add(inputPanel, BorderLayout.CENTER);
        
        // Store references for visibility control
        cashReceivedField.putClientProperty("cashLabel", cashLabel);
        cashReceivedField.putClientProperty("changeLabel", changeLabelText);
        
        return panel;
    }
    
    private JPanel createPaymentSummaryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Payment Summary"));
        
        paymentSummaryArea = new JTextArea(8, 40);
        paymentSummaryArea.setEditable(false);
        paymentSummaryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(paymentSummaryArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void loadPaymentMethods() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT PaymentMethodID, MethodName FROM PaymentMethods WHERE IsActive = TRUE ORDER BY MethodName";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("PaymentMethodID");
                String name = rs.getString("MethodName");
                paymentMethodCombo.addItem(name);
                paymentMethodIds.add(id);
            }
            
        } catch (Exception e) {
            Logger.error("Error loading payment methods: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Error loading payment methods: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void handlePaymentMethodChange() {
        String selected = (String) paymentMethodCombo.getSelectedItem();
        boolean isCash = selected != null && selected.equalsIgnoreCase("Cash");
        
        JLabel cashLabel = (JLabel) cashReceivedField.getClientProperty("cashLabel");
        JLabel changeLabelText = (JLabel) cashReceivedField.getClientProperty("changeLabel");
        
        cashReceivedField.setVisible(isCash);
        changeLabel.setVisible(isCash);
        if (cashLabel != null) cashLabel.setVisible(isCash);
        if (changeLabelText != null) changeLabelText.setVisible(isCash);
        
        if (!isCash) {
            cashReceivedField.setText("");
            changeLabel.setText("€0.00");
        }
    }
    
    private void calculateChange() {
        try {
            double cashReceived = Double.parseDouble(cashReceivedField.getText().trim());
            double amount = Double.parseDouble(amountField.getText().trim());
            double change = cashReceived - amount;
            
            if (change < 0) {
                changeLabel.setText("Insufficient");
                changeLabel.setForeground(Color.RED);
            } else {
                changeLabel.setText(String.format("€%.2f", change));
                changeLabel.setForeground(new Color(0, 100, 0));
            }
        } catch (NumberFormatException e) {
            changeLabel.setText("€0.00");
        }
    }
    
    private void addPayment() {
        try {
            String method = (String) paymentMethodCombo.getSelectedItem();
            if (method == null) {
                JOptionPane.showMessageDialog(this, "Please select a payment method", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be greater than zero", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            double remaining = totalAmount - totalPaid;
            if (amount > remaining) {
                int response = JOptionPane.showConfirmDialog(this,
                        String.format("Amount (€%.2f) exceeds remaining balance (€%.2f). Use remaining amount?", amount, remaining),
                        "Confirm", JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    amount = remaining;
                    amountField.setText(String.format("%.2f", amount));
                } else {
                    return;
                }
            }
            
            // For cash, verify sufficient payment
            if (method.equalsIgnoreCase("Cash")) {
                String cashReceivedText = cashReceivedField.getText().trim();
                if (cashReceivedText.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter cash received amount", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                double cashReceived = Double.parseDouble(cashReceivedText);
                if (cashReceived < amount) {
                    JOptionPane.showMessageDialog(this, "Cash received is less than payment amount", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Add payment
            payments.put(method + "_" + System.currentTimeMillis(), amount);
            totalPaid += amount;
            
            updateDisplay();
            
            // Clear inputs for next payment
            amountField.setText(String.format("%.2f", totalAmount - totalPaid));
            cashReceivedField.setText("");
            changeLabel.setText("€0.00");
            
            Logger.info("Payment added: " + method + " €" + amount);
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid amount format", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateDisplay() {
        StringBuilder summary = new StringBuilder();
        summary.append("Invoice Total:     €").append(String.format("%8.2f", totalAmount)).append("\n");
        summary.append("─────────────────────────────────\n");
        
        if (!payments.isEmpty()) {
            summary.append("\nPayments Received:\n");
            for (Map.Entry<String, Double> entry : payments.entrySet()) {
                String method = entry.getKey().substring(0, entry.getKey().lastIndexOf("_"));
                summary.append(String.format("  %-15s €%8.2f\n", method, entry.getValue()));
            }
            summary.append("─────────────────────────────────\n");
        }
        
        summary.append(String.format("Total Paid:        €%8.2f\n", totalPaid));
        double remaining = totalAmount - totalPaid;
        summary.append(String.format("Remaining:         €%8.2f\n", remaining));
        
        paymentSummaryArea.setText(summary.toString());
        
        // Enable complete button if fully paid
        completeButton.setEnabled(totalPaid >= totalAmount);
        
        if (totalPaid >= totalAmount) {
            addPaymentButton.setEnabled(false);
        }
    }
    
    private void completePayment() {
        if (totalPaid < totalAmount) {
            JOptionPane.showMessageDialog(this, "Payment incomplete. Total paid is less than invoice amount.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        Connection conn = null;
        try {
            conn = DBUtil.getConnection(false); // Start transaction
            
            // Save invoice
            InvoiceDAO invoiceDAO = new InvoiceDAO();
            int invoiceId = invoiceDAO.saveInvoice(invoice, conn);
            invoice.setInvoiceID(invoiceId);
            
            // Save invoice items and update stock
            InvoiceItemDAO itemDAO = new InvoiceItemDAO();
            ProductDAO productDAO = new ProductDAO();
            
            for (InvoiceItem item : invoiceItems) {
                item.setInvoiceID(invoiceId);
                itemDAO.saveInvoiceItem(item, conn);
                
                // Decrease stock with transaction
                productDAO.decreaseProductStock(item.getProductID(), item.getQuantity(), conn);
            }
            
            // Save payment records
            savePaymentRecords(conn, invoiceId);
            
            // Update invoice payment status
            updateInvoicePaymentStatus(conn, invoiceId);
            
            conn.commit();
            paymentSuccessful = true;
            
            Logger.info("Payment completed successfully for Invoice ID: " + invoiceId);
            
            JOptionPane.showMessageDialog(this, "Payment processed successfully!\nInvoice ID: " + invoiceId,
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception rollbackEx) {
                    Logger.error("Error rolling back transaction: " + rollbackEx.getMessage(), rollbackEx);
                }
            }
            Logger.error("Error completing payment: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Error processing payment: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (Exception e) {
                    Logger.error("Error closing connection: " + e.getMessage(), e);
                }
            }
        }
    }
    
    private void savePaymentRecords(Connection conn, int invoiceId) throws Exception {
        String sql = "INSERT INTO InvoicePayments (InvoiceID, PaymentMethodID, Amount) VALUES (?, ?, ?)";
        PreparedStatement stmt = conn.prepareStatement(sql);
        
        for (Map.Entry<String, Double> entry : payments.entrySet()) {
            String methodKey = entry.getKey().substring(0, entry.getKey().lastIndexOf("_"));
            int methodId = getPaymentMethodId(methodKey);
            
            stmt.setInt(1, invoiceId);
            stmt.setInt(2, methodId);
            stmt.setDouble(3, entry.getValue());
            stmt.executeUpdate();
        }
    }
    
    private int getPaymentMethodId(String methodName) throws Exception {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT PaymentMethodID FROM PaymentMethods WHERE MethodName = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, methodName);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("PaymentMethodID");
            }
            throw new Exception("Payment method not found: " + methodName);
        }
    }
    
    private void updateInvoicePaymentStatus(Connection conn, int invoiceId) throws Exception {
        String status = (totalPaid >= totalAmount) ? "PAID" : "PARTIAL";
        String sql = "UPDATE Invoices SET PaymentStatus = ?, UserID = ? WHERE InvoiceID = ?";
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, status);
        
        if (SessionManager.getInstance().isLoggedIn()) {
            stmt.setInt(2, SessionManager.getInstance().getCurrentUser().getUserID());
        } else {
            stmt.setNull(2, java.sql.Types.INTEGER);
        }
        
        stmt.setInt(3, invoiceId);
        stmt.executeUpdate();
    }
    
    public boolean isPaymentSuccessful() {
        return paymentSuccessful;
    }
    
    public Invoice getInvoice() {
        return invoice;
    }
}
