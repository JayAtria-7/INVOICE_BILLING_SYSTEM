<!--
Author: Jay Prakash Kumar
Copyright (c) 2025
Licensed under MIT License
-->

# Invoice Billing System - Deployment Guide

## Version 2.0.0

### Overview
This deployment package contains everything needed to run the Invoice Billing System on Windows, Linux, or macOS.

---

## System Requirements

### Minimum Requirements
- **Operating System**: Windows 10+, Linux (Ubuntu 18.04+), macOS 10.14+
- **Java Runtime**: JRE 11 or higher
- **Database**: MySQL 5.7+ or MariaDB 10.2+
- **RAM**: 512 MB minimum, 1 GB recommended
- **Disk Space**: 100 MB for application + database storage
- **Screen Resolution**: 1024x768 minimum

### Recommended Requirements
- Java 17 or higher
- MySQL 8.0+
- 2 GB RAM
- 1 GB free disk space

---

## Package Contents

```
InvoiceBillingSystem/
├── InvoiceBillingSystem.jar          # Main application JAR
├── lib/                               # External libraries
│   └── mysql-connector-java-8.0.33.jar
├── config.properties                  # Application configuration
├── db.properties                      # Database configuration
├── database_updates.sql               # Database schema
├── fix_login.sql                      # User setup script
├── run.bat                            # Windows startup script
├── run.sh                             # Linux/Mac startup script
├── README.md                          # This file
└── backups/                           # Backup directory (created automatically)
```

---

## Installation Steps

### 1. Install Java
**Windows:**
- Download JDK 11+ from https://adoptium.net/
- Run the installer and follow the wizard
- Verify: Open CMD and run `java -version`

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install openjdk-11-jre
java -version
```

**macOS:**
```bash
brew install openjdk@11
java -version
```

### 2. Install MySQL
**Windows:**
- Download MySQL Community Server from https://dev.mysql.com/downloads/mysql/
- Run installer and set root password
- Start MySQL service from Services panel

**Linux:**
```bash
sudo apt update
sudo apt install mysql-server
sudo systemctl start mysql
sudo mysql_secure_installation
```

**macOS:**
```bash
brew install mysql
brew services start mysql
mysql_secure_installation
```

### 3. Setup Database
1. Login to MySQL as root:
   ```bash
   mysql -u root -p
   ```

2. Create database and user:
   ```sql
   CREATE DATABASE invoice_db;
   CREATE USER 'invoice_user'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON invoice_db.* TO 'invoice_user'@'localhost';
   FLUSH PRIVILEGES;
   EXIT;
   ```

3. Import database schema:
   ```bash
   mysql -u root -p invoice_db < database_updates.sql
   mysql -u root -p invoice_db < fix_login.sql
   ```

### 4. Configure Application
Edit `db.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/invoice_db
db.username=invoice_user
db.password=your_password
```

Edit `config.properties` (optional) to customize settings.

### 5. Run Application
**Windows:**
- Double-click `run.bat`
- Or from CMD: `run.bat`

**Linux/Mac:**
```bash
chmod +x run.sh
./run.sh
```

### 6. Login
**Default Credentials:**
- **Admin**: `admin` / `admin123`
- **Cashier**: `cashier1` / `cashier123`

⚠️ **Change default passwords immediately after first login!**

---

## Building from Source

### Prerequisites
- JDK 11+ installed
- MySQL JDBC driver in `lib/` folder
- Source code in `src/` folder

### Compile
```bash
# Windows
javac -encoding UTF-8 -cp "lib/*" -d bin src/com/yourcompany/invoicesystem/**/*.java

# Linux/Mac
javac -encoding UTF-8 -cp "lib/*" -d bin src/com/yourcompany/invoicesystem/**/*.java
```

### Create JAR
```bash
# Copy resources
copy config.properties bin/
copy db.properties bin/

# Create JAR
jar cfm InvoiceBillingSystem.jar MANIFEST.MF -C bin .
```

---

## Configuration Options

### config.properties
```properties
# UI Theme: Nimbus, Metal, System
app.theme=Nimbus

# Language: en, es, fr
app.language=en

# Backup retention (days)
backup.retention.days=30

# Low stock threshold
stock.low.threshold=10

# Currency symbol
currency.symbol=€
```

### db.properties
```properties
db.url=jdbc:mysql://localhost:3306/invoice_db
db.username=your_username
db.password=your_password
db.driver=com.mysql.cj.jdbc.Driver
```

---

## Troubleshooting

### Application won't start
1. Check Java version: `java -version` (must be 11+)
2. Check MySQL is running: `netstat -an | find "3306"` (Windows) or `netstat -an | grep 3306` (Linux)
3. Verify database credentials in `db.properties`
4. Check logs in application console

### Database connection errors
1. Verify MySQL is running
2. Check database exists: `SHOW DATABASES;`
3. Verify user permissions: `SHOW GRANTS FOR 'invoice_user'@'localhost';`
4. Test connection manually: `mysql -u invoice_user -p invoice_db`

### Login fails
1. Ensure `fix_login.sql` was executed
2. Check Users table: `SELECT Username, Role FROM Users;`
3. Verify password hashes are correct

### Low memory errors
- Increase JVM heap: Edit run script and add `-Xmx1024m` to java command

---

## Security Recommendations

1. **Change Default Passwords**
   - Change all default user passwords immediately
   - Use strong passwords (min 8 characters, mixed case, numbers, symbols)

2. **Database Security**
   - Don't use root account for application
   - Use strong database password
   - Restrict MySQL to localhost if not accessing remotely

3. **File Permissions**
   - Restrict access to `db.properties` (contains password)
   - Keep backup folder secure

4. **Network Security**
   - Use firewall to restrict MySQL port 3306
   - Consider VPN for remote access

---

## Backup and Restore

### Manual Backup
**From Application:**
1. Go to Tools → Backup & Restore
2. Click "Create Backup"
3. Backups saved to `backups/` folder

**From Command Line:**
```bash
mysqldump -u root -p invoice_db > backup_$(date +%Y%m%d).sql
```

### Restore
**From Application:**
1. Go to Tools → Backup & Restore
2. Select backup file
3. Click "Restore"

**From Command Line:**
```bash
mysql -u root -p invoice_db < backup_20241120.sql
```

---

## Support and Updates

### Getting Help
- Check logs in application console
- Review database schema in `database_updates.sql`
- Check configuration files

### Updating
1. Stop the application
2. Backup database
3. Replace JAR file with new version
4. Check for database schema updates
5. Start application

---

## License
Proprietary software - All rights reserved
Version 2.0.0 - November 2025

