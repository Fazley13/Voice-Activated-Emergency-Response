package com.codevengers.voiceemergency;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnectionTester {

    public static void testConnection() {
        System.out.println("=== DATABASE CONNECTION TEST ===");

        try {
            System.out.println("🔄 Testing DBConnection.getConnection()...");
            Connection conn = DBConnection.getConnection();

            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Database connection successful!");
                System.out.println("   Connection URL: " + conn.getMetaData().getURL());
                System.out.println("   Database Product: " + conn.getMetaData().getDatabaseProductName());
                System.out.println("   Database Version: " + conn.getMetaData().getDatabaseProductVersion());

                // Test if we can execute a simple query
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeQuery("SELECT 1").close();
                    System.out.println("✅ Database query test successful!");
                } catch (SQLException e) {
                    System.err.println("❌ Database query test failed: " + e.getMessage());
                }

                conn.close();
            } else {
                System.err.println("❌ Database connection is null or closed");
            }

        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            System.err.println("💡 Please check:");
            System.err.println("   1. XAMPP is running");
            System.err.println("   2. MySQL service is started in XAMPP");
            System.err.println("   3. Database 'voice_emergency_response' exists");
            System.err.println("   4. MySQL is running on port 3306");
        }

        System.out.println("=== TEST COMPLETE ===");
    }

    public static void main(String[] args) {
        testConnection();
    }
}
