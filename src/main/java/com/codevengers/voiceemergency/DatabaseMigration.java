package com.codevengers.voiceemergency;

import java.sql.*;

public class DatabaseMigration {
    
    public static void main(String[] args) {
        System.out.println("=== DATABASE MIGRATION UTILITY ===");
        migrateDatabase();
    }
    
    public static void migrateDatabase() {
        try {
            Connection conn = DBConnection.getConnection();
            System.out.println("✅ Connected to: " + conn.getMetaData().getURL());
            
            // Check existing table structure
            checkTableStructure(conn, "users");
            checkTableStructure(conn, "admins");
            
            // Add missing columns if needed
            addMissingColumns(conn);
            
            conn.close();
            System.out.println("✅ Database migration completed!");
            
        } catch (Exception e) {
            System.err.println("❌ Migration error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void checkTableStructure(Connection conn, String tableName) {
        try {
            System.out.println("\n🔍 Checking " + tableName + " table structure:");
            
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, null);
            
            boolean tableExists = false;
            while (columns.next()) {
                tableExists = true;
                String columnName = columns.getString("COLUMN_NAME");
                String dataType = columns.getString("TYPE_NAME");
                int columnSize = columns.getInt("COLUMN_SIZE");
                String isNullable = columns.getString("IS_NULLABLE");
                
                System.out.println("   - " + columnName + " " + dataType + "(" + columnSize + ") " + 
                                 (isNullable.equals("YES") ? "NULL" : "NOT NULL"));
            }
            
            if (!tableExists) {
                System.out.println("   ❌ Table " + tableName + " does not exist");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error checking " + tableName + " structure: " + e.getMessage());
        }
    }
    
    private static void addMissingColumns(Connection conn) {
        try {
            // Check if status column exists in users table
            if (!columnExists(conn, "users", "status")) {
                System.out.println("🔧 Adding 'status' column to users table...");
                String addStatusSql = "ALTER TABLE users ADD COLUMN status VARCHAR(20) DEFAULT 'active'";
                conn.createStatement().execute(addStatusSql);
                System.out.println("✅ Added 'status' column to users table");
            } else {
                System.out.println("✅ 'status' column already exists in users table");
            }
            
            // Check if status column exists in admins table
            if (!columnExists(conn, "admins", "status")) {
                System.out.println("🔧 Adding 'status' column to admins table...");
                String addStatusSql = "ALTER TABLE admins ADD COLUMN status VARCHAR(20) DEFAULT 'active'";
                conn.createStatement().execute(addStatusSql);
                System.out.println("✅ Added 'status' column to admins table");
            } else {
                System.out.println("✅ 'status' column already exists in admins table");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error adding missing columns: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static boolean columnExists(Connection conn, String tableName, String columnName) {
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet columns = metaData.getColumns(null, null, tableName, columnName);
            return columns.next();
        } catch (SQLException e) {
            return false;
        }
    }
}
