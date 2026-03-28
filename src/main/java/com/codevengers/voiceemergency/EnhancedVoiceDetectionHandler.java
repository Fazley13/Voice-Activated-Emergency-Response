package com.codevengers.voiceemergency;

import javafx.application.Platform;

public class EnhancedVoiceDetectionHandler {

    private static volatile boolean isVoiceDetectionActive = false;
    private static VoiceConfirmationPopup currentPopup = null;

    /**
     * Handle voice detection with confirmation popup
     */
    public static void handleVoiceDetection(String detectedCommand) {
        if (!isVoiceDetectionActive) {
            System.out.println("🎤 Voice detection is not active, ignoring command: " + detectedCommand);
            return;
        }

        if (EnhancedEmergencyHandler.isEmergencyTriggered()) {
            System.out.println("⚠️ Emergency already active, ignoring voice command: " + detectedCommand);
            return;
        }

        System.out.println("👂 Processing voice command: " + detectedCommand);

        // Check if it's an emergency keyword
        if (SpeechRecognitionTest.isEmergencyKeyword(detectedCommand)) {
            System.out.println("🚨 Emergency keyword detected: " + detectedCommand);

            // Close any existing popup first
            if (currentPopup != null) {
                currentPopup.forceClose();
                currentPopup = null;
            }

            // Determine emergency type
            String emergencyType = SpeechRecognitionTest.determineEmergencyType(detectedCommand);

            // Show confirmation popup
            showVoiceConfirmationPopup(detectedCommand, emergencyType);

        } else {
            System.out.println("ℹ️ Non-emergency voice command: " + detectedCommand);
        }
    }

    /**
     * Show voice confirmation popup with emergency callback
     */
    private static void showVoiceConfirmationPopup(String detectedKeyword, String emergencyType) {
        try {
            System.out.println("🚨 Showing voice confirmation popup for: " + detectedKeyword);

            // Create confirmation popup with callback
            currentPopup = new VoiceConfirmationPopup(
                    detectedKeyword,
                    emergencyType,
                    new VoiceConfirmationPopup.EmergencyConfirmationCallback() {

                        @Override
                        public void onEmergencyConfirmed(String detectedKeyword, String emergencyType) {
                            System.out.println("✅ Emergency CONFIRMED via voice detection");
                            System.out.println("   Detected: " + detectedKeyword);
                            System.out.println("   Type: " + emergencyType);

                            // Trigger emergency using enhanced handler
                            String description = "Voice-activated emergency confirmed by user: '" + detectedKeyword + "'";
                            EnhancedEmergencyHandler.triggerEmergency("voice_confirmed", emergencyType, description);

                            // Show confirmation to user if in JavaFX context
                            Platform.runLater(() -> {
                                try {
                                    javafx.scene.control.Alert confirmAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                                    confirmAlert.setTitle("🚨 Emergency Activated");
                                    confirmAlert.setHeaderText("Voice Emergency Confirmed");
                                    confirmAlert.setContentText(
                                            "Emergency procedures have been activated!\n\n" +
                                                    "• Emergency contacts notified\n" +
                                                    "• Audio recording started\n" +
                                                    "• Location shared\n" +
                                                    "• Administrators alerted\n\n" +
                                                    "Help is on the way!"
                                    );
                                    confirmAlert.show();
                                } catch (Exception e) {
                                    System.err.println("❌ Could not show confirmation alert: " + e.getMessage());
                                }
                            });

                            currentPopup = null;
                        }

                        @Override
                        public void onEmergencyDismissed(String reason) {
                            System.out.println("🚫 Emergency DISMISSED via voice detection");
                            System.out.println("   Reason: " + reason);
                            System.out.println("   Detected keyword: " + detectedKeyword);

                            // Log false alarm
                            logFalseAlarm(detectedKeyword, reason);

                            // Show dismissal confirmation to user if in JavaFX context
                            Platform.runLater(() -> {
                                try {
                                    javafx.scene.control.Alert dismissAlert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
                                    dismissAlert.setTitle("🚫 False Alarm");
                                    dismissAlert.setHeaderText("Emergency Dismissed");
                                    dismissAlert.setContentText(
                                            "Voice detection was dismissed as false alarm.\n\n" +
                                                    "Detected: \"" + detectedKeyword + "\"\n" +
                                                    "Reason: " + reason + "\n\n" +
                                                    "No emergency procedures were triggered.\n" +
                                                    "Voice detection continues normally."
                                    );
                                    dismissAlert.show();
                                } catch (Exception e) {
                                    System.err.println("❌ Could not show dismissal alert: " + e.getMessage());
                                }
                            });

                            currentPopup = null;
                        }
                    }
            );

            // Show the popup
            currentPopup.showConfirmationPopup();

        } catch (Exception e) {
            System.err.println("❌ Error showing voice confirmation popup: " + e.getMessage());
            e.printStackTrace();

            // Fallback: Ask user via console if GUI fails
            handleVoiceDetectionFallback(detectedKeyword, emergencyType);
        }
    }

