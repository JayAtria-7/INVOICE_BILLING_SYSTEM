/**
 * Author: Jay Prakash Kumar
 * Copyright (c) 2025
 * Licensed under MIT License
 */

package com.yourcompany.invoicesystem.exception;

/**
 * Base exception for all application-specific exceptions
 */
public class InvoiceSystemException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    public InvoiceSystemException(String message) {
        super(message);
    }
    
    public InvoiceSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}

