<!--
Author: Jay Prakash Kumar
Copyright (c) 2025
Licensed under MIT License
-->

# Free Deployment Options for Invoice Billing System

## Overview
This guide covers FREE options to deploy the Invoice Billing System for public or internal use.

---

## Option 1: Oracle Cloud Free Tier ⭐ RECOMMENDED

### What You Get (Forever Free)
- 2 AMD-based Compute VMs (1 GB RAM each)
- 4 Arm-based Ampere A1 cores (24 GB RAM total)
- 200 GB block storage
- 10 GB object storage
- MySQL Database Service option

### Setup Steps
1. **Sign up**: https://www.oracle.com/cloud/free/
2. **Create VM Instance**:
   - Choose Ubuntu 20.04
   - Select ARM or AMD shape (free tier)
   - Configure networking (open port 3306 for MySQL, 8080 for app)

3. **Install Java and MySQL**:
   ```bash
   sudo apt update
   sudo apt install openjdk-11-jre mysql-server -y
   ```

4. **Upload Application**:
   ```bash
   scp -i your-key.pem InvoiceBillingSystem.jar ubuntu@your-ip:~/
   scp -i your-key.pem -r lib ubuntu@your-ip:~/
   ```

5. **Setup Database**:
   ```bash
   sudo mysql_secure_installation
   sudo mysql < database_updates.sql
   sudo mysql < fix_login.sql
   ```

6. **Run as Service**:
   Create `/etc/systemd/system/invoice-system.service`:
   ```ini
   [Unit]
   Description=Invoice Billing System
   After=network.target mysql.service

   [Service]
   Type=simple
   User=ubuntu
   WorkingDirectory=/home/ubuntu
   ExecStart=/usr/bin/java -jar InvoiceBillingSystem.jar
   Restart=on-failure

   [Install]
   WantedBy=multi-user.target
   ```

   Enable and start:
   ```bash
   sudo systemctl enable invoice-system
   sudo systemctl start invoice-system
   ```

### Pros
- Permanent free tier (not trial)
- Generous resources
- Professional infrastructure
- Good for production

### Cons
- Requires credit card for signup
- Command-line setup needed
- Public IP (need to secure MySQL)

---

## Option 2: AWS Free Tier (12 Months)

### What You Get (12 months free)
- EC2 t2.micro instance (1 vCPU, 1 GB RAM)
- 30 GB EBS storage
- RDS MySQL db.t2.micro (20 GB storage)
- 750 hours/month each

### Setup Steps
1. **Sign up**: https://aws.amazon.com/free/
2. **Launch EC2**: t2.micro, Ubuntu 20.04
3. **Launch RDS**: MySQL db.t2.micro
4. **Configure Security Groups**: Allow MySQL (3306) from EC2 only
5. **Deploy application** (similar to Oracle Cloud steps)

### Pros
- Industry standard
- RDS managed database
- Easy scaling later

### Cons
- Only 12 months free
- Requires credit card
- Can accidentally incur charges

---

## Option 3: Azure for Students (No Credit Card)

### What You Get
- $100 credit (if student)
- Free services for 12 months
- No credit card required (student verification)

### Setup Steps
1. **Sign up**: https://azure.microsoft.com/en-us/free/students/
2. **Create VM**: B1s (1 vCPU, 1 GB RAM)
3. **Create MySQL**: Azure Database for MySQL
4. **Deploy** (similar steps)

### Pros
- No credit card needed (students)
- Good learning resources
- Microsoft ecosystem

### Cons
- Student verification required
- Limited free period
- Resources more limited than Oracle

---

## Option 4: Google Cloud Free Tier

### What You Get (Always Free)
- e2-micro VM (2 vCPUs, 1 GB RAM)
- 30 GB HDD storage
- 1 GB network egress/month

### Setup Steps
1. **Sign up**: https://cloud.google.com/free
2. **Create VM**: e2-micro, Ubuntu
3. **Install MySQL** on VM (no free managed database)
4. **Deploy application**

### Pros
- Always-free tier continues after 90-day trial
- Good documentation
- Fast network

### Cons
- Requires credit card
- Shared CPU (burstable)
- No free managed database

---

## Option 5: Docker + Free Hosting

### Option 5A: Render.com (Free Tier)

**What You Get:**
- Free web service (spins down after inactivity)
- Free PostgreSQL database (90 days, then expires)

**Setup:**
1. **Dockerize Application**:
   Create `Dockerfile`:
   ```dockerfile
   FROM openjdk:11-jre-slim
   WORKDIR /app
   COPY InvoiceBillingSystem.jar .
   COPY lib/ lib/
   COPY config.properties .
   CMD ["java", "-jar", "InvoiceBillingSystem.jar"]
   ```

2. **Deploy to Render**: https://render.com
   - Connect GitHub repo
   - Select Docker deployment
   - Add MySQL addon (note: free tier expires)

**Pros:**
- No credit card needed
- Easy deployment
- HTTPS included

**Cons:**
- Spins down after 15 min inactivity
- Database expires after 90 days
- Java desktop GUI won't work (need web interface)

### Option 5B: Railway.app (Free Tier)