    /**
     * Fallback method if GUI popup fails
     */
    private static void handleVoiceDetectionFallback(String detectedKeyword, String emergencyType) {
        System.out.println("\n🚨 EMERGENCY VOICE DETECTED: " + detectedKeyword);
        System.out.println("⚠️ GUI popup failed - Using console fallback");
        System.out.println("⏰ Emergency will auto-trigger in 10 seconds unless cancelled");
        System.out.println("👉 Type 'cancel' and press ENTER to dismiss as false alarm");

        // Simple console-based confirmation with timeout
        new Thread(() -> {
            try {
                Thread.sleep(10000); // 10 second timeout

                if (!EnhancedEmergencyHandler.isEmergencyTriggered()) {
                    System.out.println("⏰ Timeout reached - Triggering emergency via fallback");
                    String description = "Voice-activated emergency (fallback): '" + detectedKeyword + "'";
                    EnhancedEmergencyHandler.triggerEmergency("voice_fallback", emergencyType, description);
                }
            } catch (InterruptedException e) {
                System.out.println("🚫 Fallback timer interrupted");
            }
        }).start();
    }

    /**
     * Log false alarm for analysis
     */
    private static void logFalseAlarm(String detectedKeyword, String reason) {
        try {
            String logEntry = String.format(
                    "[%s] FALSE ALARM - Keyword: '%s', Reason: %s, User: %s",
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    detectedKeyword,
                    reason,
                    UserSession.isLoggedIn() ? UserSession.getCurrentUsername() : "Unknown"
            );

            System.out.println("📝 Logging false alarm: " + logEntry);

            // Here you could save to database or file for analysis
            // For now, just console logging

        } catch (Exception e) {
            System.err.println("❌ Error logging false alarm: " + e.getMessage());
        }
    }

    /**
     * Start enhanced voice detection
     */
    public static void startVoiceDetection() {
        if (isVoiceDetectionActive) {
            System.out.println("⚠️ Voice detection already active");
            return;
        }

        isVoiceDetectionActive = true;
        System.out.println("🎤 Starting enhanced voice detection with confirmation popup...");

        // Start voice detection in background thread
        Thread voiceThread = new Thread(() -> {
            try {
                SpeechRecognitionTest.startVoiceDetectionWithCallback(new VoiceCallback() {
                    @Override
                    public void onVoiceDetected(String command) {
                        handleVoiceDetection(command);
                    }

                    @Override
                    public void onSilenceDetected() {
                        if (!EnhancedEmergencyHandler.isEmergencyTriggered()) {
                            System.out.println("⏰ Silence detected - No emergency action taken");
                            // Could implement silence-based emergency detection here if needed
                        }
                    }
                });
            } catch (Exception e) {
                System.err.println("❌ Enhanced voice detection error: " + e.getMessage());
                e.printStackTrace();
                isVoiceDetectionActive = false;
            }
        });

        voiceThread.setDaemon(true);
        voiceThread.start();

        System.out.println("✅ Enhanced voice detection started successfully");
    }

    /**
     * Stop enhanced voice detection
     */
    public static void stopVoiceDetection() {
        isVoiceDetectionActive = false;

        // Close any active popup
        if (currentPopup != null) {
            currentPopup.forceClose();
            currentPopup = null;
        }

        System.out.println("🛑 Enhanced voice detection stopped");
    }

    /**
     * Check if voice detection is active
     */
    public static boolean isVoiceDetectionActive() {
        return isVoiceDetectionActive;
    }

    /**
     * Get current popup (for testing/debugging)
     */
    public static VoiceConfirmationPopup getCurrentPopup() {
        return currentPopup;
    }
}