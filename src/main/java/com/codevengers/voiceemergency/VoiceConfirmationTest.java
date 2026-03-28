package com.codevengers.voiceemergency;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class VoiceConfirmationTest extends Application {

    public static void main(String[] args) {
        System.out.println("=== VOICE CONFIRMATION POPUP TEST ===");

        // Test 1: Console-based test (without JavaFX)
        testVoiceDetectionLogic();

        // Test 2: Launch JavaFX for popup test
        System.out.println("\n🚀 Launching JavaFX for popup test...");
        launch(args);
    }

    /**
     * Test voice detection logic without GUI
     */
    private static void testVoiceDetectionLogic() {
        System.out.println("\n1. Testing Voice Detection Logic:");

        // Test emergency keywords
        String[] testKeywords = {
                "help me",
                "fire",
                "emergency",
                "attack",
                "hello world", // Non-emergency
                "robbery"
        };

        for (String keyword : testKeywords) {
            boolean isEmergency = SpeechRecognitionTest.isEmergencyKeyword(keyword);
            String emergencyType = SpeechRecognitionTest.determineEmergencyType(keyword);

            System.out.println("   Keyword: '" + keyword + "' -> Emergency: " + isEmergency +
                    (isEmergency ? " (Type: " + emergencyType + ")" : ""));
        }
    }

    @Override
    public void start(Stage primaryStage) {
        System.out.println("✅ JavaFX Application started");

        // Hide primary stage (we only want the popup)
        primaryStage.hide();

        // Test popup after 2 seconds
        Platform.runLater(() -> {
            new Thread(() -> {
                try {
                    Thread.sleep(2000);

                    Platform.runLater(() -> {
                        testConfirmationPopup();
                    });

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    /**
     * Test the confirmation popup
     */
    private void testConfirmationPopup() {
        System.out.println("\n2. Testing Voice Confirmation Popup:");

        // Simulate user session
        UserSession.login(1, "Test User", "test@example.com", "user");

        // Create test popup
        VoiceConfirmationPopup popup = new VoiceConfirmationPopup(
                "help me",
                "VOICE_EMERGENCY",
                new VoiceConfirmationPopup.EmergencyConfirmationCallback() {
                    @Override
                    public void onEmergencyConfirmed(String detectedKeyword, String emergencyType) {
                        System.out.println("✅ TEST RESULT: Emergency CONFIRMED");
                        System.out.println("   Keyword: " + detectedKeyword);
                        System.out.println("   Type: " + emergencyType);

                        // In real implementation, this would trigger emergency
                        System.out.println("🚨 Would trigger emergency procedures now...");

                        // Exit test after 3 seconds
                        Platform.runLater(() -> {
                            new Thread(() -> {
                                try {
                                    Thread.sleep(3000);
                                    Platform.exit();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        });
                    }

                    @Override
                    public void onEmergencyDismissed(String reason) {
                        System.out.println("🚫 TEST RESULT: Emergency DISMISSED");
                        System.out.println("   Reason: " + reason);

                        // Exit test after 2 seconds
                        Platform.runLater(() -> {
                            new Thread(() -> {
                                try {
                                    Thread.sleep(2000);
                                    Platform.exit();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }).start();
                        });
                    }
                }
        );

        // Show the popup
        popup.showConfirmationPopup();

        System.out.println("🎯 Test Instructions:");
        System.out.println("   • Wait 15 seconds = Emergency will auto-trigger");
        System.out.println("   • Double-click 'FALSE ALARM' = Emergency dismissed");
        System.out.println("   • Click 'YES - This IS an Emergency' = Emergency confirmed");
        System.out.println("   • Close window = Emergency dismissed");
    }
}