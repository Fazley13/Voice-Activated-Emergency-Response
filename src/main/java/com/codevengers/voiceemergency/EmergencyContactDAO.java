package com.codevengers.voiceemergency;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EmergencyContactDAO {

    /**
     * Test database connection specifically for emergency contacts
     */
    public static boolean testDatabaseConnection() {
        try {
            System.out.println("🔍 Testing database connection for emergency contacts...");

            Connection conn = DBConnection.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Database connection successful");

                // Test if we can execute a simple query
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeQuery("SELECT 1").close();
                    System.out.println("✅ Database query test successful");
                } catch (SQLException e) {
                    System.err.println("❌ Database query test failed: " + e.getMessage());
                    return false;
                }

                conn.close();
                return true;
            } else {
                System.err.println("❌ Database connection is null or closed");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ Database connection test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static void initializeEmergencyContactTable() {
        System.out.println("🔧 Initializing emergency contacts table...");

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

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            System.out.println("✅ Emergency contacts table created/verified successfully");

            // Verify table structure
            verifyTableStructure(conn);

        } catch (SQLException e) {
            System.err.println("❌ Error initializing emergency contacts table: " + e.getMessage());
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Error Code: " + e.getErrorCode());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize emergency contacts table", e);
        }
    }

    /**
     * Verify the table structure exists and has correct columns
     */
    private static void verifyTableStructure(Connection conn) {
        try {
            System.out.println("🔍 Verifying emergency_contacts table structure...");

            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "emergency_contacts", null);

            boolean tableExists = false;
            List<String> columnNames = new ArrayList<>();

            while (columns.next()) {
                tableExists = true;
                String columnName = columns.getString("COLUMN_NAME");
                columnNames.add(columnName);
                System.out.println("   ✓ Column: " + columnName);
            }

            if (!tableExists) {
                throw new SQLException("emergency_contacts table does not exist after creation");
            }

            // Check for required columns
            String[] requiredColumns = {"id", "user_id", "name", "phone_number", "email", "relationship", "is_primary", "is_active"};
            for (String requiredColumn : requiredColumns) {
                if (!columnNames.contains(requiredColumn)) {
                    throw new SQLException("Required column '" + requiredColumn + "' is missing from emergency_contacts table");
                }
            }

            System.out.println("✅ Table structure verification complete");

        } catch (SQLException e) {
            System.err.println("❌ Error verifying table structure: " + e.getMessage());
            throw new RuntimeException("Table structure verification failed", e);
        }
    }

    public static int addEmergencyContact(EmergencyContact contact) {
        System.out.println("💾 Adding emergency contact: " + contact.getName());

        // Validate input
        if (contact == null) {
            throw new IllegalArgumentException("Contact cannot be null");
        }
        if (contact.getName() == null || contact.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Contact name cannot be empty");
        }
        if (contact.getPhoneNumber() == null || contact.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Contact phone number cannot be empty");
        }
        if (contact.getRelationship() == null || contact.getRelationship().trim().isEmpty()) {
            throw new IllegalArgumentException("Contact relationship cannot be empty");
        }
        if (contact.getUserId() <= 0) {
            throw new IllegalArgumentException("Invalid user ID: " + contact.getUserId());
        }

        String insertSQL = """
            INSERT INTO emergency_contacts (user_id, name, phone_number, email, relationship, is_primary, is_active) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DBConnection.getConnection()) {

            // If this contact is being set as primary, remove primary status from others first
            if (contact.isPrimary()) {
                System.out.println("🔄 Removing primary status from other contacts...");
                removePrimaryStatus(conn, contact.getUserId());
            }

            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {

                pstmt.setInt(1, contact.getUserId());
                pstmt.setString(2, contact.getName().trim());
                pstmt.setString(3, contact.getPhoneNumber().trim());
                pstmt.setString(4, contact.getEmail() != null ? contact.getEmail().trim() : null);
                pstmt.setString(5, contact.getRelationship().trim());
                pstmt.setBoolean(6, contact.isPrimary());
                pstmt.setBoolean(7, true); // Always active when first created

                System.out.println("🔧 Executing insert with parameters:");
                System.out.println("   User ID: " + contact.getUserId());
                System.out.println("   Name: " + contact.getName());
                System.out.println("   Phone: " + contact.getPhoneNumber());
                System.out.println("   Email: " + contact.getEmail());
                System.out.println("   Relationship: " + contact.getRelationship());
                System.out.println("   Primary: " + contact.isPrimary());

                int rowsAffected = pstmt.executeUpdate();
                System.out.println("📊 Rows affected: " + rowsAffected);

                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int contactId = generatedKeys.getInt(1);
                            System.out.println("✅ Emergency contact added successfully with ID: " + contactId);
                            return contactId;
                        } else {
                            System.err.println("❌ No generated key returned after insert");
                            return -1;
                        }
                    }
                } else {
                    System.err.println("❌ No rows were affected by the insert operation");
                    return -1;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ SQL Error adding emergency contact:");
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Error Code: " + e.getErrorCode());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to add emergency contact to database", e);
        } catch (Exception e) {
            System.err.println("❌ Unexpected error adding emergency contact: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unexpected error while adding emergency contact", e);
        }
    }

    public static List<EmergencyContact> getEmergencyContactsByUserId(int userId) {
        System.out.println("🔍 Getting emergency contacts for user ID: " + userId);

        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID: " + userId);
        }

        List<EmergencyContact> contacts = new ArrayList<>();
        String selectSQL = """
            SELECT id, user_id, name, phone_number, email, relationship, is_primary, is_active, 
                   created_at, updated_at 
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

                    // Handle timestamps safely
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
            System.err.println("❌ Error fetching emergency contacts for user " + userId + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch emergency contacts", e);
        }

        return contacts;
    }

    public static boolean updateEmergencyContact(EmergencyContact contact) {
        System.out.println("🔄 Updating emergency contact: " + contact.getName());

        if (contact == null || contact.getId() <= 0) {
            throw new IllegalArgumentException("Invalid contact or contact ID");
        }

        String updateSQL = """
            UPDATE emergency_contacts 
            SET name = ?, phone_number = ?, email = ?, relationship = ?, is_primary = ?, 
                updated_at = CURRENT_TIMESTAMP 
            WHERE id = ? AND user_id = ?
        """;

        try (Connection conn = DBConnection.getConnection()) {

            // If this contact is being set as primary, remove primary status from others
            if (contact.isPrimary()) {
                removePrimaryStatus(conn, contact.getUserId());
            }

            try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
                pstmt.setString(1, contact.getName());
                pstmt.setString(2, contact.getPhoneNumber());
                pstmt.setString(3, contact.getEmail());
                pstmt.setString(4, contact.getRelationship());
                pstmt.setBoolean(5, contact.isPrimary());
                pstmt.setInt(6, contact.getId());
                pstmt.setInt(7, contact.getUserId());

                int rowsAffected = pstmt.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("✅ Emergency contact updated successfully: " + contact.getName());
                    return true;
                } else {
                    System.err.println("❌ No rows affected - contact may not exist or belong to user");
                    return false;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error updating emergency contact: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to update emergency contact", e);
        }
    }

    public static boolean deleteEmergencyContact(int contactId, int userId) {
        System.out.println("🗑️ Deleting emergency contact ID: " + contactId + " for user: " + userId);

        if (contactId <= 0 || userId <= 0) {
            throw new IllegalArgumentException("Invalid contact ID or user ID");
        }

        String deleteSQL = """
            UPDATE emergency_contacts 
            SET is_active = FALSE, updated_at = CURRENT_TIMESTAMP 
            WHERE id = ? AND user_id = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {

            pstmt.setInt(1, contactId);
            pstmt.setInt(2, userId);

            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("✅ Emergency contact deleted (deactivated) successfully: " + contactId);
                return true;
            } else {
                System.err.println("❌ No rows affected - contact may not exist or not belong to user");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error deleting emergency contact: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to delete emergency contact", e);
        }
    }

    public static EmergencyContact getPrimaryContact(int userId) {
        System.out.println("🔍 Getting primary contact for user ID: " + userId);

        if (userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID: " + userId);
        }

        String selectSQL = """
            SELECT id, user_id, name, phone_number, email, relationship, is_primary, is_active, 
                   created_at, updated_at 
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

                    Timestamp createdAt = rs.getTimestamp("created_at");
                    if (createdAt != null) {
                        contact.setCreatedAt(createdAt.toLocalDateTime());
                    }

                    Timestamp updatedAt = rs.getTimestamp("updated_at");
                    if (updatedAt != null) {
                        contact.setUpdatedAt(updatedAt.toLocalDateTime());
                    }

                    System.out.println("✅ Found primary contact: " + contact.getName());
                    return contact;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error fetching primary contact: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch primary contact", e);
        }

        System.out.println("ℹ️ No primary contact found for user " + userId);
        return null;
    }

    public static List<EmergencyContact> getAllActiveContacts(int userId) {
        return getEmergencyContactsByUserId(userId);
    }

    /**
     * Remove primary status from all contacts for a user
     */
    private static void removePrimaryStatus(int userId) {
        String updateSQL = "UPDATE emergency_contacts SET is_primary = FALSE WHERE user_id = ? AND is_primary = TRUE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {

            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("🔄 Removed primary status from " + rowsAffected + " contacts for user " + userId);

        } catch (SQLException e) {
            System.err.println("❌ Error removing primary status: " + e.getMessage());
        }
    }

    /**
     * Remove primary status from all contacts for a user (with existing connection)
     */
    private static void removePrimaryStatus(Connection conn, int userId) {
        String updateSQL = "UPDATE emergency_contacts SET is_primary = FALSE WHERE user_id = ? AND is_primary = TRUE";

        try (PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {
            pstmt.setInt(1, userId);
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("🔄 Removed primary status from " + rowsAffected + " contacts for user " + userId);
        } catch (SQLException e) {
            System.err.println("❌ Error removing primary status: " + e.getMessage());
            throw new RuntimeException("Failed to remove primary status", e);
        }
    }

    public static int getContactCount(int userId) {
        if (userId <= 0) {
            return 0;
        }

        String countSQL = "SELECT COUNT(*) FROM emergency_contacts WHERE user_id = ? AND is_active = TRUE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(countSQL)) {

            pstmt.setInt(1, userId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("ℹ️ User " + userId + " has " + count + " active emergency contacts");
                    return count;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error counting emergency contacts: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Check if phone number already exists for user (to prevent duplicates)
     */
    public static boolean phoneNumberExists(int userId, String phoneNumber) {
        if (userId <= 0 || phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }

        String checkSQL = "SELECT COUNT(*) FROM emergency_contacts WHERE user_id = ? AND phone_number = ? AND is_active = TRUE";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(checkSQL)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, phoneNumber.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            System.err.println("❌ Error checking phone number existence: " + e.getMessage());
        }

        return false;
    }
}