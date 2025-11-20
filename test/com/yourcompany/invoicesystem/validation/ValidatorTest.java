package com.yourcompany.invoicesystem.validation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

/**
 * Unit tests for Validator class
 */
public class ValidatorTest {
    
    @Test
    public void testValidateUsername_Valid() {
        assertDoesNotThrow(() -> Validator.validateUsername("admin"));
        assertDoesNotThrow(() -> Validator.validateUsername("user123"));
        assertDoesNotThrow(() -> Validator.validateUsername("test_user"));
    }
    
    @Test
    public void testValidateUsername_TooShort() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> Validator.validateUsername("ab"));
        assertTrue(exception.getMessage().contains("between 3 and 20"));
    }
    
    @Test
    public void testValidateUsername_TooLong() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> Validator.validateUsername("this_is_a_very_long_username_that_exceeds_limit"));
        assertTrue(exception.getMessage().contains("between 3 and 20"));
    }
    
    @Test
    public void testValidateUsername_InvalidCharacters() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> Validator.validateUsername("user@name"));
        assertTrue(exception.getMessage().contains("letters, numbers, and underscores"));
    }
    
    @Test
    public void testValidateUsername_Empty() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> Validator.validateUsername(""));
        assertTrue(exception.getMessage().contains("cannot be empty"));
    }
    
    @Test
    public void testValidatePassword_Valid() {
        assertDoesNotThrow(() -> Validator.validatePassword("password123"));
        assertDoesNotThrow(() -> Validator.validatePassword("Admin@123"));
    }
    
    @Test
    public void testValidatePassword_TooShort() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> Validator.validatePassword("pass"));
        assertTrue(exception.getMessage().contains("at least 6 characters"));
    }
    
    @Test
    public void testValidateAmount_Valid() {
        assertDoesNotThrow(() -> Validator.validateAmount(new BigDecimal("100.50")));
        assertDoesNotThrow(() -> Validator.validateAmount(BigDecimal.ZERO));
    }
    
    @Test
    public void testValidateAmount_Negative() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> Validator.validateAmount(new BigDecimal("-10")));
        assertTrue(exception.getMessage().contains("cannot be negative"));
    }
    
    @Test
    public void testValidatePositiveAmount_Valid() {
        assertDoesNotThrow(() -> Validator.validatePositiveAmount(new BigDecimal("10.50")));
    }
    
    @Test
    public void testValidatePositiveAmount_Zero() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> Validator.validatePositiveAmount(BigDecimal.ZERO));
        assertTrue(exception.getMessage().contains("greater than zero"));
    }
    
    @Test
    public void testValidateQuantity_Valid() {
        assertDoesNotThrow(() -> Validator.validateQuantity(100));
        assertDoesNotThrow(() -> Validator.validateQuantity(0));
    }
    
    @Test
    public void testValidateQuantity_Negative() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> Validator.validateQuantity(-5));
        assertTrue(exception.getMessage().contains("cannot be negative"));
    }
    
    @Test
    public void testValidateEmail_Valid() {
        assertDoesNotThrow(() -> Validator.validateEmail("user@example.com"));
        assertDoesNotThrow(() -> Validator.validateEmail("test.user@company.co.uk"));
    }
    
    @Test
    public void testValidateEmail_Invalid() {
        assertThrows(ValidationException.class, () -> Validator.validateEmail("invalid-email"));
        assertThrows(ValidationException.class, () -> Validator.validateEmail("@example.com"));
        assertThrows(ValidationException.class, () -> Validator.validateEmail("user@"));
    }
    
    @Test
    public void testValidatePercentage_Valid() {
        assertDoesNotThrow(() -> Validator.validatePercentage(new BigDecimal("50")));
        assertDoesNotThrow(() -> Validator.validatePercentage(BigDecimal.ZERO));
        assertDoesNotThrow(() -> Validator.validatePercentage(new BigDecimal("100")));
    }
    
    @Test
    public void testValidatePercentage_Negative() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> Validator.validatePercentage(new BigDecimal("-10")));
        assertTrue(exception.getMessage().contains("cannot be negative"));
    }
    
    @Test
    public void testValidatePercentage_TooHigh() {
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> Validator.validatePercentage(new BigDecimal("150")));
        assertTrue(exception.getMessage().contains("cannot exceed 100"));
    }
}