**What You Get:**
- $5 credit/month (free tier)
- MySQL database included

**Similar to Render but with persistent database.**

---

## Option 6: Self-Hosted on Personal Hardware

### Option 6A: Raspberry Pi Server

**What You Need:**
- Raspberry Pi 4 (4GB+ RAM) - ~$55
- MicroSD card (32GB+) - ~$10
- Internet connection

**Setup:**
1. **Install Raspberry Pi OS Lite**
2. **Install Java and MySQL**:
   ```bash
   sudo apt install openjdk-11-jre mysql-server
   ```
3. **Setup application** (same as Oracle Cloud)
4. **Configure port forwarding** on router (port 3306, 8080)
5. **Use Dynamic DNS** (DuckDNS.org is free)

**Pros:**
- One-time hardware cost
- Full control
- No monthly fees
- Learning experience

**Cons:**
- Requires hardware purchase
- Home internet upload speeds
- Power/cooling considerations
- Need to manage security yourself

### Option 6B: Old PC/Laptop as Server

**Free if you have spare hardware:**
- Install Ubuntu Server
- Follow same setup as Raspberry Pi
- More powerful than Pi

---

## Option 7: Heroku (Free Tier Removed)

❌ **NOT RECOMMENDED**: Heroku removed free tier in November 2022.

---

## Option 8: Local Network Only (Free)

### For Internal Business Use Only

**Setup:**
1. **Install on any PC**:
   - Windows: Just run `run.bat`
   - Linux: Run `run.sh`

2. **Configure MySQL** to accept network connections:
   Edit `/etc/mysql/mysql.conf.d/mysqld.cnf`:
   ```
   bind-address = 0.0.0.0
   ```

3. **Update db.properties** on client machines:
   ```properties
   db.url=jdbc:mysql://192.168.1.100:3306/invoice_db
   ```

4. **Share application**:
   - Put JAR on network share
   - All users connect to central database

**Pros:**
- Completely free
- No internet required
- Fast local network speeds
- Full control

**Cons:**
- Only accessible on local network
- Server PC must stay on
- No remote access

---

## Comparison Table

| Option | Cost | Duration | Resources | Complexity | Best For |
|--------|------|----------|-----------|------------|----------|
| **Oracle Cloud** | FREE | Forever | Excellent | Medium | Production |
| **AWS Free** | FREE | 12 months | Good | Medium | Testing |
| **Azure Students** | FREE | 12 months | Good | Medium | Students |
| **GCP Free** | FREE | Forever* | Good | Medium | Light use |
| **Render.com** | FREE | Forever* | Limited | Low | Web apps |
| **Raspberry Pi** | ~$65 | Forever | Medium | Medium | Learning |
| **Local Network** | FREE | Forever | Varies | Low | Internal only |

*With limitations

---

## Recommended Deployment Strategy

### For Production Business Use:
1. **Oracle Cloud Free Tier** - Best overall free option
2. **Self-hosted** on dedicated hardware - For local business

### For Testing/Development:
1. **Local Network** - Easiest to set up
2. **AWS/Azure Free Tier** - Cloud experience

### For Students/Learning:
1. **Azure for Students** - No credit card
2. **Raspberry Pi** - Hands-on experience

---

## Security Considerations for Cloud Deployment

### Essential Security Steps:

1. **Firewall Configuration**:
   ```bash
   sudo ufw allow 22/tcp    # SSH
   sudo ufw allow 3306/tcp  # MySQL (restrict to app IP only!)
   sudo ufw enable
   ```

2. **MySQL Security**:
   ```bash
   sudo mysql_secure_installation
   # Change root password
   # Remove test database
   # Disable remote root login
   ```

3. **SSL/TLS**:
   - Use Let's Encrypt for free SSL certificates
   - Configure MySQL to use SSL

4. **Application Security**:
   - Change all default passwords
   - Use strong database passwords
   - Keep Java and MySQL updated

5. **Monitoring**:
   ```bash
   # Install fail2ban to prevent brute force
   sudo apt install fail2ban
   ```

---

## Building Executable JAR

Before deploying, build the JAR file:

```bash
# 1. Compile all source files
javac -encoding UTF-8 -cp "lib/*" -d bin src/com/yourcompany/invoicesystem/**/*.java

# 2. Copy resources
copy config.properties bin/
copy db.properties bin/

# 3. Create JAR with manifest
jar cfm InvoiceBillingSystem.jar MANIFEST.MF -C bin .

# 4. Test locally
java -jar InvoiceBillingSystem.jar
```

---

## Next Steps

1. **Choose deployment option** based on your needs
2. **Setup infrastructure** (cloud VM or local server)
3. **Deploy database** and import schema
4. **Deploy application** JAR and dependencies
5. **Configure** db.properties for your environment
6. **Test** application connectivity
7. **Secure** with firewall and strong passwords
8. **Monitor** and maintain

---

## Support Resources

- **Oracle Cloud Docs**: https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier.htm
- **AWS Free Tier**: https://aws.amazon.com/free/
- **MySQL Documentation**: https://dev.mysql.com/doc/
- **Ubuntu Server Guide**: https://ubuntu.com/server/docs

