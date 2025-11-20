package com.yourcompany.invoicesystem.model;

import java.math.BigDecimal;
import java.time.LocalDate; // Using java.time package for dates (generally preferred)

public class Invoice {

    private int invoiceID;
    private LocalDate invoiceDate; // Represents the date of the invoice
    private BigDecimal totalAmount; // Represents the final total amount
    private double discountPercentage; // Discount percentage
    private double taxAmount; // Tax amount
    private String paymentStatus; // Payment status: PAID, PARTIAL, PENDING
    private Integer customerID; // Customer ID (nullable)
    private Integer userID; // User ID who created the invoice (nullable)

    // Default constructor
    public Invoice() {
    }

    // Constructor with fields
    public Invoice(int invoiceID, LocalDate invoiceDate, BigDecimal totalAmount) {
        this.invoiceID = invoiceID;
        this.invoiceDate = invoiceDate;
        this.totalAmount = totalAmount;
    }

    // --- Getters and Setters ---

    public int getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(int invoiceID) {
        this.invoiceID = invoiceID;
    }

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Integer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Integer customerID) {
        this.customerID = customerID;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    // Optional: toString() for debugging
    @Override
    public String toString() {
        return "Invoice [invoiceID=" + invoiceID + ", invoiceDate=" + invoiceDate + ", totalAmount=" + totalAmount + 
               ", discount=" + discountPercentage + "%, tax=" + taxAmount + ", paymentStatus=" + paymentStatus + "]";
    }
    
    /**
     * Builder pattern for Invoice
     * Provides a fluent API for constructing Invoice objects
     */
    public static class Builder {
        private Invoice invoice;
        
        public Builder() {
            invoice = new Invoice();
        }
        
        public Builder invoiceID(int invoiceID) {
            invoice.invoiceID = invoiceID;
            return this;
        }
        
        public Builder invoiceDate(LocalDate invoiceDate) {
            invoice.invoiceDate = invoiceDate;
            return this;
        }
        
        public Builder totalAmount(BigDecimal totalAmount) {
            invoice.totalAmount = totalAmount;
            return this;
        }
        
        public Builder discountPercentage(double discountPercentage) {
            invoice.discountPercentage = discountPercentage;
            return this;
        }
        
        public Builder taxAmount(double taxAmount) {
            invoice.taxAmount = taxAmount;
            return this;
        }
        
        public Builder paymentStatus(String paymentStatus) {
            invoice.paymentStatus = paymentStatus;
            return this;
        }
        
        public Builder customerID(Integer customerID) {
            invoice.customerID = customerID;
            return this;
        }
        
        public Builder userID(Integer userID) {
            invoice.userID = userID;
            return this;
        }
        
        public Invoice build() {
            return invoice;
        }
    }
}
