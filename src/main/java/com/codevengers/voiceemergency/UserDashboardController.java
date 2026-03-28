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
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.control.TitledPane;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class UserDashboardController implements Initializable {

    // FXML field declarations - ALL possible FXML IDs (merged from both files)
    @FXML private Text userNameLabel;
    @FXML private Text userStatusLabel;
    @FXML private Text welcomeLabel;
    @FXML private Text userDisplayName;
    @FXML private Text currentUserName;
    @FXML private Button chatNotificationBtn;
    @FXML private Label chatUnreadBadge;
    @FXML private Button dashboardBtn;
    @FXML private Button panicBtn;
    @FXML private Button voiceActivationBtn;
    @FXML private Button incidentHistoryBtn;
    @FXML private Button manualAlertBtn;
    @FXML private Button systemStatusBtn;
    @FXML private Button quickChatBtn;
    @FXML private Button quickPanicBtn;
    @FXML private Button startChatBtn;
    @FXML private Button voiceAlertBtn;
    @FXML private Button chatSupportBtn;
    @FXML private Button emergencyContactsBtn;
    @FXML private Button settingsBtn;
    @FXML private Button logoutBtn;
    @FXML private Button logoutButton;
    @FXML private Button signOutBtn;
    @FXML private Button exitBtn;
    @FXML private Label connectionStatusLabel;
    @FXML private Label unreadMessagesLabel;
    @FXML private Text chatStatusText;
    @FXML private Text unreadMessagesText;
    @FXML private Text voiceStatusText;
    @FXML private ToggleButton voiceToggle;
    @FXML private Label sidebarChatBadge;
    @FXML private Label chatUnreadBadge2;

    // NEW: Police contacts components (from second file)
    @FXML private VBox policeContactsPanel;
    @FXML private TitledPane policeContactsSection;

    // Manual emergency control buttons
    @FXML private Button stopEmergencyBtn;
    @FXML private Button stopAudioBtn;
    @FXML private Text emergencyStatusText;

    // Emergency activity table components
    @FXML private TableView<Emergency> recentActivityTable;
    @FXML private TableColumn<Emergency, String> activityTimeColumn;
    @FXML private TableColumn<Emergency, String> activityTypeColumn;
    @FXML private TableColumn<Emergency, String> activityLocationColumn;
    @FXML private TableColumn<Emergency, String> activityStatusColumn;

    private JavaChatService chatService;

    // MERGED: Voice detection variables using both approaches
    private volatile boolean isVoiceDetectionActive = false;
    private Thread voiceDetectionThread;
    private VoiceDetector voiceDetector;

    // NEW: Police contacts controller (from second file)
    private PoliceContactsController policeContactsController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== USER DASHBOARD INITIALIZING ===");

        // Initialize emergency table
        EmergencyDAO.initializeEmergencyTable();

        // Check if user is logged in
        if (!UserSession.isLoggedIn()) {
            System.err.println("❌ User not logged in! Redirecting to login...");
            redirectToLogin();
            return;
        }

        // Set welcome message for ALL possible username labels
        String username = UserSession.getCurrentUsername();
        System.out.println("✅ Setting username: " + username);

        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + username + "!");
        }
        if (userNameLabel != null) {
            userNameLabel.setText(username);
        }
        if (userDisplayName != null) {
            userDisplayName.setText(username);
        }
        if (currentUserName != null) {
            currentUserName.setText(username);
        }
        if (userStatusLabel != null) {
            userStatusLabel.setText("Active User");
        }

        // Initialize emergency activity table
        initializeActivityTable();

        // Initialize chat service for connection status
        initializeChatService();

        // Load recent emergency activity
        loadRecentActivity();

        // MERGED: Start enhanced voice detection with confirmation popup (preferred approach)
        startEnhancedVoiceDetection();

        // Initialize emergency control buttons
        initializeEmergencyControls();

        // NEW: Initialize police contacts panel (from second file)
        initializePoliceContactsPanel();

        // Update UI periodically
        startStatusUpdater();

        System.out.println("✅ User dashboard initialized successfully");
    }

    // NEW: Initialize police contacts panel (from second file)
    private void initializePoliceContactsPanel() {
        try {
            System.out.println("🚔 Initializing police contacts panel...");

            if (policeContactsSection != null) {
                // Load the police contacts FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codevengers/voiceemergency/PoliceContacts.fxml"));
                Parent policeContactsRoot = loader.load();
                policeContactsController = loader.getController();

                // Set up the titled pane
                policeContactsSection.setText("🚔 Emergency Police Numbers");
                policeContactsSection.setContent(policeContactsRoot);
                policeContactsSection.setExpanded(false);
                policeContactsSection.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

                System.out.println("✅ Police contacts panel initialized successfully");
            } else if (policeContactsPanel != null) {
                // Alternative: Direct VBox approach if TitledPane is not available
                createDirectPolicePanel();
            } else {
                System.err.println("❌ No police contacts UI element found in FXML");
                // Fallback: Create a simple button
                createPoliceContactsButton();
            }

        } catch (Exception e) {
            System.err.println("❌ Error initializing police contacts panel: " + e.getMessage());
            e.printStackTrace();
            createPoliceContactsButton();
        }
    }

    private void createDirectPolicePanel() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codevengers/voiceemergency/PoliceContacts.fxml"));
            Parent policeContactsRoot = loader.load();
            policeContactsController = loader.getController();

            Label titleLabel = new Label("🚔 Emergency Police Numbers");
            titleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2C3E50;");

            policeContactsPanel.getChildren().addAll(titleLabel, policeContactsRoot);
            System.out.println("✅ Direct police panel created successfully");

        } catch (Exception e) {
            System.err.println("❌ Error creating direct police panel: " + e.getMessage());
            createPoliceContactsButton();
        }
    }

    private void createPoliceContactsButton() {
        try {
            Button policeBtn = new Button("🚔 Police Numbers");
            policeBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 11px;");
            policeBtn.setMaxWidth(Double.MAX_VALUE);
            policeBtn.setOnAction(e -> openPoliceContactsWindow());

            if (policeContactsPanel != null) {
                policeContactsPanel.getChildren().add(policeBtn);
            }

            System.out.println("✅ Police contacts button created as fallback");

        } catch (Exception e) {
            System.err.println("❌ Error creating police contacts button: " + e.getMessage());
        }
    }

    private void openPoliceContactsWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/codevengers/voiceemergency/PoliceContacts.fxml"));
            Parent root = loader.load();

            Stage policeStage = new Stage();
            policeStage.setTitle("Bangladesh Police Emergency Numbers");
            policeStage.setScene(new Scene(root, 400, 600));
            policeStage.show();

            System.out.println("✅ Police contacts window opened");

        } catch (Exception e) {
            System.err.println("❌ Error opening police contacts window: " + e.getMessage());
            showErrorAlert("Error", "Could not open police contacts: " + e.getMessage());
        }
    }

    // FXML Action for police contacts
    @FXML
    private void handlePoliceContacts() {
        openPoliceContactsWindow();
    }

    // Initialize emergency control buttons
    private void initializeEmergencyControls() {
        // Initially hide emergency control buttons
        if (stopEmergencyBtn != null) {
            stopEmergencyBtn.setVisible(false);
        }
        if (stopAudioBtn != null) {
            stopAudioBtn.setVisible(false);
        }
        if (emergencyStatusText != null) {
            emergencyStatusText.setText("No active emergency");
            emergencyStatusText.setStyle("-fx-fill: #27AE60;");
        }
    }

    // Update emergency control buttons visibility
    private void updateEmergencyControls() {
        boolean isEmergencyActive = EnhancedEmergencyHandler.isEmergencyTriggered();

        Platform.runLater(() -> {
            if (stopEmergencyBtn != null) {
                stopEmergencyBtn.setVisible(isEmergencyActive);
            }
            if (stopAudioBtn != null) {
                stopAudioBtn.setVisible(isEmergencyActive);
            }
            if (emergencyStatusText != null) {
                if (isEmergencyActive) {
                    emergencyStatusText.setText("🚨 EMERGENCY ACTIVE");
                    emergencyStatusText.setStyle("-fx-fill: #E74C3C; -fx-font-weight: bold;");
                } else {
                    emergencyStatusText.setText("No active emergency");
                    emergencyStatusText.setStyle("-fx-fill: #27AE60;");
                }
            }
        });
    }

    // Manual stop emergency button
    @FXML
    private void stopEmergencyManually() {
        try {
            if (EnhancedEmergencyHandler.isEmergencyTriggered()) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Stop Emergency");
                confirmAlert.setHeaderText("Stop Active Emergency?");
                confirmAlert.setContentText("Are you sure you want to stop the active emergency?\n\n" +
                        "This will:\n" +
                        "• Stop audio recording\n" +
                        "• Stop audio streaming\n" +
                        "• Mark emergency as resolved\n" +
                        "• Notify administrators");

                if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    EnhancedEmergencyHandler.stopEmergency();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Emergency Stopped");
                    successAlert.setHeaderText("Emergency Successfully Stopped");
                    successAlert.setContentText("The emergency has been stopped manually.\n" +
                            "All emergency procedures have been terminated.");
                    successAlert.show();

                    // Refresh UI
                    updateEmergencyControls();
                    loadRecentActivity();
                }
            } else {
                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("No Active Emergency");
                infoAlert.setHeaderText("No Emergency to Stop");
                infoAlert.setContentText("There is currently no active emergency to stop.");
                infoAlert.show();
            }
        } catch (Exception e) {
            System.err.println("❌ Error stopping emergency manually: " + e.getMessage());
            showErrorAlert("Error", "Could not stop emergency: " + e.getMessage());
        }
    }

    // Manual stop audio button
    @FXML
    private void stopAudioManually() {
        try {
            if (EnhancedEmergencyHandler.isEmergencyTriggered()) {
                Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                confirmAlert.setTitle("Stop Audio Recording");
                confirmAlert.setHeaderText("Stop Audio Recording Only?");
                confirmAlert.setContentText("This will stop only the audio recording while keeping the emergency active.\n\n" +
                        "• Audio recording will stop\n" +
                        "• Audio streaming will stop\n" +
                        "• Emergency will remain active\n" +
                        "• Location sharing continues");

                if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    // Stop only audio recording
                    AudioRecorder audioRecorder = EnhancedEmergencyHandler.getAudioRecorder();
                    if (audioRecorder != null && audioRecorder.isRecording()) {
                        audioRecorder.stopRecording();
                        System.out.println("🎙️ Audio recording stopped manually by user");
                    }

                    // Stop audio streaming
                    try {
                        if (AudioStreamer.isStreaming()) {
                            AudioStreamer.stopStreaming();
                            System.out.println("📡 Audio streaming stopped manually by user");
                        }
                    } catch (Exception e) {
                        System.err.println("❌ Error stopping audio streaming: " + e.getMessage());
                    }

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Audio Stopped");
                    successAlert.setHeaderText("Audio Recording Stopped");
                    successAlert.setContentText("Audio recording and streaming have been stopped.\n" +
                            "The emergency remains active for location sharing and alerts.");
                    successAlert.show();

                    // Update stop audio button to show it's no longer needed
                    if (stopAudioBtn != null) {
                        stopAudioBtn.setText("Audio Stopped");
                        stopAudioBtn.setDisable(true);
                        stopAudioBtn.setStyle("-fx-background-color: #95A5A6; -fx-text-fill: white;");
                    }
                }
            } else {
                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("No Active Emergency");
                infoAlert.setHeaderText("No Audio to Stop");
                infoAlert.setContentText("There is currently no active emergency with audio recording.");
                infoAlert.show();
            }
        } catch (Exception e) {
            System.err.println("❌ Error stopping audio manually: " + e.getMessage());
            showErrorAlert("Error", "Could not stop audio recording: " + e.getMessage());
        }
    }

    // MERGED: Start enhanced voice detection with confirmation popup (preferred from first file)
    private void startEnhancedVoiceDetection() {
        try {
            System.out.println("🎤 Starting enhanced voice detection with confirmation popup...");

            // Start enhanced voice detection
            EnhancedVoiceDetectionHandler.startVoiceDetection();

            isVoiceDetectionActive = true;

            // Update UI to show voice detection is active
            Platform.runLater(() -> {
                if (voiceStatusText != null) {
                    voiceStatusText.setText("Voice Detection: ON (with confirmation)");
                    voiceStatusText.setStyle("-fx-fill: #27AE60;");
                }
                if (voiceToggle != null) {
                    voiceToggle.setSelected(true);
                }
            });

            System.out.println("✅ Enhanced voice detection started successfully");

        } catch (Exception e) {
            System.err.println("❌ Error starting enhanced voice detection: " + e.getMessage());
            e.printStackTrace();

            // FALLBACK: Try traditional voice detection from second file
            startTraditionalVoiceDetection();
        }
    }

    // MERGED: Fallback traditional voice detection (from second file)
    private void startTraditionalVoiceDetection() {
        try {
            System.out.println("🎤 Starting traditional voice detection as fallback...");

            // Create voice detector using existing class
            voiceDetector = new VoiceDetector();

            // Start voice detection in background thread
            voiceDetectionThread = new Thread(() -> {
                try {
                    // Use existing SpeechRecognitionTest logic with callback
                    SpeechRecognitionTest.startVoiceDetectionWithCallback(new VoiceCallback() {
                        @Override
                        public void onVoiceDetected(String command) {
                            System.out.println("👂 Voice detected in dashboard: " + command);

                            // Check for emergency keywords
                            if (SpeechRecognitionTest.isEmergencyKeyword(command)) {
                                if (!EnhancedEmergencyHandler.isEmergencyTriggered()) {
                                    System.out.println("🚨 EMERGENCY KEYWORD DETECTED: " + command.toUpperCase());

                                    Platform.runLater(() -> {
                                        triggerVoiceEmergency(command);
                                    });
                                } else {
                                    System.out.println("⚠️ Emergency already triggered, ignoring voice command");
                                }
                            }
                        }

                        @Override
                        public void onSilenceDetected() {
                            if (!EnhancedEmergencyHandler.isEmergencyTriggered()) {
                                System.out.println("⏰ Silence detected! Checking for emergency...");

                                Platform.runLater(() -> {
                                    Alert silenceAlert = new Alert(Alert.AlertType.WARNING);
                                    silenceAlert.setTitle("Silence Detected");
                                    silenceAlert.setHeaderText("No voice activity detected");
                                    silenceAlert.setContentText("No voice has been detected for a while. Are you okay?\n\nClick 'Yes' if you're fine, or 'No' to trigger emergency alert.");

                                    ButtonType yesButton = new ButtonType("Yes, I'm fine");
                                    ButtonType noButton = new ButtonType("No, trigger emergency");
                                    silenceAlert.getButtonTypes().setAll(yesButton, noButton);

                                    silenceAlert.showAndWait().ifPresent(response -> {
                                        if (response == noButton) {
                                            triggerVoiceEmergency("silence timeout");
                                        }
                                    });
                                });
                            }
                        }
                    });

                } catch (Exception e) {
                    System.err.println("❌ Traditional voice detection error: " + e.getMessage());
                    e.printStackTrace();

                    Platform.runLater(() -> {
                        if (voiceStatusText != null) {
                            voiceStatusText.setText("Voice Detection: ERROR");
                            voiceStatusText.setStyle("-fx-fill: #E74C3C;");
                        }
                    });
                }
            });

            voiceDetectionThread.setDaemon(true);
            voiceDetectionThread.start();

            isVoiceDetectionActive = true;

            // Update UI to show voice detection is active
            Platform.runLater(() -> {
                if (voiceStatusText != null) {
                    voiceStatusText.setText("Voice Detection: ON (traditional)");
                    voiceStatusText.setStyle("-fx-fill: #27AE60;");
                }
                if (voiceToggle != null) {
                    voiceToggle.setSelected(true);
                }
            });

            System.out.println("✅ Traditional voice detection started successfully");

        } catch (Exception e) {
            System.err.println("❌ Error starting traditional voice detection: " + e.getMessage());
            e.printStackTrace();

            Platform.runLater(() -> {
                if (voiceStatusText != null) {
                    voiceStatusText.setText("Voice Detection: ERROR");
                    voiceStatusText.setStyle("-fx-fill: #E74C3C;");
                }
            });
        }
    }

    // MERGED: Stop enhanced voice detection (handles both approaches)
    private void stopEnhancedVoiceDetection() {
        try {
            System.out.println("🛑 Stopping voice detection...");

            // Try to stop enhanced voice detection first
            try {
                EnhancedVoiceDetectionHandler.stopVoiceDetection();
            } catch (Exception e) {
                System.err.println("❌ Error stopping enhanced voice detection: " + e.getMessage());
            }

            // Stop traditional voice detection thread if running
            if (voiceDetectionThread != null && voiceDetectionThread.isAlive()) {
                voiceDetectionThread.interrupt();
            }

            isVoiceDetectionActive = false;

            Platform.runLater(() -> {
                if (voiceStatusText != null) {
                    voiceStatusText.setText("Voice Detection: OFF");
                    voiceStatusText.setStyle("-fx-fill: #E74C3C;");
                }
                if (voiceToggle != null) {
                    voiceToggle.setSelected(false);
                }
            });

            System.out.println("✅ Voice detection stopped");

        } catch (Exception e) {
            System.err.println("❌ Error stopping voice detection: " + e.getMessage());
        }
    }

    // Trigger emergency from voice detection (from second file)
    private void triggerVoiceEmergency(String voiceCommand) {
        try {
            String emergencyType = SpeechRecognitionTest.determineEmergencyType(voiceCommand);
            String description = "Voice-activated emergency: '" + voiceCommand + "'";

            System.out.println("🚨 Triggering voice emergency: " + emergencyType);

            // Show voice emergency alert
            Alert voiceAlert = new Alert(Alert.AlertType.WARNING);
            voiceAlert.setTitle("🚨 VOICE EMERGENCY DETECTED");
            voiceAlert.setHeaderText("Emergency keyword detected!");
            voiceAlert.setContentText("Voice command: '" + voiceCommand + "'\n\n" +
                    "Emergency procedures are being activated automatically.\n" +
                    "• Emergency contacts will be notified\n" +
                    "• Audio recording will start\n" +
                    "• Location will be shared\n" +
                    "• Administrators will be alerted");

            voiceAlert.show();

            // Trigger emergency using enhanced handler
            EnhancedEmergencyHandler.triggerEmergency("voice", emergencyType, description);

            // Update emergency controls
            updateEmergencyControls();

            // Refresh the activity table
            Platform.runLater(() -> loadRecentActivity());

        } catch (Exception e) {
            System.err.println("❌ Error triggering voice emergency: " + e.getMessage());
            showErrorAlert("Voice Emergency Error", "Could not process voice emergency: " + e.getMessage());
        }
    }

    // Initialize the emergency activity table with clickable location links
    private void initializeActivityTable() {
        if (recentActivityTable != null) {
            if (activityTimeColumn != null) {
                activityTimeColumn.setCellValueFactory(cellData -> {
                    Emergency emergency = cellData.getValue();
                    String formattedTime = emergency.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
                    return new javafx.beans.property.SimpleStringProperty(formattedTime);
                });
            }

            if (activityTypeColumn != null) {
                activityTypeColumn.setCellValueFactory(new PropertyValueFactory<>("emergencyType"));
            }

            // Make location column clickable
            if (activityLocationColumn != null) {
                activityLocationColumn.setCellFactory(col -> new TableCell<Emergency, String>() {
                    private final Button locationBtn = new Button();

                    {
                        locationBtn.setOnAction(e -> {
                            Emergency emergency = getTableView().getItems().get(getIndex());
                            openLocationInBrowser(emergency.getLocation());
                        });
                        locationBtn.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-size: 10px;");
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
                                locationBtn.setText("📍 View Map");
                                setGraphic(locationBtn);
                            } else {
                                setText(location != null ? location : "Unknown");
                                setGraphic(null);
                            }
                        }
                    }
                });
            }

            if (activityStatusColumn != null) {
                activityStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
            }
        }
    }

    // Open location in browser
    private void openLocationInBrowser(String locationUrl) {
        try {
            if (locationUrl != null && locationUrl.startsWith("https://")) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(new URI(locationUrl));
                    System.out.println("📍 Opened location in browser: " + locationUrl);
                } else {
                    // Fallback: show location URL in alert
                    Alert locationAlert = new Alert(Alert.AlertType.INFORMATION);
                    locationAlert.setTitle("Location Information");
                    locationAlert.setHeaderText("Emergency Location");
                    locationAlert.setContentText("Location URL: " + locationUrl + "\n\nCopy this URL and paste it in your browser to view the location.");
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

    // Load recent emergency activity for current user
    private void loadRecentActivity() {
        if (recentActivityTable != null && UserSession.isLoggedIn()) {
            try {
                List<Emergency> userEmergencies = EmergencyDAO.getAllEmergencies()
                        .stream()
                        .filter(e -> e.getUserId() == UserSession.getCurrentUserId())
                        .limit(10) // Show last 10 emergencies
                        .toList();

                ObservableList<Emergency> emergencyData = FXCollections.observableArrayList(userEmergencies);
                recentActivityTable.setItems(emergencyData);

                System.out.println("✅ Loaded " + userEmergencies.size() + " recent emergencies for user");
            } catch (Exception e) {
                System.err.println("❌ Error loading recent activity: " + e.getMessage());
            }
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

    private void initializeChatService() {
        try {
            chatService = JavaChatService.getInstance();
            updateConnectionStatus();
            updateUnreadMessageCount();
        } catch (Exception e) {
            System.err.println("❌ Error initializing chat service: " + e.getMessage());
        }
    }

    private void startStatusUpdater() {
        Thread statusThread = new Thread(() -> {
            while (true) {
                try {
                    Platform.runLater(() -> {
                        updateConnectionStatus();
                        updateUnreadMessageCount();
                        updateEmergencyControls(); // Update emergency controls
                        loadRecentActivity(); // Refresh activity table
                    });
                    Thread.sleep(5000); // 5 seconds
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        statusThread.setDaemon(true);
        statusThread.start();
    }

    private void updateConnectionStatus() {
        if (connectionStatusLabel != null) {
            if (chatService != null && chatService.isConnected()) {
                connectionStatusLabel.setText("🟢 Connected to Support");
                connectionStatusLabel.setStyle("-fx-text-fill: #27AE60;");
            } else {
                connectionStatusLabel.setText("🔴 Disconnected");
                connectionStatusLabel.setStyle("-fx-text-fill: #E74C3C;");
            }
        }

        // Update chat status text
        if (chatStatusText != null) {
            if (chatService != null && chatService.isConnected()) {
                chatStatusText.setText("Online - Ready to help you");
                chatStatusText.setStyle("-fx-fill: #27AE60;");
            } else {
                chatStatusText.setText("Offline - Connection issues");
                chatStatusText.setStyle("-fx-fill: #E74C3C;");
            }
        }
    }

    private void updateUnreadMessageCount() {
        if (UserSession.isLoggedIn() && chatService != null) {
            try {
                int unreadCount = chatService.getUnreadMessageCount(
                        UserSession.getCurrentUserId(),
                        UserSession.getCurrentUserType()
                );

                // Update all possible unread message labels
                if (unreadMessagesLabel != null) {
                    if (unreadCount > 0) {
                        unreadMessagesLabel.setText("📬 " + unreadCount + " unread messages");
                        unreadMessagesLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                        unreadMessagesLabel.setVisible(true);
                    } else {
                        unreadMessagesLabel.setText("📭 No new messages");
                        unreadMessagesLabel.setStyle("-fx-text-fill: #27AE60;");
                        unreadMessagesLabel.setVisible(true);
                    }
                }

                if (unreadMessagesText != null) {
                    if (unreadCount > 0) {
                        unreadMessagesText.setText(unreadCount + " new messages");
                        unreadMessagesText.setVisible(true);
                        unreadMessagesText.setStyle("-fx-fill: #E74C3C; -fx-font-weight: bold;");
                    } else {
                        unreadMessagesText.setVisible(false);
                    }
                }

                // Update chat badges
                if (chatUnreadBadge != null) {
                    if (unreadCount > 0) {
                        chatUnreadBadge.setText(String.valueOf(unreadCount));
                        chatUnreadBadge.setVisible(true);
                    } else {
                        chatUnreadBadge.setVisible(false);
                    }
                }

                if (sidebarChatBadge != null) {
                    if (unreadCount > 0) {
                        sidebarChatBadge.setText(String.valueOf(unreadCount));
                        sidebarChatBadge.setVisible(true);
                    } else {
                        sidebarChatBadge.setVisible(false);
                    }
                }

            } catch (Exception e) {
                System.err.println("❌ Error updating unread count: " + e.getMessage());
            }
        }
    }

    // IMPROVED STAGE FINDING METHOD
    private Stage getCurrentStage() {
        // Try to get stage from any available component
        Button[] buttons = {
                chatSupportBtn, settingsBtn, logoutBtn, logoutButton,
                dashboardBtn, panicBtn, voiceActivationBtn, emergencyContactsBtn,
                quickPanicBtn, startChatBtn, quickChatBtn
        };

        for (Button btn : buttons) {
            if (btn != null && btn.getScene() != null && btn.getScene().getWindow() instanceof Stage) {
                return (Stage) btn.getScene().getWindow();
            }
        }

        // Try text components
        Text[] texts = {userNameLabel, welcomeLabel, userDisplayName, currentUserName};
        for (Text text : texts) {
            if (text != null && text.getScene() != null && text.getScene().getWindow() instanceof Stage) {
                return (Stage) text.getScene().getWindow();
            }
        }

        // Try labels
        Label[] labels = {connectionStatusLabel, unreadMessagesLabel};
        for (Label label : labels) {
            if (label != null && label.getScene() != null && label.getScene().getWindow() instanceof Stage) {
                return (Stage) label.getScene().getWindow();
            }
        }

        System.err.println("❌ Could not find any valid stage reference");
        return null;
    }

    // ===== NAVIGATION METHODS =====

    @FXML
    private void openVoiceAlert() {
        navigateToPage("/com/codevengers/voiceemergency/VoiceAlert.fxml", "QuickRescue - Voice Alert");
    }

    @FXML
    private void openChatSupport() {
        try {
            // Start server if not running
            if (!ServerLauncher.isServerRunning()) {
                ServerLauncher.startServer();
                Thread.sleep(1000);
            }

            navigateToPage("/com/codevengers/voiceemergency/UserChat.fxml", "QuickRescue - Chat Support");
        } catch (Exception e) {
            System.err.println("❌ Error opening Chat Support: " + e.getMessage());
            showErrorAlert("Error", "Could not open Chat Support feature: " + e.getMessage());
        }
    }

    @FXML
    private void openEmergencyContacts() {
        navigateToPage("/com/codevengers/voiceemergency/EmergencyContacts.fxml", "QuickRescue - Emergency Contacts");
    }

    @FXML
    private void openSettings() {
        navigateToPage("/com/codevengers/voiceemergency/UserSettings.fxml", "QuickRescue - Settings");
    }

    // ===== LOGOUT METHODS - ALL POSSIBLE FXML REFERENCES =====

    @FXML private void logout() { performLogout(); }
    @FXML private void handleLogout() { performLogout(); }
    @FXML private void onLogoutClick() { performLogout(); }
    @FXML private void logoutUser() { performLogout(); }
    @FXML private void userLogout() { performLogout(); }
    @FXML private void handleLogoutButton() { performLogout(); }
    @FXML private void logoutAction() { performLogout(); }
    @FXML private void signOut() { performLogout(); }
    @FXML private void handleSignOut() { performLogout(); }
    @FXML private void exitApplication() { performLogout(); }

    private void performLogout() {
        try {
            System.out.println("🔓 Logging out user...");

            // Stop voice detection
            stopEnhancedVoiceDetection();

            // Stop any active emergency
            if (EnhancedEmergencyHandler.isEmergencyTriggered()) {
                Alert stopAlert = new Alert(Alert.AlertType.CONFIRMATION);
                stopAlert.setTitle("Active Emergency");
                stopAlert.setHeaderText("Emergency is currently active");
                stopAlert.setContentText("There is an active emergency. Do you want to stop it before logging out?");

                if (stopAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    EnhancedEmergencyHandler.stopEmergency();
                }
            }

            // Clean up chat service
            if (chatService != null) {
                chatService.disconnect();
            }

            // Clear user session
            UserSession.logout();
            System.out.println("✅ User session cleared");

            // Return to login screen
            Parent root = FXMLLoader.load(getClass().getResource("/com/codevengers/voiceemergency/UserLogin.fxml"));

            Stage stage = getCurrentStage();
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle("QuickRescue - User Login");
                System.out.println("✅ Redirected to login screen");
            } else {
                System.err.println("❌ Could not find stage for logout navigation");
                showErrorAlert("Navigation Error", "Could not navigate to login screen.");
            }

        } catch (Exception e) {
            System.err.println("❌ Error during logout: " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Error", "Could not logout properly: " + e.getMessage());
        }
    }

    // ===== EMERGENCY METHODS =====

    @FXML
    private void triggerEmergencyAlert() {
        triggerSpecificEmergency("PANIC_BUTTON", "Panic button activated - immediate help needed!");
    }

    @FXML
    private void sendEmergencyMessage() {
        triggerSpecificEmergency("EMERGENCY_MESSAGE", "🚨 EMERGENCY: Need immediate assistance!");
    }

    @FXML
    private void quickEmergencyHelp() {
        triggerSpecificEmergency("HELP_REQUEST", "🚨 EMERGENCY: Help me!");
    }

    @FXML
    private void quickEmergencyDanger() {
        triggerSpecificEmergency("DANGER", "🚨 EMERGENCY: I'm in danger!");
    }

    @FXML
    private void quickEmergencyPolice() {
        triggerSpecificEmergency("POLICE_NEEDED", "🚨 EMERGENCY: Call police!");
    }

    @FXML
    private void quickEmergencyMedical() {
        triggerSpecificEmergency("MEDICAL_EMERGENCY", "🚨 MEDICAL EMERGENCY: Need immediate medical assistance!");
    }

    private void triggerSpecificEmergency(String emergencyType, String description) {
        try {
            System.out.println("🚨 Triggering emergency: " + emergencyType);

            // Show confirmation dialog
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Emergency Alert");
            confirmAlert.setHeaderText("Trigger Emergency Alert?");
            confirmAlert.setContentText("This will:\n" +
                    "• Send alert to emergency contacts\n" +
                    "• Start audio recording\n" +
                    "• Share your location\n" +
                    "• Notify administrators\n\n" +
                    "Are you sure you want to proceed?");

            if (confirmAlert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                // Trigger emergency using enhanced handler
                EnhancedEmergencyHandler.triggerEmergency("panic_button", emergencyType, description);

                // Show success alert
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Emergency Activated");
                successAlert.setHeaderText("Emergency Alert Sent");
                successAlert.setContentText("Your emergency alert has been activated!\n\n" +
                        "• Emergency contacts have been notified\n" +
                        "• Audio recording is active\n" +
                        "• Location has been shared\n" +
                        "• Administrators have been alerted\n\n" +
                        "Help is on the way!");
                successAlert.show();

                // Update emergency controls
                updateEmergencyControls();

                // Refresh the activity table
                Platform.runLater(() -> loadRecentActivity());
            }

        } catch (Exception e) {
            System.err.println("❌ Error triggering emergency: " + e.getMessage());
            showErrorAlert("Emergency Error", "Could not trigger emergency alert: " + e.getMessage());
        }
    }

    @FXML
    private void toggleVoiceActivation() {
        if (voiceToggle != null) {
            if (voiceToggle.isSelected()) {
                if (!isVoiceDetectionActive) {
                    startEnhancedVoiceDetection();
                }
            } else {
                if (isVoiceDetectionActive) {
                    stopEnhancedVoiceDetection();
                }
            }
        } else {
            // Manual toggle when button is not available
            if (isVoiceDetectionActive) {
                stopEnhancedVoiceDetection();
            } else {
                startEnhancedVoiceDetection();
            }
        }
    }

    // ===== ALL POSSIBLE FXML METHOD NAMES =====

    @FXML private void showChatSupport() { openChatSupport(); }
    @FXML private void handleVoiceAlert() { openVoiceAlert(); }
    @FXML private void handleEmergencyContacts() { openEmergencyContacts(); }
    @FXML private void handleSettings() { openSettings(); }
    @FXML private void handleChatSupport() { openChatSupport(); }
    @FXML private void showDashboardView() { /* Already on dashboard */ }
    @FXML private void showPanicButton() { triggerEmergencyAlert(); }
    @FXML private void showVoiceActivation() { toggleVoiceActivation(); }
    @FXML private void showEmergencyContacts() { openEmergencyContacts(); }
    @FXML private void showIncidentHistory() {
        navigateToPage("/com/codevengers/voiceemergency/IncidentHistory.fxml", "QuickRescue - Incident History");
    }
    @FXML private void showManualAlert() {
        navigateToPage("/com/codevengers/voiceemergency/ManualAlert.fxml", "QuickRescue - Manual Alert");
    }
    @FXML private void showSettings() { openSettings(); }
    @FXML private void showSystemStatus() {
        navigateToPage("/com/codevengers/voiceemergency/SystemStatus.fxml", "QuickRescue - System Status");
    }

    // ===== HELPER METHODS =====

    private void navigateToPage(String fxmlFile, String title) {
        try {
            System.out.println("🔄 Navigating to: " + fxmlFile);
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));

            Stage stage = getCurrentStage();
            if (stage != null) {
                stage.setScene(new Scene(root));
                stage.setTitle(title);
                System.out.println("✅ Navigation successful");
            } else {
                System.err.println("❌ Could not find stage for navigation");
                showErrorAlert("Navigation Error", "Could not navigate to " + title);
            }
        } catch (Exception e) {
            System.err.println("❌ Navigation error to " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
            showErrorAlert("Navigation Error", "Could not open " + title + ": " + e.getMessage());
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}