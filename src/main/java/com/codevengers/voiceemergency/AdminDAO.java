package com.codevengers.voiceemergency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AdminDAO {
    
    public static class Admin {
        private int id;
        private String username;
        private String email;
        private String department;
        private String status;
        private Timestamp createdAt;
        private Timestamp lastLogin;
        
        // Constructors
        public Admin() {}
        
        public Admin(int id, String username, String email, String department, String status, Timestamp createdAt) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.department = department;
            this.status = status;
            this.createdAt = createdAt;
        }
        
        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
        
        public Timestamp getLastLogin() { return lastLogin; }
        public void setLastLogin(Timestamp lastLogin) { this.lastLogin = lastLogin; }
    }
    
    /**
     * Register a new admin
     */
    public static boolean registerAdmin(String username, String email, String department, String password) {
        String sql = "INSERT INTO admins (username, email, department, password_hash) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, department);
            stmt.setString(4, PasswordUtil.hashPassword(password));
            
            return stmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error registering admin: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Authenticate admin login
     */
    public static Admin authenticateAdmin(String email, String password) {
        String sql = "SELECT id, username, email, department, password_hash, status, created_at FROM admins WHERE email = ? AND status = 'active'";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                if (PasswordUtil.verifyPassword(password, storedHash)) {
                    // Update last login
                    updateLastLogin(rs.getInt("id"));
                    
                    return new Admin(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("email"),
                        rs.getString("department"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                    );
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error authenticating admin: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Check if admin email exists
     */
    public static boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM admins WHERE email = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking admin email existence: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Check if admin username exists
     */
    public static boolean usernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM admins WHERE username = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking admin username existence: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Update admin's last login timestamp
     */
    private static void updateLastLogin(int adminId) {
        String sql = "UPDATE admins SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = MySQLConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, adminId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("Error updating admin last login: " + e.getMessage());
        }
    }
}
