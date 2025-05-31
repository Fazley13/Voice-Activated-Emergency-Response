package com.codevengers.voiceemergency;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AdminRegisterController {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label statusLabel;

    @FXML
    private void handleRegister() {
        if (AuthHandler.register(usernameField.getText(), emailField.getText(), passwordField.getText(), true)) {
            statusLabel.setText("✅ Registration successful!");
        } else {
            statusLabel.setText("❌ Registration failed.");
        }
    }
}
