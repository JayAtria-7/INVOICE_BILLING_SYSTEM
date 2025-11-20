package com.yourcompany.invoicesystem.util;

/**
 * Password Hash Test Utility
 * Run this to generate correct password hashes and test login credentials
 */
public class PasswordHashTest {
    
    public static void main(String[] args) {
        System.out.println("=== Password Hash Test ===\n");
        
        // Test passwords
        String[] testPasswords = {"admin123", "cashier123", "test", "password"};
        
        for (String password : testPasswords) {
            String hash = SecurityUtil.hashPassword(password);
            System.out.println("Password: " + password);
            System.out.println("Hash:     " + hash);
            System.out.println();
        }
        
        // Expected hashes for comparison
        System.out.println("=== Expected Hashes ===");
        System.out.println("admin123 should be:   240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9");
        System.out.println("cashier123 should be: e19d5cd5af0378da05f63f891c7467af01f8d2c9d32e2de94e89c4e18f1c5ce1");
        System.out.println();
        
        // Test verification
        System.out.println("=== Testing Verification ===");
        String adminHash = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9";
        String cashierHash = "e19d5cd5af0378da05f63f891c7467af01f8d2c9d32e2de94e89c4e18f1c5ce1";
        
        boolean adminVerify = SecurityUtil.verifyPassword("admin123", adminHash);
        boolean cashierVerify = SecurityUtil.verifyPassword("cashier123", cashierHash);
        
        System.out.println("Admin password verification: " + (adminVerify ? "✓ PASS" : "✗ FAIL"));
        System.out.println("Cashier password verification: " + (cashierVerify ? "✓ PASS" : "✗ FAIL"));
        
        // Test wrong passwords
        System.out.println("\n=== Testing Wrong Passwords ===");
        boolean wrongPass = SecurityUtil.verifyPassword("wrongpassword", adminHash);
        System.out.println("Wrong password test: " + (wrongPass ? "✗ FAIL (should reject)" : "✓ PASS (correctly rejected)"));
    }
}
