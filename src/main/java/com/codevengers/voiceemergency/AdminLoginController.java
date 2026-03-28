package com.codevengers.voiceemergency;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.sql.*;

public class AdminLoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Label statusLabel;
    @FXML private Button loginButton;

    @FXML
    private void initialize() {
        // Clear any existing session
        AdminSession.logout();

        // Test database connection on controller load
        try {
            if (DBConnection.testConnection()) {
                System.out.println("✅ Database connection ready for admin login");
            } else {
                showStatus("Database connection failed. Please ensure XAMPP is running.", "error");
            }
        } catch (Exception e) {
            showStatus("Database connection error: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        System.out.println("=== ADMIN LOGIN ATTEMPT (FIXED) ===");
        System.out.println("Email: " + email);
        System.out.println("Password length: " + password.length());

        if (email.isEmpty() || password.isEmpty()) {
            showStatus("Please enter both email and password", "error");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Signing In...");
        showStatus("Verifying administrator credentials...", "info");

        new Thread(() -> {
            try {
                AdminInfo admin = authenticateAdmin(email, password);

                Platform.runLater(() -> {
                    if (admin != null) {
                        // ✅ SET ADMIN SESSION - This was missing!
                        AdminSession.setCurrentAdmin(admin.id, admin.username, admin.email, admin.department);

                        System.out.println("✅ AdminSession set:");
                        System.out.println("   Admin ID: " + AdminSession.getCurrentAdminId());
                        System.out.println("   Username: " + AdminSession.getCurrentAdminUsername());
                        System.out.println("   Department: " + AdminSession.getCurrentAdminDepartment());

                        showStatus("✅ Login successful! Welcome Administrator!", "success");

                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Login Successful");
                        alert.setHeaderText("Welcome Administrator!");
                        alert.setContentText("Hello " + admin.username + "!\nDepartment: " + admin.department + "\n\nRedirecting to Admin Dashboard...");
                        alert.showAndWait();

                        // Redirect after delay
                        new Thread(() -> {
                            try {
                                Thread.sleep(1500);
                                Platform.runLater(this::navigateToAdminDashboard);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();

                    } else {
                        showStatus("❌ Invalid email or password. Please check your credentials.", "error");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    showStatus("❌ Login error: " + e.getMessage(), "error");
                    System.err.println("Login exception: " + e.getMessage());
                    e.printStackTrace();
                });
            } finally {
                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    loginButton.setText("Sign In to Admin Portal");
                });
            }
        }).start();
    }

    private AdminInfo authenticateAdmin(String email, String password) {
        try {
            System.out.println("🔗 Connecting to database for admin authentication...");
            Connection conn = DBConnection.getConnection();
            System.out.println("✅ Connected to: " + conn.getMetaData().getURL());

            // Query using the EXACT column names from your SQL structure
            System.out.println("🔍 Searching for admin with email: " + email);
            String sql = "SELECT id, username, email, department, status FROM admins WHERE email = ? AND password_hash = ? AND status = 'active'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2, password); // In production, hash the password before comparing

            System.out.println("🔧 Executing SQL: " + sql);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String username = rs.getString("username");
                String department = rs.getString("department");
                int adminId = rs.getInt("id");
                String status = rs.getString("status");

                System.out.println("✅ Admin found in database:");
                System.out.println("   ID: " + adminId);
                System.out.println("   Username: " + username);
                System.out.println("   Email: " + email);
                System.out.println("   Department: " + department);
                System.out.println("   Status: " + status);

                rs.close();
                stmt.close();
                conn.close();

                return new AdminInfo(adminId, username, email, department, status);
            } else {
                System.err.println("❌ No admin found with provided credentials");

                // Check if admin exists but with different status
                String checkSql = "SELECT status FROM admins WHERE email = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setString(1, email);
                ResultSet checkRs = checkStmt.executeQuery();

                if (checkRs.next()) {
                    String adminStatus = checkRs.getString("status");
                    System.err.println("   Admin exists but status is: " + adminStatus);
                } else {
                    System.err.println("   Admin does not exist in database");
                }
                checkRs.close();
                checkStmt.close();
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            System.err.println("❌ SQL ERROR during admin authentication:");
            System.err.println("   Error Code: " + e.getErrorCode());
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ GENERAL ERROR during admin authentication: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    // ✅ FIXED: Navigation to Admin Dashboard with proper error handling
    private void navigateToAdminDashboard() {
        try {
            System.out.println("🔄 Navigating to Admin Dashboard...");
            System.out.println("   Current AdminSession: " + AdminSession.isLoggedIn());

            if (!AdminSession.isLoggedIn()) {
                System.err.println("❌ AdminSession not set! Cannot navigate to dashboard.");
                showStatus("Session error. Please try logging in again.", "error");
                return;
            }

            Parent root = FXMLLoader.load(getClass().getResource("/com/codevengers/voiceemergency/AdminDashboard.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("QuickRescue - Admin Dashboard");

            System.out.println("✅ Successfully navigated to Admin Dashboard");

        } catch (Exception e) {
            System.err.println("❌ Error navigating to Admin Dashboard: " + e.getMessage());
            e.printStackTrace();
            showStatus("Navigation error: " + e.getMessage(), "error");

            // Show detailed error to user
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Navigation Error");
            errorAlert.setHeaderText("Could not open Admin Dashboard");
            errorAlert.setContentText("Error: " + e.getMessage() + "\n\nPlease check that AdminDashboard.fxml exists in the correct location.");
            errorAlert.showAndWait();
        }
    }

    @FXML
    private void handleRegisterRedirect() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/codevengers/voiceemergency/AdminRegister.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Registration");
        } catch (Exception e) {
            e.printStackTrace();
            showStatus("Navigation error: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/codevengers/voiceemergency/Dashboard.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("QuickRescue - Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
            showStatus("Navigation error: " + e.getMessage(), "error");
        }
    }

    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password Recovery");
        alert.setHeaderText("Forgot Password?");
        alert.setContentText("Please contact your system administrator to reset your password.\n\nFor security reasons, admin passwords can only be reset by authorized personnel.");
        alert.showAndWait();
    }

    private void showStatus(String message, String type) {
        if (statusLabel != null) {
            statusLabel.setText(message);
            statusLabel.setWrapText(true);
            switch (type) {
                case "error":
                    statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    break;
                case "success":
                    statusLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    break;
                case "info":
                    statusLabel.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                    break;
            }
        }
        System.out.println("UI Status: " + message);
    }

    // Helper class for admin information
    private static class AdminInfo {
        int id;
        String username;
        String email;
        String department;
        String status;

        AdminInfo(int id, String username, String email, String department, String status) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.department = department;
            this.status = status;
        }
    }
}
