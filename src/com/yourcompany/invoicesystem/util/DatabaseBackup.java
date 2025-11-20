/**
 * Author: Jay Prakash Kumar
 * Copyright (c) 2025
 * Licensed under MIT License
 */

package com.yourcompany.invoicesystem.util;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.zip.*;

/**
 * Database Backup and Restore Utility
 * Provides automated database backup and restore functionality
 */
public class DatabaseBackup {
    private static final String BACKUP_DIR = "backups";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");
    
    /**
     * Create a full database backup
     * @return Path to the backup file
     */
    public static String createBackup() throws Exception {
        return createBackup(null);
    }
    
    /**
     * Create a database backup with custom filename
     * @param customName Custom backup filename (without extension)
     * @return Path to the backup file
     */
    public static String createBackup(String customName) throws Exception {
        // Create backup directory if not exists
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        
        // Generate filename
        String timestamp = DATE_FORMAT.format(new Date());
        String filename = customName != null ? customName + "_" + timestamp : "backup_" + timestamp;
        String backupPath = BACKUP_DIR + File.separator + filename + ".sql";
        
        Logger.info("Starting database backup: " + backupPath);
        
        try (Connection conn = DBUtil.getConnection();
             FileWriter writer = new FileWriter(backupPath)) {
            
            writer.write("-- Database Backup\n");
            writer.write("-- Created: " + new Date() + "\n");
            writer.write("-- Database: invoice_db\n\n");
            writer.write("SET FOREIGN_KEY_CHECKS=0;\n\n");
            
            // Get all tables
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables("invoice_db", null, "%", new String[]{"TABLE"});
            
            while (tables.next()) {
                String tableName = tables.getString("TABLE_NAME");
                backupTable(conn, writer, tableName);
            }
            
            writer.write("\nSET FOREIGN_KEY_CHECKS=1;\n");
            
            Logger.info("Database backup completed: " + backupPath);
        }
        
        // Compress backup
        String zipPath = compressBackup(backupPath);
        
        // Delete uncompressed file
        new File(backupPath).delete();
        
        return zipPath;
    }
    
    private static void backupTable(Connection conn, FileWriter writer, String tableName) throws Exception {
        Logger.info("Backing up table: " + tableName);
        
        // Write table structure
        writer.write("-- Table: " + tableName + "\n");
        writer.write("DROP TABLE IF EXISTS `" + tableName + "`;\n");
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE `" + tableName + "`");
        if (rs.next()) {
            writer.write(rs.getString(2) + ";\n\n");
        }
        
        // Write table data
        rs = stmt.executeQuery("SELECT * FROM `" + tableName + "`");
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        
        int rowCount = 0;
        while (rs.next()) {
            if (rowCount == 0) {
                writer.write("INSERT INTO `" + tableName + "` VALUES\n");
            } else {
                writer.write(",\n");
            }
            
            writer.write("(");
            for (int i = 1; i <= columnCount; i++) {
                Object value = rs.getObject(i);
                if (value == null) {
                    writer.write("NULL");
                } else if (value instanceof Number) {
                    writer.write(value.toString());
                } else if (value instanceof Timestamp || value instanceof Date) {
                    writer.write("'" + value.toString() + "'");
                } else {
                    String strValue = value.toString().replace("'", "''");
                    writer.write("'" + strValue + "'");
                }
                
                if (i < columnCount) {
                    writer.write(", ");
                }
            }
            writer.write(")");
            rowCount++;
        }
        
        if (rowCount > 0) {
            writer.write(";\n\n");
        }
        
        Logger.info("Backed up " + rowCount + " rows from " + tableName);
    }
    
