package com.yourcompany.invoicesystem.util; // Ensure this matches your package name

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBUtil {

    private static final Properties dbProperties = new Properties();
    private static String DB_URL;
    private static String DB_USER;
    private static String DB_PASSWORD;
    private static String DB_DRIVER;

    // Load database configuration from properties file
    static {
        try {
            // Try to load from classpath first
            InputStream input = DBUtil.class.getClassLoader().getResourceAsStream("db.properties");
            if (input == null) {
                // Fallback: try loading from src directory
                input = DBUtil.class.getResourceAsStream("/db.properties");
            }
            if (input == null) {
                throw new RuntimeException("Unable to find db.properties file in classpath");
            }
            
            dbProperties.load(input);
            input.close();
            
            // Load properties
            DB_URL = dbProperties.getProperty("db.url");
            DB_USER = dbProperties.getProperty("db.username");
            DB_PASSWORD = dbProperties.getProperty("db.password");
            DB_DRIVER = dbProperties.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
            
            // Validate required properties
            if (DB_URL == null || DB_USER == null || DB_PASSWORD == null) {
                throw new RuntimeException("Missing required database configuration in db.properties");
            }
            
            // Load the JDBC driver
            Class.forName(DB_DRIVER);
            
            Logger.info("Database configuration loaded successfully");
            
        } catch (IOException e) {
            Logger.error("Error loading database properties file", e);
            throw new RuntimeException("Failed to load database configuration.", e);
        } catch (ClassNotFoundException e) {
            Logger.error("Error loading MySQL JDBC Driver: " + DB_DRIVER, e);
            throw new RuntimeException("Failed to load database driver.", e);
        }
    }

    /**
     * Establishes and returns a connection to the database.
     * Connection is set to auto-commit by default.
     *
     * @return A Connection object.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        return connection;
    }
    
    /**
     * Establishes and returns a connection with specified auto-commit mode.
     * Used for transaction management.
     *
     * @param autoCommit true to enable auto-commit, false to start a transaction.
     * @return A Connection object with specified auto-commit mode.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection(boolean autoCommit) throws SQLException {
        Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        connection.setAutoCommit(autoCommit);
        return connection;
    }

    // Optional: Add a method to close connections, statements, resultsets gracefully
    public static void close(AutoCloseable... closeables) {
        for (AutoCloseable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (Exception e) {
                    // Log or handle the exception, don't just print stack trace in production
                    System.err.println("Error closing resource: " + e.getMessage());
                    // e.printStackTrace(); // Avoid in production logging
                }
            }
        }
    }

    // Example Usage (Optional - can be removed or put in a main method for testing)
    /*
    public static void main(String[] args) {
        try (Connection conn = DBUtil.getConnection()) { // Using try-with-resources
            if (conn != null) {
                System.out.println("Database connection successful!");
                // You could perform a simple test query here
            } else {
                System.out.println("Database connection failed!");
            }
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    */
}
