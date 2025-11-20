package com.yourcompany.invoicesystem.dao; // Ensure this matches your package name

import com.yourcompany.invoicesystem.model.Product; // Import the Product model
import com.yourcompany.invoicesystem.util.DBUtil;   // Import the DB utility

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal; // Needed for price

public class ProductDAO {

    /**
     * Retrieves a list of all products from the database.
     *
     * @return A List of Product objects.
     */
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        // SQL query to select all products
        String sql = "SELECT ProductID, Name, Price, Stock FROM Products ORDER BY Name";

        // Using try-with-resources for automatic closing of Connection, PreparedStatement, ResultSet
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            // Iterate through the result set
            while (rs.next()) {
                // Create a Product object for each row
                int id = rs.getInt("ProductID");
                String name = rs.getString("Name");
                BigDecimal price = rs.getBigDecimal("Price");
                int stock = rs.getInt("Stock");
                products.add(new Product(id, name, price, stock)); // Assuming constructor exists
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all products: " + e.getMessage());
            // In a real application, log the exception properly
            // e.printStackTrace();
        }
        return products;
    }

    /**
     * Retrieves a single product by its ID.
     *
     * @param productId The ID of the product to retrieve.
     * @return The Product object, or null if not found.
     */
    public Product getProductById(int productId) {
        Product product = null;
        String sql = "SELECT ProductID, Name, Price, Stock FROM Products WHERE ProductID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, productId); // Set the ID parameter in the query

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String name = rs.getString("Name");
                    BigDecimal price = rs.getBigDecimal("Price");
                    int stock = rs.getInt("Stock");
                    product = new Product(productId, name, price, stock);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching product with ID " + productId + ": " + e.getMessage());
            // e.printStackTrace();
        }
        return product;
    }

    /**
     * Adds a new product to the database.
     * Assumes ProductID is set manually or uses DB auto-increment if PK column is defined that way.
     * If using auto-increment, modify SQL and potentially return the generated ID.
     *
     * @param product The Product object to add.
     * @return true if the product was added successfully, false otherwise.
     */
    public boolean addProduct(Product product) {
        // Adjust SQL if using AUTO_INCREMENT/SERIAL for ProductID
        String sql = "INSERT INTO Products (ProductID, Name, Price, Stock) VALUES (?, ?, ?, ?)";
        boolean success = false;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, product.getProductID()); // If ID is auto-generated, skip this line and column in SQL
            pstmt.setString(2, product.getName());
            pstmt.setBigDecimal(3, product.getPrice());
            pstmt.setInt(4, product.getStock());

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);

        } catch (SQLException e) {
            System.err.println("Error adding product: " + e.getMessage());
            // Consider checking for specific SQLState codes (e.g., duplicate key)
            // e.printStackTrace();
        }
        return success;
    }

    /**
     * Updates the stock level of a specific product.
     * USE WITH CAUTION - Consider concurrency issues in real-world apps.
     *
     * @param productId The ID of the product to update.
     * @param newStock  The new stock level.
     * @return true if the stock was updated successfully, false otherwise.
     */
     public boolean updateProductStock(int productId, int newStock) {
         String sql = "UPDATE Products SET Stock = ? WHERE ProductID = ?";
         boolean success = false;

         try (Connection conn = DBUtil.getConnection();
              PreparedStatement pstmt = conn.prepareStatement(sql)) {

             pstmt.setInt(1, newStock);
             pstmt.setInt(2, productId);

             int rowsAffected = pstmt.executeUpdate();
             success = (rowsAffected > 0);

         } catch (SQLException e) {
             System.err.println("Error updating stock for product ID " + productId + ": " + e.getMessage());
             // e.printStackTrace();
         }
         return success;
     }

     /**
      * Decreases the stock level of a product by a specified quantity.
      * Thread-safe version using pessimistic locking (SELECT FOR UPDATE).
      *
      * @param productId The ID of the product.
      * @param quantityToDecrease The amount to decrease the stock by.
      * @return true if stock was decreased successfully, false otherwise (e.g., insufficient stock).
      */
     public boolean decreaseProductStock(int productId, int quantityToDecrease) {
         Connection conn = null;
         try {
             conn = DBUtil.getConnection(false); // Start transaction
             boolean success = decreaseProductStock(productId, quantityToDecrease, conn);
             conn.commit();
             return success;
         } catch (SQLException e) {
             System.err.println("Error decreasing stock for product ID " + productId + ": " + e.getMessage());
             if (conn != null) {
                 try {
                     conn.rollback();
                 } catch (SQLException ex) {
                     System.err.println("Error rolling back transaction: " + ex.getMessage());
                 }
             }
             return false;
         } finally {
             if (conn != null) {
                 try {
                     conn.setAutoCommit(true);
                     conn.close();
                 } catch (SQLException e) {
                     System.err.println("Error closing connection: " + e.getMessage());
                 }
             }
         }
     }
     
     /**
      * Thread-safe stock decrease using pessimistic locking within a transaction.
      * Uses SELECT FOR UPDATE to lock the row and prevent concurrent modifications.
      *
      * @param productId The ID of the product.
      * @param quantityToDecrease The amount to decrease the stock by.
      * @param conn The database connection with transaction started.
      * @return true if stock was decreased successfully.
      * @throws SQLException if insufficient stock or database error occurs.
      */
     public boolean decreaseProductStock(int productId, int quantityToDecrease, Connection conn) throws SQLException {
         // Use SELECT FOR UPDATE to lock the row for this transaction
         String selectSql = "SELECT Stock FROM Products WHERE ProductID = ? FOR UPDATE";
         String updateSql = "UPDATE Products SET Stock = Stock - ? WHERE ProductID = ?";
         
         int currentStock;
         try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
             selectStmt.setInt(1, productId);
             try (ResultSet rs = selectStmt.executeQuery()) {
                 if (!rs.next()) {
                     throw new SQLException("Product not found with ID: " + productId);
                 }
                 currentStock = rs.getInt("Stock");
             }
         }
         
         // Check if sufficient stock is available
         if (currentStock < quantityToDecrease) {
             throw new SQLException("Insufficient stock. Available: " + currentStock + ", Requested: " + quantityToDecrease);
         }
         
         // Update the stock
         try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
             updateStmt.setInt(1, quantityToDecrease);
             updateStmt.setInt(2, productId);
             int rowsAffected = updateStmt.executeUpdate();
             if (rowsAffected == 0) {
                 throw new SQLException("Failed to update stock for product ID: " + productId);
             }
         }
         
         return true;
     }

    // --- Other potential methods ---
    // public boolean updateProduct(Product product) { ... }
    // public boolean deleteProduct(int productId) { ... }

}
