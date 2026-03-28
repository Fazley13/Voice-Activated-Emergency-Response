package com.codevengers.voiceemergency;

import javax.sound.sampled.*;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AudioStreamer {
    private static TargetDataLine microphone;
    private static volatile boolean isStreaming = false;
    private static Thread streamingThread;
    private static final String STREAM_DIR = "stream_recordings";

    // Audio format for streaming
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
            16000.0f,  // Sample rate
            16,        // Sample size in bits
            1,         // Channels (mono)
            true,      // Signed
            false      // Big endian
    );

    public static void startStreaming() {
        if (isStreaming) {
            System.out.println("⚠️ Audio streaming already active.");
            return;
        }

        try {
            // Create stream directory if it doesn't exist
            File streamDir = new File(STREAM_DIR);
            if (!streamDir.exists()) {
                streamDir.mkdirs();
            }

            // Get microphone line
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);

            if (!AudioSystem.isLineSupported(info)) {
                System.err.println("❌ Audio streaming not supported!");
                return;
            }

            microphone = (TargetDataLine) AudioSystem.getLine(info);
            microphone.open(AUDIO_FORMAT);
            microphone.start();

            isStreaming = true;
            System.out.println("📡 Audio streaming started...");

            // Start streaming in separate thread
            streamingThread = new Thread(AudioStreamer::streamAudio);
            streamingThread.setDaemon(true);
            streamingThread.start();

        } catch (LineUnavailableException e) {
            System.err.println("❌ Microphone unavailable for streaming: " + e.getMessage());
        }
    }

    private static void streamAudio() {
        byte[] buffer = new byte[4096];

        try {
            while (isStreaming && microphone != null) {
                int bytesRead = microphone.read(buffer, 0, buffer.length);

                if (bytesRead > 0) {
                    // Save audio chunks to file (for backup/evidence)
                    saveToFile(buffer, bytesRead);

                    // Here you could also send to server/socket if needed
                    // sendToServer(buffer, bytesRead);
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Streaming error: " + e.getMessage());
        }
    }

    private static void saveToFile(byte[] audioData, int length) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String fileName = STREAM_DIR + "/stream_chunk_" + timestamp + "_" + System.currentTimeMillis() + ".raw";

            try (FileOutputStream fos = new FileOutputStream(fileName, true)) {
                fos.write(audioData, 0, length);
            }

        } catch (IOException e) {
            System.err.println("❌ Error saving stream chunk: " + e.getMessage());
        }
    }

    public static void stopStreaming() {
        if (!isStreaming) {
            System.out.println("⚠️ No audio streaming in progress.");
            return;
        }

        isStreaming = false;

        if (microphone != null) {
            microphone.stop();
            microphone.close();
            microphone = null;
        }

        if (streamingThread != null) {
            streamingThread.interrupt();
        }

        System.out.println("📡 Audio streaming stopped.");
    }

    public static boolean isStreaming() {
        return isStreaming;
    }

    // Method to send audio to server (optional - for future use)
    private static void sendToServer(byte[] audioData, int length) {
        // This is where you would implement server communication
        // For now, we'll just simulate it
        System.out.println("📤 Sending " + length + " bytes to server...");
    }
}
