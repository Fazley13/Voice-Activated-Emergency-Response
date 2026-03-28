package com.codevengers.voiceemergency;

import com.codevengers.voiceemergency.JavaChatService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class AdminChatController implements Initializable, JavaChatService.ChatMessageListener {

    @FXML private Text adminNameLabel;
    @FXML private Text connectionStatusIcon;
    @FXML private Text connectionStatusText;
    @FXML private VBox userListContainer;
    @FXML private Button allConversationsBtn;
    @FXML private Text chatHeaderTitle;
    @FXML private Text chatHeaderSubtitle;
    @FXML private ScrollPane messagesScrollPane;
    @FXML private VBox messagesContainer;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private Button broadcastButton;
    @FXML private Text selectedUserLabel;
    @FXML private TextField searchField;
    @FXML private Text totalUsersText;
    @FXML private Text emergencyCountText;
    @FXML private Text unreadCountText;

    private JavaChatService chatService;
    private ObservableList<JavaChatService.ChatMessage> messages = FXCollections.observableArrayList();
    private ObservableList<UserInfo> users = FXCollections.observableArrayList();
    private UserInfo selectedUser;
    private int emergencyAlertCount = 0;

    // Cache for usernames to avoid repeated database queries
    private Map<Integer, String> userNameCache = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("=== ADMIN CHAT INITIALIZING ===");

        // Set admin info
        if (AdminSession.isLoggedIn()) {
            if (adminNameLabel != null) {
                adminNameLabel.setText("Admin: " + AdminSession.getCurrentAdminUsername());
            }
        }

        // Initialize chat service
        initializeChatService();

        // Setup UI
        setupChatUI();

        // Load users and cache usernames
        loadUsersAndCacheNames();

        // Setup message input
        setupMessageInput();

        // Setup search functionality
        setupSearch();
    }

    private void initializeChatService() {
        try {
            chatService = JavaChatService.getInstance();

            // Try to connect if not connected
            if (!chatService.isConnected()) {
                System.out.println("🔄 Chat service not connected, attempting to connect...");
                chatService.connect();
            }

            chatService.addMessageListener(this);

            if (AdminSession.isLoggedIn()) {
                chatService.joinRoom(AdminSession.getCurrentAdminId(), "admin");
            }

            updateConnectionStatus();
            System.out.println("✅ Chat service initialized for admin");
        } catch (Exception e) {
            System.err.println("❌ Error initializing chat service: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupChatUI() {
        // Auto-scroll to bottom when new messages are added
        if (messagesContainer != null && messagesScrollPane != null) {
            messagesContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
                Platform.runLater(() -> messagesScrollPane.setVvalue(1.0));
            });

            // Style the chat container
            messagesContainer.setSpacing(10);
            messagesContainer.setPadding(new Insets(10));
        }
    }

    private void setupMessageInput() {
        // Send message on Enter key
        if (messageInput != null) {
            messageInput.setOnAction(e -> sendMessage());
        }

        // Enable/disable send button based on text input and user selection
        if (messageInput != null && sendButton != null) {
            messageInput.textProperty().addListener((obs, oldText, newText) -> {
                sendButton.setDisable(newText.trim().isEmpty() || selectedUser == null);
            });
        }

        if (broadcastButton != null && messageInput != null) {
            messageInput.textProperty().addListener((obs, oldText, newText) -> {
                broadcastButton.setDisable(newText.trim().isEmpty());
            });
        }

        if (sendButton != null) {
            sendButton.setDisable(true);
        }
        if (broadcastButton != null) {
            broadcastButton.setDisable(true);
        }
    }

    private void setupSearch() {
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldText, newText) -> {
                filterUsers(newText.trim().toLowerCase());
            });
        }
    }

    private void filterUsers(String searchText) {
        if (userListContainer != null) {
            userListContainer.getChildren().clear();

            for (UserInfo user : users) {
                if (searchText.isEmpty() ||
                        user.getUsername().toLowerCase().contains(searchText) ||
                        user.getEmail().toLowerCase().contains(searchText)) {
                    VBox userCard = createUserCard(user);
                    userListContainer.getChildren().add(userCard);
                }
            }
        }
    }

    private void loadUsersAndCacheNames() {
        new Thread(() -> {
            try {
                System.out.println("🔄 Loading users from database...");
                Connection conn = DatabaseConnection.connect(); // Using your connect() method

                // First, let's check what tables and columns exist
                System.out.println("🔍 Checking database structure...");

                // Try different possible table/column combinations
                String userQuery = null;
                PreparedStatement userStmt = null;

                // Try the most common variations
                String[] possibleQueries = {
                        "SELECT id, username, email FROM users WHERE user_type = 'user' ORDER BY username",
                        "SELECT id, username, email FROM users WHERE role = 'user' ORDER BY username",
                        "SELECT id, name as username, email FROM users WHERE user_type = 'user' ORDER BY name",
                        "SELECT id, name as username, email FROM users WHERE role = 'user' ORDER BY name",
                        "SELECT user_id as id, username, email FROM users WHERE user_type = 'user' ORDER BY username",
                        "SELECT user_id as id, username, email FROM users WHERE role = 'user' ORDER BY username",
                        "SELECT id, username, email FROM users ORDER BY username" // Get all users regardless of type
                };

                ResultSet userRs = null;
                boolean querySuccessful = false;

                for (String query : possibleQueries) {
                    try {
                        System.out.println("🔍 Trying query: " + query);
                        userStmt = conn.prepareStatement(query);
                        userRs = userStmt.executeQuery();
                        userQuery = query;
                        querySuccessful = true;
                        System.out.println("✅ Query successful: " + query);
                        break;
                    } catch (Exception e) {
                        System.out.println("❌ Query failed: " + query + " - " + e.getMessage());
                        if (userStmt != null) {
                            try { userStmt.close(); } catch (Exception ignored) {}
                        }
                    }
                }

                if (!querySuccessful) {
                    System.err.println("❌ All user queries failed. Please check your database structure.");

                    // Show database structure info
                    try {
                        PreparedStatement showTables = conn.prepareStatement("SHOW TABLES");
                        ResultSet tables = showTables.executeQuery();
                        System.out.println("📋 Available tables:");
                        while (tables.next()) {
                            System.out.println("  - " + tables.getString(1));
                        }
                        tables.close();
                        showTables.close();

                        // Try to show columns of users table
                        try {
                            PreparedStatement showColumns = conn.prepareStatement("SHOW COLUMNS FROM users");
                            ResultSet columns = showColumns.executeQuery();
                            System.out.println("📋 Columns in 'users' table:");
                            while (columns.next()) {
                                System.out.println("  - " + columns.getString("Field") + " (" + columns.getString("Type") + ")");
                            }
                            columns.close();
                            showColumns.close();
                        } catch (Exception e) {
                            System.err.println("❌ Could not show columns: " + e.getMessage());
                        }

                    } catch (Exception e) {
                        System.err.println("❌ Could not show database structure: " + e.getMessage());
                    }

                    conn.close();

                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Database Error");
                        alert.setHeaderText("Could not load users from database");
                        alert.setContentText("Please check your database structure. The system tried multiple query variations but none worked.\n\nCheck the console for detailed information about your database structure.");
                        alert.showAndWait();

                        // Don't add sample users - leave empty
                        users.clear();
                        displayUsers();
                        updateStats();
                    });
                    return;
                }

                List<UserInfo> userList = new ArrayList<>();
                Map<Integer, String> nameCache = new HashMap<>();

                while (userRs.next()) {
                    int userId = userRs.getInt("id");
                    String username = userRs.getString("username");
                    String email = userRs.getString("email");

                    System.out.println("📋 Found user: ID=" + userId + ", Username=" + username + ", Email=" + email);

                    // Add to username cache
                    nameCache.put(userId, username);

                    // Add user to list
                    UserInfo user = new UserInfo(userId, username, email);

                    // Get unread count for this user
                    int unreadCount = getUnreadCountForUser(user.getId());
                    user.setUnreadCount(unreadCount);

                    userList.add(user);
                }

                Platform.runLater(() -> {
                    userNameCache.clear();
                    userNameCache.putAll(nameCache);

                    users.clear();
                    users.addAll(userList);
                    displayUsers();
                    updateStats();

                    System.out.println("✅ Loaded " + userList.size() + " users and cached " + nameCache.size() + " usernames");

                    if (userList.isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("No Users Found");
                        alert.setHeaderText("No users found in database");
                        alert.setContentText("The database query was successful but no users were found. Make sure you have users registered in your system.");
                        alert.showAndWait();
                    }
                });

                userRs.close();
                userStmt.close();
                conn.close();

            } catch (Exception e) {
                System.err.println("❌ Error loading users: " + e.getMessage());
                e.printStackTrace();

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Database Connection Error");
                    alert.setHeaderText("Could not connect to database");
                    alert.setContentText("Error: " + e.getMessage() + "\n\nPlease check your database connection settings.");
                    alert.showAndWait();

                    // Don't add sample users - leave empty
                    users.clear();
                    displayUsers();
                    updateStats();
                });
            }
        }).start();
    }

    private void displayUsers() {
        if (userListContainer != null) {
            userListContainer.getChildren().clear();

            if (users.isEmpty()) {
                // Show empty state
                VBox emptyState = new VBox(10);
                emptyState.setAlignment(Pos.CENTER);
                emptyState.setPadding(new Insets(20));

                Text emptyText = new Text("No users found");
                emptyText.setFont(Font.font(14));
                emptyText.setStyle("-fx-fill: #7F8C8D;");

                Text instructionText = new Text("Users will appear here when they register in your system");
                instructionText.setFont(Font.font(10));
                instructionText.setStyle("-fx-fill: #BDC3C7;");

                emptyState.getChildren().addAll(emptyText, instructionText);
                userListContainer.getChildren().add(emptyState);
            } else {
                for (UserInfo user : users) {
                    VBox userCard = createUserCard(user);
                    userListContainer.getChildren().add(userCard);
                }
            }
        }
    }

    private VBox createUserCard(UserInfo user) {
        VBox userCard = new VBox(5);
        userCard.setPadding(new Insets(10));

        // Highlight selected user
        if (selectedUser != null && selectedUser.getId() == user.getId()) {
            userCard.setStyle("-fx-background-color: #3498DB; -fx-border-color: #2980B9; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        } else {
            userCard.setStyle("-fx-background-color: white; -fx-border-color: #BDC3C7; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
        }

        HBox userInfo = new HBox(10);
        userInfo.setAlignment(Pos.CENTER_LEFT);

        // User avatar (using first letter of name)
        Label avatar = new Label(user.getUsername().substring(0, 1).toUpperCase());
        avatar.setStyle("-fx-background-color: #34495E; -fx-text-fill: white; -fx-background-radius: 20; -fx-min-width: 40; -fx-min-height: 40; -fx-alignment: center; -fx-font-weight: bold;");

        Text nameText = new Text(user.getUsername());
        nameText.setFont(Font.font(14));
        nameText.setStyle(selectedUser != null && selectedUser.getId() == user.getId() ? "-fx-fill: white;" : "-fx-fill: #2C3E50;");

        Text emailText = new Text(user.getEmail());
        emailText.setFont(Font.font(10));
        emailText.setStyle(selectedUser != null && selectedUser.getId() == user.getId() ? "-fx-fill: #AED6F1;" : "-fx-fill: #7F8C8D;");

        VBox userDetails = new VBox(2);
        userDetails.getChildren().addAll(nameText, emailText);

        userInfo.getChildren().addAll(avatar, userDetails);

        // Unread message badge
        if (user.getUnreadCount() > 0) {
            Label badge = new Label(String.valueOf(user.getUnreadCount()));
            badge.setStyle("-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 2 6; -fx-font-size: 10px; -fx-font-weight: bold;");

            HBox.setMargin(badge, new Insets(0, 0, 0, 10));
            userInfo.getChildren().add(badge);
        }

        userCard.getChildren().add(userInfo);

        // Click handler
        userCard.setOnMouseClicked(e -> selectUser(user));

        // Hover effect
        userCard.setOnMouseEntered(e -> {
            if (selectedUser == null || selectedUser.getId() != user.getId()) {
                userCard.setStyle("-fx-background-color: #ECF0F1; -fx-border-color: #BDC3C7; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
            }
        });

        userCard.setOnMouseExited(e -> {
            if (selectedUser == null || selectedUser.getId() != user.getId()) {
                userCard.setStyle("-fx-background-color: white; -fx-border-color: #BDC3C7; -fx-border-width: 0 0 1 0; -fx-cursor: hand;");
            }
        });

        return userCard;
    }

    private void selectUser(UserInfo user) {
        selectedUser = user;

        if (chatHeaderTitle != null) {
            chatHeaderTitle.setText("Chat with " + user.getUsername());
        }
        if (chatHeaderSubtitle != null) {
            chatHeaderSubtitle.setText("Direct conversation with " + user.getEmail());
        }
        if (selectedUserLabel != null) {
            selectedUserLabel.setText("Replying to: " + user.getUsername());
            selectedUserLabel.setVisible(true);
        }

        // Refresh user list to show selection
        displayUsers();

        loadChatHistoryForUser(user);

        if (sendButton != null && messageInput != null) {
            sendButton.setDisable(messageInput.getText().trim().isEmpty());
        }

        System.out.println("✅ Selected user: " + user.getUsername());
    }

    private int getUnreadCountForUser(int userId) {
        try {
            Connection conn = DatabaseConnection.connect(); // Using your connect() method

            String query = "SELECT COUNT(*) FROM chat_messages WHERE sender_id = ? AND sender_type = 'user' AND is_read = FALSE";
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
            System.err.println("❌ Error getting unread count for user: " + e.getMessage());
            return 0;
        }
    }

    private void loadChatHistoryForUser(UserInfo user) {
        if (chatService != null) {
            new Thread(() -> {
                try {
                    Connection conn = DatabaseConnection.connect(); // Using your connect() method

                    String query = "SELECT * FROM chat_messages WHERE " +
                            "(sender_id = ? AND sender_type = 'user') OR " +
                            "(receiver_id = ? AND receiver_type = 'user') OR " +
                            "(sender_type = 'admin' AND (receiver_id = ? OR receiver_type = 'user')) " +
                            "ORDER BY timestamp ASC LIMIT 100";

                    PreparedStatement stmt = conn.prepareStatement(query);
                    stmt.setInt(1, user.getId());
                    stmt.setInt(2, user.getId());
                    stmt.setInt(3, user.getId());

                    ResultSet rs = stmt.executeQuery();

                    List<JavaChatService.ChatMessage> history = new ArrayList<>();

                    while (rs.next()) {
                        JavaChatService.ChatMessage message = new JavaChatService.ChatMessage(
                                rs.getInt("id"),
                                rs.getInt("sender_id"),
                                rs.getString("sender_type"),
                                rs.getObject("receiver_id") != null ? rs.getInt("receiver_id") : null,
                                rs.getString("receiver_type"),
                                rs.getString("message"),
                                rs.getString("message_type"),
                                rs.getString("timestamp"),
                                rs.getBoolean("is_read")
                        );
                        history.add(message);
                    }

                    Platform.runLater(() -> {
                        messages.clear();
                        messages.addAll(history);
                        displayMessages();

                        // Mark user messages as read
                        markUserMessagesAsRead(user.getId());
                    });

                    rs.close();
                    stmt.close();
                    conn.close();

                    System.out.println("✅ Loaded " + history.size() + " messages for user: " + user.getUsername());

                } catch (Exception e) {
                    System.err.println("❌ Error loading chat history for user: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void markUserMessagesAsRead(int userId) {
        new Thread(() -> {
            try {
                Connection conn = DatabaseConnection.connect(); // Using your connect() method
                String query = "UPDATE chat_messages SET is_read = TRUE WHERE sender_id = ? AND sender_type = 'user' AND is_read = FALSE";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setInt(1, userId);
                int updated = stmt.executeUpdate();

                stmt.close();
                conn.close();

                if (updated > 0) {
                    Platform.runLater(() -> {
                        // Update unread count for the user
                        for (UserInfo user : users) {
                            if (user.getId() == userId) {
                                user.setUnreadCount(0);
                                displayUsers();
                                updateStats();
                                break;
                            }
                        }
                    });
                    System.out.println("✅ Marked " + updated + " messages as read for user ID: " + userId);
                }

            } catch (Exception e) {
                System.err.println("❌ Error marking messages as read: " + e.getMessage());
            }
        }).start();
    }

    private void displayMessages() {
        if (messagesContainer != null) {
            messagesContainer.getChildren().clear();

            for (JavaChatService.ChatMessage message : messages) {
                VBox messageBox = createMessageBubble(message);
                messagesContainer.getChildren().add(messageBox);
            }

            // Scroll to bottom
            Platform.runLater(() -> {
                if (messagesScrollPane != null) {
                    messagesScrollPane.setVvalue(1.0);
                }
            });
        }
    }

    private VBox createMessageBubble(JavaChatService.ChatMessage message) {
        VBox messageBox = new VBox(5);
        messageBox.setPadding(new Insets(8, 12, 8, 12));
        messageBox.setMaxWidth(400);

        boolean isAdmin = "admin".equals(message.getSenderType());

        // Get sender name from cache
        String senderName = userNameCache.getOrDefault(message.getSenderId(), "User #" + message.getSenderId());
        if (isAdmin) {
            senderName = "Admin"; // You could also cache admin names
        }

        // Message content
        Text messageText = new Text(message.getMessage());
        messageText.setWrappingWidth(350);
        messageText.setFont(Font.font(14));

        // Sender and timestamp info
        String timeStr = formatTimestamp(message.getTimestamp());
        Text senderTimeText = new Text(senderName + " • " + timeStr);
        senderTimeText.setFont(Font.font(10));
        senderTimeText.setStyle("-fx-fill: #7F8C8D;");

        messageBox.getChildren().addAll(messageText, senderTimeText);

        // Style based on sender
        if (isAdmin) {
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            messageBox.setStyle("-fx-background-color: #2C3E50; -fx-background-radius: 15;");
            messageText.setStyle("-fx-fill: white;");
            senderTimeText.setStyle("-fx-fill: #BDC3C7;");
        } else {
            messageBox.setAlignment(Pos.CENTER_LEFT);

            // Different colors for different message types
            if ("emergency".equals(message.getMessageType()) || "silent_emergency".equals(message.getMessageType())) {
                messageBox.setStyle("-fx-background-color: #E74C3C; -fx-background-radius: 15;");
                messageText.setStyle("-fx-fill: white; -fx-font-weight: bold;");
                senderTimeText.setStyle("-fx-fill: #FADBD8;");
            } else {
                messageBox.setStyle("-fx-background-color: #ECF0F1; -fx-background-radius: 15;");
                messageText.setStyle("-fx-fill: #2C3E50;");
                senderTimeText.setStyle("-fx-fill: #7F8C8D;");
            }
        }

        // Container for alignment
        HBox container = new HBox();
        if (isAdmin) {
            container.setAlignment(Pos.CENTER_RIGHT);
            container.getChildren().add(messageBox);
        } else {
            container.setAlignment(Pos.CENTER_LEFT);
            container.getChildren().add(messageBox);
        }

        VBox outerContainer = new VBox();
        outerContainer.getChildren().add(container);
        return outerContainer;
    }

    private String formatTimestamp(String timestamp) {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
        } catch (Exception e) {
            return timestamp;
        }
    }

    @FXML
    private void sendMessage() {
        if (messageInput != null) {
            String messageText = messageInput.getText().trim();
            if (!messageText.isEmpty() && selectedUser != null && chatService != null && AdminSession.isLoggedIn()) {

                System.out.println("📤 Admin sending message to " + selectedUser.getUsername() + ": " + messageText);

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

    @FXML
    private void sendBroadcast() {
        if (messageInput != null) {
            String broadcastText = messageInput.getText().trim();
            if (!broadcastText.isEmpty() && chatService != null && AdminSession.isLoggedIn()) {

                System.out.println("📢 Admin sending broadcast: " + broadcastText);

                chatService.sendMessage(
                        AdminSession.getCurrentAdminId(),
                        "admin",
                        null,
                        "user", // Send to all users
                        "📢 ADMIN BROADCAST: " + broadcastText,
                        "broadcast"
                );
                messageInput.clear();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Broadcast Sent");
                alert.setHeaderText("Message Broadcasted");
                alert.setContentText("Your message has been sent to all users.");
                alert.showAndWait();
            }
        }
    }

    @FXML
    private void showAllConversations() {
        selectedUser = null;
        if (chatHeaderTitle != null) {
            chatHeaderTitle.setText("All Emergency Communications");
        }
        if (chatHeaderSubtitle != null) {
            chatHeaderSubtitle.setText("Monitoring all emergency communications");
        }
        if (selectedUserLabel != null) {
            selectedUserLabel.setVisible(false);
        }

        // Refresh user list to remove selection highlight
        displayUsers();

        // Load all messages
        loadAllMessages();

        if (sendButton != null) {
            sendButton.setDisable(true);
        }

        System.out.println("📋 Showing all conversations");
    }

    private void loadAllMessages() {
        if (chatService != null) {
            new Thread(() -> {
                try {
                    Connection conn = DatabaseConnection.connect(); // Using your connect() method

                    String query = "SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT 100";
                    PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();

                    List<JavaChatService.ChatMessage> history = new ArrayList<>();

                    while (rs.next()) {
                        JavaChatService.ChatMessage message = new JavaChatService.ChatMessage(
                                rs.getInt("id"),
                                rs.getInt("sender_id"),
                                rs.getString("sender_type"),
                                rs.getObject("receiver_id") != null ? rs.getInt("receiver_id") : null,
                                rs.getString("receiver_type"),
                                rs.getString("message"),
                                rs.getString("message_type"),
                                rs.getString("timestamp"),
                                rs.getBoolean("is_read")
                        );
                        history.add(message);
                    }

                    // Reverse to show oldest first
                    java.util.Collections.reverse(history);

                    Platform.runLater(() -> {
                        messages.clear();
                        messages.addAll(history);
                        displayMessages();
                    });

                    rs.close();
                    stmt.close();
                    conn.close();

                    System.out.println("✅ Loaded " + history.size() + " messages for all conversations");

                } catch (Exception e) {
                    System.err.println("❌ Error loading all messages: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        }
    }

    @FXML
    private void markAllAsRead() {
        new Thread(() -> {
            try {
                Connection conn = DatabaseConnection.connect(); // Using your connect() method
                String query = "UPDATE chat_messages SET is_read = TRUE WHERE sender_type = 'user' AND is_read = FALSE";
                PreparedStatement stmt = conn.prepareStatement(query);
                int updated = stmt.executeUpdate();

                stmt.close();
                conn.close();

                Platform.runLater(() -> {
                    // Reset all unread counts
                    for (UserInfo user : users) {
                        user.setUnreadCount(0);
                    }
                    displayUsers();
                    updateStats();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Messages Marked as Read");
                    alert.setHeaderText("All messages marked as read");
                    alert.setContentText(updated + " messages have been marked as read.");
                    alert.showAndWait();
                });

                System.out.println("✅ Marked " + updated + " messages as read");

            } catch (Exception e) {
                System.err.println("❌ Error marking all messages as read: " + e.getMessage());
            }
        }).start();
    }

    @FXML
    private void handleBackToDashboard() {
        // Clean up chat listener
        if (chatService != null) {
            chatService.removeMessageListener(this);
        }

        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/codevengers/voiceemergency/AdminDashboard.fxml"));
            Stage stage = (Stage) adminNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("QuickRescue - Admin Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateConnectionStatus() {
        Platform.runLater(() -> {
            if (connectionStatusIcon != null && connectionStatusText != null) {
                if (chatService != null && chatService.isConnected()) {
                    connectionStatusIcon.setText("🟢");
                    connectionStatusText.setText("System Online");
                    connectionStatusText.setStyle("-fx-fill: #27AE60;");
                } else {
                    connectionStatusIcon.setText("🔴");
                    connectionStatusText.setText("System Offline");
                    connectionStatusText.setStyle("-fx-fill: #E74C3C;");
                }
            }
        });
    }

    private void updateStats() {
        Platform.runLater(() -> {
            if (totalUsersText != null) {
                totalUsersText.setText(String.valueOf(users.size()));
            }
            if (emergencyCountText != null) {
                emergencyCountText.setText(String.valueOf(emergencyAlertCount));
            }
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
            System.out.println("📨 Admin received new message from " +
                    userNameCache.getOrDefault(message.getSenderId(), "User #" + message.getSenderId()) +
                    ": " + message.getMessage());

            // Update emergency alert count
            if ("emergency".equals(message.getMessageType()) || "silent_emergency".equals(message.getMessageType())) {
                emergencyAlertCount++;
                updateStats();

                // Show emergency notification with username
                String senderName = userNameCache.getOrDefault(message.getSenderId(), "User #" + message.getSenderId());
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("🚨 EMERGENCY ALERT");
                alert.setHeaderText("Emergency Message from " + senderName);
                alert.setContentText("User: " + senderName + "\n\nMessage: " + message.getMessage());
                alert.show();
            }

            // Update user list unread counts
            for (UserInfo user : users) {
                if (user.getId() == message.getSenderId() && "user".equals(message.getSenderType())) {
                    if (selectedUser == null || selectedUser.getId() != user.getId()) {
                        user.setUnreadCount(user.getUnreadCount() + 1);
                    }
                    displayUsers();
                    break;
                }
            }

            // If this message is for the currently selected user, add it to the chat
            if (selectedUser != null) {
                boolean isRelevantMessage = false;

                // Message from the selected user to admin
                if (message.getSenderId() == selectedUser.getId() && "user".equals(message.getSenderType())) {
                    isRelevantMessage = true;
                }
                // Message from admin to the selected user specifically
                else if ("admin".equals(message.getSenderType()) &&
                        message.getReceiverId() != null &&
                        message.getReceiverId() == selectedUser.getId()) {
                    isRelevantMessage = true;
                }

                if (isRelevantMessage) {
                    messages.add(message);
                    VBox messageBox = createMessageBubble(message);
                    if (messagesContainer != null) {
                        messagesContainer.getChildren().add(messageBox);
                    }

                    // Auto-scroll to bottom
                    Platform.runLater(() -> {
                        if (messagesScrollPane != null) {
                            messagesScrollPane.setVvalue(1.0);
                        }
                    });

                    // Mark user messages as read immediately when viewing the conversation
                    if ("user".equals(message.getSenderType()) && chatService != null) {
                        chatService.markMessageAsRead(message.getId());
                    }
                }
            }

            // If showing all conversations, add the message
            if (selectedUser == null) {
                messages.add(message);
                VBox messageBox = createMessageBubble(message);
                if (messagesContainer != null) {
                    messagesContainer.getChildren().add(messageBox);
                }

                // Auto-scroll to bottom
                Platform.runLater(() -> {
                    if (messagesScrollPane != null) {
                        messagesScrollPane.setVvalue(1.0);
                    }
                });
            }

            updateStats();
        });
    }

    @Override
    public void onMessageStatusUpdate(int messageId, boolean isRead) {
        Platform.runLater(() -> {
            // Update message status in the list
            for (JavaChatService.ChatMessage message : messages) {
                if (message.getId() == messageId) {
                    // Note: ChatMessage class needs a setRead method for this to work
                    break;
                }
            }
        });
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

        // Getters and setters
        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public int getUnreadCount() { return unreadCount; }
        public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    }
}
