@echo off
echo ========================================
echo Invoice Billing System - Database Verification
echo ========================================
echo.

mysql -u root -php09876 invoice_db < verify_database.sql

echo.
echo ========================================
echo Verification Complete
echo ========================================
pause
