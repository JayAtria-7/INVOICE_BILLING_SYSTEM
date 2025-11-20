/**
 * Author: Jay Prakash Kumar
 * Copyright (c) 2025
 * Licensed under MIT License
 */

package com.yourcompany.invoicesystem.model;

import java.time.LocalDateTime;

public class User {
    
    public enum Role {
        ADMIN, CASHIER, MANAGER
    }
    
    private int userID;
    private String username;
    private String passwordHash;
    private String fullName;
    private Role role;
    private boolean isActive;
    private LocalDateTime createdDate;
    private LocalDateTime lastLoginDate;
    
    public User() {
    }
    
    public User(int userID, String username, String fullName, Role role, boolean isActive) {
        this.userID = userID;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.isActive = isActive;
    }
    
    // Getters and Setters
    public int getUserID() {
        return userID;
    }
    
    public void setUserID(int userID) {
        this.userID = userID;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    
    public Role getRole() {
        return role;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public LocalDateTime getLastLoginDate() {
        return lastLoginDate;
    }
    
    public void setLastLoginDate(LocalDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }
    
    @Override
    public String toString() {
        return "User [userID=" + userID + ", username=" + username + ", fullName=" + fullName + 
               ", role=" + role + ", isActive=" + isActive + "]";
    }
}

