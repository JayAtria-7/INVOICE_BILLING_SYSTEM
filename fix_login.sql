-- Author: Jay Prakash Kumar
-- Copyright (c) 2025
-- Licensed under MIT License

-- QUICK FIX - Run this to create Users table and insert default users
-- This is the minimal SQL needed to fix your login issue

USE invoice_db;

-- Create Users table
CREATE TABLE IF NOT EXISTS Users (
    UserID INT PRIMARY KEY AUTO_INCREMENT,
    Username VARCHAR(50) UNIQUE NOT NULL,
    PasswordHash VARCHAR(255) NOT NULL,
    FullName VARCHAR(100) NOT NULL,
    Role ENUM('ADMIN', 'CASHIER', 'MANAGER') NOT NULL DEFAULT 'CASHIER',
    IsActive BOOLEAN DEFAULT TRUE,
    CreatedDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LastLoginDate TIMESTAMP NULL,
    INDEX idx_username (Username),
    INDEX idx_role (Role)
);

-- Insert default users with CORRECT password hashes
-- Admin user: username=admin, password=admin123
INSERT INTO Users (Username, PasswordHash, FullName, Role, IsActive) 
VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'System Administrator', 'ADMIN', TRUE)
ON DUPLICATE KEY UPDATE Username=Username;

-- Cashier user: username=cashier1, password=cashier123
-- CORRECTED HASH: b4c94003c562bb0d89535eca77f07284fe560fd48a7cc1ed99f0a56263d616ba
INSERT INTO Users (Username, PasswordHash, FullName, Role, IsActive) 
VALUES ('cashier1', 'b4c94003c562bb0d89535eca77f07284fe560fd48a7cc1ed99f0a56263d616ba', 'John Cashier', 'CASHIER', TRUE)
ON DUPLICATE KEY UPDATE Username=Username;

-- Verify the users were created
SELECT UserID, Username, FullName, Role, IsActive FROM Users;

-- Done! Now you can login with:
-- Username: admin     Password: admin123
-- Username: cashier1  Password: cashier123

