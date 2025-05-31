package com.codevengers.voiceemergency;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class PanicButtonLauncher extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Start voice detection in a separate thread
        Thread voiceDetectionThread = new Thread(() -> {
            try {
                SpeechRecognitionTest.startVoiceDetection();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        voiceDetectionThread.setDaemon(true);
        voiceDetectionThread.start();


        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codevengers/voiceemergency/panic_button.fxml"));
        Parent root = loader.load();

        primaryStage.setTitle("Panic Button");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }


    public void handlePanicButtonClick() {
        if (!EmergencyHandler.isEmergencyTriggered) {
            System.out.println("🚨 Panic Button Pressed!");
            EmergencyHandler.triggerEmergency();
        } else {
            System.out.println("🚫 Emergency already triggered.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
