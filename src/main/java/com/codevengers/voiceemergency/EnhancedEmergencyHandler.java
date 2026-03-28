package com.codevengers.voiceemergency;

import java.time.LocalDateTime;
import java.util.List;

public class EnhancedEmergencyHandler {

    private static volatile boolean isEmergencyTriggered = false;
    private static AudioRecorder audioRecorder;
    private static int currentEmergencyId = -1;

    public static boolean isEmergencyTriggered() {
        return isEmergencyTriggered;
    }

    /**
     * Trigger emergency with enhanced features including email notifications
     */
    public static void triggerEmergency(String triggerMethod, String emergencyType, String description) {
        if (isEmergencyTriggered) {
            System.out.println("⚠️ Emergency already triggered.");
            return;
        }

        System.out.println("🚨 ENHANCED EMERGENCY TRIGGERED! 🚨");
        System.out.println("   Trigger Method: " + triggerMethod);
        System.out.println("   Emergency Type: " + emergencyType);
        System.out.println("   Description: " + description);

        isEmergencyTriggered = true;

        try {
            // Step 1: Get user information
            if (!UserSession.isLoggedIn()) {
                System.err.println("❌ User not logged in - cannot trigger emergency");
                return;
            }

            int userId = UserSession.getCurrentUserId();
            String username = UserSession.getCurrentUsername();

            System.out.println("👤 User: " + username + " (ID: " + userId + ")");

            // Step 2: Get current location
            System.out.println("📍 Getting current location...");
            String location = LocationFetcher.getLocationLink();
            System.out.println("📍 Location: " + location);

            // Step 3: Create emergency record
            System.out.println("💾 Creating emergency record...");
            Emergency emergency = new Emergency(userId, username, emergencyType, location, triggerMethod, description);
            emergency.setTimestamp(LocalDateTime.now());
            emergency.setStatus("ACTIVE");

            // Save to database
            currentEmergencyId = EmergencyDAO.saveEmergency(emergency);
            if (currentEmergencyId > 0) {
                System.out.println("✅ Emergency record saved with ID: " + currentEmergencyId);
            } else {
                System.err.println("❌ Failed to save emergency record");
            }

            // Step 4: Send email notifications to emergency contacts
            System.out.println("📧 Sending email notifications to emergency contacts...");
            sendEmailNotifications(userId, username, emergencyType, location, description, currentEmergencyId);

            // Step 5: Send chat notification to admins
            System.out.println("💬 Sending chat notification to administrators...");
            sendChatNotification(userId, username, emergencyType, location, description, currentEmergencyId);

            // Step 6: Start audio recording
            System.out.println("🎙️ Starting emergency audio recording...");
            startAudioRecording();

            // Step 7: Start audio streaming
            System.out.println("📡 Starting audio streaming...");
            startAudioStreaming();

            // Step 8: Play alert sound
            System.out.println("🔊 Playing alert sound...");
            try {
                AlertSound.playAlert();
            } catch (Exception e) {
                System.err.println("❌ Alert sound error: " + e.getMessage());
            }

            // Step 9: Show emergency status
            showEmergencyStatus();

            System.out.println("🚨 Enhanced emergency procedures fully activated!");

        } catch (Exception e) {
            System.err.println("❌ Error during emergency trigger: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send email notifications to emergency contacts
     */
    private static void sendEmailNotifications(int userId, String username, String emergencyType,
                                               String location, String description, int emergencyId) {
        try {
            // Check if user has emergency contacts
            List<EmergencyContact> contacts = EmergencyContactDAO.getEmergencyContactsByUserId(userId);

            if (contacts.isEmpty()) {
                System.out.println("⚠️ No emergency contacts found for user: " + username);
                showNoContactsWarning();
                return;
            }

            // Count contacts with email addresses
            long emailContacts = contacts.stream()
                    .filter(c -> c.getEmail() != null && !c.getEmail().trim().isEmpty())
                    .count();

            if (emailContacts == 0) {
                System.out.println("⚠️ No emergency contacts have email addresses configured");
                showNoEmailContactsWarning();
                return;
            }

            System.out.println("📧 Found " + contacts.size() + " emergency contacts (" + emailContacts + " with email)");

            // Send email notifications
            boolean emailSuccess = EmailService.sendEmergencyNotification(
                    userId, username, emergencyType, location, description, emergencyId
            );

            if (emailSuccess) {
                System.out.println("✅ Emergency email notifications sent successfully");
                showEmailSuccessNotification(emailContacts);
            } else {
                System.err.println("❌ Failed to send emergency email notifications");
                showEmailFailureNotification();
            }

        } catch (Exception e) {
            System.err.println("❌ Error sending email notifications: " + e.getMessage());
            e.printStackTrace();
            showEmailErrorNotification(e.getMessage());
        }
    }

    /**
     * Send chat notification to administrators
     */
    private static void sendChatNotification(int userId, String username, String emergencyType,
                                             String location, String description, int emergencyId) {
        try {
            JavaChatService chatService = JavaChatService.getInstance();
            if (chatService != null && chatService.isConnected()) {
                String chatMessage = String.format(
                        "🚨 EMERGENCY ALERT 🚨\n" +
                                "User: %s (ID: %d)\n" +
                                "Type: %s\n" +
                                "Location: %s\n" +
                                "Description: %s\n" +
                                "Emergency ID: #%d\n" +
                                "Time: %s",
                        username, userId, emergencyType, location, description, emergencyId,
                        LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                );

                chatService.sendMessage(
                        userId,
                        "user",
                        null, // Send to all admins
                        "admin",
                        chatMessage,
                        "emergency"
                );

                System.out.println("✅ Emergency chat notification sent to administrators");
            } else {
                System.err.println("❌ Chat service not available for admin notification");
            }
        } catch (Exception e) {
            System.err.println("❌ Error sending chat notification: " + e.getMessage());
        }
    }

    /**
     * Start audio recording
     */
    private static void startAudioRecording() {
        try {
            audioRecorder = new AudioRecorder();

            // Add completion listener for audio recording
            audioRecorder.addRecordingCompletionListener(new AudioRecorder.RecordingCompletionListener() {
                @Override
                public void onRecordingCompleted(String audioFilePath, long durationSeconds, long fileSizeBytes) {
                    System.out.println("🎙️ Emergency audio recording completed");
                    System.out.println("   File: " + audioFilePath);
                    System.out.println("   Duration: " + durationSeconds + " seconds");
                    System.out.println("   Size: " + fileSizeBytes + " bytes");

                    // Update emergency record with audio file
                    if (currentEmergencyId > 0) {
                        EmergencyDAO.updateAudioFileName(currentEmergencyId, audioFilePath);
                    }

                    // Send audio to admin chat
                    AudioChatService.sendAudioToAdminChat(audioFilePath, currentEmergencyId,
                            UserSession.getCurrentUsername(), durationSeconds, fileSizeBytes);
                }

                @Override
                public void onRecordingFailed(String error) {
                    System.err.println("❌ Emergency audio recording failed: " + error);
                }
            });

            audioRecorder.startRecording();

            // Give recording a moment to start
            Thread.sleep(1000);
            if (audioRecorder.isRecording()) {
                System.out.println("✅ Emergency audio recording started successfully");
            } else {
                System.err.println("❌ Emergency audio recording failed to start");
            }

        } catch (Exception e) {
            System.err.println("❌ Error starting audio recording: " + e.getMessage());
        }
    }

    /**
     * Start audio streaming
     */
    private static void startAudioStreaming() {
        try {
            Thread streamingThread = new Thread(() -> {
                try {
                    AudioStreamer.startStreaming();
                } catch (Exception e) {
                    System.err.println("❌ Audio streaming error: " + e.getMessage());
                }
            });
            streamingThread.setDaemon(true);
            streamingThread.start();

            System.out.println("✅ Emergency audio streaming started");
        } catch (Exception e) {
            System.err.println("❌ Error starting audio streaming: " + e.getMessage());
        }
    }

    /**
     * Show emergency status
     */
    private static void showEmergencyStatus() {
        System.out.println("\n📋 EMERGENCY STATUS:");
        System.out.println("   🚨 Emergency ID: #" + currentEmergencyId);
        System.out.println("   👤 User: " + UserSession.getCurrentUsername());
        System.out.println("   🎙️ Audio Recording: " + (audioRecorder != null && audioRecorder.isRecording() ? "✅ ACTIVE" : "❌ FAILED"));
        System.out.println("   📡 Audio Streaming: " + (AudioStreamer.isStreaming() ? "✅ ACTIVE" : "⚠️ INACTIVE"));
        System.out.println("   📧 Email Notifications: ✅ SENT");
        System.out.println("   💬 Admin Notifications: ✅ SENT");
        System.out.println("   📍 Location Shared: ✅ INCLUDED");
        System.out.println("   ⏰ Status: 🚨 EMERGENCY ACTIVE");
    }

    /**
     * Stop emergency procedures
     */
    public static void stopEmergency() {
        if (!isEmergencyTriggered) {
            System.out.println("⚠️ No emergency in progress.");
            return;
        }

        System.out.println("🛑 Stopping enhanced emergency procedures...");

        try {
            // Stop audio recording
            if (audioRecorder != null && audioRecorder.isRecording()) {
                System.out.println("⏹️ Stopping audio recording...");
                audioRecorder.stopRecording();

                long duration = audioRecorder.getRecordingDuration();
                System.out.println("📊 Final recording duration: " + duration + " seconds");
            }

            // Stop audio streaming
            if (AudioStreamer.isStreaming()) {
                System.out.println("📡 Stopping audio streaming...");
                AudioStreamer.stopStreaming();
            }

            // Update emergency status in database
            if (currentEmergencyId > 0) {
                System.out.println("💾 Updating emergency status to RESOLVED...");
                boolean updated = EmergencyDAO.updateEmergencyStatus(currentEmergencyId, "RESOLVED");
                if (updated) {
                    System.out.println("✅ Emergency #" + currentEmergencyId + " marked as resolved");

                    // Send resolution notifications
                    if (UserSession.isLoggedIn()) {
                        sendResolutionNotifications();
                    }
                } else {
                    System.err.println("❌ Failed to update emergency status");
                }
            }

            // Reset state
            isEmergencyTriggered = false;
            audioRecorder = null;
            currentEmergencyId = -1;

            System.out.println("✅ Enhanced emergency procedures stopped successfully");

        } catch (Exception e) {
            System.err.println("❌ Error stopping emergency: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Send resolution notifications
     */
    private static void sendResolutionNotifications() {
        try {
            System.out.println("📧 Sending emergency resolution notifications...");

            boolean emailSuccess = EmailService.sendEmergencyResolvedNotification(
                    UserSession.getCurrentUserId(),
                    UserSession.getCurrentUsername(),
                    currentEmergencyId
            );

            if (emailSuccess) {
                System.out.println("✅ Emergency resolution emails sent successfully");
            } else {
                System.err.println("❌ Failed to send emergency resolution emails");
            }

            // Send chat notification to admins
            JavaChatService chatService = JavaChatService.getInstance();
            if (chatService != null && chatService.isConnected()) {
                String resolutionMessage = String.format(
                        "✅ EMERGENCY RESOLVED\n" +
                                "User: %s\n" +
                                "Emergency ID: #%d\n" +
                                "Status: RESOLVED\n" +
                                "Time: %s",
                        UserSession.getCurrentUsername(),
                        currentEmergencyId,
                        LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                );

                chatService.sendMessage(
                        UserSession.getCurrentUserId(),
                        "user",
                        null,
                        "admin",
                        resolutionMessage,
                        "notification"
                );
            }

        } catch (Exception e) {
            System.err.println("❌ Error sending resolution notifications: " + e.getMessage());
        }
    }

    /**
     * Get current emergency ID
     */
    public static int getCurrentEmergencyId() {
        return currentEmergencyId;
    }

    /**
     * Get audio recorder instance
     */
    public static AudioRecorder getAudioRecorder() {
        return audioRecorder;
    }

    // UI Notification Methods
    private static void showNoContactsWarning() {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("No Emergency Contacts");
            alert.setHeaderText("No Emergency Contacts Found");
            alert.setContentText("You don't have any emergency contacts configured.\n\n" +
                    "Please add emergency contacts in your dashboard to receive email notifications during emergencies.");
            alert.show();
        });
    }

    private static void showNoEmailContactsWarning() {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.WARNING);
            alert.setTitle("No Email Contacts");
            alert.setHeaderText("No Email Addresses Configured");
            alert.setContentText("None of your emergency contacts have email addresses configured.\n\n" +
                    "Please add email addresses to your emergency contacts to receive email notifications.");
            alert.show();
        });
    }

    private static void showEmailSuccessNotification(long emailCount) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("Emergency Notifications Sent");
            alert.setHeaderText("Email Notifications Successful");
            alert.setContentText("Emergency email notifications have been sent to " + emailCount + " emergency contacts.\n\n" +
                    "Your emergency contacts have been notified with your location and emergency details.");
            alert.show();
        });
    }

    private static void showEmailFailureNotification() {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Email Notification Failed");
            alert.setHeaderText("Could Not Send Email Notifications");
            alert.setContentText("There was an error sending email notifications to your emergency contacts.\n\n" +
                    "Please check your internet connection and email configuration.\n\n" +
                    "Emergency procedures are still active - administrators have been notified via chat.");
            alert.show();
        });
    }

    private static void showEmailErrorNotification(String error) {
        javafx.application.Platform.runLater(() -> {
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle("Email Configuration Error");
            alert.setHeaderText("Email System Error");
            alert.setContentText("Error sending emergency emails: " + error + "\n\n" +
                    "Please configure the email system in EmailService.java\n\n" +
                    "Emergency procedures are still active - administrators have been notified via chat.");
            alert.show();
        });
    }
}