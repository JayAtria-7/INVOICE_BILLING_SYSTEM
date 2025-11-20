/**
 * Author: Jay Prakash Kumar
 * Copyright (c) 2025
 * Licensed under MIT License
 */

package com.yourcompany.invoicesystem.validation;

/**
 * Exception thrown when validation fails
 */
public class ValidationException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

