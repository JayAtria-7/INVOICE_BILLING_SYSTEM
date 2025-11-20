/**
 * Author: Jay Prakash Kumar
 * Copyright (c) 2025
 * Licensed under MIT License
 */

package com.yourcompany.invoicesystem.util;

import com.yourcompany.invoicesystem.model.Invoice;
import com.yourcompany.invoicesystem.model.InvoiceItem;
import com.yourcompany.invoicesystem.model.Product;
import com.yourcompany.invoicesystem.dao.InvoiceItemDAO;
import com.yourcompany.invoicesystem.dao.ProductDAO;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class for printing receipts to text format.
 * Can be extended for PDF printing using libraries like iText or Apache PDFBox.
 */
public class ReceiptPrinter {
    
    private InvoiceItemDAO invoiceItemDAO;
    private ProductDAO productDAO;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
    private static final int RECEIPT_WIDTH = 48; // Characters wide for thermal printer
    
    public ReceiptPrinter() {
        this.invoiceItemDAO = new InvoiceItemDAO();
        this.productDAO = new ProductDAO();
    }
    
    /**
     * Generate receipt text for an invoice
     */
    public String generateReceiptText(Invoice invoice) {
        StringBuilder receipt = new StringBuilder();
        
        try {
            List<InvoiceItem> items = invoiceItemDAO.getInvoiceItemsByInvoiceId(invoice.getInvoiceID());
            
            // Header
            receipt.append(centerText("PROBILLING", RECEIPT_WIDTH)).append("\n");
            receipt.append(centerText("Invoice Management System", RECEIPT_WIDTH)).append("\n");
            receipt.append(centerText("123 Business Street", RECEIPT_WIDTH)).append("\n");
            receipt.append(centerText("Tel: (555) 123-4567", RECEIPT_WIDTH)).append("\n");
            receipt.append(repeatChar('=', RECEIPT_WIDTH)).append("\n");
            receipt.append(centerText("TAX INVOICE", RECEIPT_WIDTH)).append("\n");
            receipt.append(repeatChar('=', RECEIPT_WIDTH)).append("\n\n");
            
            // Invoice details
            receipt.append(String.format("Invoice #: %d\n", invoice.getInvoiceID()));
            receipt.append(String.format("Date: %s\n", 
                invoice.getInvoiceDate().atStartOfDay().format(DATE_FORMATTER)));
            receipt.append(repeatChar('-', RECEIPT_WIDTH)).append("\n\n");
            
            // Items header
            receipt.append(String.format("%-20s %5s %9s %10s\n", "Item", "Qty", "Price", "Total"));
            receipt.append(repeatChar('-', RECEIPT_WIDTH)).append("\n");
            
            // Items
            for (InvoiceItem item : items) {
                Product product = productDAO.getProductById(item.getProductID());
                String productName = (product != null) ? product.getName() : "Unknown";
                
                // Truncate long names
                if (productName.length() > 20) {
                    productName = productName.substring(0, 17) + "...";
                }
                
                BigDecimal itemTotal = item.getPriceAtSale()
                    .multiply(new BigDecimal(item.getQuantity()));
                
                receipt.append(String.format("%-20s %5d %9.2f %10.2f\n",
                    productName,
                    item.getQuantity(),
                    item.getPriceAtSale(),
                    itemTotal));
            }
            
            receipt.append(repeatChar('-', RECEIPT_WIDTH)).append("\n");
            
            // Total
            receipt.append(String.format("%36s %10.2f\n", "TOTAL:", invoice.getTotalAmount()));
            receipt.append(repeatChar('=', RECEIPT_WIDTH)).append("\n\n");
            
            // Footer
            receipt.append(centerText("Thank you for your business!", RECEIPT_WIDTH)).append("\n");
            receipt.append(centerText("Please come again", RECEIPT_WIDTH)).append("\n\n");
            receipt.append(repeatChar('=', RECEIPT_WIDTH)).append("\n");
            
        } catch (Exception e) {
            receipt.append("Error generating receipt: ").append(e.getMessage()).append("\n");
        }
        
        return receipt.toString();
    }
    
