package com.yourcompany.invoicesystem.exception;

/**
 * Exception thrown when business logic rules are violated
 */
public class BusinessLogicException extends InvoiceSystemException {
    
    private static final long serialVersionUID = 1L;
    
    public BusinessLogicException(String message) {
        super(message);
    }
    
    public BusinessLogicException(String message, Throwable cause) {
        super(message, cause);
    }
}