    private static String compressBackup(String sqlFilePath) throws Exception {
        String zipPath = sqlFilePath + ".zip";
        
        try (FileInputStream fis = new FileInputStream(sqlFilePath);
             FileOutputStream fos = new FileOutputStream(zipPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            ZipEntry entry = new ZipEntry(new File(sqlFilePath).getName());
            zos.putNextEntry(entry);
            
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
            
            zos.closeEntry();
        }
        
        Logger.info("Backup compressed: " + zipPath);
        return zipPath;
    }
    
    /**
     * Restore database from backup file
     * @param backupFilePath Path to backup file (.sql or .sql.zip)
     */
    public static void restoreBackup(String backupFilePath) throws Exception {
        Logger.info("Starting database restore from: " + backupFilePath);
        
        String sqlFilePath = backupFilePath;
        boolean isZipped = backupFilePath.endsWith(".zip");
        
        // Decompress if needed
        if (isZipped) {
            sqlFilePath = decompressBackup(backupFilePath);
        }
        
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             BufferedReader reader = new BufferedReader(new FileReader(sqlFilePath))) {
            
            conn.setAutoCommit(false);
            
            StringBuilder sql = new StringBuilder();
            String line;
            int statementCount = 0;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                // Skip comments and empty lines
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                
                sql.append(line).append(" ");
                
                // Execute when we hit a semicolon
                if (line.endsWith(";")) {
                    try {
                        stmt.execute(sql.toString());
                        statementCount++;
                        
                        if (statementCount % 100 == 0) {
                            Logger.info("Executed " + statementCount + " statements");
                        }
                    } catch (SQLException e) {
                        Logger.warn("Error executing SQL: " + sql.toString() + " - " + e.getMessage());
                    }
                    sql.setLength(0);
                }
            }
            
            conn.commit();
            Logger.info("Database restore completed: " + statementCount + " statements executed");
            
        } catch (Exception e) {
            Logger.error("Error during restore: " + e.getMessage(), e);
            throw e;
        } finally {
            // Clean up decompressed file if it was zipped
            if (isZipped && !sqlFilePath.equals(backupFilePath)) {
                new File(sqlFilePath).delete();
            }
        }
    }
    
    private static String decompressBackup(String zipFilePath) throws Exception {
        String outputPath = zipFilePath.replace(".zip", "");
        
        try (FileInputStream fis = new FileInputStream(zipFilePath);
             ZipInputStream zis = new ZipInputStream(fis)) {
            
            ZipEntry entry = zis.getNextEntry();
            if (entry != null) {
                try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                }
            }
            zis.closeEntry();
        }
        
        Logger.info("Backup decompressed: " + outputPath);
        return outputPath;
    }
    
    /**
     * List all available backups
     * @return Array of backup filenames
     */
    public static String[] listBackups() {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists() || !backupDir.isDirectory()) {
            return new String[0];
        }
        
        File[] files = backupDir.listFiles((dir, name) -> 
            name.endsWith(".sql") || name.endsWith(".sql.zip"));
        
        if (files == null || files.length == 0) {
            return new String[0];
        }
        
        String[] backupNames = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            backupNames[i] = files[i].getName();
        }
        
        return backupNames;
    }
    
    /**
     * Delete old backups, keeping only the most recent N backups
     * @param keepCount Number of backups to keep
     */
    public static void cleanOldBackups(int keepCount) throws Exception {
        File backupDir = new File(BACKUP_DIR);
        if (!backupDir.exists()) {
            return;
        }
        
        File[] files = backupDir.listFiles((dir, name) -> 
            name.endsWith(".sql.zip") || name.endsWith(".sql"));
        
        if (files == null || files.length <= keepCount) {
            return;
        }
        
        // Sort by last modified date
        java.util.Arrays.sort(files, (f1, f2) -> 
            Long.compare(f2.lastModified(), f1.lastModified()));
        
        // Delete old backups
        for (int i = keepCount; i < files.length; i++) {
            if (files[i].delete()) {
                Logger.info("Deleted old backup: " + files[i].getName());
            }
        }
    }
    
    /**
     * Create automated backup with rotation
     * Keeps only the most recent 10 backups
     */
    public static String createAutomatedBackup() throws Exception {
        String backupPath = createBackup("auto");
        cleanOldBackups(10);
        return backupPath;
    }
}

