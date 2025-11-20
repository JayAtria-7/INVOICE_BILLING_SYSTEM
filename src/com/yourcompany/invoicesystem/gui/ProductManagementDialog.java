/**
 * Author: Jay Prakash Kumar
 * Copyright (c) 2025
 * Licensed under MIT License
 */

package com.yourcompany.invoicesystem.gui;

import com.yourcompany.invoicesystem.dao.ProductDAO;
import com.yourcompany.invoicesystem.model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class ProductManagementDialog extends JDialog {
    
    private ProductDAO productDAO;
    
    private JTable productTable;
    private DefaultTableModel tableModel;
    
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JButton closeButton;
    
    public ProductManagementDialog(JFrame parent) {
        super(parent, "Product Management", true);
        
        productDAO = new ProductDAO();
        
        initComponents();
        loadProducts();
        
        setSize(800, 600);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        // Top panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        
        JLabel titleLabel = new JLabel("Product Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Center panel with table
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        String[] columnNames = {"Product ID", "Name", "Price (€)", "Stock"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        productTable = new JTable(tableModel);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productTable.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(productTable);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // Bottom panel with buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        addButton = new JButton("Add Product");
        addButton.addActionListener(e -> addProduct());
        
        editButton = new JButton("Edit Product");
        editButton.addActionListener(e -> editProduct());
        
        deleteButton = new JButton("Delete Product");
        deleteButton.addActionListener(e -> deleteProduct());
        
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadProducts());
        
        closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadProducts() {
        try {
            tableModel.setRowCount(0);
            List<Product> products = productDAO.getAllProducts();
            
            for (Product product : products) {
                tableModel.addRow(new Object[]{
                    product.getProductID(),
                    product.getName(),
                    String.format("%.2f", product.getPrice()),
                    product.getStock()
                });
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error loading products: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void addProduct() {
        ProductEditDialog dialog = new ProductEditDialog(this, null);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            Product product = dialog.getProduct();
            try {
                if (productDAO.addProduct(product)) {
                    JOptionPane.showMessageDialog(this, 
                        "Product added successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    loadProducts();
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Failed to add product", 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error adding product: " + ex.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void editProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a product to edit", 
                "No Selection", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        try {
            int productId = (int) tableModel.getValueAt(selectedRow, 0);
            Product product = productDAO.getProductById(productId);
            
            if (product == null) {
                JOptionPane.showMessageDialog(this, 
                    "Product not found", 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            ProductEditDialog dialog = new ProductEditDialog(this, product);
            dialog.setVisible(true);
            
            if (dialog.isConfirmed()) {
                Product updatedProduct = dialog.getProduct();
                // Note: You'll need to add updateProduct method to ProductDAO
                JOptionPane.showMessageDialog(this, 
                    "Product update feature needs updateProduct() method in ProductDAO.\nProduct ID: " + updatedProduct.getProductID(), 
                    "Info", 
                    JOptionPane.INFORMATION_MESSAGE);
                loadProducts();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, 
                "Error editing product: " + ex.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a product to delete", 
                "No Selection", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int productId = (int) tableModel.getValueAt(selectedRow, 0);
        String productName = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete:\n" + productName + " (ID: " + productId + ")?", 
            "Confirm Delete", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Note: You'll need to add deleteProduct method to ProductDAO
            JOptionPane.showMessageDialog(this, 
                "Product delete feature needs deleteProduct() method in ProductDAO.\nProduct ID: " + productId, 
                "Info", 
                JOptionPane.INFORMATION_MESSAGE);
            loadProducts();
        }
    }
    
    // Inner class for Add/Edit dialog
    private class ProductEditDialog extends JDialog {
        private JTextField idField;
        private JTextField nameField;
        private JTextField priceField;
        private JTextField stockField;
        
        private boolean confirmed = false;
        private Product product;
        private boolean isEditMode;
        
        public ProductEditDialog(Dialog parent, Product existingProduct) {
            super(parent, existingProduct == null ? "Add Product" : "Edit Product", true);
            
            this.isEditMode = (existingProduct != null);
            this.product = existingProduct;
            
            initDialog();
            
            if (existingProduct != null) {
                populateFields(existingProduct);
            }
            
            setSize(400, 300);
            setLocationRelativeTo(parent);
        }
        
        private void initDialog() {
            setLayout(new BorderLayout(10, 10));
            
            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);
            
            // Product ID
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Product ID:"), gbc);
            
            gbc.gridx = 1;
            idField = new JTextField(15);
            if (isEditMode) {
                idField.setEnabled(false);
            }
            formPanel.add(idField, gbc);
            
            // Name
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Name:"), gbc);
            
            gbc.gridx = 1;
            nameField = new JTextField(15);
            formPanel.add(nameField, gbc);
            
            // Price
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Price (€):"), gbc);
            
            gbc.gridx = 1;
            priceField = new JTextField(15);
            formPanel.add(priceField, gbc);
            
            // Stock
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Stock:"), gbc);
            
            gbc.gridx = 1;
            stockField = new JTextField(15);
            formPanel.add(stockField, gbc);
            
            add(formPanel, BorderLayout.CENTER);
            
            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            
            JButton saveButton = new JButton("Save");
            saveButton.addActionListener(e -> save());
            
            JButton cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> dispose());
            
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            
            add(buttonPanel, BorderLayout.SOUTH);
        }
        
        private void populateFields(Product product) {
            idField.setText(String.valueOf(product.getProductID()));
            nameField.setText(product.getName());
            priceField.setText(product.getPrice().toString());
            stockField.setText(String.valueOf(product.getStock()));
        }
        
        private void save() {
            try {
                int id = isEditMode ? Integer.parseInt(idField.getText()) : Integer.parseInt(idField.getText().trim());
                String name = nameField.getText().trim();
                BigDecimal price = new BigDecimal(priceField.getText().trim());
                int stock = Integer.parseInt(stockField.getText().trim());
                
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(this, 
                        "Product name cannot be empty", 
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (price.compareTo(BigDecimal.ZERO) < 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Price cannot be negative", 
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (stock < 0) {
                    JOptionPane.showMessageDialog(this, 
                        "Stock cannot be negative", 
                        "Validation Error", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                product = new Product(id, name, price, stock);
                confirmed = true;
                dispose();
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Invalid number format. Please check your input.", 
                    "Validation Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
        
        public boolean isConfirmed() {
            return confirmed;
        }
        
        public Product getProduct() {
            return product;
        }
    }
}

