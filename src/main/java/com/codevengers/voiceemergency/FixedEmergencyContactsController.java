package com.codevengers.voiceemergency;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class FixedEmergencyContactsController implements Initializable {

    @FXML private Text userNameLabel;
    @FXML private Text contactCountLabel;
    @FXML private Button addContactBtn;
    @FXML private Button backToDashboardBtn;
    @FXML private TableView<EmergencyContact> contactsTable;
    @FXML private TableColumn<EmergencyContact, String> nameColumn;
    @FXML private TableColumn<EmergencyContact, String> phoneColumn;
    @FXML private TableColumn<EmergencyContact, String> emailColumn;
    @FXML private TableColumn<EmergencyContact, String> relationshipColumn;
    @FXML private TableColumn<EmergencyContact, Boolean> primaryColumn;
    @FXML private TableColumn<EmergencyContact, String> actionsColumn;

    // Add/Edit Contact Form
    @FXML private VBox addContactForm;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> relationshipCombo;
    @FXML private CheckBox primaryCheckBox;
    @FXML private Button saveContactBtn;
    @FXML private Button cancelBtn;
    @FXML private Text formTitleText;

    private EmergencyContact editingContact = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== EMERGENCY CONTACTS INITIALIZING ===");

        // Test database connection first
        if (!FixedEmergencyContactDAO.testDatabaseConnection()) {
            showErrorAlert("Database Error",
                    "Cannot connect to database. Please ensure:\n" +
                            "1. XAMPP is running\n" +
                            "2. MySQL service is started\n" +
                            "3. Database 'voice_emergency_response' exists");
            return;
        }

        // Initialize emergency contacts table
        FixedEmergencyContactDAO.initializeEmergencyContactTable();

        // Check if user is logged in
        if (!UserSession.isLoggedIn()) {
            System.err.println("❌ User not logged in! Redirecting to login...");
            redirectToLogin();
            return;
        }

        // Debug user session
        System.out.println("🔍 User Session Debug:");
        System.out.println("   User ID: " + UserSession.getCurrentUserId());
        System.out.println("   Username: " + UserSession.getCurrentUsername());
        System.out.println("   Logged in: " + UserSession.isLoggedIn());

        // Set user name
        if (userNameLabel != null) {
            userNameLabel.setText(UserSession.getCurrentUsername() + "'s Emergency Contacts");
        }

        // Initialize relationship dropdown
        initializeRelationshipCombo();

        // Initialize table columns
        initializeTableColumns();

        // Hide add contact form initially
        if (addContactForm != null) {
            addContactForm.setVisible(false);
        }

        // Load emergency contacts
        loadEmergencyContacts();

        System.out.println("✅ Emergency contacts page initialized successfully");
    }

    private void initializeRelationshipCombo() {
        if (relationshipCombo != null) {
            relationshipCombo.setItems(FXCollections.observableArrayList(
                    "Family Member",
                    "Spouse/Partner",
                    "Parent",
                    "Child",
                    "Sibling",
                    "Friend",
                    "Colleague",
                    "Neighbor",
                    "Doctor",
                    "Emergency Service",
                    "Other"
            ));
            relationshipCombo.setValue("Family Member");
        }
    }

    private void initializeTableColumns() {
        if (contactsTable != null) {
            if (nameColumn != null) {
                nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
            }

            if (phoneColumn != null) {
                phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
            }

            if (emailColumn != null) {
                emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
            }

            if (relationshipColumn != null) {
                relationshipColumn.setCellValueFactory(new PropertyValueFactory<>("relationship"));
            }

            if (primaryColumn != null) {
                primaryColumn.setCellValueFactory(new PropertyValueFactory<>("primary"));
                primaryColumn.setCellFactory(col -> new TableCell<EmergencyContact, Boolean>() {
                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                        } else {
                            setText(item != null && item ? "✅ Primary" : "");
                        }
                    }
                });
            }

            if (actionsColumn != null) {
                actionsColumn.setCellFactory(col -> new TableCell<EmergencyContact, String>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    private final Button primaryBtn = new Button("Set Primary");
                    private final VBox buttonBox = new VBox(2, editBtn, deleteBtn, primaryBtn);

                    {
                        editBtn.setOnAction(e -> {
                            EmergencyContact contact = getTableView().getItems().get(getIndex());
                            editContact(contact);
                        });

                        deleteBtn.setOnAction(e -> {
                            EmergencyContact contact = getTableView().getItems().get(getIndex());
                            deleteContact(contact);
                        });

                        primaryBtn.setOnAction(e -> {
                            EmergencyContact contact = getTableView().getItems().get(getIndex());
                            setPrimaryContact(contact);
                        });

                        editBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 9px;");
                        deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 9px;");
                        primaryBtn.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-font-size: 9px;");
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            EmergencyContact contact = getTableView().getItems().get(getIndex());
                            if (contact.isPrimary()) {
                                primaryBtn.setVisible(false);
                            } else {
                                primaryBtn.setVisible(true);
                            }
                            setGraphic(buttonBox);
                        }
                    }
                });
            }
        }
    }

    private void loadEmergencyContacts() {
        if (contactsTable != null && UserSession.isLoggedIn()) {
            try {
                System.out.println("🔄 Loading emergency contacts for user ID: " + UserSession.getCurrentUserId());

                List<EmergencyContact> contacts = FixedEmergencyContactDAO.getEmergencyContactsByUserId(
                        UserSession.getCurrentUserId()
                );

                ObservableList<EmergencyContact> contactData = FXCollections.observableArrayList(contacts);
                contactsTable.setItems(contactData);

                // Update contact count
                if (contactCountLabel != null) {
                    contactCountLabel.setText("Total Contacts: " + contacts.size());
                }

                System.out.println("✅ Loaded " + contacts.size() + " emergency contacts");

            } catch (Exception e) {
                System.err.println("❌ Error loading emergency contacts: " + e.getMessage());
                e.printStackTrace();
                showErrorAlert("Error", "Could not load emergency contacts: " + e.getMessage());
            }
        }
    }

    @FXML
    private void showAddContactForm() {
        editingContact = null;
        clearForm();

        if (formTitleText != null) {
            formTitleText.setText("Add New Emergency Contact");
        }

        if (addContactForm != null) {
            addContactForm.setVisible(true);
        }

        if (nameField != null) {
            nameField.requestFocus();
        }
    }

    @FXML
    private void hideAddContactForm() {
        if (addContactForm != null) {
            addContactForm.setVisible(false);
        }
        clearForm();
        editingContact = null;
    }

    @FXML
    private void saveContact() {
        try {
            System.out.println("💾 Attempting to save contact...");

            // Validate form
            if (!validateForm()) {
                return;
            }

            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField.getText().trim();
            String relationship = relationshipCombo.getValue();
            boolean isPrimary = primaryCheckBox.isSelected();

            System.out.println("📋 Contact Details:");
            System.out.println("   Name: " + name);
            System.out.println("   Phone: " + phone);
            System.out.println("   Email: " + email);
            System.out.println("   Relationship: " + relationship);
            System.out.println("   Primary: " + isPrimary);
            System.out.println("   User ID: " + UserSession.getCurrentUserId());

            if (editingContact == null) {
                // Add new contact
                EmergencyContact newContact = new EmergencyContact(
                        UserSession.getCurrentUserId(),
                        name, phone, email, relationship, isPrimary
                );

                System.out.println("🔄 Calling FixedEmergencyContactDAO.addEmergencyContact()...");
                int contactId = FixedEmergencyContactDAO.addEmergencyContact(newContact);

                if (contactId > 0) {
                    System.out.println("✅ Contact saved successfully with ID: " + contactId);

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Contact Added");
                    successAlert.setHeaderText("Emergency Contact Added Successfully");
                    successAlert.setContentText("Contact '" + name + "' has been added to your emergency contacts.\n\nContact ID: " + contactId);
                    successAlert.show();

                    hideAddContactForm();
                    loadEmergencyContacts();
                } else {
                    System.err.println("❌ Failed to save contact - contactId: " + contactId);
                    showErrorAlert("Error", "Could not add emergency contact. Please check:\n" +
                            "1. Database connection\n" +
                            "2. All required fields are filled\n" +
                            "3. Phone number format is valid\n\n" +
                            "Try again or contact support if the problem persists.");
                }
            } else {
                // Update existing contact
                editingContact.setName(name);
                editingContact.setPhoneNumber(phone);
                editingContact.setEmail(email);
                editingContact.setRelationship(relationship);
                editingContact.setPrimary(isPrimary);

                boolean success = FixedEmergencyContactDAO.updateEmergencyContact(editingContact);

                if (success) {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Contact Updated");
                    successAlert.setHeaderText("Emergency Contact Updated Successfully");
                    successAlert.setContentText("Contact '" + name + "' has been updated.");
                    successAlert.show();

                    hideAddContactForm();
                    loadEmergencyContacts();
                } else {
                    showErrorAlert("Error", "Could not update emergency contact. Please try again.");
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Error saving contact: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Error", "Could not save contact: " + e.getMessage() + "\n\nPlease check your database connection and try again.");
        }
    }

    private void editContact(EmergencyContact contact) {
        editingContact = contact;

        // Fill form with contact data
        if (nameField != null) nameField.setText(contact.getName());
        if (phoneField != null) phoneField.setText(contact.getPhoneNumber());
        if (emailField != null) emailField.setText(contact.getEmail() != null ? contact.getEmail() : "");
        if (relationshipCombo != null) relationshipCombo.setValue(contact.getRelationship());
        if (primaryCheckBox != null) primaryCheckBox.setSelected(contact.isPrimary());

        if (formTitleText != null) {
            formTitleText.setText("Edit Emergency Contact");
        }

        if (addContactForm != null) {
            addContactForm.setVisible(true);
        }

        if (nameField != null) {
            nameField.requestFocus();
        }
    }

    private void deleteContact(EmergencyContact contact) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Contact");
        confirmAlert.setHeaderText("Delete Emergency Contact?");
        confirmAlert.setContentText("Are you sure you want to delete '" + contact.getName() + "' from your emergency contacts?" + "\n\n" +
                "This action cannot be undone.");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean success = FixedEmergencyContactDAO.deleteEmergencyContact(contact.getId(), UserSession.getCurrentUserId());

            if (success) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Contact Deleted");
                successAlert.setHeaderText("Emergency Contact Deleted");
                successAlert.setContentText("Contact '" + contact.getName() + "' has been removed from your emergency contacts.");
                successAlert.show();

                loadEmergencyContacts();
            } else {
                showErrorAlert("Error", "Could not delete emergency contact. Please try again.");
            }
        }
    }

    private void setPrimaryContact(EmergencyContact contact) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Set Primary Contact");
        confirmAlert.setHeaderText("Set as Primary Emergency Contact?");
        confirmAlert.setContentText("Do you want to set '" + contact.getName() + "' as your primary emergency contact?" + "\n\n" +
                "This contact will be notified first during emergencies.");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            contact.setPrimary(true);
            boolean success = FixedEmergencyContactDAO.updateEmergencyContact(contact);

            if (success) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Primary Contact Set");
                successAlert.setHeaderText("Primary Contact Updated");
                successAlert.setContentText("'" + contact.getName() + "' is now your primary emergency contact.");
                successAlert.show();

                loadEmergencyContacts();
            } else {
                showErrorAlert("Error", "Could not set primary contact. Please try again.");
            }
        }
    }

    private boolean validateForm() {
        if (nameField == null || nameField.getText().trim().isEmpty()) {
            showErrorAlert("Validation Error", "Please enter a contact name.");
            if (nameField != null) nameField.requestFocus();
            return false;
        }

        if (phoneField == null || phoneField.getText().trim().isEmpty()) {
            showErrorAlert("Validation Error", "Please enter a phone number.");
            if (phoneField != null) phoneField.requestFocus();
            return false;
        }

        // Validate phone number format (basic validation)
        String phone = phoneField.getText().trim();
        if (!phone.matches("^[+]?[0-9\\s\\-\\(\\)]{10,}$")) {
            showErrorAlert("Validation Error", "Please enter a valid phone number (at least 10 digits).\n\nAccepted formats:\n• +1234567890\n• 123-456-7890\n• (123) 456-7890\n• 1234567890");
            if (phoneField != null) phoneField.requestFocus();
            return false;
        }

        if (relationshipCombo == null || relationshipCombo.getValue() == null) {
            showErrorAlert("Validation Error", "Please select a relationship.");
            if (relationshipCombo != null) relationshipCombo.requestFocus();
            return false;
        }

        // Validate email if provided
        if (emailField != null && !emailField.getText().trim().isEmpty()) {
            String email = emailField.getText().trim();
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                showErrorAlert("Validation Error", "Please enter a valid email address.");
                emailField.requestFocus();
                return false;
            }
        }

        return true;
    }

    private void clearForm() {
        if (nameField != null) nameField.clear();
        if (phoneField != null) phoneField.clear();
        if (emailField != null) emailField.clear();
        if (relationshipCombo != null) relationshipCombo.setValue("Family Member");
        if (primaryCheckBox != null) primaryCheckBox.setSelected(false);
    }

    @FXML
    private void backToDashboard() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/codevengers/voiceemergency/UserDashboard.fxml"));
            Stage stage = getCurrentStage();
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle("QuickRescue - User Dashboard");
            }
        } catch (Exception e) {
            System.err.println("❌ Error navigating back to dashboard: " + e.getMessage());
            showErrorAlert("Navigation Error", "Could not return to dashboard.");
        }
    }

    private void redirectToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/codevengers/voiceemergency/UserLogin.fxml"));
            Stage stage = getCurrentStage();
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle("QuickRescue - User Login");
            }
        } catch (Exception e) {
            System.err.println("❌ Error redirecting to login: " + e.getMessage());
        }
    }

    private Stage getCurrentStage() {
        if (backToDashboardBtn != null && backToDashboardBtn.getScene() != null) {
            return (Stage) backToDashboardBtn.getScene().getWindow();
        }
        if (userNameLabel != null && userNameLabel.getScene() != null) {
            return (Stage) userNameLabel.getScene().getWindow();
        }
        return null;
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}