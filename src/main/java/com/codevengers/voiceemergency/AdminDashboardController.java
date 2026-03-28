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
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AdminDashboardController implements Initializable, JavaChatService.ChatMessageListener {

    @FXML private Text adminNameLabel;
    @FXML private Text adminDepartmentLabel;
    @FXML private Button dashboardBtn;
    @FXML private Button emergencyReportsBtn;
    @FXML private StackPane contentArea;
    @FXML private ScrollPane dashboardView;
    @FXML private ScrollPane emergencyReportsView;
    @FXML private Label placeholderView;
    @FXML private Text totalEmergenciesLabel;
    @FXML private Text activeEmergenciesLabel;
    @FXML private Text resolvedEmergenciesLabel;
    @FXML private Text totalUsersLabel;
    @FXML private TableView<Emergency> recentActivityTable;
    @FXML private TableColumn<Emergency, String> timeColumn;
    @FXML private TableColumn<Emergency, String> userColumn;
    @FXML private TableColumn<Emergency, String> typeColumn;
    @FXML private TableColumn<Emergency, String> locationColumn;
    @FXML private TableColumn<Emergency, String> statusColumn;
    @FXML private TableColumn<Emergency, String> actionColumn;
    @FXML private TableView<Emergency> emergencyReportsTable;
    @FXML private TableColumn<Emergency, Integer> emergencyIdColumn;
    @FXML private TableColumn<Emergency, String> emergencyTimeColumn;
    @FXML private TableColumn<Emergency, String> emergencyUserColumn;
    @FXML private TableColumn<Emergency, String> emergencyTypeColumn;
    @FXML private TableColumn<Emergency, String> emergencyLocationColumn;
    @FXML private TableColumn<Emergency, String> emergencyStatusColumn;
    @FXML private TableColumn<Emergency, String> emergencyActionsColumn;

    private JavaChatService chatService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== ADMIN DASHBOARD INITIALIZING ===");

        // Initialize emergency table
        EmergencyDAO.initializeEmergencyTable();

        // Set admin info
        if (AdminSession.isLoggedIn()) {
            if (adminNameLabel != null) {
                adminNameLabel.setText(AdminSession.getCurrentAdminUsername());
            }
            if (adminDepartmentLabel != null) {
                adminDepartmentLabel.setText(AdminSession.getCurrentAdminDepartment());
            }
        }

        // Start chat server if not running
        if (!ServerLauncher.isServerRunning()) {
            ServerLauncher.startServer();
            System.out.println("🚀 Chat server started from Admin Dashboard");
        }

        // Initialize chat service
        initializeChatService();

        // Initialize table columns
        initializeTableColumns();

        // Load emergency data
        loadEmergencyData();

        // Show dashboard view by default
        showDashboardView();

        // Update UI periodically
        startStatusUpdater();
    }

    private void initializeTableColumns() {
        // Dashboard recent activity table
        if (recentActivityTable != null) {
            if (timeColumn != null) {
                timeColumn.setCellValueFactory(cellData -> {
                    Emergency emergency = cellData.getValue();
                    String formattedTime = emergency.getTimestamp().format(DateTimeFormatter.ofPattern("HH:mm"));
                    return new javafx.beans.property.SimpleStringProperty(formattedTime);
                });
            }

            if (userColumn != null) {
                userColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            }

            if (typeColumn != null) {
                typeColumn.setCellValueFactory(new PropertyValueFactory<>("emergencyType"));
            }

            // Make location column clickable in dashboard table
            if (locationColumn != null) {
                locationColumn.setCellFactory(col -> new TableCell<Emergency, String>() {
                    private final Button locationBtn = new Button();

                    {
                        locationBtn.setOnAction(e -> {
                            Emergency emergency = getTableView().getItems().get(getIndex());
                            openLocationInBrowser(emergency.getLocation());
                        });
                        locationBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 9px;");
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Emergency emergency = getTableView().getItems().get(getIndex());
                            String location = emergency.getLocation();
                            if (location != null && location.startsWith("https://")) {
                                locationBtn.setText("📍 View");
                                setGraphic(locationBtn);
                            } else {
                                setText(location != null ? location : "Unknown");
                                setGraphic(null);
                            }
                        }
                    }
                });
            }

            if (statusColumn != null) {
                statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            }

            if (actionColumn != null) {
                actionColumn.setCellFactory(col -> new TableCell<Emergency, String>() {
                    private final Button actionBtn = new Button("Resolve");

                    {
                        actionBtn.setOnAction(e -> {
                            Emergency emergency = getTableView().getItems().get(getIndex());
                            resolveEmergency(emergency);
                        });
                        actionBtn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-size: 10px;");
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Emergency emergency = getTableView().getItems().get(getIndex());
                            if ("ACTIVE".equals(emergency.getStatus())) {
                                setGraphic(actionBtn);
                            } else {
                                setGraphic(null);
                            }
                        }
                    }
                });
            }
        }

        // Emergency reports table
        if (emergencyReportsTable != null) {
            if (emergencyIdColumn != null) {
                emergencyIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
            }

            if (emergencyTimeColumn != null) {
                emergencyTimeColumn.setCellValueFactory(cellData -> {
                    Emergency emergency = cellData.getValue();
                    String formattedTime = emergency.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    return new javafx.beans.property.SimpleStringProperty(formattedTime);
                });
            }

            if (emergencyUserColumn != null) {
                emergencyUserColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
            }

            if (emergencyTypeColumn != null) {
                emergencyTypeColumn.setCellValueFactory(new PropertyValueFactory<>("emergencyType"));
            }

            // Make location column clickable in reports table
            if (emergencyLocationColumn != null) {
                emergencyLocationColumn.setCellFactory(col -> new TableCell<Emergency, String>() {
                    private final Button locationBtn = new Button();

                    {
                        locationBtn.setOnAction(e -> {
                            Emergency emergency = getTableView().getItems().get(getIndex());
                            openLocationInBrowser(emergency.getLocation());
                        });
                        locationBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 9px;");
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Emergency emergency = getTableView().getItems().get(getIndex());
                            String location = emergency.getLocation();
                            if (location != null && location.startsWith("https://")) {
                                locationBtn.setText("📍 Open Map");
                                setGraphic(locationBtn);
                            } else {
                                setText(location != null ? location : "Unknown");
                                setGraphic(null);
                            }
                        }
                    }
                });
            }

            if (emergencyStatusColumn != null) {
                emergencyStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            }

            if (emergencyActionsColumn != null) {
                emergencyActionsColumn.setCellFactory(col -> new TableCell<Emergency, String>() {
                    private final Button resolveBtn = new Button("Resolve");
                    private final Button viewBtn = new Button("View");
                    private final VBox buttonBox = new VBox(2, resolveBtn, viewBtn);

                    {
                        resolveBtn.setOnAction(e -> {
                            Emergency emergency = getTableView().getItems().get(getIndex());
                            resolveEmergency(emergency);
                        });

                        viewBtn.setOnAction(e -> {
                            Emergency emergency = getTableView().getItems().get(getIndex());
                            viewEmergencyDetails(emergency);
                        });

                        resolveBtn.setStyle("-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-size: 9px;");
                        viewBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 9px;");
                    }

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Emergency emergency = getTableView().getItems().get(getIndex());
                            if ("ACTIVE".equals(emergency.getStatus())) {
                                setGraphic(buttonBox);
                            } else {
                                setGraphic(viewBtn);
                            }
                        }
                    }
                });
            }
        }
    }

    // Open location in browser for admin
    private void openLocationInBrowser(String locationUrl) {
        try {
            if (locationUrl != null && locationUrl.startsWith("https://")) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(locationUrl));
                    System.out.println("📍 Admin opened location in browser: " + locationUrl);

                    // Show confirmation to admin
                    Alert locationAlert = new Alert(Alert.AlertType.INFORMATION);
                    locationAlert.setTitle("Location Opened");
                    locationAlert.setHeaderText("Emergency Location");
                    locationAlert.setContentText("The emergency location has been opened in your default browser.\n\nURL: " + locationUrl);
                    locationAlert.show();
                } else {
                    // Fallback: show location URL in alert
                    Alert locationAlert = new Alert(Alert.AlertType.INFORMATION);
                    locationAlert.setTitle("Location Information");
                    locationAlert.setHeaderText("Emergency Location");
                    locationAlert.setContentText("Location URL: " + locationUrl + "\n\nCopy this URL and paste it in your browser to view the emergency location on the map.");

                    // Make the text selectable
                    TextArea textArea = new TextArea(locationUrl);
                    textArea.setEditable(false);
                    textArea.setWrapText(true);
                    textArea.setPrefRowCount(3);
                    locationAlert.getDialogPane().setExpandableContent(textArea);
                    locationAlert.showAndWait();
                }
            } else {
                Alert noLocationAlert = new Alert(Alert.AlertType.WARNING);
                noLocationAlert.setTitle("No Location Available");
                noLocationAlert.setHeaderText("Location Not Available");
                noLocationAlert.setContentText("No valid location information is available for this emergency.");
                noLocationAlert.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("❌ Error opening location: " + e.getMessage());
            showErrorAlert("Location Error", "Could not open location: " + e.getMessage());
        }
    }

    private void loadEmergencyData() {
        try {
            List<Emergency> allEmergencies = EmergencyDAO.getAllEmergencies();

            // Update statistics
            updateDashboardStats();

            // Load recent activity (last 10 emergencies)
            if (recentActivityTable != null) {
                ObservableList<Emergency> recentData = FXCollections.observableArrayList(
                        allEmergencies.stream().limit(10).toList()
                );
                recentActivityTable.setItems(recentData);
            }

            // Load all emergencies for reports view
            if (emergencyReportsTable != null) {
                ObservableList<Emergency> allData = FXCollections.observableArrayList(allEmergencies);
                emergencyReportsTable.setItems(allData);
            }

            System.out.println("✅ Loaded " + allEmergencies.size() + " emergencies");

        } catch (Exception e) {
            System.err.println("❌ Error loading emergency data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void resolveEmergency(Emergency emergency) {
        try {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Resolve Emergency");
            confirmAlert.setHeaderText("Resolve Emergency #" + emergency.getId());
            confirmAlert.setContentText("Are you sure you want to mark this emergency as resolved?\n\n" +
                    "Emergency Details:\n" +
                    "• User: " + emergency.getUsername() + "\n" +
                    "• Type: " + emergency.getEmergencyType() + "\n" +
                    "• Time: " + emergency.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "\n" +
                    "• Trigger: " + emergency.getTriggerMethod());

            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                boolean success = EmergencyDAO.updateEmergencyStatus(emergency.getId(), "RESOLVED");

                if (success) {
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Emergency Resolved");
                    successAlert.setHeaderText("Success");
                    successAlert.setContentText("Emergency #" + emergency.getId() + " has been marked as resolved.\n\n" +
                            "The user and relevant authorities have been notified.");
                    successAlert.show();

                    // Refresh data
                    loadEmergencyData();
                } else {
                    showErrorAlert("Error", "Could not resolve emergency. Please try again.");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error resolving emergency: " + e.getMessage());
            showErrorAlert("Error", "Could not resolve emergency: " + e.getMessage());
        }
    }

    private void viewEmergencyDetails(Emergency emergency) {
        Alert detailAlert = new Alert(Alert.AlertType.INFORMATION);
        detailAlert.setTitle("Emergency Details");
        detailAlert.setHeaderText("Emergency #" + emergency.getId() + " - " + emergency.getEmergencyType());

        String details = String.format(
                "👤 User: %s (ID: %d)\n" +
                        "🚨 Type: %s\n" +
                        "📊 Status: %s\n" +
                        "⏰ Time: %s\n" +
                        "🎯 Trigger Method: %s\n" +
                        "📝 Description: %s\n" +
                        "📍 Location: %s\n" +
                        "🎙️ Audio File: %s",
                emergency.getUsername(),
                emergency.getUserId(),
                emergency.getEmergencyType(),
                emergency.getStatus(),
                emergency.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                emergency.getTriggerMethod(),
                emergency.getDescription(),
                emergency.getLocation() != null ? emergency.getLocation() : "Not available",
                emergency.getAudioFileName() != null ? emergency.getAudioFileName() : "No audio file"
        );

        detailAlert.setContentText(details);

        // Add location button if available
        if (emergency.getLocation() != null && emergency.getLocation().startsWith("https://")) {
            ButtonType locationButton = new ButtonType("📍 Open Location");
            ButtonType okButton = ButtonType.OK;
            detailAlert.getButtonTypes().setAll(locationButton, okButton);

            detailAlert.showAndWait().ifPresent(response -> {
                if (response == locationButton) {
                    openLocationInBrowser(emergency.getLocation());
                }
            });
        } else {
            detailAlert.showAndWait();
        }
    }

    private void initializeChatService() {
        try {
            chatService = JavaChatService.getInstance();
            if (chatService != null) {
                chatService.addMessageListener(this);
            }
        } catch (Exception e) {
            System.err.println("❌ Error initializing chat service: " + e.getMessage());
        }
    }

    private void startStatusUpdater() {
        Thread statusThread = new Thread(() -> {
            while (true) {
                try {
                    Platform.runLater(() -> {
                        updateDashboardStats();
                        loadEmergencyData(); // Refresh emergency data
                    });
                    Thread.sleep(10000); // 10 seconds
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        statusThread.setDaemon(true);
        statusThread.start();
    }

    private void updateDashboardStats() {
        try {
            int totalEmergencies = EmergencyDAO.getTotalEmergencies();
            int activeEmergencies = EmergencyDAO.getActiveEmergencies();
            int resolvedEmergencies = EmergencyDAO.getResolvedEmergencies();

            if (totalEmergenciesLabel != null) {
                totalEmergenciesLabel.setText(String.valueOf(totalEmergencies));
            }
            if (activeEmergenciesLabel != null) {
                activeEmergenciesLabel.setText(String.valueOf(activeEmergencies));
            }
            if (resolvedEmergenciesLabel != null) {
                resolvedEmergenciesLabel.setText(String.valueOf(resolvedEmergencies));
            }
            if (totalUsersLabel != null) {
                // This would need a UserDAO to get actual user count
                totalUsersLabel.setText("45"); // Placeholder
            }
        } catch (Exception e) {
            System.err.println("❌ Error updating dashboard stats: " + e.getMessage());
        }
    }

    @FXML
    private void showDashboardView() {
        hideAllViews();
        if (dashboardView != null) {
            dashboardView.setVisible(true);
        }
        updateButtonStyles(dashboardBtn);
        loadEmergencyData(); // Refresh data when showing dashboard
    }

    @FXML
    private void showEmergencyReports() {
        hideAllViews();
        if (emergencyReportsView != null) {
            emergencyReportsView.setVisible(true);
        } else if (placeholderView != null) {
            placeholderView.setText("Emergency Reports");
            placeholderView.setVisible(true);
        }
        updateButtonStyles(emergencyReportsBtn);
        loadEmergencyData(); // Refresh data when showing reports
    }

    @FXML
    private void refreshEmergencyReports() {
        System.out.println("🔄 Refreshing emergency reports...");
        loadEmergencyData();

        Alert refreshAlert = new Alert(Alert.AlertType.INFORMATION);
        refreshAlert.setTitle("Refresh Complete");
        refreshAlert.setHeaderText(null);
        refreshAlert.setContentText("Emergency reports have been refreshed with the latest data.");
        refreshAlert.show();
    }

    // ChatMessageListener implementation
    @Override
    public void onNewMessage(JavaChatService.ChatMessage message) {
        Platform.runLater(() -> {
            if ("user".equals(message.getSenderType())) {
                if ("emergency".equals(message.getMessageType()) || "silent_emergency".equals(message.getMessageType())) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("🚨 EMERGENCY ALERT");
                    alert.setHeaderText("Emergency Message Received!");
                    alert.setContentText("From User ID: " + message.getSenderId() + "\n\nMessage: " + message.getMessage());
                    alert.show();

                    // Refresh emergency data
                    loadEmergencyData();
                } else if ("audio_file".equals(message.getMessageType())) {
                    // Handle audio file messages
                    handleAudioFileMessage(message);
                } else if ("notification".equals(message.getMessageType())) {
                    // Handle notification messages
                    handleNotificationMessage(message);
                } else if ("error".equals(message.getMessageType())) {
                    // Handle error messages
                    handleErrorMessage(message);
                }
            }
        });
    }

    @Override
    public void onMessageStatusUpdate(int messageId, boolean isRead) {
        // Handle message status updates if needed
    }

    // Navigation and utility methods
    private void hideAllViews() {
        if (dashboardView != null) dashboardView.setVisible(false);
        if (emergencyReportsView != null) emergencyReportsView.setVisible(false);
        if (placeholderView != null) placeholderView.setVisible(false);
    }

    private void updateButtonStyles(Button activeButton) {
        Button[] buttons = {dashboardBtn, emergencyReportsBtn};

        for (Button btn : buttons) {
            if (btn != null) {
                btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #BDC3C7; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT; -fx-padding: 0 20; -fx-background-radius: 0;");
            }
        }

        if (activeButton != null) {
            activeButton.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 14px; -fx-alignment: CENTER_LEFT; -fx-padding: 0 20; -fx-background-radius: 0;");
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleLogout() {
        try {
            if (chatService != null) {
                chatService.disconnect();
            }
            ServerLauncher.stopServer();
            AdminSession.logout();

            Parent root = FXMLLoader.load(getClass().getResource("/com/codevengers/voiceemergency/AdminLogin.fxml"));
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("QuickRescue - Admin Login");
        } catch (Exception e) {
            System.err.println("❌ Error during logout: " + e.getMessage());
            showErrorAlert("Error", "Could not logout properly.");
        }
    }

    // ADDED: Handle audio file messages from emergency recordings
    private void handleAudioFileMessage(JavaChatService.ChatMessage message) {
        try {
            Alert audioAlert = new Alert(Alert.AlertType.INFORMATION);
            audioAlert.setTitle("🎙️ EMERGENCY AUDIO RECEIVED");
            audioAlert.setHeaderText("Emergency Audio Recording");

            // Parse the message to extract audio information
            String messageContent = message.getMessage();

            // Create a scrollable text area for the full message
            TextArea textArea = new TextArea(messageContent);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefRowCount(15);
            textArea.setPrefColumnCount(60);

            audioAlert.getDialogPane().setContent(textArea);

            // Add buttons for actions
            ButtonType saveButton = new ButtonType("💾 Save Audio");
            ButtonType playButton = new ButtonType("▶️ Play Audio");
            ButtonType closeButton = ButtonType.CLOSE;

            audioAlert.getButtonTypes().setAll(saveButton, playButton, closeButton);

            audioAlert.showAndWait().ifPresent(response -> {
                if (response == saveButton) {
                    saveAudioFromMessage(messageContent);
                } else if (response == playButton) {
                    playAudioFromMessage(messageContent);
                }
            });

            System.out.println("✅ Audio file message displayed to admin");

        } catch (Exception e) {
            System.err.println("❌ Error handling audio file message: " + e.getMessage());
            showErrorAlert("Audio Message Error", "Could not process audio file message: " + e.getMessage());
        }
    }

    // ADDED: Handle notification messages
    private void handleNotificationMessage(JavaChatService.ChatMessage message) {
        Alert notificationAlert = new Alert(Alert.AlertType.INFORMATION);
        notificationAlert.setTitle("🔔 NOTIFICATION");
        notificationAlert.setHeaderText("System Notification");
        notificationAlert.setContentText(message.getMessage());
        notificationAlert.show();

        System.out.println("✅ Notification message displayed to admin");
    }

    // ADDED: Handle error messages
    private void handleErrorMessage(JavaChatService.ChatMessage message) {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("⚠️ SYSTEM ERROR");
        errorAlert.setHeaderText("Emergency System Error");
        errorAlert.setContentText(message.getMessage());
        errorAlert.show();

        System.out.println("✅ Error message displayed to admin");
    }

    // ADDED: Save audio from Base64 encoded message
    private void saveAudioFromMessage(String messageContent) {
        try {
            // Extract Base64 audio data from message
            String base64Marker = "📎 Audio Data (Base64):\n";
            String endMarker = "\n━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━";

            int startIndex = messageContent.indexOf(base64Marker);
            int endIndex = messageContent.indexOf(endMarker, startIndex);

            if (startIndex != -1 && endIndex != -1) {
                startIndex += base64Marker.length();
                String base64Audio = messageContent.substring(startIndex, endIndex).trim();

                // Decode Base64 to bytes
                byte[] audioBytes = java.util.Base64.getDecoder().decode(base64Audio);

                // Save to file
                String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                String fileName = "admin_received_audio_" + timestamp + ".wav";
                File audioFile = new File("admin_audio/" + fileName);

                // Create directory if it doesn't exist
                audioFile.getParentFile().mkdirs();

                java.nio.file.Files.write(audioFile.toPath(), audioBytes);

                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Audio Saved");
                successAlert.setHeaderText("Success");
                successAlert.setContentText("Audio file saved successfully:\n" + audioFile.getAbsolutePath());
                successAlert.show();

                System.out.println("✅ Audio file saved: " + audioFile.getAbsolutePath());

            } else {
                showErrorAlert("Save Error", "Could not extract audio data from message");
            }

        } catch (Exception e) {
            System.err.println("❌ Error saving audio from message: " + e.getMessage());
            showErrorAlert("Save Error", "Could not save audio file: " + e.getMessage());
        }
    }

    // ADDED: Play audio from Base64 encoded message (placeholder - would need audio player implementation)
    private void playAudioFromMessage(String messageContent) {
        Alert playAlert = new Alert(Alert.AlertType.INFORMATION);
        playAlert.setTitle("Audio Playback");
        playAlert.setHeaderText("Audio Playback");
        playAlert.setContentText("Audio playback functionality would be implemented here.\n\n" +
                "For now, please save the audio file and play it with your preferred audio player.");
        playAlert.show();

        System.out.println("🔊 Audio playback requested (not implemented yet)");
    }

    // Additional navigation methods (placeholder implementations)
    @FXML private void showLocationTracking() {
        hideAllViews();
        if (placeholderView != null) {
            placeholderView.setText("Location Tracking - Coming Soon");
            placeholderView.setVisible(true);
        }
    }

    @FXML private void showIncidentLogs() {
        hideAllViews();
        if (placeholderView != null) {
            placeholderView.setText("Incident Logs - Coming Soon");
            placeholderView.setVisible(true);
        }
    }

    @FXML private void showUserManagement() {
        hideAllViews();
        if (placeholderView != null) {
            placeholderView.setText("User Management - Coming Soon");
            placeholderView.setVisible(true);
        }
    }

    @FXML private void showAlertManagement() {
        try {
            if (!ServerLauncher.isServerRunning()) {
                ServerLauncher.startServer();
                Thread.sleep(1000);
            }
            Parent root = FXMLLoader.load(getClass().getResource("/com/codevengers/voiceemergency/AdminChat.fxml"));
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("QuickRescue - Chat Management");
        } catch (Exception e) {
            System.err.println("❌ Error opening Chat Management: " + e.getMessage());
            showErrorAlert("Error", "Could not open Chat Management feature: " + e.getMessage());
        }
    }

    @FXML private void handleChatSupport() { showAlertManagement(); }
    @FXML private void showChatManagement() { showAlertManagement(); }
    @FXML private void searchEmergencies() {
        System.out.println("🔍 Search functionality - to be implemented");
    }
}