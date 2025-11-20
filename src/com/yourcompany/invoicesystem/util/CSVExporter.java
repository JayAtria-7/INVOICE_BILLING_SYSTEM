package com.yourcompany.invoicesystem.util;

import com.yourcompany.invoicesystem.model.Invoice;
import com.yourcompany.invoicesystem.model.InvoiceItem;
import com.yourcompany.invoicesystem.model.Product;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * CSV Export Utility
 * Exports invoices, products, and reports to CSV format
 */
public class CSVExporter {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Export invoices to CSV
     * @param invoices List of invoices to export
     * @param filePath Output file path
     */
    public static void exportInvoices(List<Invoice> invoices, String filePath) throws IOException {
        Logger.info("Exporting " + invoices.size() + " invoices to CSV: " + filePath);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Header
            writer.println("Invoice ID,Date,Discount %,Tax Amount,Total Amount,Payment Status");
            
            // Data rows
            for (Invoice invoice : invoices) {
                writer.printf("%d,\"%s\",%.2f,%.2f,%.2f,\"%s\"%n",
                        invoice.getInvoiceID(),
                        DATE_FORMAT.format(invoice.getInvoiceDate()),
                        invoice.getDiscountPercentage(),
                        invoice.getTaxAmount(),
                        invoice.getTotalAmount(),
                        invoice.getPaymentStatus() != null ? invoice.getPaymentStatus() : "N/A");
            }
        }
        
        Logger.info("Invoice export completed");
    }
    
    /**
     * Export invoice items to CSV
     * @param items List of invoice items
     * @param filePath Output file path
     */
    public static void exportInvoiceItems(List<InvoiceItem> items, String filePath) throws IOException {
        Logger.info("Exporting " + items.size() + " invoice items to CSV: " + filePath);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Header
            writer.println("Invoice Item ID,Invoice ID,Product ID,Quantity,Unit Price,Subtotal");
            
            // Data rows
            for (InvoiceItem item : items) {
                double subtotal = item.getQuantity() * item.getUnitPrice().doubleValue();
                writer.printf("%d,%d,%d,%d,%.2f,%.2f%n",
                        item.getInvoiceItemID(),
                        item.getInvoiceID(),
                        item.getProductID(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        subtotal);
            }
        }
        
        Logger.info("Invoice items export completed");
    }
    
    /**
     * Export products to CSV
     * @param products List of products
     * @param filePath Output file path
     */
    public static void exportProducts(List<Product> products, String filePath) throws IOException {
        Logger.info("Exporting " + products.size() + " products to CSV: " + filePath);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Header
            writer.println("Product ID,Product Name,Description,Unit Price,Stock Quantity,Barcode,Category,Low Stock Threshold");
            
            // Data rows
            for (Product product : products) {
                writer.printf("%d,\"%s\",\"%s\",%.2f,%d,\"%s\",\"%s\",%d%n",
                        product.getProductID(),
                        escapeCSV(product.getProductName()),
                        escapeCSV(product.getDescription()),
                        product.getUnitPrice(),
                        product.getStockQuantity(),
                        product.getBarcode() != null ? product.getBarcode() : "",
                        product.getCategory() != null ? product.getCategory() : "",
                        product.getLowStockThreshold());
            }
        }
        
        Logger.info("Products export completed");
    }
    
    /**
     * Export sales report data to CSV
     * @param reportData Report content
     * @param filePath Output file path
     */
    public static void exportSalesReport(String reportData, String filePath) throws IOException {
        Logger.info("Exporting sales report to CSV: " + filePath);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Parse and convert text report to CSV format
            String[] lines = reportData.split("\n");
            boolean inDataSection = false;
            
            for (String line : lines) {
                line = line.trim();
                
                // Skip empty lines and separators
                if (line.isEmpty() || line.startsWith("=") || line.startsWith("-")) {
                    continue;
                }
                
                // Detect header rows
                if (line.contains("Invoice ID") && line.contains("Date")) {
                    writer.println("Invoice ID,Date,Total Amount");
                    inDataSection = true;
                    continue;
                }
                
                if (inDataSection && line.matches("^\\d+.*")) {
                    // Parse data rows
                    String[] parts = line.split("\\s{2,}"); // Split by 2+ spaces
                    if (parts.length >= 3) {
                        writer.printf("%s,%s,%s%n", parts[0].trim(), parts[1].trim(), parts[2].trim().replace("€", ""));
                    }
                }
            }
        }
        
        Logger.info("Sales report export completed");
    }
    
    /**
     * Export inventory report to CSV
     * @param reportData Report content
     * @param filePath Output file path
     */
    public static void exportInventoryReport(String reportData, String filePath) throws IOException {
        Logger.info("Exporting inventory report to CSV: " + filePath);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            String[] lines = reportData.split("\n");
            boolean inDataSection = false;
            
            for (String line : lines) {
                line = line.trim();
                
                if (line.isEmpty() || line.startsWith("=") || line.startsWith("-")) {
                    continue;
                }
                
                if (line.contains("Product ID") && line.contains("Name")) {
                    writer.println("Product ID,Name,Stock,Unit Price,Total Value");
                    inDataSection = true;
                    continue;
                }
                
                if (inDataSection && line.matches("^\\d+.*")) {
                    String[] parts = line.split("\\s{2,}");
                    if (parts.length >= 5) {
                        writer.printf("%s,\"%s\",%s,%s,%s%n",
                                parts[0].trim(),
                                parts[1].trim(),
                                parts[2].trim(),
                                parts[3].trim().replace("€", ""),
                                parts[4].trim().replace("€", ""));
                    }
                }
            }
        }
        
        Logger.info("Inventory report export completed");
    }
    
    /**
     * Escape special characters in CSV fields
     * @param value Field value
     * @return Escaped value
     */
    private static String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // Escape quotes by doubling them
        return value.replace("\"", "\"\"");
    }
    
    /**
     * Export generic data table to CSV
     * @param headers Column headers
     * @param data Data rows (each row is array of objects)
     * @param filePath Output file path
     */
    public static void exportTable(String[] headers, List<Object[]> data, String filePath) throws IOException {
        Logger.info("Exporting table to CSV: " + filePath);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write headers
            writer.println(String.join(",", headers));
            
            // Write data
            for (Object[] row : data) {
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < row.length; i++) {
                    if (i > 0) line.append(",");
                    
                    Object value = row[i];
                    if (value == null) {
                        line.append("");
                    } else if (value instanceof String) {
                        line.append("\"").append(escapeCSV((String) value)).append("\"");
                    } else {
                        line.append(value.toString());
                    }
                }
                writer.println(line.toString());
            }
        }
        
        Logger.info("Table export completed");
    }
}
