@REM Author: Jay Prakash Kumar
@REM Copyright (c) 2025
@REM Licensed under MIT License

@echo off
REM Fix Payment Methods Table

echo Creating PaymentMethods table...
mysql -u root -php09876 invoice_db < fix_payment_methods.sql

if %errorlevel% equ 0 (
    echo Success! PaymentMethods table created.
) else (
    echo Error creating table. Check MySQL connection.
)

pause

