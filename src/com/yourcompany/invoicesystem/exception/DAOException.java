package com.yourcompany.invoicesystem.exception;

/**
 * Exception thrown when database access operations fail
 */
public class DAOException extends InvoiceSystemException {
    
    private static final long serialVersionUID = 1L;
    
    public DAOException(String message) {
        super(message);
    }
    
    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }
}
