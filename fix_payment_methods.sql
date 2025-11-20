-- Fix Missing PaymentMethods table and ensure all tables exist
USE invoice_db;

-- Create PaymentMethods table if it doesn't exist
CREATE TABLE IF NOT EXISTS PaymentMethods (
    PaymentMethodID INT PRIMARY KEY AUTO_INCREMENT,
    MethodName VARCHAR(50) NOT NULL,
    IsActive BOOLEAN DEFAULT TRUE
);

-- Insert payment methods
INSERT INTO PaymentMethods (MethodName, IsActive) VALUES
('Cash', TRUE),
('Credit Card', TRUE),
('Debit Card', TRUE),
('Mobile Payment', TRUE),
('Split Payment', TRUE)
ON DUPLICATE KEY UPDATE MethodName=VALUES(MethodName);

-- Create InvoicePayments table if it doesn't exist
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

-- Create Returns table if it doesn't exist
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

-- Add columns to Invoices if they don't exist (one at a time)
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE table_schema=DATABASE() AND table_name='Invoices' AND column_name='DiscountPercentage') = 0,
    'ALTER TABLE Invoices ADD COLUMN DiscountPercentage DECIMAL(5,2) DEFAULT 0.00',
    'SELECT "Column DiscountPercentage already exists" AS Info'));
PREPARE stmt FROM @sql;
EXECUTE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE table_schema=DATABASE() AND table_name='Invoices' AND column_name='TaxAmount') = 0,
    'ALTER TABLE Invoices ADD COLUMN TaxAmount DECIMAL(10,2) DEFAULT 0.00',
    'SELECT "Column TaxAmount already exists" AS Info'));
PREPARE stmt FROM @sql;
EXECUTE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE table_schema=DATABASE() AND table_name='Invoices' AND column_name='PaymentStatus') = 0,
    'ALTER TABLE Invoices ADD COLUMN PaymentStatus VARCHAR(20) DEFAULT ''PAID''',
    'SELECT "Column PaymentStatus already exists" AS Info'));
PREPARE stmt FROM @sql;
EXECUTE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE table_schema=DATABASE() AND table_name='Invoices' AND column_name='CustomerID') = 0,
    'ALTER TABLE Invoices ADD COLUMN CustomerID INT NULL',
    'SELECT "Column CustomerID already exists" AS Info'));
PREPARE stmt FROM @sql;
EXECUTE stmt;

SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
     WHERE table_schema=DATABASE() AND table_name='Invoices' AND column_name='UserID') = 0,
    'ALTER TABLE Invoices ADD COLUMN UserID INT NULL',
    'SELECT "Column UserID already exists" AS Info'));
PREPARE stmt FROM @sql;
EXECUTE stmt;

-- Verify tables exist
SELECT 'Tables created/verified successfully' AS Status;
SELECT TABLE_NAME FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'invoice_db' 
ORDER BY TABLE_NAME;

-- Show payment methods
SELECT * FROM PaymentMethods;
