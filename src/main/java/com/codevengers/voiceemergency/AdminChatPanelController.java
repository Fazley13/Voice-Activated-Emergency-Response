package com.codevengers.voiceemergency;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class AdminChatPanelController implements Initializable, JavaChatService.ChatMessageListener {

    @FXML private VBox userListContainer;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private VBox messagesContainer;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private Text selectedUserLabel;
    @FXML private Text connectionStatusText;
    @FXML private Text unreadCountText;

    private JavaChatService chatService;
    private ObservableList<JavaChatService.ChatMessage> messages = FXCollections.observableArrayList();
    private ObservableList<UserInfo> users = FXCollections.observableArrayList();
    private UserInfo selectedUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== ADMIN CHAT PANEL INITIALIZING ===");

        // Initialize chat service
        initializeChatService();

        // Setup UI
        setupChatUI();

        // Load users
        loadUsers();

        // Setup message input
        setupMessageInput();
    }

    private void initializeChatService() {
        try {
            chatService = JavaChatService.getInstance();
            chatService.addMessageListener(this);

            if (AdminSession.isLoggedIn()) {
                chatService.joinRoom(AdminSession.getCurrentAdminId(), "admin");
            }

            updateConnectionStatus();
            System.out.println("✅ Chat panel service initialized");
        } catch (Exception e) {
            System.err.println("❌ Error initializing chat panel service: " + e.getMessage());
        }
    }

    private void setupChatUI() {
        if (messagesContainer != null && messagesScrollPane != null) {
            messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));
            });
            messagesContainer.setSpacing(10);
            messagesContainer.setPadding(new Insets(10));
        }
    }

    private void setupMessageInput() {
        if (messageInput != null) {
            messageInput.setOnAction(e -> sendMessage());
        }

        if (messageInput != null && sendButton != null) {
            messageInput.textProperty().addListener((obs, oldText, newText) -> {
                sendButton.setDisable(newText.trim().isEmpty() || selectedUser == null);
            });
        }

        if (sendButton != null) {
            sendButton.setDisable(true);
        }
    }

    private void loadUsers() {
        new Thread(() -> {
            try {
                Connection conn = DBConnection.getConnection();
                String query = "SELECT id, username, email FROM users WHERE role = 'user'";
                PreparedStatement stmt = conn.prepareStatement(query);
                ResultSet rs = stmt.executeQuery();

                List<UserInfo> userList = new ArrayList<>();
                while (rs.next()) {
                    UserInfo user = new UserInfo(
                            rs.getInt("id"),
                            rs.getString("username"),
                            rs.getString("email")
                    );

                    if (chatService != null) {
                        int unreadCount = getUnreadCountForUser(user.getId());
                        user.setUnreadCount(unreadCount);
                    }

                    userList.add(user);
                }

                Platform.runLater(() -> {
                    users.clear();
                    users.addAll(userList);
                    displayUsers();
                    updateUnreadCount();
                });

                rs.close();
                stmt.close();
                conn.close();

            } catch (Exception e) {
                System.err.println("❌ Error loading users: " + e.getMessage());
                Platform.runLater(() -> {
                    users.add(new UserInfo(1, "John Doe", "john@example.com"));
                    users.add(new UserInfo(2, "Jane Smith", "jane@example.com"));
                    displayUsers();
                });
            }
        }).start();
    }

    private void displayUsers() {
        if (userListContainer != null) {
            userListContainer.getChildren().clear();

            for (UserInfo user : users) {
                HBox userCard = createUserCard(user);
                userListContainer.getChildren().add(userCard);
            }
        }
    }

    private HBox createUserCard(UserInfo user) {
        HBox userCard = new HBox(10);
        userCard.setPadding(new Insets(8));
        userCard.setAlignment(Pos.CENTER_LEFT);
        userCard.setStyle("-fx-background-color: white; -fx-border-color: #BDC3C7; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");

        Text nameText = new Text(user.getUsername());
        nameText.setFont(Font.font(12));

        userCard.getChildren().add(nameText);

        if (user.getUnreadCount() > 0) {
            Label badge = new Label(String.valueOf(user.getUnreadCount()));
            badge.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 1 4; -fx-font-size: 9px;");
            userCard.getChildren().add(badge);
        }

        userCard.setOnMouseClicked(e -> selectUser(user));
        return userCard;
    }

    private void selectUser(UserInfo user) {
        selectedUser = user;
        if (selectedUserLabel != null) {
            selectedUserLabel.setText("Chat with: " + user.getUsername());
        }

        loadChatHistoryForUser(user);

        if (sendButton != null && messageInput != null) {
            sendButton.setDisable(messageInput.getText().trim().isEmpty());
        }
    }

    private int getUnreadCountForUser(int userId) {
        try {
            Connection conn = DBConnection.getConnection();
            String query = "SELECT COUNT(*) FROM chat_messages WHERE sender_id = ? AND sender_type = 'user' AND is_read = false";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }

            rs.close();
            stmt.close();
            conn.close();
            return count;

        } catch (Exception e) {
            System.err.println("❌ Error getting unread count: " + e.getMessage());
            return 0;
        }
    }

    private void loadChatHistoryForUser(UserInfo user) {
        if (chatService != null) {
            new Thread(() -> {
                try {
                    List<JavaChatService.ChatMessage> history = chatService.getChatHistory(user.getId(), "user");

                    Platform.runLater(() -> {
                        messages.clear();
                        messages.addAll(history);
                        displayMessages();
                    });
                } catch (Exception e) {
                    System.err.println("❌ Error loading chat history: " + e.getMessage());
                }
            }).start();
        }
    }

    private void displayMessages() {
        if (messagesContainer != null) {
            messagesContainer.getChildren().clear();

            for (JavaChatService.ChatMessage message : messages) {
                VBox messageBox = createMessageBubble(message);
                messagesContainer.getChildren().add(messageBox);
            }

            Platform.runLater(() -> {
                if (messagesScrollPane != null) {
                    messagesScrollPane.setVvalue(1.0);
                }
            });
        }
    }

    private VBox createMessageBubble(JavaChatService.ChatMessage message) {
        VBox messageBox = new VBox(3);
        messageBox.setPadding(new Insets(6, 10, 6, 10));
        messageBox.setMaxWidth(250);

        boolean isAdmin = "admin".equals(message.getSenderType());

        Text messageText = new Text(message.getMessage());
        messageText.setWrappingWidth(230);
        messageText.setFont(Font.font(11));

        Text timestampText = new Text(formatTimestamp(message.getTimestamp()));
        timestampText.setFont(Font.font(8));
        timestampText.setStyle("-fx-fill: #7F8C8D;");

        messageBox.getChildren().addAll(messageText, timestampText);

        if (isAdmin) {
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            messageBox.setStyle("-fx-background-color: #3498DB; -fx-background-radius: 10;");
            messageText.setStyle("-fx-fill: white;");
        } else {
            messageBox.setAlignment(Pos.CENTER_LEFT);
            if ("emergency".equals(message.getMessageType()) || "silent_emergency".equals(message.getMessageType())) {
                messageBox.setStyle("-fx-background-color: #E74C3C; -fx-background-radius: 10;");
                messageText.setStyle("-fx-fill: white;");
            } else {
                messageBox.setStyle("-fx-background-color: #ECF0F1; -fx-background-radius: 10;");
                messageText.setStyle("-fx-fill: #2C3E50;");
            }
        }

        HBox container = new HBox();
        if (isAdmin) {
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

    @FXML
    private void sendMessage() {
        if (messageInput != null && selectedUser != null) {
            String messageText = messageInput.getText().trim();
            if (!messageText.isEmpty() && chatService != null && AdminSession.isLoggedIn()) {
                chatService.sendMessage(
                        AdminSession.getCurrentAdminId(),
                        "admin",
                        selectedUser.getId(),
                        "user",
                        messageText,
                        "text"
                );
                messageInput.clear();
            }
        }
    }

    private void updateConnectionStatus() {
        Platform.runLater(() -> {
            if (connectionStatusText != null) {
                if (chatService != null && chatService.isConnected()) {
                    connectionStatusText.setText("🟢 Online");
                    connectionStatusText.setStyle("-fx-fill: #27AE60;");
                } else {
                    connectionStatusText.setText("🔴 Offline");
                    connectionStatusText.setStyle("-fx-fill: #E74C3C;");
                }
            }
        });
    }

    private void updateUnreadCount() {
        Platform.runLater(() -> {
            if (unreadCountText != null) {
                int totalUnread = users.stream().mapToInt(UserInfo::getUnreadCount).sum();
                unreadCountText.setText(String.valueOf(totalUnread));
            }
        });
    }

    // ChatMessageListener implementation
    @Override
    public void onNewMessage(JavaChatService.ChatMessage message) {
        Platform.runLater(() -> {
            // Update user unread counts
            for (UserInfo user : users) {
                if (user.getId() == message.getSenderId() && "user".equals(message.getSenderType())) {
                    user.setUnreadCount(user.getUnreadCount() + 1);
                    displayUsers();
                    updateUnreadCount();
                    break;
                }
            }

            // If viewing this user's chat, add message
            if (selectedUser != null &&
                    ((message.getSenderId() == selectedUser.getId() && "user".equals(message.getSenderType())) ||
                            (message.getReceiverId() != null && message.getReceiverId() == selectedUser.getId() && "admin".equals(message.getSenderType())))) {

                messages.add(message);
                VBox messageBox = createMessageBubble(message);
                if (messagesContainer != null) {
                    messagesContainer.getChildren().add(messageBox);
                }

                Platform.runLater(() -> {
                    if (messagesScrollPane != null) {
                        messagesScrollPane.setVvalue(1.0);
                    }
                });

                // Mark as read if user message
                if ("user".equals(message.getSenderType()) && chatService != null) {
                    chatService.markMessageAsRead(message.getId());
                    selectedUser.setUnreadCount(Math.max(0, selectedUser.getUnreadCount() - 1));
                    displayUsers();
                    updateUnreadCount();
                }
            }
        });
    }

    @Override
    public void onMessageStatusUpdate(int messageId, boolean isRead) {
        // Update message status
    }

    // Inner class for user information
    public static class UserInfo {
        private int id;
        private String username;
        private String email;
        private int unreadCount;

        public UserInfo(int id, String username, String email) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.unreadCount = 0;
        }

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public int getUnreadCount() { return unreadCount; }
        public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    }
}
