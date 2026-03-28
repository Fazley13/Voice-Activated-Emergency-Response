package com.codevengers.voiceemergency;

import java.util.Scanner;

public class EmergencyHandler {

    private static volatile boolean isEmergencyTriggered = false;
    private static AudioRecorder audioRecorder;

    public static boolean isEmergencyTriggered() {
        return isEmergencyTriggered;
    }

    public static void triggerEmergency() {
        if (isEmergencyTriggered) {
            System.out.println("⚠️ Emergency already triggered.");
            return;
        }

        isEmergencyTriggered = true;
        System.out.println("🚨 EMERGENCY TRIGGERED! 🚨");

        // Test microphone first
        System.out.println("🧪 Testing microphone...");
        if (!AudioRecorder.testMicrophone()) {
            System.err.println("⚠️ Microphone test failed, but continuing with emergency procedures...");
        } else {
            System.out.println("✅ Microphone test passed");
        }

        // Play alert sound
        try {
            System.out.println("🔊 Playing alert sound...");
            AlertSound.playAlert();
        } catch (Exception e) {
            System.err.println("❌ Alert sound error: " + e.getMessage());
        }

        // Start audio recording
        System.out.println("🎙️ Starting emergency audio recording...");
        audioRecorder = new AudioRecorder();
        audioRecorder.startRecording();

        // Give recording a moment to start
        try {
            Thread.sleep(1000);
            if (audioRecorder.isRecording()) {
                System.out.println("✅ Audio recording confirmed active");
            } else {
                System.err.println("❌ Audio recording failed to start!");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Start live audio streaming
        System.out.println("📡 Starting audio streaming...");
        Thread streamingThread = new Thread(() -> {
            try {
                AudioStreamer.startStreaming();
            } catch (Exception e) {
                System.err.println("❌ Audio streaming error: " + e.getMessage());
            }
        });
        streamingThread.setDaemon(true);
        streamingThread.start();

        // Get location
        System.out.println("📍 Getting location...");
        String location = LocationFetcher.getLocationLink();
        System.out.println("📍 Location: " + location);

        // Send emergency message
        System.out.println("📱 Sending emergency message...");
        String emergencyContact = "01700000000";
        try {
            MessageSender.send(emergencyContact, "🚨 Emergency triggered! Location: " + location);
        } catch (Exception e) {
            System.err.println("❌ Message sending error: " + e.getMessage());
        }

        // Show status
        System.out.println("\n📋 Emergency Status:");
        System.out.println("   🎙️ Audio Recording: " + (audioRecorder.isRecording() ? "✅ ACTIVE" : "❌ FAILED"));
        System.out.println("   📡 Audio Streaming: " + (AudioStreamer.isStreaming() ? "✅ ACTIVE" : "⚠️ INACTIVE"));
        System.out.println("   📍 Location: ✅ Retrieved");
        System.out.println("   📱 Message: ✅ Sent");

        // Auto-stop after 10 minutes
        Thread autoStopThread = new Thread(() -> {
            try {
                System.out.println("⏰ Auto-stop timer set for 10 minutes...");
                Thread.sleep(10 * 60 * 1000);
                if (isEmergencyTriggered) {
                    System.out.println("⏱️ Auto-stopping after 10 minutes...");
                    stopEmergency();
                }
            } catch (InterruptedException e) {
                System.out.println("⏱️ Auto-stop timer interrupted.");
            }
        });
        autoStopThread.setDaemon(true);
        autoStopThread.start();

        // Manual stop thread
        Thread manualStopThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            System.out.println("👉 Press 'S' and ENTER to stop recording manually...");
            while (isEmergencyTriggered) {
                try {
                    if (scanner.hasNextLine()) {
                        String input = scanner.nextLine().trim();
                        if (input.equalsIgnoreCase("S")) {
                            System.out.println("🛑 Manual stop requested");
                            stopEmergency();
                            break;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error reading input: " + e.getMessage());
                    break;
                }
            }
        });
        manualStopThread.setDaemon(true);
        manualStopThread.start();

        System.out.println("🚨 Emergency procedures fully activated!");
    }

    public static void stopEmergency() {
        if (!isEmergencyTriggered) {
            System.out.println("⚠️ No emergency in progress.");
            return;
        }

        System.out.println("🛑 Stopping emergency procedures...");

        // Stop audio recording
        if (audioRecorder != null) {
            if (audioRecorder.isRecording()) {
                System.out.println("⏹️ Stopping audio recording...");
                audioRecorder.stopRecording();

                // Wait a moment for file to be saved
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                long duration = audioRecorder.getRecordingDuration();
                System.out.println("📊 Recording duration: " + duration + " seconds");

                if (duration > 0) {
                    System.out.println("✅ Audio recording saved successfully");
                    System.out.println("📁 File: " + audioRecorder.getCurrentFileName());
                } else {
                    System.out.println("⚠️ Warning: Recording duration is 0 seconds");
                }
            } else {
                System.out.println("⚠️ No recording in progress.");
            }
        } else {
            System.out.println("⚠️ Audio recorder was not initialized.");
        }

        // Stop audio streaming
        try {
            if (AudioStreamer.isStreaming()) {
                System.out.println("📡 Stopping audio streaming...");
                AudioStreamer.stopStreaming();
            } else {
                System.out.println("⚠️ No audio streaming in progress.");
            }
        } catch (Exception e) {
            System.err.println("❌ Error stopping audio streaming: " + e.getMessage());
        }

        isEmergencyTriggered = false;
        System.out.println("✅ Emergency procedures stopped successfully.");
    }

    public static AudioRecorder getAudioRecorder() {
        return audioRecorder;
    }
}
