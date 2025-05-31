package com.codevengers.voiceemergency;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioRecorder {
    private static TargetDataLine microphone;
    private static boolean isRecording = false;

    // Start recording
    public static void startRecording() {
        new Thread(() -> {
            try {
                // Set up the microphone and the audio format
                AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                if (!AudioSystem.isLineSupported(info)) {
                    System.err.println("Microphone not supported");
                    return;
                }

                microphone = (TargetDataLine) AudioSystem.getLine(info);
                microphone.open(format);
                microphone.start();
                isRecording = true;

                // Set up output file
                File file = new File("emergency_audio.wav");
                AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;

                // Record the audio until user stops it
                System.out.println("Recording started...");
                AudioSystem.write(new AudioInputStream(microphone), fileType, file);
            } catch (LineUnavailableException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Stop recording
    public static void stopRecording() {
        if (microphone != null && isRecording) {
            microphone.stop();
            microphone.close();
            isRecording = false;
            System.out.println("Recording stopped. File saved as 'emergency_audio.wav'");
        }
    }
}
