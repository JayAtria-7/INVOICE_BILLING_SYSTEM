/**
 * Author: Jay Prakash Kumar
 * Copyright (c) 2025
 * Licensed under MIT License
 */

package com.yourcompany.invoicesystem.util;

import com.yourcompany.invoicesystem.dao.InvoiceDAO;
import com.yourcompany.invoicesystem.dao.InvoiceItemDAO;
import com.yourcompany.invoicesystem.dao.ProductDAO;
import com.yourcompany.invoicesystem.model.Invoice;
import com.yourcompany.invoicesystem.model.InvoiceItem;
import com.yourcompany.invoicesystem.model.Product;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportGenerator {
    
    private InvoiceDAO invoiceDAO;
    private InvoiceItemDAO invoiceItemDAO;
    private ProductDAO productDAO;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    
    public ReportGenerator() {
        this.invoiceDAO = new InvoiceDAO();
        this.invoiceItemDAO = new InvoiceItemDAO();
        this.productDAO = new ProductDAO();
    }
    
    /**
     * Generate sales report for a date range
     */
    public String generateSalesReport(LocalDate startDate, LocalDate endDate) {
        StringBuilder report = new StringBuilder();
        
        report.append("═══════════════════════════════════════════════════\n");
        report.append("                  SALES REPORT\n");
        report.append("═══════════════════════════════════════════════════\n");
        report.append("Period: ").append(startDate.format(DATE_FORMATTER))
              .append(" to ").append(endDate.format(DATE_FORMATTER)).append("\n");
        report.append("Generated: ").append(LocalDate.now().format(DATE_FORMATTER)).append("\n\n");
        
        try {
            List<Invoice> invoices = invoiceDAO.getInvoicesByDateRange(startDate, endDate);
            
            if (invoices.isEmpty()) {
                report.append("No sales data available for this period.\n");
                return report.toString();
            }
            
            BigDecimal totalRevenue = BigDecimal.ZERO;
            int totalInvoices = invoices.size();
            int totalItemsSold = 0;
            
            report.append("───────────────────────────────────────────────────\n");
            report.append("Invoice Details:\n");
            report.append("───────────────────────────────────────────────────\n\n");
            
            for (Invoice invoice : invoices) {
                List<InvoiceItem> items = invoiceItemDAO.getInvoiceItemsByInvoiceId(invoice.getInvoiceID());
                int itemCount = items.size();
                totalItemsSold += itemCount;
                totalRevenue = totalRevenue.add(invoice.getTotalAmount());
                
                report.append(String.format("Invoice #%-6d  Date: %-15s  Amount: €%10.2f  Items: %d\n",
                    invoice.getInvoiceID(),
                    invoice.getInvoiceDate().format(DATE_FORMATTER),
                    invoice.getTotalAmount(),
                    itemCount));
            }
            
            report.append("\n═══════════════════════════════════════════════════\n");
            report.append("Summary:\n");
            report.append("───────────────────────────────────────────────────\n");
            report.append(String.format("Total Invoices:        %d\n", totalInvoices));
            report.append(String.format("Total Items Sold:      %d\n", totalItemsSold));
            report.append(String.format("Total Revenue:         €%.2f\n", totalRevenue));
            
            if (totalInvoices > 0) {
                BigDecimal avgInvoice = totalRevenue.divide(new BigDecimal(totalInvoices), 2, java.math.RoundingMode.HALF_UP);
                report.append(String.format("Average Invoice:       €%.2f\n", avgInvoice));
            }
            
            report.append("═══════════════════════════════════════════════════\n");
            
        } catch (Exception e) {
            report.append("\nError generating report: ").append(e.getMessage()).append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * Generate inventory report
     */
    public String generateInventoryReport() {
        StringBuilder report = new StringBuilder();
        
        report.append("═══════════════════════════════════════════════════\n");
        report.append("                INVENTORY REPORT\n");
        report.append("═══════════════════════════════════════════════════\n");
        report.append("Generated: ").append(LocalDate.now().format(DATE_FORMATTER)).append("\n\n");
        
        try {
            List<Product> products = productDAO.getAllProducts();
            
            if (products.isEmpty()) {
                report.append("No products in inventory.\n");
                return report.toString();
            }
            
            BigDecimal totalInventoryValue = BigDecimal.ZERO;
            int totalProducts = products.size();
            int totalStockUnits = 0;
            int lowStockCount = 0;
            int outOfStockCount = 0;
            
            report.append("───────────────────────────────────────────────────\n");
            report.append(String.format("%-6s %-25s %12s %10s %15s\n", 
                "ID", "Product Name", "Price", "Stock", "Total Value"));
            report.append("───────────────────────────────────────────────────\n");
            
            for (Product product : products) {
                BigDecimal productValue = product.getPrice().multiply(new BigDecimal(product.getStock()));
                totalInventoryValue = totalInventoryValue.add(productValue);
                totalStockUnits += product.getStock();
                
                String status = "";
                if (product.getStock() == 0) {
                    status = " [OUT OF STOCK]";
                    outOfStockCount++;
                } else if (product.getStock() < 10) {
                    status = " [LOW STOCK]";
                    lowStockCount++;
                }
                
                String name = product.getName();
                if (name.length() > 25) {
                    name = name.substring(0, 22) + "...";
                }
                
                report.append(String.format("%-6d %-25s €%11.2f %10d €%14.2f%s\n",
                    product.getProductID(),
                    name,
                    product.getPrice(),
                    product.getStock(),
                    productValue,
                    status));
            }
            
            report.append("\n═══════════════════════════════════════════════════\n");
            report.append("Summary:\n");
            report.append("───────────────────────────────────────────────────\n");
            report.append(String.format("Total Products:        %d\n", totalProducts));
            report.append(String.format("Total Stock Units:     %d\n", totalStockUnits));
            report.append(String.format("Total Inventory Value: €%.2f\n", totalInventoryValue));
            report.append(String.format("Low Stock Items:       %d\n", lowStockCount));
            report.append(String.format("Out of Stock Items:    %d\n", outOfStockCount));
            report.append("═══════════════════════════════════════════════════\n");
            
        } catch (Exception e) {
            report.append("\nError generating report: ").append(e.getMessage()).append("\n");
        }
        
        return report.toString();
    }
    
    /**
     * Generate revenue analysis report
     */
    public String generateRevenueAnalysisReport(LocalDate startDate, LocalDate endDate) {
        StringBuilder report = new StringBuilder();
        
        report.append("═══════════════════════════════════════════════════\n");
        report.append("              REVENUE ANALYSIS REPORT\n");
        report.append("═══════════════════════════════════════════════════\n");
        report.append("Period: ").append(startDate.format(DATE_FORMATTER))
              .append(" to ").append(endDate.format(DATE_FORMATTER)).append("\n");
        report.append("Generated: ").append(LocalDate.now().format(DATE_FORMATTER)).append("\n\n");
        
        try {
            List<Invoice> invoices = invoiceDAO.getInvoicesByDateRange(startDate, endDate);
            
            if (invoices.isEmpty()) {
                report.append("No data available for this period.\n");
                return report.toString();
            }
            
            // Analyze product sales
            Map<Integer, ProductSalesInfo> productSales = new HashMap<>();
            BigDecimal revenueAccumulator = BigDecimal.ZERO;
            
            for (Invoice invoice : invoices) {
                revenueAccumulator = revenueAccumulator.add(invoice.getTotalAmount());
                List<InvoiceItem> items = invoiceItemDAO.getInvoiceItemsByInvoiceId(invoice.getInvoiceID());
                
                for (InvoiceItem item : items) {
                    int productId = item.getProductID();
                    ProductSalesInfo info = productSales.getOrDefault(productId, 
                        new ProductSalesInfo(productId));
                    
                    info.quantitySold += item.getQuantity();
                    BigDecimal itemTotal = item.getPriceAtSale()
                        .multiply(new BigDecimal(item.getQuantity()));
                    info.revenue = info.revenue.add(itemTotal);
                    
                    productSales.put(productId, info);
                }
            }
            
            report.append("───────────────────────────────────────────────────\n");
            report.append("Top Selling Products by Revenue:\n");
            report.append("───────────────────────────────────────────────────\n\n");
            report.append(String.format("%-6s %-25s %12s %12s %10s\n", 
                "ID", "Product Name", "Qty Sold", "Revenue", "% of Total"));
            report.append("───────────────────────────────────────────────────\n");
            
            // Make final for lambda usage
            final BigDecimal totalRevenue = revenueAccumulator;
            
            // Sort by revenue
            productSales.values().stream()
                .sorted((a, b) -> b.revenue.compareTo(a.revenue))
                .limit(10)
                .forEach(info -> {
                    try {
                        Product product = productDAO.getProductById(info.productId);
                        String name = (product != null) ? product.getName() : "Unknown";
                        if (name.length() > 25) {
                            name = name.substring(0, 22) + "...";
                        }
                        
                        BigDecimal percentage = info.revenue
                            .divide(totalRevenue, 4, java.math.RoundingMode.HALF_UP)
                            .multiply(new BigDecimal(100));
                        
                        report.append(String.format("%-6d %-25s %12d €%11.2f %9.2f%%\n",
                            info.productId,
                            name,
                            info.quantitySold,
                            info.revenue,
                            percentage));
                    } catch (Exception e) {
                        // Skip this product if error occurs
                    }
                });
            
            report.append("\n═══════════════════════════════════════════════════\n");
            report.append("Revenue Summary:\n");
            report.append("───────────────────────────────────────────────────\n");
            report.append(String.format("Total Revenue:         €%.2f\n", totalRevenue));
            report.append(String.format("Total Invoices:        %d\n", invoices.size()));
            report.append(String.format("Unique Products Sold:  %d\n", productSales.size()));
            
            if (!invoices.isEmpty()) {
                BigDecimal avgRevenue = totalRevenue.divide(
                    new BigDecimal(invoices.size()), 2, java.math.RoundingMode.HALF_UP);
                report.append(String.format("Average per Invoice:   €%.2f\n", avgRevenue));
            }
            
            report.append("═══════════════════════════════════════════════════\n");
            
        } catch (Exception e) {
            report.append("\nError generating report: ").append(e.getMessage()).append("\n");
        }
        
        return report.toString();
    }
    
    // Helper class for product sales tracking
    private static class ProductSalesInfo {
        int productId;
        int quantitySold = 0;
        BigDecimal revenue = BigDecimal.ZERO;
        
        ProductSalesInfo(int productId) {
            this.productId = productId;
        }
    }
}

