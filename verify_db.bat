@REM Author: Jay Prakash Kumar
@REM Copyright (c) 2025
@REM Licensed under MIT License

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

