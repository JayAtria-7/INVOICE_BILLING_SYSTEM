# Author: Jay Prakash Kumar
# Copyright (c) 2025
# Licensed under MIT License

#!/bin/bash
# Invoice Billing System Startup Script for Linux/Mac
#
# This script starts the Invoice Billing System application

echo "Starting Invoice Billing System..."
echo

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 11 or higher"
    exit 1
fi

# Check Java version
java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ "$java_version" -lt 11 ]; then
    echo "Error: Java 11 or higher is required"
    echo "Current version: $(java -version 2>&1 | head -n 1)"
    exit 1
fi

# Check if MySQL is running
echo "Checking MySQL connection..."
if ! netstat -an | grep -q ":3306"; then
    echo "Warning: MySQL might not be running on port 3306"
    echo "Please ensure MySQL is started"
    echo
fi

# Get the script directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Start the application
java -cp "InvoiceBillingSystem.jar:lib/*" com.yourcompany.invoicesystem.gui.InvoiceAppGUI

exit_code=$?
if [ $exit_code -ne 0 ]; then
    echo
    echo "Application exited with error code $exit_code"
    read -p "Press Enter to continue..."
fi

