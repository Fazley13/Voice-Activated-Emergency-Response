package com.codevengers.voiceemergency;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;

import java.util.Timer;
import java.util.TimerTask;

public class SpeechRecognitionTest {

    private static final long SILENCE_THRESHOLD_MS = 60000;  // 60 seconds of silence
    private static long lastVoiceDetectionTime = System.currentTimeMillis();

    public static void startVoiceDetection() {
        try {
            Configuration configuration = new Configuration();
            configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
            configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
            configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

            configuration.setGrammarPath("resource:/grammar");
            configuration.setGrammarName("emergency");
            configuration.setUseGrammar(true);

            LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
            recognizer.startRecognition(true);

            // Silence timer
            Timer silenceTimer = new Timer(true);
            silenceTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (System.currentTimeMillis() - lastVoiceDetectionTime > SILENCE_THRESHOLD_MS) {
                        if (!EmergencyHandler.isEmergencyTriggered) {
                            System.out.println("⏰ No voice detected for 60 seconds! Triggering panic alert!");
                            EmergencyHandler.triggerEmergency();
                        }
                    }
                }
            }, 0, 1000);

            System.out.println("🎤 Listening for keywords: help me, fire, robbery, attack");

            while (true) {
                SpeechResult result = recognizer.getResult();
                if (result != null) {
                    lastVoiceDetectionTime = System.currentTimeMillis();
                    String command = result.getHypothesis().toLowerCase();
                    System.out.println("👂 Heard: " + command);

                    switch (command) {
                        case "help me":
                        case "fire":
                        case "robbery":
                        case "attack":
                            if (!EmergencyHandler.isEmergencyTriggered) {
                                System.out.println("🚨 EMERGENCY: " + command.toUpperCase() + " DETECTED!");
                                EmergencyHandler.triggerEmergency();
                            } else {
                                System.out.println("⚠️ Emergency already triggered.");
                            }
                            break;
                        default:
                            System.out.println("⚠️ Unknown command.");
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
