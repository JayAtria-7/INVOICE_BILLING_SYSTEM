/**
 * Author: Jay Prakash Kumar
 * Copyright (c) 2025
 * Licensed under MIT License
 */

package com.yourcompany.invoicesystem.service;

import com.yourcompany.invoicesystem.dao.ProductDAO;
import com.yourcompany.invoicesystem.exception.BusinessLogicException;
import com.yourcompany.invoicesystem.model.Product;
import com.yourcompany.invoicesystem.validation.ValidationException;
import com.yourcompany.invoicesystem.validation.Validator;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service layer for Product business logic
 * Handles validation and business rules for product operations
 */
public class ProductService {
    
    private final ProductDAO productDAO;
    
    public ProductService() {
        this.productDAO = new ProductDAO();
    }
    
    /**
     * Create a new product with validation
     * @param name Product name
     * @param price Product price
     * @param stock Initial stock quantity
     * @return Created product
     * @throws BusinessLogicException if creation fails
     */
    public Product createProduct(String name, BigDecimal price, int stock) throws BusinessLogicException {
        try {
            // Validate inputs
            Validator.validateProductName(name);
            Validator.validatePositiveAmount(price);
            Validator.validateQuantity(stock);
            
            // Check for duplicate product name
            List<Product> existingProducts = productDAO.getAllProducts();
            for (Product p : existingProducts) {
                if (p.getName().equalsIgnoreCase(name.trim())) {
                    throw new BusinessLogicException("Product with name '" + name + "' already exists");
                }
            }
            
            // Create product
            Product product = new Product();
            product.setName(name.trim());
            product.setPrice(price);
            product.setStock(stock);
            
            productDAO.addProduct(product);
            return product;
            
        } catch (ValidationException e) {
            throw new BusinessLogicException("Validation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to create product: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update product with validation
     * @param product Product to update
     * @throws BusinessLogicException if update fails
     */
    public void updateProduct(Product product) throws BusinessLogicException {
        try {
            // Validate inputs
            Validator.validateId(product.getProductID(), "Product ID");
            Validator.validateProductName(product.getName());
            Validator.validatePositiveAmount(product.getPrice());
            Validator.validateQuantity(product.getStock());
            
            productDAO.updateProduct(product);
            
        } catch (ValidationException e) {
            throw new BusinessLogicException("Validation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to update product: " + e.getMessage(), e);
        }
    }
    
    /**
     * Delete product with business rule checks
     * @param productId Product ID to delete
     * @throws BusinessLogicException if deletion fails or violates business rules
     */
    public void deleteProduct(int productId) throws BusinessLogicException {
        try {
            Validator.validateId(productId, "Product ID");
            
            // Check if product exists
            Product product = productDAO.getProductById(productId);
            if (product == null) {
                throw new BusinessLogicException("Product not found with ID: " + productId);
            }
            
            // Business rule: Don't allow deletion if product has stock
            if (product.getStock() > 0) {
                throw new BusinessLogicException(
                    "Cannot delete product with existing stock. Current stock: " + product.getStock());
            }
            
            productDAO.deleteProduct(productId);
            
        } catch (ValidationException e) {
            throw new BusinessLogicException("Validation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to delete product: " + e.getMessage(), e);
        }
    }
    
    /**
     * Update product stock with validation
     * @param productId Product ID
     * @param quantity Quantity to add (positive) or remove (negative)
     * @throws BusinessLogicException if update fails
     */
    public void updateStock(int productId, int quantity) throws BusinessLogicException {
        try {
            Validator.validateId(productId, "Product ID");
            
            Product product = productDAO.getProductById(productId);
            if (product == null) {
                throw new BusinessLogicException("Product not found with ID: " + productId);
            }
            
            int newStock = product.getStock() + quantity;
            if (newStock < 0) {
                throw new BusinessLogicException(
                    "Insufficient stock. Available: " + product.getStock() + ", Requested: " + Math.abs(quantity));
            }
            
            product.setStock(newStock);
            productDAO.updateProduct(product);
            
        } catch (ValidationException e) {
            throw new BusinessLogicException("Validation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to update stock: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all products
     * @return List of all products
     * @throws BusinessLogicException if retrieval fails
     */
    public List<Product> getAllProducts() throws BusinessLogicException {
        try {
            return productDAO.getAllProducts();
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to retrieve products: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get product by ID
     * @param productId Product ID
     * @return Product or null if not found
     * @throws BusinessLogicException if retrieval fails
     */
    public Product getProductById(int productId) throws BusinessLogicException {
        try {
            Validator.validateId(productId, "Product ID");
            return productDAO.getProductById(productId);
        } catch (ValidationException e) {
            throw new BusinessLogicException("Validation failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to retrieve product: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if product is low on stock
     * @param product Product to check
     * @return true if stock is below threshold
     */
    public boolean isLowStock(Product product) {
        return product.getStock() <= product.getLowStockThreshold();
    }
    
    /**
     * Get all low stock products
     * @return List of products with low stock
     * @throws BusinessLogicException if retrieval fails
     */
    public List<Product> getLowStockProducts() throws BusinessLogicException {
        try {
            List<Product> allProducts = productDAO.getAllProducts();
            return allProducts.stream()
                .filter(this::isLowStock)
                .toList();
        } catch (Exception e) {
            throw new BusinessLogicException("Failed to retrieve low stock products: " + e.getMessage(), e);
        }
    }
}

