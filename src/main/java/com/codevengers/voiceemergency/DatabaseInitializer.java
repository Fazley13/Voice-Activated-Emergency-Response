package com.codevengers.voiceemergency;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {
    
    public static void initializeDatabase() {
        System.out.println("Initializing database...");
        
        try {
            // Test connection first
            if (!MySQLConnection.testConnection()) {
                throw new RuntimeException("Cannot connect to MySQL database. Please ensure XAMPP is running.");
            }
            
            System.out.println("✓ Database connection successful");
            
            // Initialize database structure
            MySQLConnection.initializeDatabase();
            
            System.out.println("✓ Database initialized successfully");
            System.out.println("✓ Backend is ready to use!");
            
        } catch (Exception e) {
            System.err.println("✗ Database initialization failed: " + e.getMessage());
            System.err.println("\nPlease ensure:");
            System.err.println("1. XAMPP is running");
            System.err.println("2. MySQL service is started");
            System.err.println("3. Database 'voice_emergency_response' exists");
            System.err.println("4. MySQL JDBC driver is in classpath");
            throw new RuntimeException("Database initialization failed", e);
        }
    }
    
    public static void main(String[] args) {
        initializeDatabase();
    }
}
