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

public class EmergencyContactsController implements Initializable {

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

    // NEW: Email test button
    @FXML private Button testEmailBtn;

    private EmergencyContact editingContact = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== EMERGENCY CONTACTS INITIALIZING ===");

        // Test database connection first
        if (!EmergencyContactDAO.testDatabaseConnection()) {
            showErrorAlert("Database Error",
                    "Cannot connect to database. Please ensure:\n" +
                            "1. XAMPP is running\n" +
                            "2. MySQL service is started\n" +
                            "3. Database 'voice_emergency_response' exists");
            return;
        }

        // Initialize emergency contacts table
        EmergencyContactDAO.initializeEmergencyContactTable();

        // Check if user is logged in
        if (!UserSession.isLoggedIn()) {
            System.err.println("User not logged in! Redirecting to login...");
            redirectToLogin();
            return;
        }

        // Debug user session
        System.out.println("User Session Debug:");
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

        System.out.println("Emergency contacts page initialized successfully");
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
                            setText(item != null && item ? "Primary" : "");
                        }
                    }
                });
            }

            if (actionsColumn != null) {
                actionsColumn.setCellFactory(col -> new TableCell<EmergencyContact, String>() {
                    private final Button editBtn = new Button("Edit");
                    private final Button deleteBtn = new Button("Delete");
                    private final Button primaryBtn = new Button("Set Primary");
                    private final Button testEmailBtn = new Button("Test Email"); // NEW
                    private final VBox buttonBox = new VBox(2, editBtn, deleteBtn, primaryBtn, testEmailBtn);

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

                        // NEW: Test email button
                        testEmailBtn.setOnAction(e -> {
                            EmergencyContact contact = getTableView().getItems().get(getIndex());
                            testContactEmail(contact);
                        });

                        editBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 9px;");
                        deleteBtn.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-size: 9px;");
                        primaryBtn.setStyle("-fx-background-color: #F39C12; -fx-text-fill: white; -fx-font-size: 9px;");
                        testEmailBtn.setStyle("-fx-background-color: #9B59B6; -fx-text-fill: white; -fx-font-size: 9px;");
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

                            // Show/hide test email button based on email availability
                            if (contact.getEmail() != null && !contact.getEmail().trim().isEmpty()) {
                                testEmailBtn.setVisible(true);
                            } else {
                                testEmailBtn.setVisible(false);
                            }

                            setGraphic(buttonBox);
                        }
                    }
                });
            }
        }
    }

    // NEW: Test email functionality for individual contact
    private void testContactEmail(EmergencyContact contact) {
        if (contact.getEmail() == null || contact.getEmail().trim().isEmpty()) {
            showErrorAlert("No Email", "This contact does not have an email address configured.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Test Email");
        confirmAlert.setHeaderText("Send Test Email?");
        confirmAlert.setContentText("Send a test email to " + contact.getName() + " at " + contact.getEmail() + "?\n\n" +
                "This will send a test emergency notification to verify the email works correctly.");

        if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Show progress
            Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
            progressAlert.setTitle("Sending Test Email");
            progressAlert.setHeaderText("Please wait...");
            progressAlert.setContentText("Sending test email to " + contact.getEmail());
            progressAlert.show();

            // Send test email in background
            new Thread(() -> {
                try {
                    boolean success = EmailService.sendTestEmail(contact.getEmail(), contact.getName());

                    Platform.runLater(() -> {
                        progressAlert.close();

                        if (success) {
                            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                            successAlert.setTitle("Test Email Sent");
                            successAlert.setHeaderText("Email Test Successful");
                            successAlert.setContentText("Test email sent successfully to:\n\n" +
                                    "Name: " + contact.getName() + "\n" +
                                    "Email: " + contact.getEmail() + "\n\n" +
                                    "Please check their inbox to confirm delivery.");
                            successAlert.show();
                        } else {
                            showErrorAlert("Email Test Failed",
                                    "Could not send test email to " + contact.getEmail() + "\n\n" +
                                            "Please check:\n" +
                                            "• Email address is correct\n" +
                                            "• Email service is configured\n" +
                                            "• Internet connection is working");
                        }
                    });

                } catch (Exception e) {
                    Platform.runLater(() -> {
                        progressAlert.close();
                        showErrorAlert("Email Test Error", "Error sending test email: " + e.getMessage());
                    });
                }
            }).start();
        }
    }

    private void loadEmergencyContacts() {
        if (contactsTable != null && UserSession.isLoggedIn()) {
            try {
                System.out.println("Loading emergency contacts for user ID: " + UserSession.getCurrentUserId());

                List<EmergencyContact> contacts = EmergencyContactDAO.getEmergencyContactsByUserId(
                        UserSession.getCurrentUserId()
                );

                ObservableList<EmergencyContact> contactData = FXCollections.observableArrayList(contacts);
                contactsTable.setItems(contactData);

                // Update contact count with email info
                if (contactCountLabel != null) {
                    long emailCount = contacts.stream()
                            .filter(c -> c.getEmail() != null && !c.getEmail().trim().isEmpty())
                            .count();
                    contactCountLabel.setText("Total Contacts: " + contacts.size() +
                            " (📧 " + emailCount + " with email)");
                }

                System.out.println("Loaded " + contacts.size() + " emergency contacts for user");

            } catch (Exception e) {
                System.err.println("Error loading emergency contacts: " + e.getMessage());
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
            System.out.println("Starting contact save process...");

            // Step 1: Test database connection first
            System.out.println("Step 1: Testing database connection...");
            if (!EmergencyContactDAO.testDatabaseConnection()) {
                System.err.println("Database connection failed!");
                showDetailedErrorAlert("Database Connection Error",
                        "Cannot connect to database. Please check:\n" +
                                "1. XAMPP is running\n" +
                                "2. MySQL service is started\n" +
                                "3. Database 'voice_emergency_response' exists\n" +
                                "4. MySQL is running on port 3306");
                return;
            }
            System.out.println("Database connection successful");

            // Step 2: Validate user session
            System.out.println("Step 2: Validating user session...");
            if (!UserSession.isLoggedIn()) {
                System.err.println("User not logged in!");
                showDetailedErrorAlert("Session Error", "User session is not valid. Please log in again.");
                redirectToLogin();
                return;
            }

            int userId = UserSession.getCurrentUserId();
            String username = UserSession.getCurrentUsername();
            System.out.println("User session valid - User ID: " + userId + ", Username: " + username);

            // Step 3: Validate form fields
            System.out.println("Step 3: Validating form fields...");

            if (nameField == null || nameField.getText() == null || nameField.getText().trim().isEmpty()) {
                System.err.println("Name field is empty");
                showDetailedErrorAlert("Validation Error", "Please enter a contact name.");
                if (nameField != null) nameField.requestFocus();
                return;
            }

            if (phoneField == null || phoneField.getText() == null || phoneField.getText().trim().isEmpty()) {
                System.err.println("Phone field is empty");
                showDetailedErrorAlert("Validation Error", "Please enter a phone number.");
                if (phoneField != null) phoneField.requestFocus();
                return;
            }

            if (relationshipCombo == null || relationshipCombo.getValue() == null) {
                System.err.println("Relationship not selected");
                showDetailedErrorAlert("Validation Error", "Please select a relationship.");
                if (relationshipCombo != null) relationshipCombo.requestFocus();
                return;
            }

            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();
            String email = emailField != null ? emailField.getText().trim() : "";
            String relationship = relationshipCombo.getValue();
            boolean isPrimary = primaryCheckBox != null ? primaryCheckBox.isSelected() : false;

            System.out.println("Form validation passed");
            System.out.println("   Name: '" + name + "'");
            System.out.println("   Phone: '" + phone + "'");
            System.out.println("   Email: '" + email + "'");
            System.out.println("   Relationship: '" + relationship + "'");
            System.out.println("   Primary: " + isPrimary);

            // Step 4: Validate phone number format
            System.out.println("Step 4: Validating phone number format...");
            if (!isValidPhoneNumber(phone)) {
                System.err.println("Invalid phone number format: " + phone);
                showDetailedErrorAlert("Phone Number Error",
                        "Please enter a valid phone number.\n\n" +
                                "Accepted formats:\n" +
                                "• +8801700000000\n" +
                                "• 01700000000\n" +
                                "• 017-0000-0000\n" +
                                "• (017) 000-0000\n\n" +
                                "Your input: '" + phone + "'");
                if (phoneField != null) phoneField.requestFocus();
                return;
            }
            System.out.println("Phone number format valid");

            // Step 5: Validate email if provided
            if (!email.isEmpty()) {
                System.out.println("Step 5: Validating email format...");
                if (!isValidEmail(email)) {
                    System.err.println("Invalid email format: " + email);
                    showDetailedErrorAlert("Email Error",
                            "Please enter a valid email address.\n\n" +
                                    "Example: user@example.com\n\n" +
                                    "Your input: '" + email + "'");
                    if (emailField != null) emailField.requestFocus();
                    return;
                }
                System.out.println("Email format valid");
            } else {
                System.out.println("No email provided (optional)");
            }

            // Step 6: Check for duplicate phone numbers (safer approach)
            System.out.println("Step 6: Checking for duplicate contacts...");
            if (editingContact == null) {
                try {
                    // Use a safer method to check for duplicates
                    List<EmergencyContact> existingContacts = EmergencyContactDAO.getEmergencyContactsByUserId(userId);
                    boolean phoneExists = false;

                    for (EmergencyContact existingContact : existingContacts) {
                        if (existingContact.getPhoneNumber().equals(phone)) {
                            phoneExists = true;
                            break;
                        }
                    }

                    if (phoneExists) {
                        System.err.println("Contact with this phone number already exists");
                        showDetailedErrorAlert("Duplicate Contact",
                                "A contact with this phone number already exists.\n\n" +
                                        "Phone: " + phone + "\n\n" +
                                        "Please use a different phone number or edit the existing contact.");
                        if (phoneField != null) phoneField.requestFocus();
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("Error checking for duplicates: " + e.getMessage());
                    // Continue anyway - duplicate check is not critical
                }
            }
            System.out.println("No duplicate contacts found");

            // Step 7: Save or update contact
            if (editingContact == null) {
                System.out.println("Step 7: Adding new contact...");

                EmergencyContact newContact = new EmergencyContact(
                        userId, name, phone, email, relationship, isPrimary
                );

                int contactId = EmergencyContactDAO.addEmergencyContact(newContact);

                if (contactId > 0) {
                    System.out.println("Contact saved successfully with ID: " + contactId);

                    showSuccessAlert("Contact Added",
                            "Emergency contact added successfully!\n\n" +
                                    "Name: " + name + "\n" +
                                    "Phone: " + phone + "\n" +
                                    (email.isEmpty() ? "" : "Email: " + email + "\n") +
                                    "Relationship: " + relationship + "\n" +
                                    "Primary: " + (isPrimary ? "Yes" : "No") + "\n\n" +
                                    "Contact ID: " + contactId +
                                    (email.isEmpty() ? "\n\n💡 Tip: Add an email address to receive emergency notifications!" : ""));

                    hideAddContactForm();
                    loadEmergencyContacts();
                } else {
                    System.err.println("Failed to save contact - database returned ID: " + contactId);
                    showDetailedErrorAlert("Save Error",
                            "Could not save the emergency contact to database.\n\n" +
                                    "This could be due to:\n" +
                                    "• Database connection issues\n" +
                                    "• Database table structure problems\n" +
                                    "• Insufficient database permissions\n\n" +
                                    "Please check the console for detailed error messages.");
                }
            } else {
                System.out.println("Step 7: Updating existing contact...");

                editingContact.setName(name);
                editingContact.setPhoneNumber(phone);
                editingContact.setEmail(email);
                editingContact.setRelationship(relationship);
                editingContact.setPrimary(isPrimary);

                boolean success = EmergencyContactDAO.updateEmergencyContact(editingContact);

                if (success) {
                    System.out.println("Contact updated successfully");

                    showSuccessAlert("Contact Updated",
                            "Emergency contact updated successfully!\n\n" +
                                    "Name: " + name + "\n" +
                                    "Phone: " + phone + "\n" +
                                    (email.isEmpty() ? "" : "Email: " + email + "\n") +
                                    "Relationship: " + relationship + "\n" +
                                    "Primary: " + (isPrimary ? "Yes" : "No"));

                    hideAddContactForm();
                    loadEmergencyContacts();
                } else {
                    System.err.println("Failed to update contact");
                    showDetailedErrorAlert("Update Error", "Could not update emergency contact. Please try again.");
                }
            }

        } catch (Exception e) {
            System.err.println("Unexpected error in saveContact(): " + e.getMessage());
            e.printStackTrace();
            showDetailedErrorAlert("Unexpected Error",
                    "An unexpected error occurred while saving the contact:\n\n" +
                            e.getClass().getSimpleName() + ": " + e.getMessage() + "\n\n" +
                            "Please check the console for detailed error information.");
        }
    }

    // Helper method for phone number validation
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }

        // Remove all non-digit characters for validation
        String digitsOnly = phone.replaceAll("[^0-9]", "");

        // Check various valid formats - fixed the regex pattern
        return phone.matches("^[+]?[0-9\\s\\-()]{10,}$") && digitsOnly.length() >= 10;
    }

    // Helper method for email validation
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Email is optional
        }

        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // Helper method for detailed error alerts
    private void showDetailedErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText("Emergency Contact Error");
        alert.setContentText(message);

        // Make the alert resizable for long messages
        alert.setResizable(true);
        alert.getDialogPane().setPrefWidth(500);

        alert.showAndWait();
    }

    // Helper method for success alerts
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText("Success");
        alert.setContentText(message);

        alert.setResizable(true);
        alert.getDialogPane().setPrefWidth(400);

        alert.show();
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
            boolean success = EmergencyContactDAO.deleteEmergencyContact(contact.getId(), UserSession.getCurrentUserId());

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
            boolean success = EmergencyContactDAO.updateEmergencyContact(contact);

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
            System.err.println("Error navigating back to dashboard: " + e.getMessage());
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
            System.err.println("Error redirecting to login: " + e.getMessage());
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