    /**
     * Generate detailed receipt text for printing
     */
    public String generateDetailedReceipt(Invoice invoice, BigDecimal discountPercent, 
                                         BigDecimal discountAmount, BigDecimal subtotal) {
        StringBuilder receipt = new StringBuilder();
        
        try {
            List<InvoiceItem> items = invoiceItemDAO.getInvoiceItemsByInvoiceId(invoice.getInvoiceID());
            
            // Header with logo area
            receipt.append("\n");
            receipt.append(repeatChar('=', 60)).append("\n");
            receipt.append(centerText("PROBILLING - INVOICE MANAGEMENT SYSTEM", 60)).append("\n");
            receipt.append(centerText("Your Trusted Billing Partner", 60)).append("\n");
            receipt.append(repeatChar('=', 60)).append("\n\n");
            
            // Company details
            receipt.append(centerText("123 Business Street, Suite 100", 60)).append("\n");
            receipt.append(centerText("City, State 12345", 60)).append("\n");
            receipt.append(centerText("Phone: (555) 123-4567 | Email: info@probilling.com", 60)).append("\n");
            receipt.append(centerText("Tax ID: 12-3456789", 60)).append("\n\n");
            
            receipt.append(repeatChar('-', 60)).append("\n");
            receipt.append(centerText("TAX INVOICE / RECEIPT", 60)).append("\n");
            receipt.append(repeatChar('-', 60)).append("\n\n");
            
            // Invoice information
            receipt.append(String.format("Invoice Number : #%-10d", invoice.getInvoiceID())).append("\n");
            receipt.append(String.format("Invoice Date   : %s\n", 
                invoice.getInvoiceDate().atStartOfDay().format(DATE_FORMATTER)));
            receipt.append(String.format("Cashier        : %s\n", "Admin")); // Can be extended for multi-user
            receipt.append("\n");
            receipt.append(repeatChar('-', 60)).append("\n");
            
            // Items table header
            receipt.append(String.format("%-30s %6s %10s %12s\n", 
                "Product Description", "Qty", "Unit Price", "Amount"));
            receipt.append(repeatChar('-', 60)).append("\n");
            
            // Items
            for (InvoiceItem item : items) {
                Product product = productDAO.getProductById(item.getProductID());
                String productName = (product != null) ? product.getName() : "Unknown Product";
                
                if (productName.length() > 30) {
                    productName = productName.substring(0, 27) + "...";
                }
                
                BigDecimal itemTotal = item.getPriceAtSale()
                    .multiply(new BigDecimal(item.getQuantity()));
                
                receipt.append(String.format("%-30s %6d  €%9.2f  €%10.2f\n",
                    productName,
                    item.getQuantity(),
                    item.getPriceAtSale(),
                    itemTotal));
            }
            
            receipt.append(repeatChar('-', 60)).append("\n\n");
            
            // Totals section
            receipt.append(String.format("%48s  €%10.2f\n", "Subtotal:", subtotal));
            
            if (discountPercent.compareTo(BigDecimal.ZERO) > 0) {
                receipt.append(String.format("%48s  €%10.2f\n", 
                    String.format("Discount (%.1f%%):", discountPercent), 
                    discountAmount));
            }
            
            receipt.append(repeatChar('-', 60)).append("\n");
            receipt.append(String.format("%48s  €%10.2f\n", "GRAND TOTAL:", invoice.getTotalAmount()));
            receipt.append(repeatChar('=', 60)).append("\n\n");
            
            // Payment information
            receipt.append("Payment Method : Cash\n"); // Can be extended
            receipt.append(String.format("Amount Paid    : €%.2f\n", invoice.getTotalAmount()));
            receipt.append(String.format("Change         : €%.2f\n", BigDecimal.ZERO));
            receipt.append("\n");
            
            // Footer
            receipt.append(repeatChar('-', 60)).append("\n");
            receipt.append(centerText("Thank You For Your Business!", 60)).append("\n");
            receipt.append(centerText("Please Keep This Receipt For Your Records", 60)).append("\n");
            receipt.append(centerText("Returns Accepted Within 30 Days With Receipt", 60)).append("\n");
            receipt.append(repeatChar('-', 60)).append("\n\n");
            
            receipt.append(centerText("This is a computer-generated receipt", 60)).append("\n");
            receipt.append(centerText("For support: support@probilling.com", 60)).append("\n\n");
            
        } catch (Exception e) {
            receipt.append("Error generating detailed receipt: ").append(e.getMessage()).append("\n");
        }
        
        return receipt.toString();
    }
    
    /**
     * Save receipt to a text file
     */
    public boolean saveReceiptToFile(Invoice invoice, String filePath) {
        try {
            String receiptText = generateReceiptText(invoice);
            FileWriter writer = new FileWriter(filePath);
            writer.write(receiptText);
            writer.close();
            return true;
        } catch (IOException e) {
            System.err.println("Error saving receipt to file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Save detailed receipt to file
     */
    public boolean saveDetailedReceiptToFile(Invoice invoice, BigDecimal discountPercent,
                                            BigDecimal discountAmount, BigDecimal subtotal,
                                            String filePath) {
        try {
            String receiptText = generateDetailedReceipt(invoice, discountPercent, 
                                                        discountAmount, subtotal);
            FileWriter writer = new FileWriter(filePath);
            writer.write(receiptText);
            writer.close();
            return true;
        } catch (IOException e) {
            System.err.println("Error saving detailed receipt to file: " + e.getMessage());
            return false;
        }
    }
    
    // Helper methods
    private String centerText(String text, int width) {
        int padding = (width - text.length()) / 2;
        if (padding < 0) padding = 0;
        return repeatChar(' ', padding) + text;
    }
    
    private String repeatChar(char c, int times) {
        return new String(new char[times]).replace('\0', c);
    }
}

