package com.yourcompany.invoicesystem.dao; // Ensure this matches your package name

import com.yourcompany.invoicesystem.model.Invoice; // Import the Invoice model
import com.yourcompany.invoicesystem.util.DBUtil;   // Import the DB utility

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; // Needed for retrieving generated keys
import java.sql.Date;      // Use java.sql.Date for PreparedStatement if column type is DATE
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {

    /**
     * Saves a new invoice to the database and returns the generated Invoice ID.
     * Assumes the InvoiceID column in the database is set to auto-increment.
     *
     * @param invoice The Invoice object to save (InvoiceID can be 0 or ignored).
     * @return The auto-generated InvoiceID from the database, or -1 if saving failed.
     */
    public int saveInvoice(Invoice invoice) {
        String sql = "INSERT INTO Invoices (InvoiceDate, TotalAmount) VALUES (?, ?)";
        int generatedInvoiceId = -1; // Default to -1 indicating failure

        // Using try-with-resources for Connection and PreparedStatement
        // Note: Statement.RETURN_GENERATED_KEYS tells the driver to make generated keys available
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Convert LocalDate to java.sql.Date for JDBC
            pstmt.setDate(1, Date.valueOf(invoice.getInvoiceDate()));
            pstmt.setBigDecimal(2, invoice.getTotalAmount());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                // Retrieve the auto-generated key (InvoiceID)
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedInvoiceId = generatedKeys.getInt(1); // Get the first generated key
                    } else {
                        System.err.println("Creating invoice failed, no ID obtained.");
                        // Consider throwing an exception here
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error saving invoice: " + e.getMessage());
            // e.printStackTrace();
            // Consider throwing a custom exception
        }
        return generatedInvoiceId;
    }

    /**
     * Retrieves a single invoice by its ID.
     *
     * @param invoiceId The ID of the invoice to retrieve.
     * @return The Invoice object, or null if not found.
     */
    public Invoice getInvoiceById(int invoiceId) {
        Invoice invoice = null;
        String sql = "SELECT InvoiceID, InvoiceDate, TotalAmount FROM Invoices WHERE InvoiceID = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, invoiceId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    LocalDate date = rs.getDate("InvoiceDate").toLocalDate(); // Convert java.sql.Date to LocalDate
                    java.math.BigDecimal total = rs.getBigDecimal("TotalAmount");
                    invoice = new Invoice(invoiceId, date, total);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching invoice with ID " + invoiceId + ": " + e.getMessage());
            // e.printStackTrace();
        }
        return invoice;
    }

    /**
     * Retrieves invoices within a specific date range.
     *
     * @param startDate The start date (inclusive).
     * @param endDate   The end date (inclusive).
     * @return A List of Invoice objects found within the date range.
     */
    public List<Invoice> getInvoicesByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT InvoiceID, InvoiceDate, TotalAmount FROM Invoices WHERE InvoiceDate BETWEEN ? AND ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, Date.valueOf(startDate)); // Convert LocalDate to java.sql.Date
            pstmt.setDate(2, Date.valueOf(endDate));   // Convert LocalDate to java.sql.Date

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("InvoiceID");
                    LocalDate date = rs.getDate("InvoiceDate").toLocalDate();
                    java.math.BigDecimal total = rs.getBigDecimal("TotalAmount");
                    invoices.add(new Invoice(id, date, total));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching invoices between " + startDate + " and " + endDate + ": " + e.getMessage());
            // e.printStackTrace();
        }
        return invoices;
    }
 // --- Add this method inside your InvoiceDAO.java class ---

    /**
     * Retrieves all invoices, ordered by date descending.
     * @return A List of all Invoice objects.
     */
    public List<Invoice> getAllInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        String sql = "SELECT InvoiceID, InvoiceDate, TotalAmount FROM Invoices ORDER BY InvoiceDate DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("InvoiceID");
                // Use java.sql.Date for direct compatibility if needed, or convert
                java.sql.Date dbSqlDate = rs.getDate("InvoiceDate");
                LocalDate date = (dbSqlDate != null) ? dbSqlDate.toLocalDate() : null; // Handle potential null dates

                java.math.BigDecimal total = rs.getBigDecimal("TotalAmount");
                if (date != null) { // Only add if date is valid
                   invoices.add(new Invoice(id, date, total));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all invoices: " + e.getMessage());
            // e.printStackTrace(); // Log properly
        }
        return invoices;
    }
    // --- End of method to add ---
    
    /**
     * Saves an invoice using an existing connection (for transaction support).
     * Does NOT commit or close the connection - caller is responsible.
     *
     * @param invoice The Invoice object to save.
     * @param conn The existing database connection.
     * @return The auto-generated InvoiceID from the database, or -1 if saving failed.
     * @throws SQLException if a database access error occurs.
     */
    public int saveInvoice(Invoice invoice, Connection conn) throws SQLException {
        String sql = "INSERT INTO Invoices (InvoiceDate, TotalAmount) VALUES (?, ?)";
        int generatedInvoiceId = -1;

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setDate(1, Date.valueOf(invoice.getInvoiceDate()));
            pstmt.setBigDecimal(2, invoice.getTotalAmount());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedInvoiceId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creating invoice failed, no ID obtained.");
                    }
                }
            }
        }
        return generatedInvoiceId;
    }
}
