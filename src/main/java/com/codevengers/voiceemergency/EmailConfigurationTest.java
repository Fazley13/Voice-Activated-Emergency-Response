package com.codevengers.voiceemergency;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;

public class EmailConfigurationTest {

    /**
     * Test and setup email configuration
     */
    public static void main(String[] args) {
        System.out.println("=== EMAIL CONFIGURATION TEST ===");

        // Test database connection first
        if (!EmergencyContactDAO.testDatabaseConnection()) {
            System.err.println("❌ Database connection failed - cannot test email system");
            return;
        }

        // Initialize emergency contacts table
        EmergencyContactDAO.initializeEmergencyContactTable();

        // Test email configuration
        testEmailConfiguration();
    }

    public static void testEmailConfiguration() {
        System.out.println("📧 Testing email configuration...");

        try {
            // Try to send a test email
            boolean success = EmailService.sendTestEmail("test@example.com", "Test User");

            if (success) {
                System.out.println("✅ Email configuration test successful!");
                showSuccessMessage();
            } else {
                System.err.println("❌ Email configuration test failed!");
                showConfigurationInstructions();
            }

        } catch (Exception e) {
            System.err.println("❌ Email configuration error: " + e.getMessage());
            e.printStackTrace();
            showConfigurationError(e.getMessage());
        }
    }

    private static void showSuccessMessage() {
        System.out.println("""
            ✅ EMAIL CONFIGURATION SUCCESSFUL!
            
            Your email system is properly configured and ready to send emergency notifications.
            
            When a user triggers an emergency:
            1. Location will be automatically fetched
            2. Emergency contacts will receive detailed email notifications
            3. Emails will include Google Maps links to the location
            4. Professional HTML emails with emergency details
            5. Resolution notifications when emergency is resolved
            """);
    }

    private static void showConfigurationInstructions() {
        System.out.println("""
            ❌ EMAIL CONFIGURATION NEEDED
            
            To enable emergency email notifications, please configure EmailService.java:
            
            📧 GMAIL SETUP (Recommended):
            1. Go to your Google Account settings
            2. Enable 2-Factor Authentication
            3. Generate an App Password:
               - Go to Security → 2-Step Verification → App passwords
               - Select "Mail" and generate password
            4. Update EmailService.java lines 15-18:
               - EMAIL_USERNAME = "your-email@gmail.com"
               - EMAIL_PASSWORD = "your-16-digit-app-password"
            
            🔧 OTHER PROVIDERS:
            - Outlook: smtp.outlook.com, port 587
            - Yahoo: smtp.mail.yahoo.com, port 587
            
            ⚠️ SECURITY:
            - Never use your regular email password
            - Always use App Passwords
            - Keep credentials secure
            """);
    }

    private static void showConfigurationError(String error) {
        System.out.println("❌ EMAIL CONFIGURATION ERROR: " + error);
        System.out.println("""
            
            Common issues and solutions:
            
            1. Authentication failed:
               - Check EMAIL_USERNAME and EMAIL_PASSWORD
               - Use App Password, not regular password
               - Enable 2-Factor Authentication first
            
            2. Connection timeout:
               - Check internet connection
               - Verify SMTP_HOST and SMTP_PORT
               - Check firewall settings
            
            3. SSL/TLS errors:
               - Update Java to latest version
               - Check TLS version compatibility
            
            4. Gmail specific:
               - Enable "Less secure app access" (not recommended)
               - Use App Passwords instead (recommended)
            """);
    }

    /**
     * Show configuration dialog in JavaFX application
     */
    public static void showConfigurationDialog() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Email Configuration Required");
            alert.setHeaderText("Setup Emergency Email Notifications");

            String instructions = """
                To enable emergency email notifications with location sharing:
                
                📧 QUICK SETUP FOR GMAIL:
                1. Enable 2-Factor Authentication on your Google account
                2. Generate App Password: Security → App passwords → Mail
                3. Edit src/main/java/EmailService.java:
                   - Line 15: EMAIL_USERNAME = "your-email@gmail.com"
                   - Line 16: EMAIL_PASSWORD = "your-app-password"
                
                🚨 EMERGENCY EMAIL FEATURES:
                ✅ Automatic location sharing with Google Maps links
                ✅ Professional HTML email templates
                ✅ Emergency contact relationship identification
                ✅ Emergency service numbers included
                ✅ Resolution notifications when emergency ends
                
                📱 WHEN EMERGENCY IS TRIGGERED:
                • Location automatically fetched and shared
                • All emergency contacts receive detailed emails
                • Clickable Google Maps links for exact location
                • Emergency type, time, and ID included
                • Action steps for emergency contacts
                """;

            alert.setContentText(instructions);
            alert.setResizable(true);
            alert.getDialogPane().setPrefWidth(700);
            alert.getDialogPane().setPrefHeight(500);

            // Add code example
            TextArea codeExample = new TextArea();
            codeExample.setText("""
                // EmailService.java configuration:
                private static final String EMAIL_USERNAME = "your-emergency-app@gmail.com";
                private static final String EMAIL_PASSWORD = "abcd efgh ijkl mnop"; // 16-digit app password
                
                // Gmail App Password steps:
                // 1. myaccount.google.com → Security
                // 2. 2-Step Verification → App passwords
                // 3. Select "Mail" → Generate
                // 4. Copy 16-digit password (remove spaces)
                """);
            codeExample.setEditable(false);
            codeExample.setWrapText(true);
            codeExample.setPrefRowCount(8);

            alert.getDialogPane().setExpandableContent(codeExample);

            // Add test button
            ButtonType testButton = new ButtonType("Test Email Config");
            alert.getButtonTypes().add(testButton);

            alert.showAndWait().ifPresent(response -> {
                if (response == testButton) {
                    testEmailConfiguration();
                }
            });
        });
    }
}