package com.codevengers.voiceemergency;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class AddEmergencyContact extends Application {

    private String username = "User";
    private String userEmail = "test@example.com";

    public AddEmergencyContact() {}

    public AddEmergencyContact(String username, String userEmail) {
        this.username = username;
        this.userEmail = userEmail;
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("add_emergency_contact.fxml"));
        Parent root = loader.load();

        AddEmergencyContactController controller = loader.getController();
        controller.setUserInfo(username, userEmail);

        stage.setTitle("Add Emergency Contact");
        stage.setScene(new Scene(root, 350, 250));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
