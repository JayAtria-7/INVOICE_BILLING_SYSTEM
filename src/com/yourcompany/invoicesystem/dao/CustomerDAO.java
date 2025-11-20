package com.yourcompany.invoicesystem.dao;

import com.yourcompany.invoicesystem.model.Customer;
import com.yourcompany.invoicesystem.util.DBUtil;
import com.yourcompany.invoicesystem.util.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    
    /**
     * Get all customers
     */
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM Customers ORDER BY CustomerName";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                customers.add(extractCustomerFromResultSet(rs));
            }
        } catch (SQLException e) {
            Logger.error("Error fetching all customers", e);
        }
        return customers;
    }
    
    /**
     * Get customer by ID
     */
    public Customer getCustomerById(int customerID) {
        String sql = "SELECT * FROM Customers WHERE CustomerID = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, customerID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractCustomerFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            Logger.error("Error fetching customer by ID: " + customerID, e);
        }
        return null;
    }
    
    /**
     * Search customers by name or phone
     */
    public List<Customer> searchCustomers(String searchTerm) {
        List<Customer> customers = new ArrayList<>();
        String sql = "SELECT * FROM Customers WHERE CustomerName LIKE ? OR Phone LIKE ? ORDER BY CustomerName";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String pattern = "%" + searchTerm + "%";
            pstmt.setString(1, pattern);
            pstmt.setString(2, pattern);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(extractCustomerFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            Logger.error("Error searching customers: " + searchTerm, e);
        }
        return customers;
    }
    
    /**
     * Create new customer
     */
    public boolean createCustomer(Customer customer) {
        String sql = "INSERT INTO Customers (CustomerName, Email, Phone, Address) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, customer.getCustomerName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getPhone());
            pstmt.setString(4, customer.getAddress());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        customer.setCustomerID(generatedKeys.getInt(1));
                    }
                }
                Logger.info("Customer created: " + customer.getCustomerName());
                return true;
            }
        } catch (SQLException e) {
            Logger.error("Error creating customer: " + customer.getCustomerName(), e);
        }
        return false;
    }
    
    /**
     * Update customer
     */
    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE Customers SET CustomerName = ?, Email = ?, Phone = ?, Address = ? WHERE CustomerID = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, customer.getCustomerName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getPhone());
            pstmt.setString(4, customer.getAddress());
            pstmt.setInt(5, customer.getCustomerID());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                Logger.info("Customer updated: " + customer.getCustomerID());
                return true;
            }
        } catch (SQLException e) {
            Logger.error("Error updating customer: " + customer.getCustomerID(), e);
        }
        return false;
    }
    
    /**
     * Update loyalty points
     */
    public boolean updateLoyaltyPoints(int customerID, int points) {
        String sql = "UPDATE Customers SET LoyaltyPoints = LoyaltyPoints + ? WHERE CustomerID = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, points);
            pstmt.setInt(2, customerID);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.error("Error updating loyalty points for customer: " + customerID, e);
        }
        return false;
    }
    
    /**
     * Extract Customer object from ResultSet
     */
    private Customer extractCustomerFromResultSet(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerID(rs.getInt("CustomerID"));
        customer.setCustomerName(rs.getString("CustomerName"));
        customer.setEmail(rs.getString("Email"));
        customer.setPhone(rs.getString("Phone"));
        customer.setAddress(rs.getString("Address"));
        customer.setLoyaltyPoints(rs.getInt("LoyaltyPoints"));
        customer.setTotalPurchases(rs.getBigDecimal("TotalPurchases"));
        
        Timestamp created = rs.getTimestamp("CreatedDate");
        if (created != null) {
            customer.setCreatedDate(created.toLocalDateTime());
        }
        
        Timestamp lastPurchase = rs.getTimestamp("LastPurchaseDate");
        if (lastPurchase != null) {
            customer.setLastPurchaseDate(lastPurchase.toLocalDateTime());
        }
        
        return customer;
    }
}
