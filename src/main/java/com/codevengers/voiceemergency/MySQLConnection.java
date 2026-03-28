package com.codevengers.voiceemergency;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class MySQLConnection {

    // XAMPP MySQL default configuration
    private static final String DB_URL = "jdbc:mysql://localhost:3306/voice_emergency_response";
    private static final String DB_USER = "root";  // Default XAMPP username
    private static final String DB_PASSWORD = "";  // Default XAMPP password (empty)

    // Connection pool settings
    private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";

    static {
        try {
            Class.forName(DB_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        Properties props = new Properties();
        props.setProperty("user", DB_USER);
        props.setProperty("password", DB_PASSWORD);
        props.setProperty("useSSL", "false");
        props.setProperty("allowPublicKeyRetrieval", "true");
        props.setProperty("serverTimezone", "UTC");
        props.setProperty("autoReconnect", "true");
        props.setProperty("useUnicode", "true");
        props.setProperty("characterEncoding", "UTF-8");

        return DriverManager.getConnection(DB_URL, props);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            // Test if database exists and is accessible
            conn.createStatement().executeQuery("SELECT 1").close();
            System.out.println("Database connection successful!");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            throw new RuntimeException("Cannot connect to database", e);
        }
    }
}
