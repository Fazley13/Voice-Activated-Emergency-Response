package com.codevengers.voiceemergency;

import javax.sound.sampled.AudioFormat;

public class AudioUtils {
    /**
     * Returns the audio format for recording and streaming.
     */
    public static AudioFormat getAudioFormat() {
        // 44100 Hz sample rate, 16-bit, mono, signed, little endian
        return new AudioFormat(44100.0f, 16, 1, true, false);
    }
}
