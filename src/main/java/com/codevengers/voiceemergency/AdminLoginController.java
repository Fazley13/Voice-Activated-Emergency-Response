package com.codevengers.voiceemergency;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AdminLoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    private void handleLogin() {
        if (AuthHandler.login(emailField.getText(), passwordField.getText(), true)) {
            statusLabel.setText("✅ Login successful!");
        } else {
            statusLabel.setText("❌ Login failed!");
        }
    }
}
