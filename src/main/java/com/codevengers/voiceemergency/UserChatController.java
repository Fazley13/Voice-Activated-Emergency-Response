package com.codevengers.voiceemergency;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class UserChatController implements Initializable, JavaChatService.ChatMessageListener {

    @FXML private Text userNameLabel;
    @FXML private Text connectionStatusIcon;
    @FXML private Text connectionStatusText;
    @FXML private VBox messagesContainer;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private Text typingIndicator;

    private JavaChatService chatService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== USER CHAT INITIALIZING ===");

        // Check user session first
        if (!UserSession.isLoggedIn()) {
            System.err.println("❌ User not logged in!");
            showAlert("Session Error", "User session not found. Please login again.");
            return;
        }

        System.out.println("✅ User session valid: " + UserSession.getCurrentUsername());

        // Set user name
        if (userNameLabel != null) {
            userNameLabel.setText(UserSession.getCurrentUsername());
            System.out.println("✅ Set username label: " + UserSession.getCurrentUsername());
        }

        // Initialize chat service
        initializeChatService();

        // Setup message input
        setupMessageInput();

        // Setup auto-scroll
        setupAutoScroll();

        // Load existing chat history
        loadChatHistory();

        System.out.println("✅ User chat initialized successfully");
    }

    private void initializeChatService() {
        try {
            System.out.println("🔄 Initializing chat service...");

            chatService = JavaChatService.getInstance();
            System.out.println("✅ Got JavaChatService instance");

            chatService.addMessageListener(this);
            System.out.println("✅ Added message listener");

            // Join the chat room
            if (UserSession.isLoggedIn()) {
                chatService.joinRoom(UserSession.getCurrentUserId(), "user");
                System.out.println("✅ User joined chat room: " + UserSession.getCurrentUserId());
            }

            updateConnectionStatus();
            System.out.println("✅ Chat service initialized for user");

        } catch (Exception e) {
            System.err.println("❌ Error initializing chat service: " + e.getMessage());
            e.printStackTrace();
            showAlert("Connection Error", "Failed to initialize chat service: " + e.getMessage());
        }
    }

    private void loadChatHistory() {
        if (chatService != null && UserSession.isLoggedIn()) {
            new Thread(() -> {
                try {
                    System.out.println("🔄 Loading chat history...");
                    List<JavaChatService.ChatMessage> history = chatService.getChatHistory(
                            UserSession.getCurrentUserId(),
                            "user"
                    );

                    Platform.runLater(() -> {
                        if (messagesContainer != null) {
                            // Clear welcome message
                            messagesContainer.getChildren().clear();

                            // Add chat history
                            for (JavaChatService.ChatMessage message : history) {
                                addMessageToUI(message);
                            }
                        }
                    });

                    System.out.println("✅ Loaded " + history.size() + " chat messages");
                } catch (Exception e) {
                    System.err.println("❌ Error loading chat history: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void addMessageToUI(JavaChatService.ChatMessage message) {
        if (messagesContainer == null) {
            System.err.println("❌ messagesContainer is null!");
            return;
        }

        try {
            VBox messageBox = createMessageBubble(message);
            messagesContainer.getChildren().add(messageBox);

            // Auto-scroll to bottom
            Platform.runLater(() -> {
                if (messagesScrollPane != null) {
                    messagesScrollPane.setVvalue(1.0);
                }
            });

            System.out.println("✅ Added message to UI: " + message.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error adding message to UI: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createMessageBubble(JavaChatService.ChatMessage message) {
        VBox messageBox = new VBox(5);
        messageBox.setPadding(new Insets(8, 12, 8, 12));
        messageBox.setMaxWidth(400);

        boolean isCurrentUser = message.getSenderId() == UserSession.getCurrentUserId();
        boolean isAdmin = "admin".equals(message.getSenderType());

        // Message content
        Text messageText = new Text(message.getMessage());
        messageText.setWrappingWidth(350);
        messageText.setFont(Font.font(14));

        // Timestamp
        Text timestampText = new Text(formatTimestamp(message.getTimestamp()));
        timestampText.setFont(Font.font(10));
        timestampText.setStyle("-fx-fill: #7F8C8D;");

        messageBox.getChildren().addAll(messageText, timestampText);

        // Style based on sender and message type
        if (isCurrentUser) {
            // User's own messages - right aligned, blue
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            messageBox.setStyle("-fx-background-color: #3498DB; -fx-background-radius: 15;");
            messageText.setStyle("-fx-fill: white;");
            timestampText.setStyle("-fx-fill: #AED6F1;");
        } else if (isAdmin) {
            // Admin messages - left aligned, dark
            messageBox.setAlignment(Pos.CENTER_LEFT);
            messageBox.setStyle("-fx-background-color: #2C3E50; -fx-background-radius: 15;");
            messageText.setStyle("-fx-fill: white;");
            timestampText.setStyle("-fx-fill: #BDC3C7;");
        } else {
            // Other user messages - left aligned, light
            messageBox.setAlignment(Pos.CENTER_LEFT);
            messageBox.setStyle("-fx-background-color: #ECF0F1; -fx-background-radius: 15;");
            messageText.setStyle("-fx-fill: #2C3E50;");
        }

        // Special styling for emergency messages
        if ("emergency".equals(message.getMessageType()) || "silent_emergency".equals(message.getMessageType())) {
            messageBox.setStyle("-fx-background-color: #E74C3C; -fx-background-radius: 15;");
            messageText.setStyle("-fx-fill: white; -fx-font-weight: bold;");
            timestampText.setStyle("-fx-fill: #FADBD8;");
        }

        // Container for alignment
        HBox container = new HBox();
        if (isCurrentUser) {
            container.setAlignment(Pos.CENTER_RIGHT);
        } else {
            container.setAlignment(Pos.CENTER_LEFT);
        }
        container.getChildren().add(messageBox);

        VBox outerContainer = new VBox();
        outerContainer.getChildren().add(container);
        return outerContainer;
    }

    private String formatTimestamp(String timestamp) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            return timestamp;
        }
    }

    private void setupMessageInput() {
        System.out.println("🔄 Setting up message input...");

        if (messageInput != null) {
            messageInput.setOnAction(e -> {
                System.out.println("📝 Enter key pressed in message input");
                sendMessage();
            });
            System.out.println("✅ Message input Enter key handler set");
        } else {
            System.err.println("❌ messageInput is null!");
        }

        if (sendButton != null) {
            sendButton.setOnAction(e -> {
                System.out.println("🖱️ Send button clicked");
                sendMessage();
            });
            System.out.println("✅ Send button click handler set");
        } else {
            System.err.println("❌ sendButton is null!");
        }

        // Enable/disable send button based on input
        if (messageInput != null && sendButton != null) {
            messageInput.textProperty().addListener((obs, oldText, newText) -> {
                boolean shouldDisable = newText.trim().isEmpty() || !isConnected();
                sendButton.setDisable(shouldDisable);
                System.out.println("🔄 Send button disabled: " + shouldDisable);
            });
            System.out.println("✅ Message input text listener set");
        }
    }

    private void setupAutoScroll() {
        if (messagesContainer != null && messagesScrollPane != null) {
            messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));
            });
            System.out.println("✅ Auto-scroll setup complete");
        }
    }

    private void updateConnectionStatus() {
        boolean connected = isConnected();
        System.out.println("🔄 Updating connection status: " + connected);

        if (connectionStatusIcon != null && connectionStatusText != null) {
            if (connected) {
                connectionStatusIcon.setText("🟢");
                connectionStatusText.setText("Connected to Emergency Services");
                connectionStatusText.setStyle("-fx-fill: #27AE60;");
            } else {
                connectionStatusIcon.setText("🔴");
                connectionStatusText.setText("Disconnected");
                connectionStatusText.setStyle("-fx-fill: #E74C3C;");
            }
        }

        // Update send button state
        if (sendButton != null && messageInput != null) {
            boolean shouldDisable = !connected || messageInput.getText().trim().isEmpty();
            sendButton.setDisable(shouldDisable);
            System.out.println("🔄 Send button disabled: " + shouldDisable);
        }
    }

    private boolean isConnected() {
        boolean connected = chatService != null && chatService.isConnected();
        System.out.println("🔍 Connection check: chatService=" + (chatService != null) + ", connected=" + connected);
        return connected;
    }

    // FXML Action Methods - These must match the FXML exactly
    @FXML
    private void sendMessage() {
        System.out.println("🚀 sendMessage() called");

        try {
            if (messageInput == null) {
                System.err.println("❌ messageInput is null!");
                showAlert("Error", "Message input not found!");
                return;
            }

            if (!UserSession.isLoggedIn()) {
                System.err.println("❌ User not logged in!");
                showAlert("Session Error", "User session expired. Please login again.");
                return;
            }

            if (!isConnected()) {
                System.err.println("❌ Not connected to chat service!");
                showAlert("Connection Error", "Not connected to chat service!");
                return;
            }

            String messageText = messageInput.getText().trim();
            if (messageText.isEmpty()) {
                System.err.println("❌ Message text is empty!");
                return;
            }

            System.out.println("📤 Sending message: " + messageText);

            // Send message using JavaChatService
            chatService.sendMessage(
                    UserSession.getCurrentUserId(),
                    "user",
                    null, // receiverId - null means send to admins
                    "admin", // receiverType
                    messageText,
                    "text" // messageType
            );

            messageInput.clear();
            System.out.println("✅ Message sent successfully");

        } catch (Exception e) {
            System.err.println("❌ Error sending message: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to send message: " + e.getMessage());
        }
    }

    @FXML
    private void sendEmergencyMessage(javafx.event.ActionEvent event) {
        System.out.println("🚨 sendEmergencyMessage() called");

        try {
            if (!UserSession.isLoggedIn()) {
                System.err.println("❌ User not logged in!");
                showAlert("Session Error", "User session expired. Please login again.");
                return;
            }

            if (!isConnected()) {
                System.err.println("❌ Not connected to chat service!");
                showAlert("Connection Error", "Not connected to emergency services!");
                return;
            }

            // Get the emergency message from the button
            Button sourceButton = (Button) event.getSource();
            String emergencyMessage = sourceButton.getText(); // Use button text instead of userData

            System.out.println("🚨 Emergency message: " + emergencyMessage);

            // Send emergency message using JavaChatService
            chatService.sendMessage(
                    UserSession.getCurrentUserId(),
                    "user",
                    null, // receiverId - null means send to admins
                    "admin", // receiverType
                    "🚨 EMERGENCY: " + emergencyMessage,
                    "emergency" // messageType
            );

            System.out.println("✅ Emergency message sent successfully");

            // Show confirmation
            showAlert("Emergency Alert Sent", "Your emergency message has been sent to emergency services!");

        } catch (Exception e) {
            System.err.println("❌ Error sending emergency message: " + e.getMessage());
            e.printStackTrace();
            showAlert("Error", "Failed to send emergency message: " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToDashboard() {
        System.out.println("🔙 handleBackToDashboard() called");

        try {
            // Clean up chat service listener
            if (chatService != null) {
                chatService.removeMessageListener(this);
                System.out.println("✅ Removed message listener");
            }

            Parent root = FXMLLoader.load(getClass().getResource("/com/codevengers/voiceemergency/UserDashboard.fxml"));
            Stage stage = (Stage) userNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("QuickRescue - User Dashboard");
            System.out.println("✅ Navigated back to dashboard");

        } catch (Exception e) {
            System.err.println("❌ Error navigating to dashboard: " + e.getMessage());
            e.printStackTrace();
            showAlert("Navigation Error", "Failed to return to dashboard: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        } catch (Exception e) {
            System.err.println("❌ Error showing alert: " + e.getMessage());
        }
    }

    // ChatMessageListener implementation
    @Override
    public void onNewMessage(JavaChatService.ChatMessage message) {
        System.out.println("📨 New message received: " + message.getMessage());

        Platform.runLater(() -> {
            try {
                addMessageToUI(message);

                // Mark message as read if it's for this user
                if (message.getReceiverId() != null && message.getReceiverId() == UserSession.getCurrentUserId()) {
                    chatService.markMessageAsRead(message.getId());
                }
            } catch (Exception e) {
                System.err.println("❌ Error handling new message: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onMessageStatusUpdate(int messageId, boolean isRead) {
        System.out.println("📋 Message status update: ID=" + messageId + ", read=" + isRead);
        // Handle message status updates if needed
    }
}
