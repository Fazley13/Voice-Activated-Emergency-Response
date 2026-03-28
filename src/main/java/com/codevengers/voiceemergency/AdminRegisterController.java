package com.codevengers.voiceemergency;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.application.Platform;
import java.sql.*;
import java.util.regex.Pattern;

public class AdminRegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField departmentField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField adminCodeField;
    @FXML private CheckBox termsCheckbox;
    @FXML private CheckBox responsibilityCheckbox;
    @FXML private Label statusLabel;
    @FXML private Button registerButton;

    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final String ADMIN_ACCESS_CODE = "VAER2024ADMIN";

    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String department = departmentField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String adminCode = adminCodeField.getText().trim();

        System.out.println("=== ADMIN REGISTRATION (FIXED) ===");
        System.out.println("Username: " + username);
        System.out.println("Email: " + email);
        System.out.println("Department: " + department);

        // Input validation
        if (username.isEmpty() || email.isEmpty() || department.isEmpty() || 
            password.isEmpty() || confirmPassword.isEmpty() || adminCode.isEmpty()) {
            showStatus("Please fill in all required fields", "error");
            return;
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showStatus("Please enter a valid email address", "error");
            return;
        }

        if (password.length() < 8) {
            showStatus("Password must be at least 8 characters long", "error");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showStatus("Passwords don't match", "error");
            return;
        }

        if (!adminCode.equals(ADMIN_ACCESS_CODE)) {
            showStatus("Invalid administrator access code. Use: VAER2024ADMIN", "error");
            return;
        }

        if (!termsCheckbox.isSelected() || !responsibilityCheckbox.isSelected()) {
            showStatus("Please accept all terms and responsibilities", "error");
            return;
        }

        registerButton.setDisable(true);
        registerButton.setText("Creating Admin Account...");
        showStatus("Creating administrator account, please wait...", "info");

        new Thread(() -> {
            try {
                boolean success = createAdminAccount(username, email, department, password);
                
                Platform.runLater(() -> {
                    if (success) {
                        showStatus("✅ SUCCESS! Admin account created for " + username + "!", "success");
                        clearFields();
                        
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Registration Successful");
                        alert.setHeaderText("Administrator Account Created!");
                        alert.setContentText("Welcome Administrator " + username + "!\n\nDepartment: " + department + "\n\nYour admin account has been created successfully.\nYou can now login with your credentials.");
                        alert.showAndWait();
                        
                        // Redirect to login after 2 seconds
                        new Thread(() -> {
                            try {
                                Thread.sleep(2000);
                                Platform.runLater(this::handleLoginRedirect);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    } else {
                        showStatus("❌ Failed to create admin account. Please try again.", "error");
                    }
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showStatus("❌ Registration error: " + e.getMessage(), "error");
                    e.printStackTrace();
                });
            } finally {
                Platform.runLater(() -> {
                    registerButton.setDisable(false);
                    registerButton.setText("Create Administrator Account");
                });
            }
        }).start();
    }

    private boolean createAdminAccount(String username, String email, String department, String password) {
        try {
            System.out.println("🔗 Connecting to database...");
            Connection conn = DBConnection.getConnection();
            System.out.println("✅ Connected to: " + conn.getMetaData().getURL());
            
            // Check if email already exists
            System.out.println("🔍 Checking for existing admin email...");
            String checkEmailSql = "SELECT COUNT(*) FROM admins WHERE email = ?";
            PreparedStatement checkEmailStmt = conn.prepareStatement(checkEmailSql);
            checkEmailStmt.setString(1, email);
            ResultSet emailRs = checkEmailStmt.executeQuery();
            
            if (emailRs.next() && emailRs.getInt(1) > 0) {
                System.err.println("❌ Admin email already exists: " + email);
                Platform.runLater(() -> showStatus("❌ Email already exists. Please use a different email.", "error"));
                emailRs.close();
                checkEmailStmt.close();
                conn.close();
                return false;
            }
            emailRs.close();
            checkEmailStmt.close();
            
            // Check if username already exists
            System.out.println("🔍 Checking for existing admin username...");
            String checkUserSql = "SELECT COUNT(*) FROM admins WHERE username = ?";
            PreparedStatement checkUserStmt = conn.prepareStatement(checkUserSql);
            checkUserStmt.setString(1, username);
            ResultSet userRs = checkUserStmt.executeQuery();
            
            if (userRs.next() && userRs.getInt(1) > 0) {
                System.err.println("❌ Admin username already exists: " + username);
                Platform.runLater(() -> showStatus("❌ Username already exists. Please choose a different username.", "error"));
                userRs.close();
                checkUserStmt.close();
                conn.close();
                return false;
            }
            userRs.close();
            checkUserStmt.close();
            
            // Insert new admin - USING EXACT SQL STRUCTURE
            System.out.println("💾 Inserting new admin with password_hash column...");
            String insertSql = "INSERT INTO admins (username, email, department, password_hash, status) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            insertStmt.setString(1, username);
            insertStmt.setString(2, email);
            insertStmt.setString(3, department);
            insertStmt.setString(4, password); // For now using plain password, you can hash it later
            insertStmt.setString(5, "active");
            
            System.out.println("🔧 Executing SQL: " + insertSql);
            System.out.println("📝 Parameters: [" + username + ", " + email + ", " + department + ", [password], active]");
            
            int rowsAffected = insertStmt.executeUpdate();
            insertStmt.close();
            
            System.out.println("📊 Rows affected: " + rowsAffected);
            
            if (rowsAffected > 0) {
                System.out.println("✅ Admin account created successfully!");
                
                // Verify the insertion
                String verifySql = "SELECT id, username, email, department, status FROM admins WHERE email = ?";
                PreparedStatement verifyStmt = conn.prepareStatement(verifySql);
                verifyStmt.setString(1, email);
                ResultSet verifyRs = verifyStmt.executeQuery();
                
                if (verifyRs.next()) {
                    System.out.println("✅ Admin verified in database:");
                    System.out.println("   ID: " + verifyRs.getInt("id"));
                    System.out.println("   Username: " + verifyRs.getString("username"));
                    System.out.println("   Email: " + verifyRs.getString("email"));
                    System.out.println("   Department: " + verifyRs.getString("department"));
                    System.out.println("   Status: " + verifyRs.getString("status"));
                } else {
                    System.err.println("❌ Admin not found after insertion!");
                }
                verifyRs.close();
                verifyStmt.close();
            } else {
                System.err.println("❌ No rows affected during insertion");
            }
            
            conn.close();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ SQL ERROR:");
            System.err.println("   Error Code: " + e.getErrorCode());
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Message: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ GENERAL ERROR: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    @FXML
    private void handleLoginRedirect() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/codevengers/voiceemergency/AdminLogin.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/codevengers/voiceemergency/Dashboard.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
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

    private void clearFields() {
        if (usernameField != null) usernameField.clear();
        if (emailField != null) emailField.clear();
        if (departmentField != null) departmentField.clear();
        if (passwordField != null) passwordField.clear();
        if (confirmPasswordField != null) confirmPasswordField.clear();
        if (adminCodeField != null) adminCodeField.clear();
        if (termsCheckbox != null) termsCheckbox.setSelected(false);
        if (responsibilityCheckbox != null) responsibilityCheckbox.setSelected(false);
        if (statusLabel != null) statusLabel.setText("");
    }
}
