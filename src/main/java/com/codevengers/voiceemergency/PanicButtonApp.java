package com.codevengers.voiceemergency;

import javafx.application.Application;
import javafx.stage.Stage;

public class PanicButtonApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        System.out.println("✅ Application started. Listening for voice or button...");

        // Start voice detection thread
        Thread voiceThread = new Thread(new VoiceDetector());
        voiceThread.setDaemon(true);  // Ends when app exits
        voiceThread.start();

        // Panic button simulation0
        System.out.println("Press 'P' to manually trigger panic...");

        java.util.Scanner scanner = new java.util.Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("P")) {
                System.out.println("🖐️ Manual panic activated.");
                EmergencyHandler.triggerEmergency();
                break;  // Optional: break or continue listening
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
