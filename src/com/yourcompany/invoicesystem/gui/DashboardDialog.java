/**
 * Author: Jay Prakash Kumar
 * Copyright (c) 2025
 * Licensed under MIT License
 */

package com.yourcompany.invoicesystem.gui;

import com.yourcompany.invoicesystem.dao.InvoiceDAO;
import com.yourcompany.invoicesystem.dao.ProductDAO;
import com.yourcompany.invoicesystem.model.Invoice;
import com.yourcompany.invoicesystem.model.Product;
import com.yourcompany.invoicesystem.util.DBUtil;
import com.yourcompany.invoicesystem.util.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.List;

/**
 * Dashboard Dialog
 * Displays key performance indicators and analytics
 */
public class DashboardDialog extends JDialog {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd");
    
    // KPI Labels
    private JLabel todaySalesLabel;
    private JLabel monthSalesLabel;
    private JLabel totalProductsLabel;
    private JLabel lowStockCountLabel;
    
    // Data areas
    private JTextArea recentInvoicesArea;
    private JTextArea topProductsArea;
    private JTextArea revenueTrendArea;
    
    public DashboardDialog(Frame parent) {
        super(parent, "Dashboard - Business Analytics", false);
        initComponents();
        loadDashboardData();
        setSize(1000, 700);
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Title
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("📊 Business Dashboard");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 18f));
        titlePanel.add(titleLabel);
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        
        // KPI Panel (Top)
        JPanel kpiPanel = createKPIPanel();
        mainPanel.add(kpiPanel, BorderLayout.NORTH);
        
        // Center Panel - Analytics
        JPanel centerPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        
        // Recent Invoices
        JPanel recentPanel = new JPanel(new BorderLayout());
        recentPanel.setBorder(BorderFactory.createTitledBorder("Recent Invoices"));
        recentInvoicesArea = new JTextArea();
        recentInvoicesArea.setEditable(false);
        recentInvoicesArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        recentPanel.add(new JScrollPane(recentInvoicesArea), BorderLayout.CENTER);
        centerPanel.add(recentPanel);
        
        // Top Products
        JPanel topProductsPanel = new JPanel(new BorderLayout());
        topProductsPanel.setBorder(BorderFactory.createTitledBorder("Top Selling Products"));
        topProductsArea = new JTextArea();
        topProductsArea.setEditable(false);
        topProductsArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        topProductsPanel.add(new JScrollPane(topProductsArea), BorderLayout.CENTER);
        centerPanel.add(topProductsPanel);
        
        // Revenue Trend
        JPanel trendPanel = new JPanel(new BorderLayout());
        trendPanel.setBorder(BorderFactory.createTitledBorder("7-Day Revenue Trend"));
        revenueTrendArea = new JTextArea();
        revenueTrendArea.setEditable(false);
        revenueTrendArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        trendPanel.add(new JScrollPane(revenueTrendArea), BorderLayout.CENTER);
        centerPanel.add(trendPanel);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadDashboardData());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(refreshButton);
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createKPIPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 10, 5));
        panel.setBorder(new EmptyBorder(10, 0, 10, 0));
        
        // Today's Sales
        JPanel todayPanel = createKPICard("Today's Sales", "€0.00", Color.decode("#2196F3"));
        todaySalesLabel = (JLabel) ((JPanel) todayPanel.getComponent(1)).getComponent(0);
        panel.add(todayPanel);
        
        // This Month's Sales
        JPanel monthPanel = createKPICard("This Month", "€0.00", Color.decode("#4CAF50"));
        monthSalesLabel = (JLabel) ((JPanel) monthPanel.getComponent(1)).getComponent(0);
        panel.add(monthPanel);
        
        // Total Products
        JPanel productsPanel = createKPICard("Total Products", "0", Color.decode("#FF9800"));
        totalProductsLabel = (JLabel) ((JPanel) productsPanel.getComponent(1)).getComponent(0);
        panel.add(productsPanel);
        
        // Low Stock Items
        JPanel lowStockPanel = createKPICard("Low Stock Items", "0", Color.decode("#F44336"));
        lowStockCountLabel = (JLabel) ((JPanel) lowStockPanel.getComponent(1)).getComponent(0);
        panel.add(lowStockPanel);
        
        return panel;
    }
    
    private JPanel createKPICard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                new EmptyBorder(10, 10, 10, 10)));
        card.setBackground(Color.WHITE);
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.PLAIN, 11f));
        titleLabel.setForeground(Color.GRAY);
        card.add(titleLabel, BorderLayout.NORTH);
        
        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        valuePanel.setBackground(Color.WHITE);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(valueLabel.getFont().deriveFont(Font.BOLD, 20f));
        valueLabel.setForeground(color);
        valuePanel.add(valueLabel);
        card.add(valuePanel, BorderLayout.CENTER);
        
        return card;
    }
    
    private void loadDashboardData() {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                loadKPIs();
                loadRecentInvoices();
                loadTopProducts();
                loadRevenueTrend();
                return null;
            }
        };
        worker.execute();
    }
    
    private void loadKPIs() {
        try (Connection conn = DBUtil.getConnection()) {
            // Today's sales
            String todaySql = "SELECT SUM(TotalAmount) FROM Invoices WHERE DATE(InvoiceDate) = CURDATE()";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(todaySql);
            if (rs.next()) {
                double todaySales = rs.getDouble(1);
                todaySalesLabel.setText(String.format("€%.2f", todaySales));
            }
            
            // This month's sales
            String monthSql = "SELECT SUM(TotalAmount) FROM Invoices WHERE MONTH(InvoiceDate) = MONTH(CURDATE()) AND YEAR(InvoiceDate) = YEAR(CURDATE())";
            rs = stmt.executeQuery(monthSql);
            if (rs.next()) {
                double monthSales = rs.getDouble(1);
                monthSalesLabel.setText(String.format("€%.2f", monthSales));
            }
            
            // Total products
            ProductDAO productDAO = new ProductDAO();
            List<Product> products = productDAO.getAllProducts();
            totalProductsLabel.setText(String.valueOf(products.size()));
            
            // Low stock count
            long lowStockCount = products.stream()
                    .filter(p -> p.getStockQuantity() <= p.getLowStockThreshold())
                    .count();
            lowStockCountLabel.setText(String.valueOf(lowStockCount));
            
        } catch (Exception e) {
            Logger.error("Error loading KPIs: " + e.getMessage(), e);
        }
    }
    
    private void loadRecentInvoices() {
        try {
            InvoiceDAO invoiceDAO = new InvoiceDAO();
            List<Invoice> invoices = invoiceDAO.getAllInvoices();
            
            // Sort by date descending and take last 10
            invoices.sort((a, b) -> b.getInvoiceDate().compareTo(a.getInvoiceDate()));
            List<Invoice> recent = invoices.subList(0, Math.min(10, invoices.size()));
            
            StringBuilder text = new StringBuilder();
            text.append(String.format("%-8s %-20s %10s%n", "ID", "Date", "Amount"));
            text.append("─".repeat(40)).append("\n");
            
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            for (Invoice inv : recent) {
                text.append(String.format("%-8d %-20s €%8.2f%n",
                        inv.getInvoiceID(),
                        df.format(inv.getInvoiceDate()),
                        inv.getTotalAmount()));
            }
            
            recentInvoicesArea.setText(text.toString());
            
        } catch (Exception e) {
            Logger.error("Error loading recent invoices: " + e.getMessage(), e);
            recentInvoicesArea.setText("Error loading data");
        }
    }
    
    private void loadTopProducts() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT p.ProductID, p.ProductName, SUM(ii.Quantity) as TotalSold " +
                        "FROM InvoiceItems ii " +
                        "JOIN Products p ON ii.ProductID = p.ProductID " +
                        "GROUP BY p.ProductID, p.ProductName " +
                        "ORDER BY TotalSold DESC " +
                        "LIMIT 10";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            StringBuilder text = new StringBuilder();
            text.append(String.format("%-5s %-25s %8s%n", "ID", "Product", "Sold"));
            text.append("─".repeat(40)).append("\n");
            
            while (rs.next()) {
                text.append(String.format("%-5d %-25s %8d%n",
                        rs.getInt("ProductID"),
                        truncate(rs.getString("ProductName"), 25),
                        rs.getInt("TotalSold")));
            }
            
            topProductsArea.setText(text.toString());
            
        } catch (Exception e) {
            Logger.error("Error loading top products: " + e.getMessage(), e);
            topProductsArea.setText("Error loading data");
        }
    }
    
    private void loadRevenueTrend() {
        try (Connection conn = DBUtil.getConnection()) {
            String sql = "SELECT DATE(InvoiceDate) as SaleDate, SUM(TotalAmount) as DailyRevenue " +
                        "FROM Invoices " +
                        "WHERE InvoiceDate >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                        "GROUP BY DATE(InvoiceDate) " +
                        "ORDER BY SaleDate";
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            StringBuilder text = new StringBuilder();
            text.append(String.format("%-12s %12s %s%n", "Date", "Revenue", "Chart"));
            text.append("─".repeat(50)).append("\n");
            
            double maxRevenue = 0;
            Map<Date, Double> data = new LinkedHashMap<>();
            
            while (rs.next()) {
                Date date = rs.getDate("SaleDate");
                double revenue = rs.getDouble("DailyRevenue");
                data.put(date, revenue);
                maxRevenue = Math.max(maxRevenue, revenue);
            }
            
            // Create ASCII bar chart
            for (Map.Entry<Date, Double> entry : data.entrySet()) {
                double revenue = entry.getValue();
                int barLength = maxRevenue > 0 ? (int) ((revenue / maxRevenue) * 20) : 0;
                String bar = "█".repeat(barLength);
                
                text.append(String.format("%-12s €%10.2f %s%n",
                        DATE_FORMAT.format(entry.getKey()),
                        revenue,
                        bar));
            }
            
            revenueTrendArea.setText(text.toString());
            
        } catch (Exception e) {
            Logger.error("Error loading revenue trend: " + e.getMessage(), e);
            revenueTrendArea.setText("Error loading data");
        }
    }
    
    private String truncate(String str, int length) {
        if (str == null) return "";
        return str.length() > length ? str.substring(0, length - 3) + "..." : str;
    }
}

