/**
 * Author: Jay Prakash Kumar
 * Copyright (c) 2025
 * Licensed under MIT License
 */

package com.yourcompany.invoicesystem.validation;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Utility class for input validation
 * Provides comprehensive validation methods for all application inputs
 */
public class Validator {
    
    // Regex patterns
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+\\-\\s()]{7,20}$");
    
    /**
     * Validates username format
     * @param username Username to validate
     * @throws ValidationException if validation fails
     */
    public static void validateUsername(String username) throws ValidationException {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }
        
        if (username.length() < 3 || username.length() > 20) {
            throw new ValidationException("Username must be between 3 and 20 characters");
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new ValidationException("Username can only contain letters, numbers, and underscores");
        }
    }
    
    /**
     * Validates password strength
     * @param password Password to validate
     * @throws ValidationException if validation fails
     */
    public static void validatePassword(String password) throws ValidationException {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Password cannot be empty");
        }
        
        if (password.length() < 6) {
            throw new ValidationException("Password must be at least 6 characters long");
        }
        
        if (password.length() > 100) {
            throw new ValidationException("Password is too long (max 100 characters)");
        }
    }
    
    /**
     * Validates product name
     * @param name Product name to validate
     * @throws ValidationException if validation fails
     */
    public static void validateProductName(String name) throws ValidationException {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Product name cannot be empty");
        }
        
        if (name.length() < 2) {
            throw new ValidationException("Product name must be at least 2 characters");
        }
        
        if (name.length() > 100) {
            throw new ValidationException("Product name is too long (max 100 characters)");
        }
    }
    
    /**
     * Validates price/amount
     * @param amount Amount to validate
     * @throws ValidationException if validation fails
     */
    public static void validateAmount(BigDecimal amount) throws ValidationException {
        if (amount == null) {
            throw new ValidationException("Amount cannot be null");
        }
        
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Amount cannot be negative");
        }
        
        if (amount.compareTo(new BigDecimal("999999.99")) > 0) {
            throw new ValidationException("Amount is too large (max 999,999.99)");
        }
    }
    
    /**
     * Validates positive amount (must be greater than zero)
     * @param amount Amount to validate
     * @throws ValidationException if validation fails
     */
    public static void validatePositiveAmount(BigDecimal amount) throws ValidationException {
        validateAmount(amount);
        
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Amount must be greater than zero");
        }
    }
    
    /**
     * Validates stock quantity
     * @param quantity Quantity to validate
     * @throws ValidationException if validation fails
     */
    public static void validateQuantity(int quantity) throws ValidationException {
        if (quantity < 0) {
            throw new ValidationException("Quantity cannot be negative");
        }
        
        if (quantity > 1000000) {
            throw new ValidationException("Quantity is too large (max 1,000,000)");
        }
    }
    
    /**
     * Validates positive quantity (must be greater than zero)
     * @param quantity Quantity to validate
     * @throws ValidationException if validation fails
     */
    public static void validatePositiveQuantity(int quantity) throws ValidationException {
        if (quantity <= 0) {
            throw new ValidationException("Quantity must be greater than zero");
        }
        
        validateQuantity(quantity);
    }
    
    /**
     * Validates email format
     * @param email Email to validate
     * @throws ValidationException if validation fails
     */
    public static void validateEmail(String email) throws ValidationException {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email cannot be empty");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }
    
    /**
     * Validates phone number format
     * @param phone Phone number to validate
     * @throws ValidationException if validation fails
     */
    public static void validatePhone(String phone) throws ValidationException {
        if (phone == null || phone.trim().isEmpty()) {
            throw new ValidationException("Phone number cannot be empty");
        }
        
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new ValidationException("Invalid phone number format");
        }
    }
    
    /**
     * Validates string is not empty
     * @param value String to validate
     * @param fieldName Field name for error message
     * @throws ValidationException if validation fails
     */
    public static void validateNotEmpty(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty");
        }
    }
    
    /**
     * Validates string length
     * @param value String to validate
     * @param fieldName Field name for error message
     * @param minLength Minimum length
     * @param maxLength Maximum length
     * @throws ValidationException if validation fails
     */
    public static void validateLength(String value, String fieldName, int minLength, int maxLength) 
            throws ValidationException {
        validateNotEmpty(value, fieldName);
        
        if (value.length() < minLength) {
            throw new ValidationException(fieldName + " must be at least " + minLength + " characters");
        }
        
        if (value.length() > maxLength) {
            throw new ValidationException(fieldName + " cannot exceed " + maxLength + " characters");
        }
    }
    
    /**
     * Validates ID is positive
     * @param id ID to validate
     * @param fieldName Field name for error message
     * @throws ValidationException if validation fails
     */
    public static void validateId(int id, String fieldName) throws ValidationException {
        if (id <= 0) {
            throw new ValidationException(fieldName + " must be a positive number");
        }
    }
    
    /**
     * Validates percentage (0-100)
     * @param percentage Percentage to validate
     * @throws ValidationException if validation fails
     */
    public static void validatePercentage(BigDecimal percentage) throws ValidationException {
        if (percentage == null) {
            throw new ValidationException("Percentage cannot be null");
        }
        
        if (percentage.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("Percentage cannot be negative");
        }
        
        if (percentage.compareTo(new BigDecimal("100")) > 0) {
            throw new ValidationException("Percentage cannot exceed 100");
        }
    }
}

