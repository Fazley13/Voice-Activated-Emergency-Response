package com.codevengers.voiceemergency;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FixedEmergencyContactDAO {

    // Test database connection
    public static boolean testDatabaseConnection() {
        try (Connection conn = DBConnection.getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("❌ Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    // Initialize emergency contacts table
    public static void initializeEmergencyContactTable() {
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
                INDEX idx_is_primary (is_primary),
                INDEX idx_is_active (is_active)
            )
        """;

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            System.out.println("✅ Emergency contacts table initialized successfully");

        } catch (SQLException e) {
            System.err.println("❌ Error initializing emergency contacts table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Add new emergency contact - returns contact ID
    public static int addEmergencyContact(EmergencyContact contact) {
        System.out.println("🔄 Adding emergency contact to database...");
        System.out.println("   User ID: " + contact.getUserId());
        System.out.println("   Name: " + contact.getName());
        System.out.println("   Phone: " + contact.getPhoneNumber());
        System.out.println("   Email: " + contact.getEmail());
        System.out.println("   Relationship: " + contact.getRelationship());
        System.out.println("   Is Primary: " + contact.isPrimary());

        // If this is set as primary, first remove primary status from other contacts
        if (contact.isPrimary()) {
            removePrimaryStatusFromOtherContacts(contact.getUserId());
        }

        String insertSQL = """
            INSERT INTO emergency_contacts (user_id, name, phone_number, email, relationship, is_primary, is_active) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, contact.getUserId());
            pstmt.setString(2, contact.getName());
            pstmt.setString(3, contact.getPhoneNumber());
            pstmt.setString(4, contact.getEmail());
            pstmt.setString(5, contact.getRelationship());
            pstmt.setBoolean(6, contact.isPrimary());
            pstmt.setBoolean(7, contact.isActive());

            System.out.println("🔄 Executing SQL: " + insertSQL);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("📊 Rows affected: " + rowsAffected);

            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int contactId = generatedKeys.getInt(1);
                        System.out.println("✅ Emergency contact saved with ID: " + contactId);
                        return contactId;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error adding emergency contact: " + e.getMessage());
            e.printStackTrace();

            // Provide more specific error information
            if (e.getMessage().contains("Duplicate entry")) {
                System.err.println("💡 Suggestion: Contact with this phone number might already exist");
            } else if (e.getMessage().contains("cannot be null")) {
                System.err.println("💡 Suggestion: Required field is missing");
            } else if (e.getMessage().contains("Connection")) {
                System.err.println("💡 Suggestion: Database connection issue - check if XAMPP MySQL is running");
            }
        }

        return -1;
    }

    // OVERLOADED METHOD: Add emergency contact - returns boolean (for DatabaseDebugger compatibility)
    public static boolean addEmergencyContact(EmergencyContact contact, boolean returnBoolean) {
        int result = addEmergencyContact(contact);
        return result > 0;
    }

    // Remove primary status from other contacts when setting a new primary
    private static void removePrimaryStatusFromOtherContacts(int userId) {
        String updateSQL = "UPDATE emergency_contacts SET is_primary = FALSE WHERE user_id = ? AND is_primary = TRUE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {

            pstmt.setInt(1, userId);
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("✅ Removed primary status from " + rowsUpdated + " existing contacts");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error removing primary status: " + e.getMessage());
        }
    }

    // Get all emergency contacts for a user
    public static List<EmergencyContact> getEmergencyContactsByUserId(int userId) {
        List<EmergencyContact> contacts = new ArrayList<>();
        String selectSQL = """
            SELECT id, user_id, name, phone_number, email, relationship, is_primary, is_active, created_at, updated_at 
            FROM emergency_contacts 
            WHERE user_id = ? AND is_active = TRUE 
            ORDER BY is_primary DESC, name ASC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    EmergencyContact contact = new EmergencyContact();
                    contact.setId(rs.getInt("id"));
                    contact.setUserId(rs.getInt("user_id"));
                    contact.setName(rs.getString("name"));
                    contact.setPhoneNumber(rs.getString("phone_number"));
                    contact.setEmail(rs.getString("email"));
                    contact.setRelationship(rs.getString("relationship"));
                    contact.setPrimary(rs.getBoolean("is_primary"));
                    contact.setActive(rs.getBoolean("is_active"));

                    // Handle timestamps
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        contact.setCreatedAt(createdAt.toLocalDateTime());
                    }

                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    if (updatedAt != null) {
                        contact.setUpdatedAt(updatedAt.toLocalDateTime());
                    }

                    contacts.add(contact);
                }
            }

            System.out.println("✅ Retrieved " + contacts.size() + " emergency contacts for user " + userId);

        } catch (SQLException e) {
            System.err.println("❌ Error retrieving emergency contacts: " + e.getMessage());
            e.printStackTrace();
        }

        return contacts;
    }

    // Get primary contact for a user
    public static EmergencyContact getPrimaryContact(int userId) {
        String selectSQL = """
            SELECT id, user_id, name, phone_number, email, relationship, is_primary, is_active, created_at, updated_at 
            FROM emergency_contacts 
            WHERE user_id = ? AND is_primary = TRUE AND is_active = TRUE 
            LIMIT 1
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    EmergencyContact contact = new EmergencyContact();
                    contact.setId(rs.getInt("id"));
                    contact.setUserId(rs.getInt("user_id"));
                    contact.setName(rs.getString("name"));
                    contact.setPhoneNumber(rs.getString("phone_number"));
                    contact.setEmail(rs.getString("email"));
                    contact.setRelationship(rs.getString("relationship"));
                    contact.setPrimary(rs.getBoolean("is_primary"));
                    contact.setActive(rs.getBoolean("is_active"));

                    // Handle timestamps
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        contact.setCreatedAt(createdAt.toLocalDateTime());
                    }

                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    if (updatedAt != null) {
                        contact.setUpdatedAt(updatedAt.toLocalDateTime());
                    }

                    return contact;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error retrieving primary contact: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // Update emergency contact
    public static boolean updateEmergencyContact(EmergencyContact contact) {
        // If this is set as primary, first remove primary status from other contacts
        if (contact.isPrimary()) {
            removePrimaryStatusFromOtherContacts(contact.getUserId());
        }

        String updateSQL = """
            UPDATE emergency_contacts 
            SET name = ?, phone_number = ?, email = ?, relationship = ?, is_primary = ?, updated_at = CURRENT_TIMESTAMP 
            WHERE id = ? AND user_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {

            pstmt.setString(1, contact.getName());
            pstmt.setString(2, contact.getPhoneNumber());
            pstmt.setString(3, contact.getEmail());
            pstmt.setString(4, contact.getRelationship());
            pstmt.setBoolean(5, contact.isPrimary());
            pstmt.setInt(6, contact.getId());
            pstmt.setInt(7, contact.getUserId());

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Emergency contact updated successfully");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error updating emergency contact: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Delete emergency contact (soft delete - marks as inactive)
    public static boolean deleteEmergencyContact(int contactId, int userId) {
        String deleteSQL = "UPDATE emergency_contacts SET is_active = FALSE, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {

            pstmt.setInt(1, contactId);
            pstmt.setInt(2, userId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Emergency contact deleted successfully (soft delete)");
                return true;
            } else {
                System.out.println("⚠️ No contact found with ID " + contactId + " for user " + userId);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error deleting emergency contact: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Hard delete emergency contact (permanently removes from database)
    public static boolean hardDeleteEmergencyContact(int contactId, int userId) {
        String deleteSQL = "DELETE FROM emergency_contacts WHERE id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {

            pstmt.setInt(1, contactId);
            pstmt.setInt(2, userId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Emergency contact permanently deleted");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error permanently deleting emergency contact: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Get contact count for a user
    public static int getContactCount(int userId) {
        String countSQL = "SELECT COUNT(*) FROM emergency_contacts WHERE user_id = ? AND is_active = TRUE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(countSQL)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error counting emergency contacts: " + e.getMessage());
        }

        return 0;
    }

    // Get all contacts including inactive ones (for admin purposes)
    public static List<EmergencyContact> getAllContactsByUserId(int userId) {
        List<EmergencyContact> contacts = new ArrayList<>();
        String selectSQL = """
            SELECT id, user_id, name, phone_number, email, relationship, is_primary, is_active, created_at, updated_at 
            FROM emergency_contacts 
            WHERE user_id = ? 
            ORDER BY is_active DESC, is_primary DESC, name ASC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    EmergencyContact contact = new EmergencyContact();
                    contact.setId(rs.getInt("id"));
                    contact.setUserId(rs.getInt("user_id"));
                    contact.setName(rs.getString("name"));
                    contact.setPhoneNumber(rs.getString("phone_number"));
                    contact.setEmail(rs.getString("email"));
                    contact.setRelationship(rs.getString("relationship"));
                    contact.setPrimary(rs.getBoolean("is_primary"));
                    contact.setActive(rs.getBoolean("is_active"));

                    // Handle timestamps
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        contact.setCreatedAt(createdAt.toLocalDateTime());
                    }

                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    if (updatedAt != null) {
                        contact.setUpdatedAt(updatedAt.toLocalDateTime());
                    }

                    contacts.add(contact);
                }
            }

            System.out.println("✅ Retrieved " + contacts.size() + " total contacts (including inactive) for user " + userId);

        } catch (SQLException e) {
            System.err.println("❌ Error retrieving all contacts: " + e.getMessage());
            e.printStackTrace();
        }

        return contacts;
    }

    // Restore deleted contact (reactivate)
    public static boolean restoreEmergencyContact(int contactId, int userId) {
        String restoreSQL = "UPDATE emergency_contacts SET is_active = TRUE, updated_at = CURRENT_TIMESTAMP WHERE id = ? AND user_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(restoreSQL)) {

            pstmt.setInt(1, contactId);
            pstmt.setInt(2, userId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Emergency contact restored successfully");
                return true;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error restoring emergency contact: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Check if contact exists by phone number (ensure this method exists)
    public static boolean contactExistsByPhone(int userId, String phoneNumber) {
        String checkSQL = "SELECT COUNT(*) FROM emergency_contacts WHERE user_id = ? AND phone_number = ? AND is_active = TRUE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSQL)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, phoneNumber);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("📊 Found " + count + " existing contacts with phone: " + phoneNumber);
                    return count > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error checking contact existence: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    // Alternative method to check duplicates
    public static boolean phoneNumberExists(int userId, String phoneNumber) {
        List<EmergencyContact> contacts = getEmergencyContactsByUserId(userId);

        for (EmergencyContact contact : contacts) {
            if (contact.getPhoneNumber().equals(phoneNumber)) {
                return true;
            }
        }

        return false;
    }
}
