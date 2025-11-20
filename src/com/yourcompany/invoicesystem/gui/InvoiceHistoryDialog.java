/**
 * Author: Jay Prakash Kumar
 * Copyright (c) 2025
 * Licensed under MIT License
 */

package com.yourcompany.invoicesystem.gui;

import com.yourcompany.invoicesystem.dao.InvoiceDAO;
import com.yourcompany.invoicesystem.dao.InvoiceItemDAO;
import com.yourcompany.invoicesystem.dao.ProductDAO;
import com.yourcompany.invoicesystem.model.Invoice;
import com.yourcompany.invoicesystem.model.InvoiceItem;
import com.yourcompany.invoicesystem.model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InvoiceHistoryDialog extends JDialog {
    
    private InvoiceDAO invoiceDAO;
    private InvoiceItemDAO invoiceItemDAO;
    private ProductDAO productDAO;
    
    private JTable invoicesTable;
    private DefaultTableModel invoicesTableModel;
    private JTable itemsTable;
    private DefaultTableModel itemsTableModel;
    
    private JTextField searchField;
    private JButton searchButton;
    private JButton viewDetailsButton;
    private JButton closeButton;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    
    public InvoiceHistoryDialog(JFrame parent) {
        super(parent, "Invoice History", true);
        
        invoiceDAO = new InvoiceDAO();
        invoiceItemDAO = new InvoiceItemDAO();
        productDAO = new ProductDAO();
        
        initComponents();
        loadAllInvoices();
        
        setSize(1000, 700);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Top panel with search
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Invoice History");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel searchLabel = new JLabel("Search Invoice ID:");
        searchField = new JTextField(15);
        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchInvoice());
        
        JButton showAllButton = new JButton("Show All");
        showAllButton.addActionListener(e -> loadAllInvoices());
        
        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);
        topPanel.add(searchPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel with split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        
        // Top part - Invoices list
        JPanel invoicesPanel = new JPanel(new BorderLayout());
        invoicesPanel.setBorder(BorderFactory.createTitledBorder("Invoices"));
        
        String[] invoiceColumns = {"Invoice ID", "Date", "Total Amount", "Items Count"};
        invoicesTableModel = new DefaultTableModel(invoiceColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        invoicesTable = new JTable(invoicesTableModel);
        invoicesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        invoicesTable.setRowHeight(25);
        invoicesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadInvoiceItems();
            }
        });
        
        // Format currency column
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        invoicesTable.getColumnModel().getColumn(2).setCellRenderer(rightRenderer);
        
        JScrollPane invoicesScroll = new JScrollPane(invoicesTable);
        invoicesPanel.add(invoicesScroll, BorderLayout.CENTER);
        
        splitPane.setTopComponent(invoicesPanel);
        
        // Bottom part - Invoice items
        JPanel itemsPanel = new JPanel(new BorderLayout());
        itemsPanel.setBorder(BorderFactory.createTitledBorder("Invoice Items"));
        
        String[] itemColumns = {"Item ID", "Product ID", "Product Name", "Quantity", "Unit Price", "Total"};
        itemsTableModel = new DefaultTableModel(itemColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        itemsTable = new JTable(itemsTableModel);
        itemsTable.setRowHeight(25);
        itemsTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        itemsTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        
        JScrollPane itemsScroll = new JScrollPane(itemsTable);
        itemsPanel.add(itemsScroll, BorderLayout.CENTER);
        
        splitPane.setBottomComponent(itemsPanel);
        
        add(splitPane, BorderLayout.CENTER);
        
        // Bottom panel with buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        viewDetailsButton = new JButton("View Full Details");
        viewDetailsButton.addActionListener(e -> viewFullDetails());
        
        closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(viewDetailsButton);
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadAllInvoices() {
        try {
            invoicesTableModel.setRowCount(0);
            itemsTableModel.setRowCount(0);
            
            List<Invoice> invoices = invoiceDAO.getAllInvoices();
            for (Invoice invoice : invoices) {
                int itemCount = invoiceItemDAO.getInvoiceItemsByInvoiceId(invoice.getInvoiceID()).size();
                invoicesTableModel.addRow(new Object[]{
                    invoice.getInvoiceID(),
                    invoice.getInvoiceDate().format(DATE_FORMATTER),
                    String.format("€ %.2f", invoice.getTotalAmount()),
                    itemCount
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading invoices: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void searchInvoice() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please enter an invoice ID to search", 
                "Search", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            int invoiceId = Integer.parseInt(searchText);
            Invoice invoice = invoiceDAO.getInvoiceById(invoiceId);
            
            invoicesTableModel.setRowCount(0);
            itemsTableModel.setRowCount(0);
            
            if (invoice != null) {
                int itemCount = invoiceItemDAO.getInvoiceItemsByInvoiceId(invoice.getInvoiceID()).size();
                invoicesTableModel.addRow(new Object[]{
                    invoice.getInvoiceID(),
                    invoice.getInvoiceDate().format(DATE_FORMATTER),
                    String.format("€ %.2f", invoice.getTotalAmount()),
                    itemCount
                });
                invoicesTable.setRowSelectionInterval(0, 0);
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Invoice #" + invoiceId + " not found", 
                    "Not Found", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, 
                "Invalid invoice ID format", 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error searching invoice: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadInvoiceItems() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow == -1) {
            itemsTableModel.setRowCount(0);
            return;
        }
        
        try {
            int invoiceId = (int) invoicesTableModel.getValueAt(selectedRow, 0);
            List<InvoiceItem> items = invoiceItemDAO.getInvoiceItemsByInvoiceId(invoiceId);
            
            itemsTableModel.setRowCount(0);
            
            for (InvoiceItem item : items) {
                Product product = productDAO.getProductById(item.getProductID());
                String productName = (product != null) ? product.getName() : "Unknown";
                
                java.math.BigDecimal itemTotal = item.getPriceAtSale()
                    .multiply(new java.math.BigDecimal(item.getQuantity()));
                
                itemsTableModel.addRow(new Object[]{
                    item.getInvoiceItemID(),
                    item.getProductID(),
                    productName,
                    item.getQuantity(),
                    String.format("€ %.2f", item.getPriceAtSale()),
                    String.format("€ %.2f", itemTotal)
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading invoice items: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void viewFullDetails() {
        int selectedRow = invoicesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select an invoice to view details", 
                "No Selection", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            int invoiceId = (int) invoicesTableModel.getValueAt(selectedRow, 0);
            Invoice invoice = invoiceDAO.getInvoiceById(invoiceId);
            List<InvoiceItem> items = invoiceItemDAO.getInvoiceItemsByInvoiceId(invoiceId);
            
            StringBuilder details = new StringBuilder();
            details.append("═══════════════════════════════════════\n");
            details.append("           INVOICE DETAILS\n");
            details.append("═══════════════════════════════════════\n\n");
            details.append("Invoice ID: #").append(invoice.getInvoiceID()).append("\n");
            details.append("Date: ").append(invoice.getInvoiceDate().format(DATE_FORMATTER)).append("\n");
            details.append("Total Amount: €").append(String.format("%.2f", invoice.getTotalAmount())).append("\n\n");
            details.append("───────────────────────────────────────\n");
            details.append("Items:\n");
            details.append("───────────────────────────────────────\n\n");
            
            for (InvoiceItem item : items) {
                Product product = productDAO.getProductById(item.getProductID());
                String productName = (product != null) ? product.getName() : "Unknown";
                java.math.BigDecimal itemTotal = item.getPriceAtSale()
                    .multiply(new java.math.BigDecimal(item.getQuantity()));
                
                details.append("• ").append(productName).append("\n");
                details.append("  Quantity: ").append(item.getQuantity()).append("\n");
                details.append("  Unit Price: €").append(String.format("%.2f", item.getPriceAtSale())).append("\n");
                details.append("  Subtotal: €").append(String.format("%.2f", itemTotal)).append("\n\n");
            }
            
            details.append("═══════════════════════════════════════\n");
            details.append("GRAND TOTAL: €").append(String.format("%.2f", invoice.getTotalAmount())).append("\n");
            details.append("═══════════════════════════════════════\n");
            
            JTextArea textArea = new JTextArea(details.toString());
            textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
            textArea.setEditable(false);
            textArea.setCaretPosition(0);
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 600));
            
            JOptionPane.showMessageDialog(this, 
                scrollPane, 
                "Invoice #" + invoiceId + " - Full Details", 
                JOptionPane.PLAIN_MESSAGE);
            
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error viewing details: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
}

