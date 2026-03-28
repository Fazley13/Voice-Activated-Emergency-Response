package com.codevengers.voiceemergency;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.net.URL;

public class Main extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // Debug: Check if file exists
        URL fxmlUrl = getClass().getResource("/com/codevengers/voiceemergency/Dashboard.fxml");
        System.out.println("Looking for FXML at: /com/codevengers/voiceemergency/Dashboard.fxml");
        System.out.println("Found: " + (fxmlUrl != null ? fxmlUrl.toString() : "NOT FOUND"));

        if (fxmlUrl == null) {
            System.err.println("Dashboard.fxml not found! Check file location.");
            System.exit(1);
        }

        Parent root = FXMLLoader.load(fxmlUrl);
        stage.setTitle("QuickRescue");

        // Set Icon
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/codevengers/voiceemergency/sound-sensor.png")));
        } catch (Exception e) {
            System.out.println("Warning: Could not load icon: " + e.getMessage());
        }

        Scene scene = new Scene(root);
        stage.setScene(scene);

        WindowManager.setupMaximizedWindow(stage);

        stage.show();
    }
}