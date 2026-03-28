package com.codevengers.voiceemergency;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class AlertSound {

    public static void playAlert() {
        try {
            // Generate a simple beep sound
            int sampleRate = 8000;
            int duration = 1000; // 1 second
            byte[] buffer = new byte[sampleRate * duration / 1000];

            for (int i = 0; i < buffer.length; i++) {
                double angle = i / (sampleRate / 440.0) * 2.0 * Math.PI; // 440 Hz tone
                buffer[i] = (byte) (Math.sin(angle) * 127.0);
            }

            AudioFormat format = new AudioFormat(sampleRate, 8, 1, true, true);
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
            AudioInputStream audioInputStream = new AudioInputStream(bais, format, buffer.length);

            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();

            System.out.println("🔊 Alert sound played");

        } catch (LineUnavailableException | IOException e) {
            System.err.println("❌ Error playing alert sound: " + e.getMessage());
        }
    }
}
