package com.codevengers.voiceemergency;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;

public class RegisterOptionsController {

    @FXML
    private void handleUserRegistration(ActionEvent event) {
        // ✅ FIXED: Added absolute path
        navigateToPage(event, "/com/codevengers/voiceemergency/UserRegister.fxml", "User Registration");
    }

    @FXML
    private void handleAdminRegistration(ActionEvent event) {
        // ✅ FIXED: Added absolute path
        navigateToPage(event, "/com/codevengers/voiceemergency/AdminRegister.fxml", "Admin Registration");
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        // ✅ FIXED: Added absolute path
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