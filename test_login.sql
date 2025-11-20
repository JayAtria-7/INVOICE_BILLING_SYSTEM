-- Author: Jay Prakash Kumar
-- Copyright (c) 2025
-- Licensed under MIT License

-- Test Login - Check if users exist and verify password hashes
-- Run this in MySQL to verify your user accounts

USE invoice_db;

-- Check if Users table exists
SHOW TABLES LIKE 'Users';

-- View all users
SELECT UserID, Username, FullName, Role, IsActive, CreatedDate 
FROM Users;

-- Check password hashes
SELECT Username, PasswordHash, LENGTH(PasswordHash) as HashLength
FROM Users;

-- Expected password hashes (SHA-256):
-- admin123 = 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- cashier123 = e19d5cd5af0378da05f63f891c7467af01f8d2c9d32e2de94e89c4e18f1c5ce1

-- If users don't exist, run these inserts:
/*
INSERT INTO Users (Username, PasswordHash, FullName, Role, IsActive) 
VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'System Administrator', 'ADMIN', TRUE);

INSERT INTO Users (Username, PasswordHash, FullName, Role, IsActive) 
VALUES ('cashier1', 'e19d5cd5af0378da05f63f891c7467af01f8d2c9d32e2de94e89c4e18f1c5ce1', 'John Cashier', 'CASHIER', TRUE);
*/

