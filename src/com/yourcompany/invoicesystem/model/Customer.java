/**
 * Author: Jay Prakash Kumar
 * Copyright (c) 2025
 * Licensed under MIT License
 */

package com.yourcompany.invoicesystem.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Customer {
    
    private int customerID;
    private String customerName;
    private String email;
    private String phone;
    private String address;
    private int loyaltyPoints;
    private BigDecimal totalPurchases;
    private LocalDateTime createdDate;
    private LocalDateTime lastPurchaseDate;
    
    public Customer() {
        this.totalPurchases = BigDecimal.ZERO;
        this.loyaltyPoints = 0;
    }
    
    public Customer(int customerID, String customerName, String phone, String email) {
        this();
        this.customerID = customerID;
        this.customerName = customerName;
        this.phone = phone;
        this.email = email;
    }
    
    // Getters and Setters
    public int getCustomerID() {
        return customerID;
    }
    
    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }
    
    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }
    
    public BigDecimal getTotalPurchases() {
        return totalPurchases;
    }
    
    public void setTotalPurchases(BigDecimal totalPurchases) {
        this.totalPurchases = totalPurchases;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getLastPurchaseDate() {
        return lastPurchaseDate;
    }
    
    public void setLastPurchaseDate(LocalDateTime lastPurchaseDate) {
        this.lastPurchaseDate = lastPurchaseDate;
    }
    
    @Override
    public String toString() {
        return customerName + " (" + phone + ")";
    }
}

