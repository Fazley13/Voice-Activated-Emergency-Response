package com.codevengers.voiceemergency;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    public static Connection connect() throws SQLException {
        try {
            // Load the MySQL driver
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Connect to the XAMPP MySQL database
            return DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/voice_emergency_response", // Database URL (localhost for XAMPP)
                "root", // Default username for MySQL in XAMPP
                "" // Default password is empty for XAMPP
            );
        } catch (ClassNotFoundException | SQLException e) {
            throw new SQLException("Database connection error", e);
        }
    }
}
