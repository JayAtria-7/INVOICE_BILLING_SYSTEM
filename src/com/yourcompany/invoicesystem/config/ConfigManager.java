package com.yourcompany.invoicesystem.config;

import com.yourcompany.invoicesystem.util.Logger;

import java.io.*;
import java.util.Properties;

/**
 * Configuration Manager
 * Loads and manages application configuration from config.properties
 */
public class ConfigManager {
    
    private static final String CONFIG_FILE = "config.properties";
    private static Properties properties;
    private static boolean loaded = false;
    
    static {
        loadConfiguration();
    }
    
    /**
     * Load configuration from file
     */
    private static void loadConfiguration() {
        properties = new Properties();
        
        try {
            // Try to load from file system first
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    properties.load(fis);
                    Logger.info("Configuration loaded from: " + configFile.getAbsolutePath());
                    loaded = true;
                    return;
                }
            }
            
            // Try to load from classpath
            try (InputStream is = ConfigManager.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
                if (is != null) {
                    properties.load(is);
                    Logger.info("Configuration loaded from classpath");
                    loaded = true;
                    return;
                }
            }
            
            // If neither worked, load defaults
            loadDefaults();
            Logger.warn("Configuration file not found, using defaults");
            
        } catch (IOException e) {
            Logger.error("Error loading configuration: " + e.getMessage(), e);
            loadDefaults();
        }
    }
    
    /**
     * Load default configuration values
     */
    private static void loadDefaults() {
        properties.setProperty("app.theme", "Nimbus");
        properties.setProperty("app.language", "en");
        properties.setProperty("app.title", "Invoice Billing System");
        properties.setProperty("app.version", "2.0.0");
        properties.setProperty("backup.directory", "backups");
        properties.setProperty("backup.retention.days", "30");
        properties.setProperty("export.directory", "exports");
        properties.setProperty("stock.low.threshold", "10");
        properties.setProperty("currency.symbol", "€");
        properties.setProperty("date.format", "yyyy-MM-dd");
    }
    
    /**
     * Get string property
     * @param key Property key
     * @return Property value or null
     */
    public static String getString(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Get string property with default
     * @param key Property key
     * @param defaultValue Default value if not found
     * @return Property value or default
     */
    public static String getString(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Get integer property
     * @param key Property key
     * @param defaultValue Default value if not found or invalid
     * @return Property value or default
     */
    public static int getInt(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            Logger.warn("Invalid integer value for " + key + ": " + value);
            return defaultValue;
        }
    }
    
    /**
     * Get double property
     * @param key Property key
     * @param defaultValue Default value if not found or invalid
     * @return Property value or default
     */
    public static double getDouble(String key, double defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            Logger.warn("Invalid double value for " + key + ": " + value);
            return defaultValue;
        }
    }
    
    /**
     * Get boolean property
     * @param key Property key
     * @param defaultValue Default value if not found
     * @return Property value or default
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(value);
    }
    
    /**
     * Set property value
     * @param key Property key
     * @param value Property value
     */
    public static void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }
    
    /**
     * Save configuration to file
     * @throws IOException if save fails
     */
    public static void saveConfiguration() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Invoice Billing System Configuration");
            Logger.info("Configuration saved to: " + new File(CONFIG_FILE).getAbsolutePath());
        }
    }
    
    /**
     * Check if configuration was successfully loaded
     * @return true if loaded from file
     */
    public static boolean isLoaded() {
        return loaded;
    }
    
    /**
     * Reload configuration from file
     */
    public static void reload() {
        loadConfiguration();
    }
}
