package com.codevengers.voiceemergency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmergencyContactDatabaseFixer {

    public static void main(String[] args) {
        System.out.println("=== EMERGENCY CONTACTS DATABASE FIXER ===");
        fixEmergencyContactsTable();
    }

    public static void fixEmergencyContactsTable() {
        try {
            System.out.println("🔧 Starting database structure fix...");

            Connection conn = DBConnection.getConnection();
            System.out.println("✅ Connected to database: " + conn.getMetaData().getURL());

            // Step 1: Check if table exists
            if (!tableExists(conn, "emergency_contacts")) {
                System.out.println("📋 Table 'emergency_contacts' does not exist. Creating it...");
                createEmergencyContactsTable(conn);
            } else {
                System.out.println("📋 Table 'emergency_contacts' exists. Checking structure...");

                // Step 2: Check and fix column structure
                fixTableStructure(conn);
            }

            // Step 3: Verify final structure
            verifyTableStructure(conn);

            conn.close();
            System.out.println("✅ Database structure fix completed successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error fixing database structure: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static boolean tableExists(Connection conn, String tableName) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, null, tableName, null);
            boolean exists = tables.next();
            tables.close();
            return exists;
        } catch (SQLException e) {
            System.err.println("❌ Error checking if table exists: " + e.getMessage());
            return false;
        }
    }

    private static void createEmergencyContactsTable(Connection conn) {
        String createTableSQL = """
            CREATE TABLE emergency_contacts (
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

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
            System.out.println("✅ Created emergency_contacts table with correct structure");
        } catch (SQLException e) {
            System.err.println("❌ Error creating table: " + e.getMessage());
            throw new RuntimeException("Failed to create emergency_contacts table", e);
        }
    }

    private static void fixTableStructure(Connection conn) {
        try {
            System.out.println("🔍 Analyzing current table structure...");

            // Get current columns
            List<String> currentColumns = getCurrentColumns(conn, "emergency_contacts");
            System.out.println("📋 Current columns: " + currentColumns);

            // Check and add missing columns
            addMissingColumns(conn, currentColumns);

            // Rename columns if needed
            renameColumnsIfNeeded(conn, currentColumns);

        } catch (SQLException e) {
            System.err.println("❌ Error fixing table structure: " + e.getMessage());
            throw new RuntimeException("Failed to fix table structure", e);
        }
    }

    private static List<String> getCurrentColumns(Connection conn, String tableName) throws SQLException {
        List<String> columns = new ArrayList<>();

        DatabaseMetaData metaData = conn.getMetaData();
        ResultSet rs = metaData.getColumns(null, null, tableName, null);

        while (rs.next()) {
            String columnName = rs.getString("COLUMN_NAME");
            columns.add(columnName.toLowerCase());
            System.out.println("   - Found column: " + columnName);
        }
        rs.close();

        return columns;
    }

    private static void addMissingColumns(Connection conn, List<String> currentColumns) throws SQLException {
        System.out.println("🔧 Checking for missing columns...");

        // Required columns with their definitions
        String[][] requiredColumns = {
                {"user_id", "INT NOT NULL"},
                {"name", "VARCHAR(100) NOT NULL"},
                {"phone_number", "VARCHAR(20) NOT NULL"},
                {"email", "VARCHAR(100)"},
                {"relationship", "VARCHAR(50) NOT NULL"},
                {"is_primary", "BOOLEAN DEFAULT FALSE"},
                {"is_active", "BOOLEAN DEFAULT TRUE"},
                {"created_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP"},
                {"updated_at", "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP"}
        };

        for (String[] column : requiredColumns) {
            String columnName = column[0];
            String columnDefinition = column[1];

            if (!currentColumns.contains(columnName.toLowerCase())) {
                System.out.println("➕ Adding missing column: " + columnName);
                String addColumnSQL = "ALTER TABLE emergency_contacts ADD COLUMN " + columnName + " " + columnDefinition;

                try (Statement stmt = conn.createStatement()) {
                    stmt.execute(addColumnSQL);
                    System.out.println("✅ Added column: " + columnName);
                } catch (SQLException e) {
                    System.err.println("❌ Error adding column " + columnName + ": " + e.getMessage());
                    // Continue with other columns
                }
            }
        }
    }

    private static void renameColumnsIfNeeded(Connection conn, List<String> currentColumns) throws SQLException {
        System.out.println("🔧 Checking for columns that need renaming...");

        // Check for common column name variations
        if (currentColumns.contains("phone") && !currentColumns.contains("phone_number")) {
            System.out.println("🔄 Renaming 'phone' column to 'phone_number'...");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE emergency_contacts CHANGE COLUMN phone phone_number VARCHAR(20) NOT NULL");
                System.out.println("✅ Renamed 'phone' to 'phone_number'");
            } catch (SQLException e) {
                System.err.println("❌ Error renaming phone column: " + e.getMessage());
            }
        }

        if (currentColumns.contains("primary") && !currentColumns.contains("is_primary")) {
            System.out.println("🔄 Renaming 'primary' column to 'is_primary'...");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE emergency_contacts CHANGE COLUMN `primary` is_primary BOOLEAN DEFAULT FALSE");
                System.out.println("✅ Renamed 'primary' to 'is_primary'");
            } catch (SQLException e) {
                System.err.println("❌ Error renaming primary column: " + e.getMessage());
            }
        }

        if (currentColumns.contains("active") && !currentColumns.contains("is_active")) {
            System.out.println("🔄 Renaming 'active' column to 'is_active'...");
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE emergency_contacts CHANGE COLUMN active is_active BOOLEAN DEFAULT TRUE");
                System.out.println("✅ Renamed 'active' to 'is_active'");
            } catch (SQLException e) {
                System.err.println("❌ Error renaming active column: " + e.getMessage());
            }
        }
    }

    private static void verifyTableStructure(Connection conn) {
        System.out.println("🔍 Verifying final table structure...");

        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, "emergency_contacts", null);

            System.out.println("📋 Final table structure:");
            while (columns.next()) {
                String columnName = columns.getString("COLUMN_NAME");
                String dataType = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                String isNullable = columns.getString("IS_NULLABLE");
                String defaultValue = columns.getString("COLUMN_DEF");

                System.out.println(String.format("   ✓ %s %s(%d) %s %s",
                        columnName,
                        dataType,
                        columnSize,
                        isNullable.equals("YES") ? "NULL" : "NOT NULL",
                        defaultValue != null ? "DEFAULT " + defaultValue : ""
                ));
            }
            columns.close();

            // Test a simple query
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM emergency_contacts");
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("✅ Table verification successful. Current record count: " + count);
                }
                rs.close();
            }

        } catch (SQLException e) {
            System.err.println("❌ Error verifying table structure: " + e.getMessage());
        }
    }

    /**
     * Test the fixed table structure
     */
    public static void testTableStructure() {
        System.out.println("\n=== TESTING FIXED TABLE STRUCTURE ===");

        try {
            Connection conn = DBConnection.getConnection();

            // Test insert
            String insertSQL = """
                INSERT INTO emergency_contacts (user_id, name, phone_number, email, relationship, is_primary, is_active) 
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
                pstmt.setInt(1, 999); // Test user ID
                pstmt.setString(2, "Test Contact");
                pstmt.setString(3, "01700000999");
                pstmt.setString(4, "test@example.com");
                pstmt.setString(5, "Test Relationship");
                pstmt.setBoolean(6, false);
                pstmt.setBoolean(7, true);

                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("✅ Test insert successful!");

                    // Clean up test data
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute("DELETE FROM emergency_contacts WHERE user_id = 999");
                        System.out.println("✅ Test data cleaned up");
                    }
                } else {
                    System.err.println("❌ Test insert failed");
                }
            }

            conn.close();

        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}