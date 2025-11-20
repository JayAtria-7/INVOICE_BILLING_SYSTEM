-- Fix invalid role values in Users table
-- The Role column should only contain: ADMIN, MANAGER, or CASHIER

USE invoice_db;

-- Check current roles
SELECT UserID, Username, Role FROM Users;

-- Fix any roles that have extra text after the role name
UPDATE Users 
SET Role = 'ADMIN' 
WHERE Role LIKE 'ADMIN%' AND Role != 'ADMIN';

UPDATE Users 
SET Role = 'MANAGER' 
WHERE Role LIKE 'MANAGER%' AND Role != 'MANAGER';

UPDATE Users 
SET Role = 'CASHIER' 
WHERE Role LIKE 'CASHIER%' AND Role != 'CASHIER';

-- Verify the fix
SELECT UserID, Username, Role FROM Users;
