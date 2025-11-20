package com.yourcompany.invoicesystem.dao; // Ensure this matches your package name

import com.yourcompany.invoicesystem.model.InvoiceItem; // Import the InvoiceItem model
import com.yourcompany.invoicesystem.util.DBUtil;      // Import the DB utility

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal; // Needed for price

public class InvoiceItemDAO {

    /**
     * Saves a single invoice item to the database.
     * Assumes InvoiceItemID is auto-incrementing in the DB (optional to retrieve).
     *
     * @param item The InvoiceItem object to save. It must have InvoiceID and ProductID set correctly.
     * @return true if the item was saved successfully, false otherwise.
     */
    public boolean saveInvoiceItem(InvoiceItem item) {
        // Note: We don't need to specify InvoiceItemID if it's auto-incrementing
        String sql = "INSERT INTO InvoiceItems (InvoiceID, ProductID, Quantity, PriceAtSale) VALUES (?, ?, ?, ?)";
        boolean success = false;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, item.getInvoiceID());
            pstmt.setInt(2, item.getProductID());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setBigDecimal(4, item.getPriceAtSale());

            int rowsAffected = pstmt.executeUpdate();
            success = (rowsAffected > 0);

        } catch (SQLException e) {
            System.err.println("Error saving invoice item: " + e.getMessage());
            // e.printStackTrace();
        }
        return success;
    }

    /**
     * Retrieves all items associated with a specific invoice ID.
     *
     * @param invoiceId The ID of the invoice whose items are to be retrieved.
     * @return A List of InvoiceItem objects for the given invoice.
     */
    public List<InvoiceItem> getInvoiceItemsByInvoiceId(int invoiceId) {
        List<InvoiceItem> items = new ArrayList<>();
        String sql = "SELECT InvoiceItemID, InvoiceID, ProductID, Quantity, PriceAtSale FROM InvoiceItems WHERE InvoiceID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, invoiceId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int itemId = rs.getInt("InvoiceItemID");
                    // int invId = rs.getInt("InvoiceID"); // We already know this (it's the input param)
                    int prodId = rs.getInt("ProductID");
                    int quantity = rs.getInt("Quantity");
                    BigDecimal price = rs.getBigDecimal("PriceAtSale");

                    // Create new InvoiceItem and add to list
                    items.add(new InvoiceItem(itemId, invoiceId, prodId, quantity, price));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching items for invoice ID " + invoiceId + ": " + e.getMessage());
            // e.printStackTrace();
        }
        return items;
    }

    // Optional: Method to save multiple items at once (more efficient)
    /**
     * Saves a list of invoice items, typically for a single invoice.
     * Consider using batch updates for better performance with large lists.
     *
     * @param items The List of InvoiceItem objects to save. All items should belong to the same InvoiceID.
     * @return true if all items were saved successfully (basic check), false otherwise.
     */
    public boolean saveInvoiceItems(List<InvoiceItem> items) {
        // Simple implementation: save one by one.
        // For performance with many items, look into JDBC Batch Updates.
        boolean allSuccess = true;
        for (InvoiceItem item : items) {
            if (!saveInvoiceItem(item)) {
                allSuccess = false;
                System.err.println("Failed to save item for product ID: " + item.getProductID());
                // Maybe stop or log more details
            }
        }
        return allSuccess;
        /*
        // --- Example structure for Batch Update (more complex) ---
        String sql = "INSERT INTO InvoiceItems (InvoiceID, ProductID, Quantity, PriceAtSale) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false); // Start transaction

            for (InvoiceItem item : items) {
                pstmt.setInt(1, item.getInvoiceID());
                pstmt.setInt(2, item.getProductID());
                pstmt.setInt(3, item.getQuantity());
                pstmt.setBigDecimal(4, item.getPriceAtSale());
                pstmt.addBatch(); // Add statement to batch
            }

            int[] results = pstmt.executeBatch(); // Execute all batched statements
            conn.commit(); // Commit transaction
            // Check results array for errors if needed
            return true; // Simplified success check

        } catch (SQLException e) {
             // Handle potential BatchUpdateException, rollback transaction etc.
             System.err.println("Error saving invoice items in batch: " + e.getMessage());
             // Rollback conn.rollback();
             return false;
        } finally {
            // Reset auto-commit conn.setAutoCommit(true);
        }
        */
    }
    
    /**
     * Saves a single invoice item using an existing connection (for transaction support).
     * Does NOT commit or close the connection - caller is responsible.
     *
     * @param item The InvoiceItem object to save.
     * @param conn The existing database connection.
     * @throws SQLException if a database access error occurs.
     */
    public void saveInvoiceItem(InvoiceItem item, Connection conn) throws SQLException {
        String sql = "INSERT INTO InvoiceItems (InvoiceID, ProductID, Quantity, PriceAtSale) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, item.getInvoiceID());
            pstmt.setInt(2, item.getProductID());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setBigDecimal(4, item.getPriceAtSale());

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Failed to save invoice item for product ID: " + item.getProductID());
            }
        }
    }
}
