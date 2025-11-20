-- Author: Jay Prakash Kumar
-- Copyright (c) 2025
-- Licensed under MIT License

-- Comprehensive Database Fix and Verification Script
-- This script ensures all tables and columns exist for the Invoice Billing System

USE invoice_db;

-- ========================================
-- 1. VERIFY AND FIX CORE TABLES
-- ========================================

-- Products table verification
SELECT 'Checking Products table...' AS Status;
SELECT COUNT(*) AS ProductCount FROM Products;

-- Invoices table verification  
SELECT 'Checking Invoices table...' AS Status;
SELECT COUNT(*) AS InvoiceCount FROM Invoices;

-- InvoiceItems table verification
SELECT 'Checking InvoiceItems table...' AS Status;
SELECT COUNT(*) AS InvoiceItemCount FROM InvoiceItems;

-- Users table verification
SELECT 'Checking Users table...' AS Status;
SELECT COUNT(*) AS UserCount FROM Users;

-- ========================================
-- 2. VERIFY PAYMENT METHODS
-- ========================================

SELECT 'Checking PaymentMethods table...' AS Status;
SELECT * FROM PaymentMethods ORDER BY PaymentMethodID;

-- ========================================
-- 3. CHECK FOR DUPLICATE PAYMENT METHODS
-- ========================================

SELECT 'Checking for duplicate payment methods...' AS Status;
SELECT MethodName, COUNT(*) as Count 
FROM PaymentMethods 
GROUP BY MethodName 
HAVING COUNT(*) > 1;

-- Remove duplicates if any
DELETE pm1 FROM PaymentMethods pm1
INNER JOIN PaymentMethods pm2 
WHERE pm1.PaymentMethodID > pm2.PaymentMethodID 
AND pm1.MethodName = pm2.MethodName;

-- ========================================
-- 4. VERIFY INVOICE COLUMNS
-- ========================================

SELECT 'Checking Invoice table columns...' AS Status;
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'invoice_db' AND TABLE_NAME = 'Invoices'
ORDER BY ORDINAL_POSITION;

-- ========================================
-- 5. VERIFY PRODUCT COLUMNS
-- ========================================

SELECT 'Checking Product table columns...' AS Status;
SELECT COLUMN_NAME, DATA_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'invoice_db' AND TABLE_NAME = 'Products'
ORDER BY ORDINAL_POSITION;

-- ========================================
-- 6. TEST DATA VERIFICATION
-- ========================================

-- Check if there are products
SELECT 'Sample Products...' AS Status;
SELECT ProductID, Name, Price, Stock FROM Products LIMIT 5;

-- Check recent invoices
SELECT 'Recent Invoices...' AS Status;
SELECT InvoiceID, InvoiceDate, TotalAmount, PaymentStatus 
FROM Invoices 
ORDER BY InvoiceDate DESC 
LIMIT 5;

-- ========================================
-- 7. VERIFY RETURNS TABLE
-- ========================================

SELECT 'Checking Returns table...' AS Status;
SHOW TABLES LIKE 'Returns';

-- ========================================
-- 8. VERIFY INVOICE PAYMENTS TABLE
-- ========================================

SELECT 'Checking InvoicePayments table...' AS Status;
SHOW TABLES LIKE 'InvoicePayments';

-- ========================================
-- 9. SUMMARY
-- ========================================

SELECT 'Database Verification Complete!' AS Status;
SELECT 
    (SELECT COUNT(*) FROM Users) AS Users,
    (SELECT COUNT(*) FROM Products) AS Products,
    (SELECT COUNT(*) FROM Invoices) AS Invoices,
    (SELECT COUNT(*) FROM InvoiceItems) AS InvoiceItems,
    (SELECT COUNT(*) FROM PaymentMethods) AS PaymentMethods,
    (SELECT COUNT(*) FROM Returns) AS Returns;

