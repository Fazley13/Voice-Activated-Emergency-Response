package com.codevengers.voiceemergency;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;

public class VoiceDetector implements Runnable {

    @Override
    public void run() {
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

            System.out.println("🎤 Listening for keywords: help me, fire, robbery, attack");

            while (true) {
                SpeechResult result = recognizer.getResult();
                if (result != null) {
                    String command = result.getHypothesis().toLowerCase();
                    System.out.println("👂 Heard: " + command);

                    switch (command) {
                        case "help me":
                        case "fire":
                        case "robbery":
                        case "attack":
                            if (!EmergencyHandler.isEmergencyTriggered) {
                                System.out.println("🚨 Keyword detected! Triggering emergency...");
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
            System.err.println("❌ ERROR in VoiceDetector: " + e.getMessage());
        }
    }
}
