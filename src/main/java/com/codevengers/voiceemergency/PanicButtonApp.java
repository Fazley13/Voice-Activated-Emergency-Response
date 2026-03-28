package com.codevengers.voiceemergency;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PanicButtonApp extends Application {

    private TextArea logArea;
    private Label statusLabel;
    private Button panicButton;
    private Button stopButton;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Emergency Voice Helper");

        // Create UI components
        statusLabel = new Label("🟢 System Ready - Listening for voice commands...");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        panicButton = new Button("🚨 PANIC BUTTON");
        panicButton.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        panicButton.setPrefSize(200, 50);
        panicButton.setOnAction(e -> triggerPanic());

        stopButton = new Button("⏹️ STOP EMERGENCY");
        stopButton.setStyle("-fx-background-color: #44ff44; -fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: bold;");
        stopButton.setPrefSize(200, 40);
        stopButton.setDisable(true);
        stopButton.setOnAction(e -> stopEmergency());

        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefRowCount(15);
        logArea.setStyle("-fx-font-family: monospace;");

        // Layout
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(statusLabel, panicButton, stopButton, logArea);

        Scene scene = new Scene(root, 500, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Start voice detection thread
        Thread voiceThread = new Thread(new VoiceDetector());
        voiceThread.setDaemon(true);
        voiceThread.start();

        addLog("✅ Application started successfully!");
        addLog("🎤 Voice detection active - Keywords: help me, fire, robbery, attack");
        addLog("🖐️ Manual panic button ready");

        // Handle window closing
        primaryStage.setOnCloseRequest(e -> {
            if (EmergencyHandler.isEmergencyTriggered()) {
                EmergencyHandler.stopEmergency();
            }
            Platform.exit();
        });
    }

    private void triggerPanic() {
        addLog("🖐️ Manual panic button pressed!");
        panicButton.setDisable(true);
        stopButton.setDisable(false);
        statusLabel.setText("🔴 EMERGENCY ACTIVE - Recording in progress...");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: red;");

        EmergencyHandler.triggerEmergency();
    }

    private void stopEmergency() {
        addLog("🛑 Emergency stopped by user");
        panicButton.setDisable(false);
        stopButton.setDisable(true);
        statusLabel.setText("🟢 System Ready - Listening for voice commands...");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: green;");

        EmergencyHandler.stopEmergency();
    }

    private void addLog(String message) {
        Platform.runLater(() -> {
            String timestamp = java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            logArea.appendText("[" + timestamp + "] " + message + "\n");
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}