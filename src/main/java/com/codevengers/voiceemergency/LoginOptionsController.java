package com.codevengers.voiceemergency;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;

public class LoginOptionsController {

    @FXML
    private void handleUserLogin(ActionEvent event) {
        navigateToPage(event, "/com/codevengers/voiceemergency/UserLogin.fxml", "User Login");
    }

    @FXML
    private void handleAdminLogin(ActionEvent event) {
        navigateToPage(event, "/com/codevengers/voiceemergency/AdminLogin.fxml", "Admin Login");
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        navigateToPage(event, "/com/codevengers/voiceemergency/Dashboard.fxml", "Voice Activated Emergency Response - Dashboard");
    }

    private void navigateToPage(ActionEvent event, String fxmlFile, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            
            Platform.runLater(() -> {
                WindowManager.maximizeWindow(stage);
            });
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error navigating to " + fxmlFile + ": " + e.getMessage());
        }
    }
}