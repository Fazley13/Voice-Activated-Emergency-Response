package com.codevengers.voiceemergency;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ManualAudioStopService {

    /**
     * Handles manual stopping of audio recording by user
     * This will stop recording and immediately send to admin chat
     */
    public static void handleManualAudioStop() {
        try {
            System.out.println("🛑 Processing manual audio stop request...");

            // Check if emergency is active
            if (!EnhancedEmergencyHandler.isEmergencyTriggered()) {
                System.out.println("⚠️ No active emergency to stop audio for");
                return;
            }

            // Get the audio recorder
            AudioRecorder audioRecorder = EnhancedEmergencyHandler.getAudioRecorder();
            if (audioRecorder == null || !audioRecorder.isRecording()) {
                System.out.println("⚠️ No active audio recording to stop");
                return;
            }

            // Get current emergency details
            int emergencyId = EnhancedEmergencyHandler.getCurrentEmergencyId();
            String username = UserSession.isLoggedIn() ? UserSession.getCurrentUsername() : "Unknown User";

            System.out.println("🎙️ Stopping audio recording for emergency ID: " + emergencyId);

            // Stop the recording - this will automatically trigger the callback
            // which will send the audio to admin chat
            audioRecorder.stopRecording();

            // Send immediate notification to admin about manual stop
            sendManualStopNotification(emergencyId, username);

            System.out.println("✅ Manual audio stop processed successfully");

        } catch (Exception e) {
            System.err.println("❌ Error handling manual audio stop: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends notification to admin that user manually stopped audio recording
     */
    private static void sendManualStopNotification(int emergencyId, String username) {
        try {
            if (!UserSession.isLoggedIn()) {
                return;
            }

            JavaChatService chatService = JavaChatService.getInstance();
            if (chatService == null) {
                return;
            }

            String notificationMessage = String.format(
                    "🛑 AUDIO RECORDING MANUALLY STOPPED\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "📋 Emergency ID: %d\n" +
                            "👤 User: %s\n" +
                            "🕐 Stopped At: %s\n" +
                            "🎯 Action: Manual Stop by User\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "ℹ️ The user has manually stopped the audio recording.\n" +
                            "📤 The recorded audio file will be sent shortly.\n" +
                            "🚨 The emergency remains active for location tracking.\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━",
                    emergencyId,
                    username,
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );

            chatService.sendMessage(
                    UserSession.getCurrentUserId(),
                    "user",
                    null,
                    "admin",
                    notificationMessage,
                    "manual_stop"
            );

            System.out.println("✅ Manual stop notification sent to admin");

        } catch (Exception e) {
            System.err.println("❌ Error sending manual stop notification: " + e.getMessage());
        }
    }

    /**
     * Checks if audio can be manually stopped
     */
    public static boolean canStopAudio() {
        try {
            if (!EnhancedEmergencyHandler.isEmergencyTriggered()) {
                return false;
            }

            AudioRecorder audioRecorder = EnhancedEmergencyHandler.getAudioRecorder();
            return audioRecorder != null && audioRecorder.isRecording();

        } catch (Exception e) {
            System.err.println("❌ Error checking if audio can be stopped: " + e.getMessage());
            return false;
        }
    }

    /**
     * Gets current recording status information
     */
    public static String getRecordingStatus() {
        try {
            if (!EnhancedEmergencyHandler.isEmergencyTriggered()) {
                return "No active emergency";
            }

            AudioRecorder audioRecorder = EnhancedEmergencyHandler.getAudioRecorder();
            if (audioRecorder == null) {
                return "No audio recorder initialized";
            }

            if (audioRecorder.isRecording()) {
                long recordingSize = audioRecorder.getCurrentRecordingSize();
                long estimatedDuration = recordingSize / (44100 * 2); // Rough estimate
                return String.format("Recording active - ~%d seconds", estimatedDuration);
            } else {
                return "Audio recording stopped";
            }

        } catch (Exception e) {
            return "Error getting status";
        }
    }
}