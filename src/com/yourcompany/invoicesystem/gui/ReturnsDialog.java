package com.yourcompany.invoicesystem.gui;

import com.yourcompany.invoicesystem.dao.InvoiceDAO;
import com.yourcompany.invoicesystem.dao.InvoiceItemDAO;
import com.yourcompany.invoicesystem.dao.ProductDAO;
import com.yourcompany.invoicesystem.model.InvoiceItem;
import com.yourcompany.invoicesystem.model.Product;
import com.yourcompany.invoicesystem.util.DBUtil;
import com.yourcompany.invoicesystem.util.Logger;
import com.yourcompany.invoicesystem.util.SessionManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.List;

/**
 * Returns and Refunds Dialog
 * Handles product returns, refunds, and inventory restoration
 */
public class ReturnsDialog extends JDialog {
    
    private JTextField invoiceIdField;
    private JButton searchButton;
    private JTable itemsTable;
    private DefaultTableModel tableModel;
    private JTextArea invoiceInfoArea;
    private JComboBox<String> reasonCombo;
    private JButton processReturnButton;
    
    private int currentInvoiceId = -1;
    private double invoiceTotal = 0.0;
    
    public ReturnsDialog(Frame parent) {
        super(parent, "Process Returns & Refunds", true);
        initComponents();
        setSize(900, 700);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Search Panel
        mainPanel.add(createSearchPanel(), BorderLayout.NORTH);
        
        // Center Panel with split
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.4);
        
        // Invoice Info
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Invoice Information"));
        invoiceInfoArea = new JTextArea(6, 50);
        invoiceInfoArea.setEditable(false);
        invoiceInfoArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        infoPanel.add(new JScrollPane(invoiceInfoArea), BorderLayout.CENTER);
        splitPane.setTopComponent(infoPanel);
        
        // Items Table
        JPanel itemsPanel = createItemsPanel();
        splitPane.setBottomComponent(itemsPanel);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        // Return Details Panel
        mainPanel.add(createReturnDetailsPanel(), BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        processReturnButton = new JButton("Process Return");
        processReturnButton.setEnabled(false);
        processReturnButton.addActionListener(e -> processReturn());
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(processReturnButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Search Invoice"));
        
        panel.add(new JLabel("Invoice ID:"));
        invoiceIdField = new JTextField(15);
        invoiceIdField.addActionListener(e -> searchInvoice());
        panel.add(invoiceIdField);
        
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchInvoice());
        panel.add(searchButton);
        
        return panel;
    }
    
    private JPanel createItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Invoice Items (Select items to return)"));
        
