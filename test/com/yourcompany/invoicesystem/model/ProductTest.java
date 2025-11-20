package com.yourcompany.invoicesystem.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

/**
 * Unit tests for Product model
 */
public class ProductTest {
    
    @Test
    public void testProductCreation() {
        Product product = new Product(1, "Test Product", new BigDecimal("10.50"), 100);
        
        assertEquals(1, product.getProductID());
        assertEquals("Test Product", product.getName());
        assertEquals(new BigDecimal("10.50"), product.getPrice());
        assertEquals(100, product.getStock());
    }
    
    @Test
    public void testProductSetters() {
        Product product = new Product();
        
        product.setProductID(2);
        product.setName("Updated Product");
        product.setPrice(new BigDecimal("25.99"));
        product.setStock(50);
        
        assertEquals(2, product.getProductID());
        assertEquals("Updated Product", product.getName());
        assertEquals(new BigDecimal("25.99"), product.getPrice());
        assertEquals(50, product.getStock());
    }
    
    @Test
    public void testProductAliasGetters() {
        Product product = new Product(1, "Test Product", new BigDecimal("10.50"), 100);
        
        assertEquals("Test Product", product.getProductName());
        assertEquals(new BigDecimal("10.50"), product.getUnitPrice());
        assertEquals(100, product.getStockQuantity());
        assertEquals(10, product.getLowStockThreshold());
    }
    
    @Test
    public void testProductToString() {
        Product product = new Product(1, "Test Product", new BigDecimal("10.50"), 100);
        String toString = product.toString();
        
        assertTrue(toString.contains("productID=1"));
        assertTrue(toString.contains("name=Test Product"));
        assertTrue(toString.contains("price=10.50"));
        assertTrue(toString.contains("stock=100"));
    }
}
