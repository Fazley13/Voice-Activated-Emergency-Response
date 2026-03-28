package com.codevengers.voiceemergency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmergencyContactDatabaseCleanup {

    public static void main(String[] args) {
        System.out.println("=== EMERGENCY CONTACTS DATABASE CLEANUP ===");
        cleanupEmergencyContactsTable();
    }

    public static void cleanupEmergencyContactsTable() {
        try {
            System.out.println("🧹 Starting complete database cleanup...");

            Connection conn = DBConnection.getConnection();
            System.out.println("✅ Connected to database: " + conn.getMetaData().getURL());

            // Step 1: Backup existing data if any
            List<ContactBackup> backupData = backupExistingData(conn);

            // Step 2: Drop the problematic table completely
            dropExistingTable(conn);

            // Step 3: Create clean table with correct structure
            createCleanTable(conn);

            // Step 4: Restore backed up data if any
            restoreBackupData(conn, backupData);

            // Step 5: Verify the clean structure
            verifyCleanStructure(conn);

            conn.close();
            System.out.println("✅ Database cleanup completed successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error during database cleanup: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<ContactBackup> backupExistingData(Connection conn) {
        System.out.println("💾 Backing up existing emergency contacts data...");
        List<ContactBackup> backupData = new ArrayList<>();

        try {
            // Try to read existing data with various column name combinations
            String[] possibleQueries = {
                    "SELECT user_id, name, phone_number, email, relationship, is_primary, is_active FROM emergency_contacts WHERE is_active = 1",
                    "SELECT user_id, name, phone, email, relationship, priority, is_active FROM emergency_contacts WHERE is_active = 1",
                    "SELECT user_id, contact_name, phone, contact_email, relationship, priority, is_active FROM emergency_contacts WHERE is_active = 1",
                    "SELECT user_id, name, phone_number, email, relationship FROM emergency_contacts",
                    "SELECT user_id, name, phone, email, relationship FROM emergency_contacts"
            };

            for (String query : possibleQueries) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {

                    while (rs.next()) {
                        ContactBackup backup = new ContactBackup();
                        backup.userId = rs.getInt("user_id");

                        // Try different name column possibilities
                        try {
                            backup.name = rs.getString("name");
                        } catch (SQLException e) {
                            backup.name = rs.getString("contact_name");
                        }

                        // Try different phone column possibilities
                        try {
                            backup.phoneNumber = rs.getString("phone_number");
                        } catch (SQLException e) {
                            backup.phoneNumber = rs.getString("phone");
                        }

                        // Try different email column possibilities
                        try {
                            backup.email = rs.getString("email");
                        } catch (SQLException e) {
                            backup.email = rs.getString("contact_email");
                        }

                        backup.relationship = rs.getString("relationship");

                        // Try different primary column possibilities
                        try {
                            backup.isPrimary = rs.getBoolean("is_primary");
                        } catch (SQLException e) {
                            try {
                                backup.isPrimary = rs.getInt("priority") == 1;
                            } catch (SQLException e2) {
                                backup.isPrimary = false;
                            }
                        }

                        backupData.add(backup);
                    }

                    if (!backupData.isEmpty()) {
                        System.out.println("✅ Backed up " + backupData.size() + " emergency contacts");
                        break; // Successfully read data, no need to try other queries
                    }

                } catch (SQLException e) {
                    // Try next query format
                    continue;
                }
            }

            if (backupData.isEmpty()) {
                System.out.println("ℹ️ No existing data found to backup");
            }

        } catch (Exception e) {
            System.err.println("⚠️ Error backing up data (continuing anyway): " + e.getMessage());
        }

        return backupData;
    }

    private static void dropExistingTable(Connection conn) {
        System.out.println("🗑️ Dropping existing emergency_contacts table...");

        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS emergency_contacts");
            System.out.println("✅ Existing table dropped successfully");
        } catch (SQLException e) {
            System.err.println("❌ Error dropping table: " + e.getMessage());
            throw new RuntimeException("Failed to drop existing table", e);
        }
    }

    private static void createCleanTable(Connection conn) {
        System.out.println("🏗️ Creating clean emergency_contacts table...");

        String createTableSQL = """
            CREATE TABLE emergency_contacts (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                name VARCHAR(100) NOT NULL,
                phone_number VARCHAR(20) NOT NULL,
                email VARCHAR(100) NULL,
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

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("✅ Clean emergency_contacts table created successfully");
        } catch (SQLException e) {
            System.err.println("❌ Error creating clean table: " + e.getMessage());
            throw new RuntimeException("Failed to create clean table", e);
        }
    }

    private static void restoreBackupData(Connection conn, List<ContactBackup> backupData) {
        if (backupData.isEmpty()) {
            System.out.println("ℹ️ No backup data to restore");
            return;
        }

        System.out.println("📥 Restoring " + backupData.size() + " backed up contacts...");

        String insertSQL = """
            INSERT INTO emergency_contacts (user_id, name, phone_number, email, relationship, is_primary, is_active) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        int restoredCount = 0;

        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            for (ContactBackup backup : backupData) {
                try {
                    pstmt.setInt(1, backup.userId);
                    pstmt.setString(2, backup.name != null ? backup.name : "Unknown Contact");
                    pstmt.setString(3, backup.phoneNumber != null ? backup.phoneNumber : "0000000000");
                    pstmt.setString(4, backup.email);
                    pstmt.setString(5, backup.relationship != null ? backup.relationship : "Family Member");
                    pstmt.setBoolean(6, backup.isPrimary);
                    pstmt.setBoolean(7, true);

                    pstmt.executeUpdate();
                    restoredCount++;

                } catch (SQLException e) {
                    System.err.println("⚠️ Error restoring contact: " + backup.name + " - " + e.getMessage());
                }
            }

            System.out.println("✅ Restored " + restoredCount + " contacts successfully");

        } catch (SQLException e) {
            System.err.println("❌ Error restoring backup data: " + e.getMessage());
        }
    }

    private static void verifyCleanStructure(Connection conn) {
        System.out.println("🔍 Verifying clean table structure...");

        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "emergency_contacts", null);

            System.out.println("📋 Clean table structure:");
            List<String> columnNames = new ArrayList<>();

            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String dataType = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                String isNullable = columns.getString("IS_NULLABLE");
                String defaultValue = columns.getString("COLUMN_DEF");

                columnNames.add(columnName.toLowerCase());

                System.out.println(String.format("   ✓ %s %s(%d) %s %s",
                        columnName,
                        dataType,
                        columnSize,
                        isNullable.equals("YES") ? "NULL" : "NOT NULL",
                        defaultValue != null ? "DEFAULT " + defaultValue : ""
                ));
            }
            columns.close();

            // Verify required columns exist
            String[] requiredColumns = {"id", "user_id", "name", "phone_number", "email", "relationship", "is_primary", "is_active"};
            boolean allColumnsExist = true;

            for (String requiredColumn : requiredColumns) {
                if (!columnNames.contains(requiredColumn.toLowerCase())) {
                    System.err.println("❌ Missing required column: " + requiredColumn);
                    allColumnsExist = false;
                }
            }

            if (allColumnsExist) {
                System.out.println("✅ All required columns exist");

                // Test a simple query
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM emergency_contacts");
                    if (rs.next()) {
                        int count = rs.getInt(1);
                        System.out.println("✅ Table verification successful. Current record count: " + count);
                    }
                    rs.close();
                }

                // Test insert/delete to verify structure
                testTableOperations(conn);

            } else {
                throw new RuntimeException("Table structure verification failed - missing required columns");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error verifying table structure: " + e.getMessage());
            throw new RuntimeException("Table structure verification failed", e);
        }
    }

    private static void testTableOperations(Connection conn) {
        System.out.println("🧪 Testing table operations...");

        String insertSQL = """
            INSERT INTO emergency_contacts (user_id, name, phone_number, email, relationship, is_primary, is_active) 
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, 99999); // Test user ID
            pstmt.setString(2, "Test Contact");
            pstmt.setString(3, "01700000999");
            pstmt.setString(4, "test@example.com");
            pstmt.setString(5, "Test Relationship");
            pstmt.setBoolean(6, false);
            pstmt.setBoolean(7, true);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int testId = generatedKeys.getInt(1);
                        System.out.println("✅ Test insert successful! Generated ID: " + testId);

                        // Clean up test data
                        try (Statement stmt = conn.createStatement()) {
                            stmt.execute("DELETE FROM emergency_contacts WHERE id = " + testId);
                            System.out.println("✅ Test data cleaned up");
                        }
                    }
                }
            } else {
                System.err.println("❌ Test insert failed");
            }
        } catch (SQLException e) {
            System.err.println("❌ Test operations failed: " + e.getMessage());
            throw new RuntimeException("Table operations test failed", e);
        }
    }

    // Helper class for backing up contact data
    private static class ContactBackup {
        int userId;
        String name;
        String phoneNumber;
        String email;
        String relationship;
        boolean isPrimary;
    }
}