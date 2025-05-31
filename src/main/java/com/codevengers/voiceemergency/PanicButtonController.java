package com.codevengers.voiceemergency;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class PanicButtonController {

    @FXML
    private Button panicButton;

    // Handle the Panic Button click event
    @FXML
    private void handlePanicButtonClick(ActionEvent event) {
        System.out.println("🚨 Panic Button Clicked!");

        // Trigger the emergency actions
        EmergencyHandler.triggerEmergency();
    }
}
