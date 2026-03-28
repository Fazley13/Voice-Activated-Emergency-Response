
package com.codevengers.voiceemergency;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class DebugController {
    
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox termsCheckbox;
    @FXML private Label statusLabel;
    @FXML private Button registerButton;
    
    @FXML
    private void initialize() {
        System.out.println("=== FXML INJECTION DEBUG ===");
        System.out.println("usernameField: " + (usernameField != null ? "✅ OK" : "❌ NULL"));
        System.out.println("emailField: " + (emailField != null ? "✅ OK" : "❌ NULL"));
        System.out.println("passwordField: " + (passwordField != null ? "✅ OK" : "❌ NULL"));
        System.out.println("confirmPasswordField: " + (confirmPasswordField != null ? "✅ OK" : "❌ NULL"));
        System.out.println("termsCheckbox: " + (termsCheckbox != null ? "✅ OK" : "❌ NULL"));
        System.out.println("statusLabel: " + (statusLabel != null ? "✅ OK" : "❌ NULL"));
        System.out.println("registerButton: " + (registerButton != null ? "✅ OK" : "❌ NULL"));
        System.out.println("=============================");
    }
    
    @FXML
    private void handleRegister() {
        if (statusLabel != null) {
            statusLabel.setText("✅ FXML injection working! Button clicked successfully!");
            statusLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        }
        System.out.println("✅ Register button clicked - FXML working!");
    }
}
