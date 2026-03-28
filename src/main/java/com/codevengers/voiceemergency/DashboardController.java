package com.codevengers.voiceemergency;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Platform;

public class DashboardController {

    @FXML
    private void handleUserRegisterClick(ActionEvent event) {
        navigateToPage(event, "/com/codevengers/voiceemergency/UserRegister.fxml", "User Registration");
    }

    @FXML
    private void handleAdminRegisterClick(ActionEvent event) {
        // ✅ FIXED: Added absolute path
        navigateToPage(event, "/com/codevengers/voiceemergency/AdminRegister.fxml", "Admin Registration");
    }

    @FXML
    private void handleUserLoginClick(ActionEvent event) {
        navigateToPage(event, "/com/codevengers/voiceemergency/UserLogin.fxml", "User Login");
    }

    @FXML
    private void handleAdminLoginClick(ActionEvent event) {
        navigateToPage(event, "/com/codevengers/voiceemergency/AdminLogin.fxml", "Admin Login");
    }

    @FXML
    private void showLoginOptions(ActionEvent event) {
        navigateToPage(event, "/com/codevengers/voiceemergency/LoginOptions.fxml", "Login Options");
    }

    @FXML
    private void showRegisterOptions(ActionEvent event) {
        navigateToPage(event, "/com/codevengers/voiceemergency/RegisterOptions.fxml", "Registration Options");
    }

    @FXML
    private void handleLoginButton(ActionEvent event) {
        navigateToPage(event, "/com/codevengers/voiceemergency/LoginOptions.fxml", "Login Options");
    }

    @FXML
    private void handleAccessPortal(ActionEvent event) {
        navigateToPage(event, "/com/codevengers/voiceemergency/LoginOptions.fxml", "Login Options");
    }

    @FXML
    private void handleGetStarted(ActionEvent event) {
        navigateToPage(event, "/com/codevengers/voiceemergency/RegisterOptions.fxml", "Registration Options");
    }

    @FXML
    private void handleStartYourJourney(ActionEvent event) {
        // ✅ FIXED: Added absolute path
        navigateToPage(event, "/com/codevengers/voiceemergency/RegisterOptions.fxml", "Registration Options");
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
            System.err.println("Error loading FXML file: " + fxmlFile);
            e.printStackTrace();
        }
    }
}