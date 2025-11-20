/**
 * Author: Jay Prakash Kumar
 * Copyright (c) 2025
 * Licensed under MIT License
 */

package com.yourcompany.invoicesystem.dao;

import com.yourcompany.invoicesystem.model.User;
import com.yourcompany.invoicesystem.util.DBUtil;
import com.yourcompany.invoicesystem.util.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    
    /**
     * Authenticate user by username and password
     */
    public User authenticate(String username, String passwordHash) {
        String sql = "SELECT UserID, Username, PasswordHash, FullName, Role, IsActive, CreatedDate, LastLoginDate " +
                    "FROM Users WHERE Username = ? AND PasswordHash = ? AND IsActive = TRUE";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    User user = extractUserFromResultSet(rs);
                    updateLastLogin(user.getUserID());
                    return user;
                }
            }
        } catch (SQLException e) {
            Logger.error("Error authenticating user: " + username, e);
        }
        return null;
    }
    
    /**
     * Get user by ID
     */
    public User getUserById(int userID) {
        String sql = "SELECT UserID, Username, PasswordHash, FullName, Role, IsActive, CreatedDate, LastLoginDate " +
                    "FROM Users WHERE UserID = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            Logger.error("Error fetching user by ID: " + userID, e);
        }
        return null;
    }
    
    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT UserID, Username, PasswordHash, FullName, Role, IsActive, CreatedDate, LastLoginDate " +
                    "FROM Users ORDER BY FullName";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            Logger.error("Error fetching all users", e);
        }
        return users;
    }
    
    /**
     * Create new user
     */
    public boolean createUser(User user) {
        String sql = "INSERT INTO Users (Username, PasswordHash, FullName, Role, IsActive) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getFullName());
            pstmt.setString(4, user.getRole().name());
            pstmt.setBoolean(5, user.isActive());
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setUserID(generatedKeys.getInt(1));
                    }
                }
                Logger.info("User created: " + user.getUsername());
                return true;
            }
        } catch (SQLException e) {
            Logger.error("Error creating user: " + user.getUsername(), e);
        }
        return false;
    }
    
    /**
     * Update user
     */
    public boolean updateUser(User user) {
        String sql = "UPDATE Users SET FullName = ?, Role = ?, IsActive = ? WHERE UserID = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getFullName());
            pstmt.setString(2, user.getRole().name());
            pstmt.setBoolean(3, user.isActive());
            pstmt.setInt(4, user.getUserID());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                Logger.info("User updated: " + user.getUsername());
                return true;
            }
        } catch (SQLException e) {
            Logger.error("Error updating user: " + user.getUserID(), e);
        }
        return false;
    }
    
    /**
     * Change user password
     */
    public boolean changePassword(int userID, String newPasswordHash) {
        String sql = "UPDATE Users SET PasswordHash = ? WHERE UserID = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newPasswordHash);
            pstmt.setInt(2, userID);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                Logger.info("Password changed for user ID: " + userID);
                return true;
            }
        } catch (SQLException e) {
            Logger.error("Error changing password for user ID: " + userID, e);
        }
        return false;
    }
    
    /**
     * Update last login timestamp
     */
    private void updateLastLogin(int userID) {
        String sql = "UPDATE Users SET LastLoginDate = CURRENT_TIMESTAMP WHERE UserID = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Logger.warn("Error updating last login for user ID: " + userID, e);
        }
    }
    
    /**
     * Extract User object from ResultSet
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserID(rs.getInt("UserID"));
        user.setUsername(rs.getString("Username"));
        user.setPasswordHash(rs.getString("PasswordHash"));
        user.setFullName(rs.getString("FullName"));
        
        // Extract role - handle formats like "ADMIN-JayPrakash" by taking only the part before hyphen
        String roleString = rs.getString("Role");
        if (roleString.contains("-")) {
            roleString = roleString.substring(0, roleString.indexOf("-"));
        }
        user.setRole(User.Role.valueOf(roleString));
        user.setActive(rs.getBoolean("IsActive"));
        
        Timestamp created = rs.getTimestamp("CreatedDate");
        if (created != null) {
            user.setCreatedDate(created.toLocalDateTime());
        }
        
        Timestamp lastLogin = rs.getTimestamp("LastLoginDate");
        if (lastLogin != null) {
            user.setLastLoginDate(lastLogin.toLocalDateTime());
        }
        
        return user;
    }
}

