@REM Author: Jay Prakash Kumar
@REM Copyright (c) 2025
@REM Licensed under MIT License

@echo off
REM Invoice Billing System Startup Script for Windows
REM
REM This script starts the Invoice Billing System application

echo Starting Invoice Billing System...
echo.

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 11 or higher
    pause
    exit /b 1
)

REM Check if MySQL is running
echo Checking MySQL connection...
netstat -an | find "3306" >nul
if %errorlevel% neq 0 (
    echo Warning: MySQL might not be running on port 3306
    echo Please ensure MySQL is started
    echo.
)

REM Start the application
cd /d "%~dp0"
java -cp "InvoiceBillingSystem.jar;lib/*" com.yourcompany.invoicesystem.gui.InvoiceAppGUI

if %errorlevel% neq 0 (
    echo.
    echo Application exited with error code %errorlevel%
    pause
)

