package com.codevengers.voiceemergency;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class AudioChatService {

    /**
     * Sends an audio file to admin chat when emergency recording is completed
     */
    public static void sendAudioToAdminChat(String audioFilePath, int emergencyId, String username, long durationSeconds, long fileSizeBytes) {
        sendAudioToAdminChat(audioFilePath, emergencyId, username, durationSeconds, fileSizeBytes, false);
    }

    /**
     * Sends an audio file to admin chat with option to specify if manually stopped
     */
    public static void sendAudioToAdminChat(String audioFilePath, int emergencyId, String username, long durationSeconds, long fileSizeBytes, boolean manuallyStoppedByUser) {
        try {
            System.out.println("📤 Sending audio file to admin chat (Manual Stop: " + manuallyStoppedByUser + ")...");

            File audioFile = new File(audioFilePath);
            if (!audioFile.exists()) {
                System.err.println("❌ Audio file not found: " + audioFilePath);
                return;
            }

            // Get chat service instance
            JavaChatService chatService = JavaChatService.getInstance();
            if (chatService == null) {
                System.err.println("❌ Chat service not available");
                return;
            }

            // Create audio message content with manual stop indication
            String audioMessage = createAudioMessageWithManualFlag(audioFile, emergencyId, username, durationSeconds, fileSizeBytes, manuallyStoppedByUser);

            // Send message to admin chat
            if (UserSession.isLoggedIn()) {
                chatService.sendMessage(
                        UserSession.getCurrentUserId(),
                        "user",
                        null, // Send to any available admin
                        "admin",
                        audioMessage,
                        manuallyStoppedByUser ? "manual_audio_stop" : "audio_file" // Different message type for manual stops
                );

                System.out.println("✅ Audio file message sent to admin chat");
                System.out.println("📁 File: " + audioFile.getName());
                System.out.println("⏱️ Duration: " + durationSeconds + " seconds");
                System.out.println("📊 Size: " + formatFileSize(fileSizeBytes));
                System.out.println("🛑 Manual Stop: " + (manuallyStoppedByUser ? "Yes" : "No"));

            } else {
                System.err.println("❌ User not logged in, cannot send audio to admin chat");
            }

        } catch (Exception e) {
            System.err.println("❌ Error sending audio to admin chat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a formatted message for the audio file with manual stop indication
     */
    private static String createAudioMessageWithManualFlag(File audioFile, int emergencyId, String username, long durationSeconds, long fileSizeBytes, boolean manuallyStoppedByUser) {
        try {
            // Read audio file and encode to Base64 for transmission
            byte[] audioBytes = Files.readAllBytes(audioFile.toPath());
            String audioBase64 = Base64.getEncoder().encodeToString(audioBytes);

            // Create structured message
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            String stopMethod = manuallyStoppedByUser ? "🛑 MANUALLY STOPPED BY USER" : "⏰ AUTOMATICALLY COMPLETED";
            String alertLevel = manuallyStoppedByUser ? "🔴 USER INTERVENTION" : "🟡 NORMAL COMPLETION";

            String message = String.format(
                    "🎙️ EMERGENCY AUDIO RECORDING\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "📋 Emergency ID: %d\n" +
                            "👤 User: %s\n" +
                            "📁 File: %s\n" +
                            "⏱️ Duration: %d seconds (%s)\n" +
                            "📊 File Size: %s\n" +
                            "🕐 Recorded: %s\n" +
                            "🎯 Stop Method: %s\n" +
                            "⚠️ Alert Level: %s\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "📎 Audio Data (Base64):\n%s\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "%s",
                    emergencyId,
                    username,
                    audioFile.getName(),
                    durationSeconds,
                    formatDuration(durationSeconds),
                    formatFileSize(fileSizeBytes),
                    timestamp,
                    stopMethod,
                    alertLevel,
                    audioBase64,
                    manuallyStoppedByUser ?
                            "🛑 This recording was manually stopped by the user.\n" +
                                    "⚠️ Please review immediately and contact the user.\n" +
                                    "🚨 The emergency is still active." :
                            "⚠️ This is an emergency recording. Please review immediately."
            );

            return message;

        } catch (IOException e) {
            System.err.println("❌ Error reading audio file for message: " + e.getMessage());

            // Fallback message without audio data
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String stopMethod = manuallyStoppedByUser ? "🛑 MANUALLY STOPPED BY USER" : "⏰ AUTOMATICALLY COMPLETED";

            return String.format(
                    "🎙️ EMERGENCY AUDIO RECORDING\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "📋 Emergency ID: %d\n" +
                            "👤 User: %s\n" +
                            "📁 File: %s\n" +
                            "⏱️ Duration: %d seconds (%s)\n" +
                            "📊 File Size: %s\n" +
                            "🕐 Recorded: %s\n" +
                            "🎯 Stop Method: %s\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "❌ Audio data could not be attached\n" +
                            "📁 File location: %s\n" +
                            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                            "%s",
                    emergencyId,
                    username,
                    audioFile.getName(),
                    durationSeconds,
                    formatDuration(durationSeconds),
                    formatFileSize(fileSizeBytes),
                    timestamp,
                    stopMethod,
                    audioFile.getAbsolutePath(),
                    manuallyStoppedByUser ?
                            "🛑 This recording was manually stopped by the user.\n" +
                                    "⚠️ Please review immediately and contact the user.\n" +
                                    "🚨 The emergency is still active." :
                            "⚠️ This is an emergency recording. Please review immediately."
            );
        }
    }

    /**
     * Formats duration in seconds to human-readable format
     */
    private static String formatDuration(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long remainingSeconds = seconds % 60;
            return minutes + "m " + remainingSeconds + "s";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long remainingSeconds = seconds % 60;
            return hours + "h " + minutes + "m " + remainingSeconds + "s";
        }
    }

    /**
     * Formats file size in bytes to human-readable format
     */
    private static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        } else {
            return String.format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    /**
     * Sends a notification to admin about audio file availability
     */
    public static void sendAudioNotificationToAdmin(String audioFilePath, int emergencyId, String username) {
        try {
            File audioFile = new File(audioFilePath);
            if (!audioFile.exists()) {
                return;
            }

            JavaChatService chatService = JavaChatService.getInstance();
            if (chatService == null || !UserSession.isLoggedIn()) {
                return;
            }

            String notificationMessage = String.format(
                    "🔔 AUDIO RECORDING COMPLETED\n" +
                            "Emergency ID: %d\n" +
                            "User: %s\n" +
                            "File: %s\n" +
                            "Location: %s\n" +
                            "The emergency audio recording has been completed and saved.",
                    emergencyId,
                    username,
                    audioFile.getName(),
                    audioFile.getAbsolutePath()
            );

            chatService.sendMessage(
                    UserSession.getCurrentUserId(),
                    "user",
                    null,
                    "admin",
                    notificationMessage,
                    "notification"
            );

            System.out.println("✅ Audio completion notification sent to admin");

        } catch (Exception e) {
            System.err.println("❌ Error sending audio notification: " + e.getMessage());
        }
    }
}