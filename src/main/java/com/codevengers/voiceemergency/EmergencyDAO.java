package com.codevengers.voiceemergency;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EmergencyDAO {

    public static void initializeEmergencyTable() {
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
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            System.out.println("✅ Emergency table initialized successfully");

        } catch (SQLException e) {
            System.err.println("❌ Error initializing emergency table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static int saveEmergency(Emergency emergency) {
        String insertSQL = """
            INSERT INTO emergencies (user_id, username, emergency_type, location, 
                                   description, trigger_method, status) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, emergency.getUserId());
            pstmt.setString(2, emergency.getUsername());
            pstmt.setString(3, emergency.getEmergencyType());
            pstmt.setString(4, emergency.getLocation());
            pstmt.setString(5, emergency.getDescription());
            pstmt.setString(6, emergency.getTriggerMethod());
            pstmt.setString(7, emergency.getStatus());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int emergencyId = generatedKeys.getInt(1);
                        System.out.println("✅ Emergency saved with ID: " + emergencyId);
                        return emergencyId;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error saving emergency: " + e.getMessage());
            e.printStackTrace();
        }

        return -1;
    }

    public static List<Emergency> getAllEmergencies() {
        List<Emergency> emergencies = new ArrayList<>();
        String selectSQL = "SELECT * FROM emergencies ORDER BY timestamp DESC";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {

            while (rs.next()) {
                Emergency emergency = new Emergency();
                emergency.setId(rs.getInt("id"));
                emergency.setUserId(rs.getInt("user_id"));
                emergency.setUsername(rs.getString("username"));
                emergency.setEmergencyType(rs.getString("emergency_type"));
                emergency.setLocation(rs.getString("location"));
                emergency.setStatus(rs.getString("status"));
                emergency.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                emergency.setAudioFileName(rs.getString("audio_file_name"));
                emergency.setDescription(rs.getString("description"));
                emergency.setTriggerMethod(rs.getString("trigger_method"));

                emergencies.add(emergency);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching emergencies: " + e.getMessage());
            e.printStackTrace();
        }

        return emergencies;
    }

    public static List<Emergency> getEmergenciesByStatus(String status) {
        List<Emergency> emergencies = new ArrayList<>();
        String selectSQL = "SELECT * FROM emergencies WHERE status = ? ORDER BY timestamp DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {

            pstmt.setString(1, status);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Emergency emergency = new Emergency();
                    emergency.setId(rs.getInt("id"));
                    emergency.setUserId(rs.getInt("user_id"));
                    emergency.setUsername(rs.getString("username"));
                    emergency.setEmergencyType(rs.getString("emergency_type"));
                    emergency.setLocation(rs.getString("location"));
                    emergency.setStatus(rs.getString("status"));
                    emergency.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                    emergency.setAudioFileName(rs.getString("audio_file_name"));
                    emergency.setDescription(rs.getString("description"));
                    emergency.setTriggerMethod(rs.getString("trigger_method"));

                    emergencies.add(emergency);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching emergencies by status: " + e.getMessage());
            e.printStackTrace();
        }

        return emergencies;
    }

    public static boolean updateEmergencyStatus(int emergencyId, String newStatus) {
        String updateSQL = "UPDATE emergencies SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {

            pstmt.setString(1, newStatus);
            pstmt.setInt(2, emergencyId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Emergency " + emergencyId + " status updated to: " + newStatus);
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error updating emergency status: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public static void updateAudioFileName(int emergencyId, String audioFileName) {
        String updateSQL = "UPDATE emergencies SET audio_file_name = ? WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {

            pstmt.setString(1, audioFileName);
            pstmt.setInt(2, emergencyId);

            pstmt.executeUpdate();
            System.out.println("✅ Audio file name updated for emergency " + emergencyId);

        } catch (SQLException e) {
            System.err.println("❌ Error updating audio file name: " + e.getMessage());
        }
    }

    public static int getTotalEmergencies() {
        String countSQL = "SELECT COUNT(*) FROM emergencies";

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSQL)) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error counting emergencies: " + e.getMessage());
        }

        return 0;
    }

    public static int getActiveEmergencies() {
        return getEmergenciesByStatus("ACTIVE").size();
    }

    public static int getResolvedEmergencies() {
        return getEmergenciesByStatus("RESOLVED").size();
    }
}
