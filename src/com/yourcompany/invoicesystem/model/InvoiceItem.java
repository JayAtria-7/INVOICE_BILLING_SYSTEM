package com.yourcompany.invoicesystem.model;

import java.math.BigDecimal;

public class InvoiceItem {

    private int invoiceItemID;
    private int invoiceID; // Foreign key to link to Invoice table
    private int productID; // Foreign key to link to Product table
    private int quantity;
    private BigDecimal priceAtSale; // Price of the product when this specific sale occurred

    // Default constructor
    public InvoiceItem() {
    }

    // Constructor with fields
    public InvoiceItem(int invoiceItemID, int invoiceID, int productID, int quantity, BigDecimal priceAtSale) {
        this.invoiceItemID = invoiceItemID;
        this.invoiceID = invoiceID;
        this.productID = productID;
        this.quantity = quantity;
        this.priceAtSale = priceAtSale;
    }

    // --- Getters and Setters ---

    public int getInvoiceItemID() {
        return invoiceItemID;
    }

    public void setInvoiceItemID(int invoiceItemID) {
        this.invoiceItemID = invoiceItemID;
    }

    public int getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(int invoiceID) {
        this.invoiceID = invoiceID;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPriceAtSale() {
        return priceAtSale;
    }

    public void setPriceAtSale(BigDecimal priceAtSale) {
        this.priceAtSale = priceAtSale;
    }

    // Alias method for compatibility
    public BigDecimal getUnitPrice() {
        return priceAtSale;
    }

    // Optional: toString() for debugging
    @Override
    public String toString() {
        return "InvoiceItem [invoiceItemID=" + invoiceItemID + ", invoiceID=" + invoiceID + ", productID=" + productID
                + ", quantity=" + quantity + ", priceAtSale=" + priceAtSale + "]";
    }
}
