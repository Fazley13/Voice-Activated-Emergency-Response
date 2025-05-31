package com.codevengers.voiceemergency;

import javax.sound.sampled.*;

public class AlertSound {
    public static void playAlert() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                    AlertSound.class.getResource("/beep-02.wav")
            );

            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();

            Thread.sleep(clip.getMicrosecondLength() / 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
