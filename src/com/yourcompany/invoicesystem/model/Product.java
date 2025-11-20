package com.yourcompany.invoicesystem.model; // Package declaration should be first

import java.math.BigDecimal; // Use BigDecimal for precise currency values

public class Product {

    // Private fields corresponding to database columns
    private int productID;
    private String name;
    private BigDecimal price; // Use BigDecimal for money
    private int stock;

    // Constructor (you might have multiple constructors later)
    public Product() {
        // Default constructor
    }

    public Product(int productID, String name, BigDecimal price, int stock) {
        this.productID = productID;
        this.name = name;
        this.price = price;
        this.stock = stock;
    }

    // Getter and Setter methods for each field
    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    // Alias methods for compatibility with new features
    public String getProductName() {
        return name;
    }
    
    public String getDescription() {
        return name; // Using name as description for now
    }
    
    public BigDecimal getUnitPrice() {
        return price;
    }
    
    public int getStockQuantity() {
        return stock;
    }
    
    public int getLowStockThreshold() {
        return 10; // Default threshold
    }
    
    public String getBarcode() {
        return null; // Not implemented yet
    }
    
    public String getCategory() {
        return null; // Not implemented yet
    }

    // Optional: Override toString() for easy printing/debugging
    @Override
    public String toString() {
        return "Product [productID=" + productID + ", name=" + name + ", price=" + price + ", stock=" + stock + "]";
    }
}
