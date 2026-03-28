package com.codevengers.voiceemergency;

import java.sql.*;

public class DatabaseManager {

    // Unified database configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/voice_emergency_response";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            Class.forName(DB_DRIVER);
            System.out.println("✅ MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ MySQL JDBC Driver not found: " + e.getMessage());
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("✅ Database connection established");
            return conn;
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed: " + e.getMessage());
            throw e;
        }
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Database connection test successful");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("❌ Database connection test failed: " + e.getMessage());
        }
        return false;
    }

    public static void initializeDatabase() {
        System.out.println("🔧 Initializing database...");

        // Test connection first
        if (!testConnection()) {
            throw new RuntimeException("Cannot connect to database. Please ensure XAMPP is running and MySQL service is started.");
        }

        // Initialize tables
        createEmergencyContactsTable();
        createEmergenciesTable();
        createUsersTable();

        System.out.println("✅ Database initialization completed");
    }

    private static void createEmergencyContactsTable() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS emergency_contacts (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                name VARCHAR(100) NOT NULL,
                phone_number VARCHAR(20) NOT NULL,
                email VARCHAR(100),
                relationship VARCHAR(50) NOT NULL,
                is_primary BOOLEAN DEFAULT FALSE,
                is_active BOOLEAN DEFAULT TRUE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_user_id (user_id),
                INDEX idx_is_active (is_active),
                INDEX idx_is_primary (is_primary)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            System.out.println("✅ Emergency contacts table created/verified");

        } catch (SQLException e) {
            System.err.println("❌ Error creating emergency contacts table: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create emergency contacts table", e);
        }
    }

    private static void createEmergenciesTable() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS emergencies (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                username VARCHAR(100) NOT NULL,
                emergency_type VARCHAR(50) NOT NULL,
                location TEXT,
                status VARCHAR(20) DEFAULT 'ACTIVE',
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
                audio_file_name VARCHAR(255),
                description TEXT,
                trigger_method VARCHAR(20) DEFAULT 'manual',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_user_id (user_id),
                INDEX idx_status (status),
                INDEX idx_timestamp (timestamp)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            System.out.println("✅ Emergencies table created/verified");

        } catch (SQLException e) {
            System.err.println("❌ Error creating emergencies table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createUsersTable() {
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS users (
                id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                email VARCHAR(100) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                full_name VARCHAR(100),
                phone_number VARCHAR(20),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                is_active BOOLEAN DEFAULT TRUE,
                INDEX idx_username (username),
                INDEX idx_email (email)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            System.out.println("✅ Users table created/verified");

        } catch (SQLException e) {
            System.err.println("❌ Error creating users table: " + e.getMessage());
            e.printStackTrace();
        }
    }
}