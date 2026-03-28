package com.codevengers.voiceemergency;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

public class EmailConfigurationHelper {

    /**
     * Show email configuration instructions to the user
     */
    public static void showEmailSetupInstructions() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Email Configuration Required");
            alert.setHeaderText("Setup Email Notifications");

            String instructions = """
                To enable emergency email notifications, you need to configure the email settings:
                
                📧 GMAIL SETUP (Recommended):
                1. Go to your Google Account settings
                2. Enable 2-Factor Authentication
                3. Generate an App Password:
                   - Go to Security → 2-Step Verification → App passwords
                   - Select "Mail" and generate password
                4. Update EmailService.java with your credentials:
                   - EMAIL_USERNAME = "your-email@gmail.com"
                   - EMAIL_PASSWORD = "your-app-password"
                
                🔧 OTHER EMAIL PROVIDERS:
                - Outlook: smtp.outlook.com, port 587
                - Yahoo: smtp.mail.yahoo.com, port 587
                - Custom SMTP: Update SMTP_HOST and SMTP_PORT
                
                ⚠️ SECURITY NOTES:
                - Never use your regular email password
                - Always use App Passwords or OAuth2
                - Keep credentials secure
                
                📁 FILE TO EDIT:
                src/main/java/EmailService.java
                Lines 15-18 (EMAIL_USERNAME and EMAIL_PASSWORD)
                """;

            alert.setContentText(instructions);

            // Make the dialog larger for better readability
            alert.setResizable(true);
            alert.getDialogPane().setPrefWidth(600);
            alert.getDialogPane().setPrefHeight(500);

            // Add expandable content with code example
            TextArea codeExample = new TextArea();
            codeExample.setText("""
                // Example configuration in EmailService.java:
                private static final String EMAIL_USERNAME = "your-emergency-app@gmail.com";
                private static final String EMAIL_PASSWORD = "your-16-digit-app-password";
                private static final String FROM_EMAIL = "QuickRescue Emergency <your-emergency-app@gmail.com>";
                
                // For Gmail App Password:
                // 1. Go to myaccount.google.com
                // 2. Security → 2-Step Verification → App passwords
                // 3. Generate password for "Mail"
                // 4. Use the 16-digit password (no spaces)
                """);
            codeExample.setEditable(false);
            codeExample.setWrapText(true);
            codeExample.setPrefRowCount(10);

            alert.getDialogPane().setExpandableContent(codeExample);
            alert.getDialogPane().setExpanded(true);

            alert.showAndWait();
        });
    }

    /**
     * Test email configuration
     */
    public static void testEmailConfiguration() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Testing Email Configuration");
            alert.setHeaderText("Email Test");
            alert.setContentText("Testing email configuration...\n\nCheck the console for results.");
            alert.show();
        });

        // Test in background thread
        new Thread(() -> {
            try {
                boolean success = EmailService.sendTestEmail("test@example.com", "Test User");

                Platform.runLater(() -> {
                    Alert resultAlert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.ERROR);
                    resultAlert.setTitle("Email Test Result");
                    resultAlert.setHeaderText(success ? "Email Test Successful" : "Email Test Failed");
                    resultAlert.setContentText(success ?
                            "Email configuration is working correctly!" :
                            "Email configuration failed. Please check your settings and internet connection.");
                    resultAlert.show();
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Email Test Error");
                    errorAlert.setHeaderText("Configuration Error");
                    errorAlert.setContentText("Error testing email: " + e.getMessage());
                    errorAlert.show();
                });
            }
        }).start();
    }
}