        String[] columns = {"Select", "Product ID", "Product Name", "Quantity", "Price", "Subtotal", "Return Qty"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class;
                if (columnIndex == 6) return Integer.class;
                return String.class;
            }
            
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0 || column == 6; // Select and Return Qty editable
            }
        };
        
        itemsTable = new JTable(tableModel);
        itemsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        itemsTable.setRowHeight(25);
        itemsTable.getColumnModel().getColumn(0).setMaxWidth(60);
        itemsTable.getColumnModel().getColumn(1).setMaxWidth(80);
        itemsTable.getColumnModel().getColumn(3).setMaxWidth(80);
        itemsTable.getColumnModel().getColumn(6).setMaxWidth(80);
        
        panel.add(new JScrollPane(itemsTable), BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createReturnDetailsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Return Reason"));
        
        panel.add(new JLabel("Reason:"));
        reasonCombo = new JComboBox<>(new String[]{
            "Defective Product",
            "Wrong Item Received",
            "Customer Changed Mind",
            "Duplicate Order",
            "Product Damaged",
            "Not as Described",
            "Other"
        });
        reasonCombo.setPreferredSize(new Dimension(250, 25));
        panel.add(reasonCombo);
        
        return panel;
    }
    
    private void searchInvoice() {
        String invoiceIdText = invoiceIdField.getText().trim();
        if (invoiceIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an Invoice ID", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        try {
            int invoiceId = Integer.parseInt(invoiceIdText);
            loadInvoice(invoiceId);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid Invoice ID format", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadInvoice(int invoiceId) {
        try (Connection conn = DBUtil.getConnection()) {
            // Load invoice details
            String invoiceSql = "SELECT * FROM Invoices WHERE InvoiceID = ?";
            PreparedStatement stmt = conn.prepareStatement(invoiceSql);
            stmt.setInt(1, invoiceId);
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                JOptionPane.showMessageDialog(this, "Invoice not found", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            currentInvoiceId = invoiceId;
            Timestamp invoiceDate = rs.getTimestamp("InvoiceDate");
            double discount = rs.getDouble("DiscountPercentage");
            double taxAmount = rs.getDouble("TaxAmount");
            invoiceTotal = rs.getDouble("TotalAmount");
            String paymentStatus = rs.getString("PaymentStatus");
            
            // Display invoice info
            StringBuilder info = new StringBuilder();
            info.append("Invoice ID: ").append(invoiceId).append("\n");
            info.append("Date: ").append(invoiceDate).append("\n");
            info.append("Discount: ").append(discount).append("%\n");
            info.append("Tax: €").append(String.format("%.2f", taxAmount)).append("\n");
            info.append("Total: €").append(String.format("%.2f", invoiceTotal)).append("\n");
            info.append("Payment Status: ").append(paymentStatus != null ? paymentStatus : "N/A");
            invoiceInfoArea.setText(info.toString());
            
            // Load invoice items
            loadInvoiceItems(invoiceId);
            processReturnButton.setEnabled(true);
            
            Logger.info("Loaded invoice " + invoiceId + " for returns");
            
        } catch (Exception e) {
            Logger.error("Error loading invoice: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Error loading invoice: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadInvoiceItems(int invoiceId) {
        tableModel.setRowCount(0);
        
        try (Connection conn = DBUtil.getConnection()) {
            InvoiceItemDAO itemDAO = new InvoiceItemDAO();
            ProductDAO productDAO = new ProductDAO();
            List<InvoiceItem> items = itemDAO.getInvoiceItemsByInvoiceId(invoiceId);
            
            for (InvoiceItem item : items) {
                Product product = productDAO.getProductById(item.getProductID());
                String productName = product != null ? product.getProductName() : "Unknown";
                
                double subtotal = item.getQuantity() * item.getUnitPrice().doubleValue();
                
                tableModel.addRow(new Object[]{
                    false, // Select
                    item.getProductID(),
                    productName,
                    item.getQuantity(),
                    String.format("€%.2f", item.getUnitPrice()),
                    String.format("€%.2f", subtotal),
                    item.getQuantity() // Default return quantity
                });
            }
            
        } catch (Exception e) {
            Logger.error("Error loading invoice items: " + e.getMessage(), e);
        }
    }
    
    private void processReturn() {
        // Collect selected items
        java.util.List<ReturnItem> returnItems = new java.util.ArrayList<>();
        
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            Boolean selected = (Boolean) tableModel.getValueAt(row, 0);
            if (selected != null && selected) {
                int productId = Integer.parseInt(tableModel.getValueAt(row, 1).toString());
                int originalQty = Integer.parseInt(tableModel.getValueAt(row, 3).toString());
                Object returnQtyObj = tableModel.getValueAt(row, 6);
                int returnQty = returnQtyObj instanceof Integer ? (Integer) returnQtyObj : 
                               Integer.parseInt(returnQtyObj.toString());
                
                String priceStr = tableModel.getValueAt(row, 4).toString().replace("€", "").trim();
                double unitPrice = Double.parseDouble(priceStr);
                
                if (returnQty > originalQty) {
                    JOptionPane.showMessageDialog(this, 
                            "Return quantity cannot exceed original quantity for product ID " + productId,
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (returnQty > 0) {
                    returnItems.add(new ReturnItem(productId, returnQty, unitPrice));
                }
            }
        }
        
        if (returnItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select at least one item to return",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Calculate refund amount
        double refundAmount = 0.0;
        for (ReturnItem item : returnItems) {
            refundAmount += item.quantity * item.unitPrice;
        }
        
        // Confirm return
        String reason = (String) reasonCombo.getSelectedItem();
        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("Process return for %d item(s)?\nRefund Amount: €%.2f\nReason: %s",
                        returnItems.size(), refundAmount, reason),
                "Confirm Return", JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Process return
        Connection conn = null;
        try {
            conn = DBUtil.getConnection(false);
            
            for (ReturnItem item : returnItems) {
                // Insert return record
                String returnSql = "INSERT INTO Returns (InvoiceID, ProductID, Quantity, RefundAmount, Reason, ProcessedByUserID) " +
                                 "VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(returnSql);
                stmt.setInt(1, currentInvoiceId);
                stmt.setInt(2, item.productId);
                stmt.setInt(3, item.quantity);
                stmt.setDouble(4, item.quantity * item.unitPrice);
                stmt.setString(5, reason);
                
                if (SessionManager.getInstance().isLoggedIn()) {
                    stmt.setInt(6, SessionManager.getInstance().getCurrentUser().getUserID());
                } else {
                    stmt.setNull(6, java.sql.Types.INTEGER);
                }
                
                stmt.executeUpdate();
                
                // Restore stock
                String updateStock = "UPDATE Products SET StockQuantity = StockQuantity + ? WHERE ProductID = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateStock);
                updateStmt.setInt(1, item.quantity);
                updateStmt.setInt(2, item.productId);
                updateStmt.executeUpdate();
            }
            
            conn.commit();
            
            Logger.info("Processed return for invoice " + currentInvoiceId + ", refund: €" + refundAmount);
            
            JOptionPane.showMessageDialog(this,
                    String.format("Return processed successfully!\nRefund Amount: €%.2f", refundAmount),
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Clear and reset
            invoiceIdField.setText("");
            invoiceInfoArea.setText("");
            tableModel.setRowCount(0);
            currentInvoiceId = -1;
            processReturnButton.setEnabled(false);
            
        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (Exception rollbackEx) {
                    Logger.error("Error rolling back: " + rollbackEx.getMessage(), rollbackEx);
                }
            }
            Logger.error("Error processing return: " + e.getMessage(), e);
            JOptionPane.showMessageDialog(this, "Error processing return: " + e.getMessage(),
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
    
    private static class ReturnItem {
        int productId;
        int quantity;
        double unitPrice;
        
        ReturnItem(int productId, int quantity, double unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }
    }
}
