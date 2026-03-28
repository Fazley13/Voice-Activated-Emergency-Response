package com.codevengers.voiceemergency;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.sql.*;

public class UserLoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberMeCheckbox;
    @FXML private Label statusLabel;
    @FXML private Button loginButton;

    @FXML
    private void initialize() {
        // Clear any existing session
        UserSession.logout();

        // Test database connection on controller load
        try {
            if (DBConnection.testConnection()) {
                System.out.println("✅ Database connection ready for user login");
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

        System.out.println("=== USER LOGIN ATTEMPT ===");
        System.out.println("Email: " + email);
        System.out.println("Password length: " + password.length());

        if (email.isEmpty() || password.isEmpty()) {
            showStatus("Please fill in all fields", "error");
            return;
        }

        loginButton.setDisable(true);
        loginButton.setText("Logging in...");
        showStatus("Authenticating user...", "info");

        // Perform authentication in background thread
        new Thread(() -> {
            try {
                UserInfo user = authenticateUser(email, password);

                Platform.runLater(() -> {
                    if (user != null) {
                        // ✅ SET USER SESSION PROPERLY
                        UserSession.login(user.id, user.username, user.email, "user");

                        // ✅ VERIFY SESSION WAS SET
                        if (UserSession.isLoggedIn()) {
                            System.out.println("✅ UserSession verified after login:");
                            UserSession.printSessionInfo();

                            showStatus("✅ Login successful! Welcome " + user.username, "success");

                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("Login Successful");
                            alert.setHeaderText("Welcome Back!");
                            alert.setContentText("Hello " + user.username + "!\nLogin successful.\n\nRedirecting to user dashboard...");
                            alert.showAndWait();

                            // Redirect after delay
                            new Thread(() -> {
                                try {
                                    Thread.sleep(1000);
                                    Platform.runLater(this::navigateToUserDashboard);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        } else {
                            System.err.println("❌ Session validation failed after login!");
                            showStatus("Session error. Please try again.", "error");
                        }

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
                    loginButton.setText("Login");
                });
            }
        }).start();
    }

    private UserInfo authenticateUser(String email, String password) {
        try {
            System.out.println("🔗 Connecting to database for authentication...");
            Connection conn = DBConnection.getConnection();
            System.out.println("✅ Connected to: " + conn.getMetaData().getURL());

            // Query using the EXACT column names from your SQL structure
            System.out.println("🔍 Searching for user with email: " + email);
            String sql = "SELECT id, username, email, password_hash, status FROM users WHERE email = ? AND status = 'active'";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, email);

            System.out.println("🔧 Executing SQL: " + sql);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPasswordHash = rs.getString("password_hash");
                String username = rs.getString("username");
                int userId = rs.getInt("id");
                String status = rs.getString("status");

                System.out.println("✅ User found in database:");
                System.out.println("   ID: " + userId);
                System.out.println("   Username: " + username);
                System.out.println("   Email: " + email);
                System.out.println("   Status: " + status);

                // For now, doing simple password comparison
                if (password.equals(storedPasswordHash)) {
                    System.out.println("✅ Password matches!");

                    // Update last login
                    updateLastLogin(conn, userId);

                    rs.close();
                    stmt.close();
                    conn.close();

                    return new UserInfo(userId, username, email, status);
                } else {
                    System.err.println("❌ Password does not match!");
                }
            } else {
                System.err.println("❌ No user found with email: " + email);
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            System.err.println("❌ SQL ERROR during authentication:");
            System.err.println("   Error Code: " + e.getErrorCode());
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ GENERAL ERROR during authentication: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private void updateLastLogin(Connection conn, int userId) {
        try {
            String updateSql = "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setInt(1, userId);
            updateStmt.executeUpdate();
            updateStmt.close();
            System.out.println("✅ Last login updated for user ID: " + userId);
        } catch (SQLException e) {
            System.err.println("❌ Error updating last login: " + e.getMessage());
        }
    }

    @FXML
    private void handleForgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Password Recovery");
        alert.setHeaderText("Forgot Your Password?");
        alert.setContentText("Please contact support at support@quickrescue-system.com for password recovery assistance.\n\nFor security reasons, password resets require manual verification.");
        alert.showAndWait();
    }

    @FXML
    private void handleRegisterRedirect() {
        navigateToPage("UserRegister.fxml", "User Registration");
    }

    @FXML
    private void handleBackToDashboard() {
        navigateToPage("/com/codevengers/voiceemergency/Dashboard.fxml", "Voice Activated Emergency Response - Dashboard");
    }

    // ✅ FIXED: Navigation to User Dashboard with proper error handling
    private void navigateToUserDashboard() {
        try {
            System.out.println("🔄 Navigating to User Dashboard...");

            // ✅ DOUBLE CHECK SESSION BEFORE NAVIGATION
            if (!UserSession.isLoggedIn()) {
                System.err.println("❌ UserSession not valid! Cannot navigate to dashboard.");
                UserSession.printSessionInfo();
                showStatus("Session error. Please try logging in again.", "error");
                return;
            }

            System.out.println("✅ Session valid, proceeding with navigation...");
            UserSession.printSessionInfo();

            Parent root = FXMLLoader.load(getClass().getResource("/com/codevengers/voiceemergency/UserDashboard.fxml"));
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("QuickRescue - User Dashboard");

            System.out.println("✅ Successfully navigated to User Dashboard");

        } catch (Exception e) {
            System.err.println("❌ Error navigating to User Dashboard: " + e.getMessage());
            e.printStackTrace();
            showStatus("Navigation error: " + e.getMessage(), "error");

            // Show detailed error to user
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Navigation Error");
            errorAlert.setHeaderText("Could not open User Dashboard");
            errorAlert.setContentText("Error: " + e.getMessage() + "\n\nPlease check that UserDashboard.fxml exists in the correct location.");
            errorAlert.showAndWait();
        }
    }

    private void navigateToPage(String fxmlFile, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) emailField.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle(title);

        } catch (Exception e) {
            e.printStackTrace();
            showStatus("Navigation error: " + e.getMessage(), "error");
        }
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

    // Helper class for user information
    private static class UserInfo {
        int id;
        String username;
        String email;
        String status;

        UserInfo(int id, String username, String email, String status) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.status = status;
        }
    }
}
