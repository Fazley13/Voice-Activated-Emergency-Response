package com.codevengers.voiceemergency;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class PoliceContactsController implements Initializable {
    
    @FXML private VBox policeContactsContainer;
    @FXML private ScrollPane policeScrollPane;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== POLICE CONTACTS INITIALIZING ===");
        loadPoliceContacts();
    }
    
    private void loadPoliceContacts() {
        try {
            if (policeContactsContainer == null) {
                System.err.println("❌ policeContactsContainer is null!");
                return;
            }
            
            policeContactsContainer.getChildren().clear();
            policeContactsContainer.setSpacing(10);
            policeContactsContainer.setPadding(new Insets(10));
            
            // Add title
            Text titleText = new Text("🚔 Police Emergency Numbers");
            titleText.setFont(Font.font("System", FontWeight.BOLD, 14));
            titleText.setStyle("-fx-fill: #2C3E50;");
            policeContactsContainer.getChildren().add(titleText);
            
            // Add separator
            Separator separator = new Separator();
            separator.setStyle("-fx-background-color: #BDC3C7;");
            policeContactsContainer.getChildren().add(separator);
            
            // Get all divisions and create expandable sections
            List<String> divisions = PoliceContactService.getAllDivisions();
            
            for (String division : divisions) {
                createDivisionSection(division);
            }
            
            System.out.println("✅ Police contacts loaded successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error loading police contacts: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void createDivisionSection(String division) {
        try {
            // Create collapsible section using TitledPane (JDK 8 compatible)
            TitledPane divisionPane = new TitledPane();
            divisionPane.setText("📍 " + division + " Division");
            divisionPane.setExpanded(false);
            divisionPane.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            
            // Create content for this division
            VBox divisionContent = new VBox(5);
            divisionContent.setPadding(new Insets(5));
            
            List<PoliceContact> contacts = PoliceContactService.getContactsByDivision(division);
            
            for (PoliceContact contact : contacts) {
                HBox contactRow = createContactRow(contact);
                divisionContent.getChildren().add(contactRow);
            }
            
            divisionPane.setContent(divisionContent);
            policeContactsContainer.getChildren().add(divisionPane);
            
        } catch (Exception e) {
            System.err.println("❌ Error creating division section for " + division + ": " + e.getMessage());
        }
    }
    
    private HBox createContactRow(PoliceContact contact) {
        HBox contactRow = new HBox(10);
        contactRow.setAlignment(Pos.CENTER_LEFT);
        contactRow.setPadding(new Insets(3, 5, 3, 5));
        contactRow.setStyle("-fx-background-color: #F8F9FA; -fx-background-radius: 5;");
        
        // Contact info
        VBox contactInfo = new VBox(2);
        
        Text areaText = new Text(contact.getArea());
        areaText.setFont(Font.font("System", FontWeight.BOLD, 11));
        areaText.setStyle("-fx-fill: #2C3E50;");
        
        Text numberText = new Text("📞 " + contact.getContactNumber());
        numberText.setFont(Font.font("System", 10));
        numberText.setStyle("-fx-fill: #27AE60;");
        
        contactInfo.getChildren().addAll(areaText, numberText);
        
        // Call button
        Button callButton = new Button("📞 Call");
        callButton.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-size: 9px; -fx-padding: 2 8 2 8;");
        callButton.setOnAction(e -> handleCallContact(contact));
        
        contactRow.getChildren().addAll(contactInfo, callButton);
        
        // Set HBox to expand contactInfo and keep button on right
        HBox.setHgrow(contactInfo, javafx.scene.layout.Priority.ALWAYS);
        
        return contactRow;
    }
    
    private void handleCallContact(PoliceContact contact) {
        try {
            System.out.println("📞 Attempting to call: " + contact.getContactNumber());
            
            // Show confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Call Police");
            confirmAlert.setHeaderText("Call " + contact.getArea() + "?");
            confirmAlert.setContentText("Number: " + contact.getContactNumber() + "\n" +
                    "Division: " + contact.getDivision() + "\n" +
                    "Description: " + contact.getDescription() + "\n\n" +
                    "This will attempt to open your default phone application or dialer.");
            
            ButtonType callButton = new ButtonType("📞 Call Now");
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmAlert.getButtonTypes().setAll(callButton, cancelButton);
            
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == callButton) {
                    try {
                        // Try to open tel: URI (works on systems with phone capability)
                        if (Desktop.isDesktopSupported()) {
                            Desktop.getDesktop().browse(new URI("tel:" + contact.getContactNumber()));
                        } else {
                            // Fallback: copy to clipboard and show number
                            showContactDetails(contact);
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Error opening dialer: " + e.getMessage());
                        showContactDetails(contact);
                    }
                }
            });
            
        } catch (Exception e) {
            System.err.println("❌ Error handling call contact: " + e.getMessage());
            showContactDetails(contact);
        }
    }
    
    private void showContactDetails(PoliceContact contact) {
        Alert detailAlert = new Alert(Alert.AlertType.INFORMATION);
        detailAlert.setTitle("Police Contact Details");
        detailAlert.setHeaderText(contact.getArea());
        detailAlert.setContentText("Phone Number: " + contact.getContactNumber() + "\n" +
                "Division: " + contact.getDivision() + "\n" +
                "Type: " + contact.getEmergencyType() + "\n" +
                "Description: " + contact.getDescription() + "\n\n" +
                "Please dial this number manually from your phone.");
        
        // Make the dialog resizable for better text display
        detailAlert.setResizable(true);
        detailAlert.getDialogPane().setPrefWidth(400);
        
        detailAlert.showAndWait();
    }
    
    // Method to refresh the police contacts (can be called from parent controller)
    public void refreshPoliceContacts() {
        Platform.runLater(this::loadPoliceContacts);
    }
}
