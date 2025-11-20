/**
 * Author: Jay Prakash Kumar
 * Copyright (c) 2025
 * Licensed under MIT License
 */

package com.yourcompany.invoicesystem.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Simple logging utility for the Invoice Billing System.
 * Provides basic logging functionality without external dependencies.
 * For production use, consider using SLF4J + Logback or Log4j2.
 */
public class Logger {
    
    public enum Level {
        DEBUG, INFO, WARN, ERROR
    }
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "application.log";
    private static Level currentLevel = Level.INFO;
    private static boolean consoleOutput = true;
    private static boolean fileOutput = true;
    
    static {
        // Create logs directory if it doesn't exist
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
    }
    
    /**
     * Set the minimum logging level
     */
    public static void setLevel(Level level) {
        currentLevel = level;
    }
    
    /**
     * Enable or disable console output
     */
    public static void setConsoleOutput(boolean enable) {
        consoleOutput = enable;
    }
    
    /**
     * Enable or disable file output
     */
    public static void setFileOutput(boolean enable) {
        fileOutput = enable;
    }
    
    /**
     * Log a debug message
     */
    public static void debug(String message) {
        log(Level.DEBUG, message, null);
    }
    
    /**
     * Log an info message
     */
    public static void info(String message) {
        log(Level.INFO, message, null);
    }
    
    /**
     * Log a warning message
     */
    public static void warn(String message) {
        log(Level.WARN, message, null);
    }
    
    /**
     * Log a warning message with exception
     */
    public static void warn(String message, Throwable throwable) {
        log(Level.WARN, message, throwable);
    }
    
    /**
     * Log an error message
     */
    public static void error(String message) {
        log(Level.ERROR, message, null);
    }
    
    /**
     * Log an error message with exception
     */
    public static void error(String message, Throwable throwable) {
        log(Level.ERROR, message, throwable);
    }
    
    /**
     * Internal logging method
     */
    private static void log(Level level, String message, Throwable throwable) {
        if (level.ordinal() < currentLevel.ordinal()) {
            return; // Skip if below minimum level
        }
        
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String threadName = Thread.currentThread().getName();
        StackTraceElement caller = getCallerInfo();
        String className = caller.getClassName();
        String methodName = caller.getMethodName();
        int lineNumber = caller.getLineNumber();
        
        // Shorten class name
        String shortClassName = className.substring(className.lastIndexOf('.') + 1);
        
        // Build log message
        StringBuilder logMessage = new StringBuilder();
        logMessage.append(timestamp)
                  .append(" [").append(threadName).append("]")
                  .append(" ").append(level.name())
                  .append(" ").append(shortClassName)
                  .append(".").append(methodName)
                  .append(":").append(lineNumber)
                  .append(" - ").append(message);
        
        String logLine = logMessage.toString();
        
        // Output to console
        if (consoleOutput) {
            if (level == Level.ERROR || level == Level.WARN) {
                System.err.println(logLine);
            } else {
                System.out.println(logLine);
            }
            
            if (throwable != null) {
                throwable.printStackTrace(System.err);
            }
        }
        
        // Output to file
        if (fileOutput) {
            writeToFile(logLine, throwable);
        }
    }
    
    /**
     * Write log message to file
     */
    private static void writeToFile(String message, Throwable throwable) {
        File logFile = new File(LOG_DIR, LOG_FILE);
        
        try (FileWriter fw = new FileWriter(logFile, true);
             PrintWriter pw = new PrintWriter(fw)) {
            
            pw.println(message);
            
            if (throwable != null) {
                throwable.printStackTrace(pw);
            }
            
        } catch (IOException e) {
            // Can't log this error without causing recursion, so just print to stderr
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }
    
    /**
     * Get information about the caller (who called the log method)
     */
    private static StackTraceElement getCallerInfo() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        // Skip: getStackTrace, getCallerInfo, log, and the public log method (debug/info/warn/error)
        for (int i = 4; i < stackTrace.length; i++) {
            StackTraceElement element = stackTrace[i];
            if (!element.getClassName().equals(Logger.class.getName())) {
                return element;
            }
        }
        // Fallback
        return stackTrace[stackTrace.length - 1];
    }
    
    /**
     * Create a logger instance for a specific class (for compatibility with standard loggers)
     */
    public static Logger getLogger(Class<?> clazz) {
        return new Logger(clazz.getName());
    }
    
    // Instance fields for class-specific logger
    private String className;
    
    private Logger(String className) {
        this.className = className;
    }
    
    // Instance methods (delegate to static methods)
    public void debugInstance(String message) {
        debug("[" + getShortClassName() + "] " + message);
    }
    
    public void infoInstance(String message) {
        info("[" + getShortClassName() + "] " + message);
    }
    
    public void warnInstance(String message) {
        warn("[" + getShortClassName() + "] " + message);
    }
    
    public void warnInstance(String message, Throwable throwable) {
        warn("[" + getShortClassName() + "] " + message, throwable);
    }
    
    public void errorInstance(String message) {
        error("[" + getShortClassName() + "] " + message);
    }
    
    public void errorInstance(String message, Throwable throwable) {
        error("[" + getShortClassName() + "] " + message, throwable);
    }
    
    private String getShortClassName() {
        return className.substring(className.lastIndexOf('.') + 1);
    }
}

