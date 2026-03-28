package com.codevengers.voiceemergency;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;

public class SpeechRecognitionTest {

    public static void main(String[] args) {
        try {
            startVoiceDetection();
        } catch (Exception e) {
            System.err.println("❌ Error in main: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void startVoiceDetection() {
        try {
            System.out.println("🎤 Starting voice detection...");

            Configuration configuration = new Configuration();
            configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
            configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
            configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

            // Try to use grammar if available
            try {
                configuration.setGrammarPath("resource:/grammar");
                configuration.setGrammarName("emergency");
                configuration.setUseGrammar(true);
                System.out.println("✅ Using emergency grammar");
            } catch (Exception e) {
                System.out.println("⚠️ Grammar not found, using default language model");
                configuration.setUseGrammar(false);
            }

            LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
            recognizer.startRecognition(true);

            System.out.println("🎤 Voice detection active - Say 'help me', 'fire', 'robbery', 'attack', etc.");

            while (true) {
                SpeechResult result = recognizer.getResult();
                if (result != null) {
                    String command = result.getHypothesis().toLowerCase().trim();
                    System.out.println("👂 Voice detected: " + command);

                    // Check for emergency keywords
                    if (isEmergencyKeyword(command)) {
                        System.out.println("🚨 EMERGENCY KEYWORD DETECTED: " + command.toUpperCase());

                        if (!EnhancedEmergencyHandler.isEmergencyTriggered()) {
                            // Trigger emergency
                            String emergencyType = determineEmergencyType(command);
                            String description = "Voice-activated emergency: '" + command + "'";
                            EnhancedEmergencyHandler.triggerEmergency("voice", emergencyType, description);
                        } else {
                            System.out.println("⚠️ Emergency already triggered, ignoring voice command");
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Voice detection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method with callback support for dashboard integration
    public static void startVoiceDetectionWithCallback(VoiceCallback callback) {
        try {
            System.out.println("🎤 Starting voice detection with callback...");

            Configuration configuration = new Configuration();
            configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
            configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
            configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

            // Try to use grammar if available
            try {
                configuration.setGrammarPath("resource:/grammar");
                configuration.setGrammarName("emergency");
                configuration.setUseGrammar(true);
                System.out.println("✅ Using emergency grammar");
            } catch (Exception e) {
                System.out.println("⚠️ Grammar not found, using default language model");
                configuration.setUseGrammar(false);
            }

            LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
            recognizer.startRecognition(true);

            System.out.println("🎤 Voice detection active with callback - Listening for keywords");

            // Silence detection variables
            long lastVoiceDetectionTime = System.currentTimeMillis();
            final long SILENCE_THRESHOLD_MS = 60000; // 60 seconds

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    SpeechResult result = recognizer.getResult();

                    if (result != null) {
                        lastVoiceDetectionTime = System.currentTimeMillis();
                        String command = result.getHypothesis().toLowerCase().trim();

                        System.out.println("👂 Voice detected: " + command);

                        // Notify callback
                        if (callback != null) {
                            callback.onVoiceDetected(command);
                        }
                    }

                    // Check for silence timeout
                    if (System.currentTimeMillis() - lastVoiceDetectionTime > SILENCE_THRESHOLD_MS) {
                        System.out.println("⏰ Silence timeout detected");
                        if (callback != null) {
                            callback.onSilenceDetected();
                        }
                        // Reset silence timer
                        lastVoiceDetectionTime = System.currentTimeMillis();
                    }

                    // Small delay to prevent excessive CPU usage
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    System.out.println("🛑 Voice detection interrupted");
                    break;
                } catch (Exception e) {
                    System.err.println("❌ Voice detection error: " + e.getMessage());
                    Thread.sleep(1000); // Wait before retrying
                }
            }

            recognizer.stopRecognition();
            System.out.println("🎤 Voice detection stopped");

        } catch (Exception e) {
            System.err.println("❌ Voice detection initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean isEmergencyKeyword(String command) {
        if (command == null) return false;

        String[] emergencyKeywords = {
                "help me", "help", "emergency", "fire", "robbery", "attack",
                "danger", "police", "ambulance", "medical", "hurt", "injured"
        };

        for (String keyword : emergencyKeywords) {
            if (command.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static String determineEmergencyType(String voiceCommand) {
        if (voiceCommand == null) return "VOICE_EMERGENCY";

        voiceCommand = voiceCommand.toLowerCase();

        if (voiceCommand.contains("fire")) {
            return "FIRE_EMERGENCY";
        } else if (voiceCommand.contains("robbery") || voiceCommand.contains("attack")) {
            return "SECURITY_EMERGENCY";
        } else if (voiceCommand.contains("medical") || voiceCommand.contains("hurt") || voiceCommand.contains("injured")) {
            return "MEDICAL_EMERGENCY";
        } else if (voiceCommand.contains("police")) {
            return "POLICE_NEEDED";
        } else {
            return "VOICE_EMERGENCY";
        }
    }
}
