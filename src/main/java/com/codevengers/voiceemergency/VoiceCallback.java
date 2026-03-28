package com.codevengers.voiceemergency;

public interface VoiceCallback {
    void onVoiceDetected(String command);
    void onSilenceDetected();
}
