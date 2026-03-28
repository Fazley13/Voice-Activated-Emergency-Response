package com.codevengers.voiceemergency;
import java.sql.*;

public class LoginDebugger {
    
    public static void main(String[] args) {
        System.out.println("=== LOGIN DEBUG UTILITY ===");
        
        // Test with your actual credentials
        System.out.println("Enter the email and password you're trying to login with:");
        
        // For testing, you can hardcode your credentials here temporarily
        String testEmail = "user@mail.com"; // Replace with your actual email
        String testPassword = "yourpassword"; // Replace with your actual password
        
        debugUserLogin(testEmail, testPassword);
        
        // If you have admin credentials, test those too
        // debugAdminLogin("admin@mail.com", "adminpassword");
    }
    
    public static void debugUserLogin(String email, String password) {
        System.out.println("\n🔍 DEBUGGING USER LOGIN");
        System.out.println("Email: " + email);
        System.out.println("Password: " + password);
        
        try {
            Connection conn = DBConnection.getConnection();
            System.out.println("✅ Database connected");
            
            // First, let's see what users exist
            System.out.println("\n📋 ALL USERS IN DATABASE:");
            Statement stmt = conn.createStatement();
            ResultSet allUsers = stmt.executeQuery("SELECT id, username, email, password_hash, status FROM users");
            
            while (allUsers.next()) {
                System.out.println("   ID: " + allUsers.getInt("id") + 
                                 " | Username: " + allUsers.getString("username") + 
                                 " | Email: " + allUsers.getString("email") + 
                                 " | Password: " + allUsers.getString("password_hash") + 
                                 " | Status: " + allUsers.getString("status"));
            }
            allUsers.close();
            stmt.close();
            
            // Now test the specific login
            System.out.println("\n🔍 TESTING LOGIN FOR: " + email);
            String sql = "SELECT id, username, email, password_hash, status FROM users WHERE email = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password_hash");
                String username = rs.getString("username");
                String status = rs.getString("status");
                
                System.out.println("✅ User found:");
                System.out.println("   Username: " + username);
                System.out.println("   Email: " + email);
                System.out.println("   Status: " + status);
                System.out.println("   Stored password: " + storedPassword);
                System.out.println("   Entered password: " + password);
                System.out.println("   Passwords match: " + password.equals(storedPassword));
                
                if (!"active".equals(status)) {
                    System.err.println("❌ User status is not 'active': " + status);
                }
                
            } else {
                System.err.println("❌ No user found with email: " + email);
            }
            
            rs.close();
            pstmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.err.println("❌ Debug error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void debugAdminLogin(String email, String password) {
        System.out.println("\n🔍 DEBUGGING ADMIN LOGIN");
        System.out.println("Email: " + email);
        System.out.println("Password: " + password);
        
        try {
            Connection conn = DBConnection.getConnection();
            System.out.println("✅ Database connected");
            
            // First, let's see what admins exist
            System.out.println("\n📋 ALL ADMINS IN DATABASE:");
            Statement stmt = conn.createStatement();
            ResultSet allAdmins = stmt.executeQuery("SELECT id, username, email, department, password_hash, status FROM admins");
            
            while (allAdmins.next()) {
                System.out.println("   ID: " + allAdmins.getInt("id") + 
                                 " | Username: " + allAdmins.getString("username") + 
                                 " | Email: " + allAdmins.getString("email") + 
                                 " | Department: " + allAdmins.getString("department") + 
                                 " | Password: " + allAdmins.getString("password_hash") + 
                                 " | Status: " + allAdmins.getString("status"));
            }
            allAdmins.close();
            stmt.close();
            
            // Now test the specific login
            System.out.println("\n🔍 TESTING ADMIN LOGIN FOR: " + email);
            String sql = "SELECT id, username, email, department, password_hash, status FROM admins WHERE email = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password_hash");
                String username = rs.getString("username");
                String department = rs.getString("department");
                String status = rs.getString("status");
                
                System.out.println("✅ Admin found:");
                System.out.println("   Username: " + username);
                System.out.println("   Email: " + email);
                System.out.println("   Department: " + department);
                System.out.println("   Status: " + status);
                System.out.println("   Stored password: " + storedPassword);
                System.out.println("   Entered password: " + password);
                System.out.println("   Passwords match: " + password.equals(storedPassword));
                
                if (!"active".equals(status)) {
                    System.err.println("❌ Admin status is not 'active': " + status);
                }
                
            } else {
                System.err.println("❌ No admin found with email: " + email);
            }
            
            rs.close();
            pstmt.close();
            conn.close();
            
        } catch (Exception e) {
            System.err.println("❌ Debug error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
