package com.codevengers.voiceemergency;

import javax.sound.sampled.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AudioRecorder {
    private TargetDataLine targetDataLine;
    private Thread recordingThread;
    private volatile boolean isRecording = false;
    private String fileName;
    private static final String RECORDINGS_DIR = "recordings";
    private ByteArrayOutputStream recordedAudio;

    // ADDED: Callback interface for recording completion
    public interface RecordingCompletionListener {
        void onRecordingCompleted(String audioFilePath, long durationSeconds, long fileSizeBytes);
        void onRecordingFailed(String error);
    }

    // ADDED: List of listeners for recording completion
    private List<RecordingCompletionListener> completionListeners = new ArrayList<>();

    // Audio format configuration - using the format that worked in diagnostics
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
            AudioFormat.Encoding.PCM_SIGNED,
            44100.0f,  // Sample rate
            16,        // Sample size in bits
            1,         // Channels (mono)
            2,         // Frame size (16 bit = 2 bytes)
            44100.0f,  // Frame rate
            false      // Little endian (as shown in diagnostics)
    );

    public AudioRecorder() {
        // Create recordings directory if it doesn't exist
        File recordingsDir = new File(RECORDINGS_DIR);
        if (!recordingsDir.exists()) {
            boolean created = recordingsDir.mkdirs();
            if (created) {
                System.out.println("📁 Created recordings directory: " + recordingsDir.getAbsolutePath());
            } else {
                System.err.println("❌ Failed to create recordings directory!");
            }
        }
        recordedAudio = new ByteArrayOutputStream();
    }

    // ADDED: Method to add recording completion listeners
    public void addRecordingCompletionListener(RecordingCompletionListener listener) {
        completionListeners.add(listener);
    }

    // ADDED: Method to remove recording completion listeners
    public void removeRecordingCompletionListener(RecordingCompletionListener listener) {
        completionListeners.remove(listener);
    }

    // ADDED: Method to notify all listeners when recording is completed
    private void notifyRecordingCompleted(String audioFilePath, long durationSeconds, long fileSizeBytes) {
        for (RecordingCompletionListener listener : completionListeners) {
            try {
                listener.onRecordingCompleted(audioFilePath, durationSeconds, fileSizeBytes);
            } catch (Exception e) {
                System.err.println("❌ Error notifying recording completion listener: " + e.getMessage());
            }
        }
    }

    // ADDED: Method to notify all listeners when recording fails
    private void notifyRecordingFailed(String error) {
        for (RecordingCompletionListener listener : completionListeners) {
            try {
                listener.onRecordingFailed(error);
            } catch (Exception e) {
                System.err.println("❌ Error notifying recording failure listener: " + e.getMessage());
            }
        }
    }

    public void startRecording() {
        if (isRecording) {
            System.out.println("⚠️ Recording already in progress.");
            return;
        }

        System.out.println("🎙️ Initializing audio recording...");

        try {
            // Generate filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            fileName = RECORDINGS_DIR + "/emergency_" + timestamp + ".wav";
            System.out.println("📝 Recording file: " + fileName);

            // Clear previous recording data
            recordedAudio.reset();

            // Get the specific microphone mixer (from diagnostics: "Microphone (Realtek Audio)")
            Mixer microphone = null;
            Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
            for (Mixer.Info mixerInfo : mixerInfos) {
                if (mixerInfo.getName().contains("Microphone") &&
                        mixerInfo.getDescription().contains("DirectSound Capture")) {
                    microphone = AudioSystem.getMixer(mixerInfo);
                    System.out.println("🎛️ Using mixer: " + mixerInfo.getName());
                    break;
                }
            }

            // Get microphone line
            DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);

            if (microphone != null && microphone.isLineSupported(dataLineInfo)) {
                targetDataLine = (TargetDataLine) microphone.getLine(dataLineInfo);
                System.out.println("✅ Got microphone line from specific mixer");
            } else {
                // Fallback to system default
                targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
                System.out.println("✅ Got microphone line from system default");
            }

            System.out.println("🔌 Opening audio line...");
            targetDataLine.open(AUDIO_FORMAT);

            System.out.println("▶️ Starting audio line...");
            targetDataLine.start();

            isRecording = true;
            System.out.println("🎙️ Recording started successfully!");

            // Start recording in separate thread with better approach
            recordingThread = new Thread(this::recordAudioToMemory, "AudioRecorderThread");
            recordingThread.setDaemon(false); // Don't make it daemon so it completes
            recordingThread.start();
            System.out.println("🧵 Recording thread started");

        } catch (LineUnavailableException e) {
            System.err.println("❌ Audio line unavailable: " + e.getMessage());
            e.printStackTrace();
            notifyRecordingFailed("Audio line unavailable: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Unexpected error starting recording: " + e.getMessage());
            e.printStackTrace();
            notifyRecordingFailed("Unexpected error starting recording: " + e.getMessage());
        }
    }

    private void recordAudioToMemory() {
        System.out.println("🎵 Starting audio capture to memory...");
        byte[] buffer = new byte[4096];

        try {
            while (isRecording && targetDataLine != null) {
                int bytesRead = targetDataLine.read(buffer, 0, buffer.length);
                if (bytesRead > 0) {
                    recordedAudio.write(buffer, 0, bytesRead);
                    // Print progress every 10 seconds (approximately)
                    if (recordedAudio.size() % (44100 * 2 * 10) < 4096) {
                        System.out.println("📊 Recorded: " + (recordedAudio.size() / (44100 * 2)) + " seconds");
                    }
                }
            }
            System.out.println("🎵 Audio capture completed. Total bytes: " + recordedAudio.size());
        } catch (Exception e) {
            System.err.println("❌ Error during recording: " + e.getMessage());
            e.printStackTrace();
            notifyRecordingFailed("Error during recording: " + e.getMessage());
        }
    }

    public void stopRecording() {
        if (!isRecording) {
            System.out.println("⚠️ No recording in progress.");
            return;
        }

        System.out.println("🛑 Stopping recording...");
        isRecording = false;

        // Stop and close the audio line
        if (targetDataLine != null) {
            System.out.println("⏹️ Stopping audio line...");
            targetDataLine.stop();
            System.out.println("🔌 Closing audio line...");
            targetDataLine.close();
            targetDataLine = null;
        }

        // Wait for recording thread to finish
        if (recordingThread != null && recordingThread.isAlive()) {
            System.out.println("🧵 Waiting for recording thread to finish...");
            try {
                recordingThread.join(5000); // Wait up to 5 seconds
                if (recordingThread.isAlive()) {
                    System.out.println("⚠️ Recording thread did not stop gracefully, interrupting...");
                    recordingThread.interrupt();
                } else {
                    System.out.println("✅ Recording thread finished");
                }
            } catch (InterruptedException e) {
                System.out.println("⚠️ Interrupted while waiting for recording thread to stop");
                Thread.currentThread().interrupt();
            }
        }

        // Save the recorded audio to file
        saveRecordingToFile();
    }

    private void saveRecordingToFile() {
        if (recordedAudio.size() == 0) {
            System.out.println("⚠️ No audio data recorded");
            notifyRecordingFailed("No audio data recorded");
            return;
        }

        try {
            System.out.println("💾 Saving " + recordedAudio.size() + " bytes to file...");

            // Convert byte array to audio input stream
            byte[] audioData = recordedAudio.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
            AudioInputStream audioInputStream = new AudioInputStream(bais, AUDIO_FORMAT, audioData.length / AUDIO_FORMAT.getFrameSize());

            // Save to WAV file
            File audioFile = new File(fileName);
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, audioFile);

            audioInputStream.close();
            bais.close();

            System.out.println("⏹️ Recording saved successfully: " + fileName);
            System.out.println("📁 File location: " + audioFile.getAbsolutePath());
            System.out.println("📊 File size: " + audioFile.length() + " bytes");

            // Verify the file
            if (audioFile.exists() && audioFile.length() > 44) { // WAV header is 44 bytes
                System.out.println("✅ Audio file verification passed");

                // ADDED: Calculate duration and notify listeners
                long durationSeconds = getRecordingDuration();
                long fileSizeBytes = audioFile.length();

                System.out.println("📤 Notifying recording completion listeners...");
                notifyRecordingCompleted(audioFile.getAbsolutePath(), durationSeconds, fileSizeBytes);

            } else {
                System.out.println("❌ Audio file verification failed");
                notifyRecordingFailed("Audio file verification failed");
            }

        } catch (IOException e) {
            System.err.println("❌ Error saving audio file: " + e.getMessage());
            e.printStackTrace();
            notifyRecordingFailed("Error saving audio file: " + e.getMessage());
        }
    }

    public boolean isRecording() {
        return isRecording;
    }

    public String getCurrentFileName() {
        return fileName;
    }

    // Get recording duration
    public long getRecordingDuration() {
        if (fileName != null) {
            File audioFile = new File(fileName);
            if (audioFile.exists() && audioFile.length() > 44) { // WAV header is 44 bytes
                try {
                    AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);
                    AudioFormat format = audioInputStream.getFormat();
                    long frames = audioInputStream.getFrameLength();
                    double durationInSeconds = (frames + 0.0) / format.getFrameRate();
                    audioInputStream.close();
                    return (long) durationInSeconds;
                } catch (Exception e) {
                    System.err.println("❌ Error getting audio duration: " + e.getMessage());
                }
            }
        }

        // Fallback: calculate from recorded data
        if (recordedAudio.size() > 0) {
            long bytesPerSecond = (long) (AUDIO_FORMAT.getSampleRate() * AUDIO_FORMAT.getFrameSize());
            return recordedAudio.size() / bytesPerSecond;
        }

        return 0;
    }

    // Get current recording size in bytes
    public long getCurrentRecordingSize() {
        return recordedAudio.size();
    }

    // Test microphone with this exact configuration
    public static boolean testMicrophone() {
        System.out.println("🧪 Testing microphone with recording configuration...");
        try {
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);

            if (AudioSystem.isLineSupported(info)) {
                TargetDataLine testLine = (TargetDataLine) AudioSystem.getLine(info);
                testLine.open(AUDIO_FORMAT);
                testLine.start();

                // Test for 2 seconds
                byte[] buffer = new byte[4096];
                int totalBytesRead = 0;
                long startTime = System.currentTimeMillis();

                while (System.currentTimeMillis() - startTime < 2000) {
                    int bytesRead = testLine.read(buffer, 0, buffer.length);
                    totalBytesRead += bytesRead;
                }

                testLine.stop();
                testLine.close();

                System.out.println("✅ Microphone test successful, read " + totalBytesRead + " bytes in 2 seconds");
                return totalBytesRead > 0;
            } else {
                System.out.println("❌ Audio format not supported");
                return false;
            }
        } catch (Exception e) {
            System.err.println("❌ Microphone test failed: " + e.getMessage());
            return false;
        }
    }
}