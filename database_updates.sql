-- Author: Jay Prakash Kumar
-- Copyright (c) 2025
-- Licensed under MIT License

-- User Authentication System Database Schema
-- Run this SQL script in your MySQL database (invoice_db)

-- Users table for authentication
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

-- Insert default admin user (password: admin123)
-- Password hash is SHA-256 of 'admin123'
INSERT INTO Users (Username, PasswordHash, FullName, Role, IsActive) 
VALUES ('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'System Administrator', 'ADMIN', TRUE)
ON DUPLICATE KEY UPDATE Username=Username;

-- Insert sample cashier (password: cashier123)
INSERT INTO Users (Username, PasswordHash, FullName, Role, IsActive) 
VALUES ('cashier1', 'b4c94003c562bb0d89535eca77f07284fe560fd48a7cc1ed99f0a56263d616ba', 'John Cashier', 'CASHIER', TRUE)
ON DUPLICATE KEY UPDATE Username=Username;

-- Tax Configuration table
CREATE TABLE IF NOT EXISTS TaxConfiguration (
    TaxID INT PRIMARY KEY AUTO_INCREMENT,
    TaxName VARCHAR(50) NOT NULL,
    TaxRate DECIMAL(5,2) NOT NULL,
    IsActive BOOLEAN DEFAULT TRUE,
    AppliedBy ENUM('PERCENTAGE', 'FIXED') DEFAULT 'PERCENTAGE',
    CreatedDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert default tax rates
INSERT INTO TaxConfiguration (TaxName, TaxRate, IsActive, AppliedBy)
VALUES ('VAT (Standard)', 20.00, TRUE, 'PERCENTAGE')
ON DUPLICATE KEY UPDATE TaxName=TaxName;

INSERT INTO TaxConfiguration (TaxName, TaxRate, IsActive, AppliedBy)
VALUES ('GST', 18.00, FALSE, 'PERCENTAGE')
ON DUPLICATE KEY UPDATE TaxName=TaxName;

-- Payment Methods table
CREATE TABLE IF NOT EXISTS PaymentMethods (
    PaymentMethodID INT PRIMARY KEY AUTO_INCREMENT,
    MethodName VARCHAR(50) NOT NULL,
    IsActive BOOLEAN DEFAULT TRUE
);

INSERT INTO PaymentMethods (MethodName, IsActive) VALUES
('Cash', TRUE),
('Credit Card', TRUE),
('Debit Card', TRUE),
('Mobile Payment', TRUE),
('Split Payment', TRUE)
ON DUPLICATE KEY UPDATE MethodName=MethodName;

-- Invoice Payments table (for tracking payment splits)
CREATE TABLE IF NOT EXISTS InvoicePayments (
    PaymentID INT PRIMARY KEY AUTO_INCREMENT,
    InvoiceID INT NOT NULL,
    PaymentMethodID INT NOT NULL,
    Amount DECIMAL(10,2) NOT NULL,
    PaymentDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (InvoiceID) REFERENCES Invoices(InvoiceID) ON DELETE CASCADE,
    FOREIGN KEY (PaymentMethodID) REFERENCES PaymentMethods(PaymentMethodID),
    INDEX idx_invoice (InvoiceID)
);

-- Customer Management tables
CREATE TABLE IF NOT EXISTS Customers (
    CustomerID INT PRIMARY KEY AUTO_INCREMENT,
    CustomerName VARCHAR(100) NOT NULL,
    Email VARCHAR(100) UNIQUE,
    Phone VARCHAR(20),
    Address TEXT,
    LoyaltyPoints INT DEFAULT 0,
    TotalPurchases DECIMAL(12,2) DEFAULT 0.00,
    CreatedDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    LastPurchaseDate TIMESTAMP NULL,
    INDEX idx_phone (Phone),
    INDEX idx_email (Email)
);

-- Link customers to invoices
ALTER TABLE Invoices ADD COLUMN IF NOT EXISTS CustomerID INT NULL;
ALTER TABLE Invoices ADD COLUMN IF NOT EXISTS UserID INT NULL; -- Track which user created invoice
ALTER TABLE Invoices ADD COLUMN IF NOT EXISTS TaxAmount DECIMAL(10,2) DEFAULT 0.00;
ALTER TABLE Invoices ADD COLUMN IF NOT EXISTS PaymentStatus ENUM('PAID', 'PARTIAL', 'PENDING') DEFAULT 'PAID';

-- Add foreign keys if they don't exist
-- Note: This might fail if columns already have different constraints
-- ALTER TABLE Invoices ADD FOREIGN KEY (CustomerID) REFERENCES Customers(CustomerID) ON DELETE SET NULL;
-- ALTER TABLE Invoices ADD FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE SET NULL;

-- Product barcode support
ALTER TABLE Products ADD COLUMN IF NOT EXISTS Barcode VARCHAR(50) UNIQUE NULL;
ALTER TABLE Products ADD COLUMN IF NOT EXISTS LowStockThreshold INT DEFAULT 10;
ALTER TABLE Products ADD COLUMN IF NOT EXISTS Category VARCHAR(50) NULL;

-- Returns/Refunds table
CREATE TABLE IF NOT EXISTS Returns (
    ReturnID INT PRIMARY KEY AUTO_INCREMENT,
    InvoiceID INT NOT NULL,
    ProductID INT NOT NULL,
    Quantity INT NOT NULL,
    RefundAmount DECIMAL(10,2) NOT NULL,
    Reason TEXT,
    ProcessedByUserID INT,
    ReturnDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (InvoiceID) REFERENCES Invoices(InvoiceID),
    FOREIGN KEY (ProductID) REFERENCES Products(ProductID),
    FOREIGN KEY (ProcessedByUserID) REFERENCES Users(UserID) ON DELETE SET NULL,
    INDEX idx_invoice (InvoiceID),
    INDEX idx_product (ProductID)
);

-- Audit log for important operations
CREATE TABLE IF NOT EXISTS AuditLog (
    LogID INT PRIMARY KEY AUTO_INCREMENT,
    UserID INT,
    Action VARCHAR(100) NOT NULL,
    TableName VARCHAR(50),
    RecordID INT,
    OldValue TEXT,
    NewValue TEXT,
    LogDate TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (UserID) REFERENCES Users(UserID) ON DELETE SET NULL,
    INDEX idx_user (UserID),
    INDEX idx_date (LogDate),
    INDEX idx_action (Action)
);

-- System settings table
CREATE TABLE IF NOT EXISTS SystemSettings (
    SettingKey VARCHAR(50) PRIMARY KEY,
    SettingValue TEXT NOT NULL,
    Description VARCHAR(255),
    LastModified TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert default settings
INSERT INTO SystemSettings (SettingKey, SettingValue, Description) VALUES
('TAX_ENABLED', 'false', 'Enable/disable tax calculation'),
('TAX_RATE', '20.00', 'Default tax rate percentage'),
('CURRENCY_SYMBOL', '€', 'Currency symbol to display'),
('LOW_STOCK_ALERT_ENABLED', 'true', 'Enable low stock alerts'),
('BACKUP_ENABLED', 'false', 'Enable automatic database backups'),
('LANGUAGE', 'en', 'System language code')
ON DUPLICATE KEY UPDATE SettingKey=SettingKey;

