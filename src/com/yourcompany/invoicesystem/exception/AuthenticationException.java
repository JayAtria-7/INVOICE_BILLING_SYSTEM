package com.yourcompany.invoicesystem.exception;

/**
 * Exception thrown when authentication fails
 */
public class AuthenticationException extends InvoiceSystemException {
    
    private static final long serialVersionUID = 1L;
    
    public AuthenticationException(String message) {
        super(message);
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
