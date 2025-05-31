package com.codevengers.voiceemergency;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EmergencyContacts extends Application {

    private static String userEmail = "test@example.com"; // Default for testing

    public EmergencyContacts(String email) {
        userEmail = email;
    }

    public EmergencyContacts() {} // Default constructor

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("emergency_contacts.fxml"));
        Parent root = loader.load();

        // Pass email to controller
        EmergencyContactsController controller = loader.getController();
        controller.setUserEmail(userEmail);
        controller.loadContacts();

        stage.setTitle("Emergency Contacts");
        stage.setScene(new Scene(root, 400, 300));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
