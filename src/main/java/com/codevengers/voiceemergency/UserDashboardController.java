package com.codevengers.voiceemergency;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class UserDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Button updateProfileBtn;
    @FXML private Button emergencyContactsBtn;
    @FXML private Button addContactBtn;
    @FXML private Button sosBtn;
    @FXML private Button logoutBtn;

    private String username;

    public void setUsername(String username) {
        this.username = username;
        welcomeLabel.setText("Welcome, " + username + "!");
    }

    @FXML
    public void initialize() {
        

        emergencyContactsBtn.setOnAction(e -> {
            EmergencyContacts contactsPage = new EmergencyContacts(username);
            try {
                contactsPage.start(new Stage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        sosBtn.setOnAction(e -> {
            System.out.println("SOS Trigger clicked");
        });

        logoutBtn.setOnAction(e -> {
            // Close the window
            Stage stage = (Stage) logoutBtn.getScene().getWindow();
            stage.close();
        });

        addContactBtn.setOnAction(e -> {
            System.out.println("Add Contact Clicked (Not Implemented)");
        });
    }
}
