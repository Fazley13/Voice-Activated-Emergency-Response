package com.codevengers.voiceemergency;

import org.json.JSONObject;
import org.json.JSONArray;
import java.io.*;
import java.net.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import javafx.application.Platform;

public class JavaChatService {
    private static JavaChatService instance;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean connected = false;
    private List<ChatMessageListener> listeners = new CopyOnWriteArrayList<>();
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8080;

    // Added retry configuration
    private static final int MAX_RETRY_ATTEMPTS = 5;
    private static final int RETRY_DELAY_MS = 2000;

    // Private constructor for singleton
    private JavaChatService() {
        // Modified: Don't auto-connect, let caller control when to connect
        // connectToServer();
    }

    public static synchronized JavaChatService getInstance() {
        if (instance == null) {
            instance = new JavaChatService();
        }
        return instance;
    }

    // Modified: Enhanced connection method with retry logic
    private void connectToServer() {
        connectToServerWithRetry(MAX_RETRY_ATTEMPTS);
    }

    // Added: New method with retry logic
    private boolean connectToServerWithRetry(int maxAttempts) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                System.out.println("🔄 Connection attempt " + attempt + "/" + maxAttempts);

                // Check if server is running, start if needed
                ensureServerIsRunning();

                socket = new Socket(SERVER_HOST, SERVER_PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                connected = true;

                // Start listening for messages
                startMessageListener();

                System.out.println("✅ Connected to chat server on attempt " + attempt);
                return true;

            } catch (IOException e) {
                System.err.println("❌ Connection attempt " + attempt + " failed: " + e.getMessage());
                connected = false;

                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        System.err.println("❌ Failed to connect to chat server after " + maxAttempts + " attempts");
        return false;
    }

    // Added: Method to ensure server is running
    private void ensureServerIsRunning() {
        try {
            // Check if ServerLauncher class exists and use it
            Class<?> serverLauncherClass = Class.forName("com.codevengers.voiceemergency.ServerLauncher");
            java.lang.reflect.Method isRunningMethod = serverLauncherClass.getMethod("isServerRunning");
            java.lang.reflect.Method startServerMethod = serverLauncherClass.getMethod("startServer");

            Boolean isRunning = (Boolean) isRunningMethod.invoke(null);
            if (!isRunning) {
                System.out.println("🚀 Starting server via ServerLauncher...");
                startServerMethod.invoke(null);
                Thread.sleep(3000); // Wait for server to start
            }
        } catch (Exception e) {
            // ServerLauncher not available, try to start server directly
            System.out.println("🔄 ServerLauncher not available, attempting direct server start...");
            startServerDirectly();
        }
    }

    // Added: Direct server startup as fallback
    private void startServerDirectly() {
        try {
            // Try to start JavaSocketServer in a separate thread
            Thread serverThread = new Thread(() -> {
                try {
                    JavaSocketServer server = new JavaSocketServer();
                    server.start();
                } catch (Exception e) {
                    System.err.println("❌ Failed to start server directly: " + e.getMessage());
                }
            });
            serverThread.setDaemon(true);
            serverThread.start();

            Thread.sleep(3000); // Wait for server to start
            System.out.println("✅ Server started directly");
        } catch (Exception e) {
            System.err.println("❌ Failed to start server directly: " + e.getMessage());
        }
    }

    // Added: Public method to manually connect
    public boolean connect() {
        if (!connected) {
            return connectToServerWithRetry(MAX_RETRY_ATTEMPTS);
        }
        return connected;
    }

    // Modified: Enhanced message listener with reconnection logic
    private void startMessageListener() {
        Thread listenerThread = new Thread(() -> {
            try {
                String message;
                while (connected && (message = in.readLine()) != null) {
                    handleIncomingMessage(message);
                }
            } catch (IOException e) {
                if (connected) {
                    System.err.println("❌ Connection lost: " + e.getMessage());
                    connected = false;

                    // Attempt to reconnect after a delay
                    Platform.runLater(() -> {
                        new Thread(() -> {
                            try {
                                Thread.sleep(5000); // Wait 5 seconds before reconnecting
                                System.out.println("🔄 Attempting to reconnect...");
                                connectToServerWithRetry(3); // Try 3 times to reconnect
                            } catch (InterruptedException ie) {
                                Thread.currentThread().interrupt();
                            }
                        }).start();
                    });
                }
            }
        });
        listenerThread.setDaemon(true);
        listenerThread.start();
    }

    private void handleIncomingMessage(String jsonMessage) {
        try {
            System.out.println("📨 Received from server: " + jsonMessage);

            JSONObject json = new JSONObject(jsonMessage);
            String type = json.getString("type");

            if ("message".equals(type)) {
                ChatMessage message = new ChatMessage(
                        json.getInt("id"),
                        json.getInt("senderId"),
                        json.getString("senderType"),
                        json.has("receiverId") && !json.isNull("receiverId") ? json.getInt("receiverId") : null,
                        json.getString("receiverType"),
                        json.getString("message"),
                        json.getString("messageType"),
                        json.getString("timestamp"),
                        json.getBoolean("isRead")
                );

                System.out.println("📋 Parsed message - From: " + message.getSenderId() +
                        " (" + message.getSenderType() + ") To: " + message.getReceiverId() +
                        " (" + message.getReceiverType() + ")");

                // Notify all listeners
                for (ChatMessageListener listener : listeners) {
                    Platform.runLater(() -> listener.onNewMessage(message));
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Error handling incoming message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendMessage(int senderId, String senderType, Integer receiverId, String receiverType, String message, String messageType) {
        // Modified: Auto-connect if not connected
        if (!connected) {
            System.out.println("🔄 Not connected, attempting to connect...");
            if (!connect()) {
                System.err.println("❌ Failed to connect to server, cannot send message");
                return;
            }
        }

        try {
            // Enhanced logging for debugging
            System.out.println("📤 Sending message:");
            System.out.println("   From: " + senderId + " (" + senderType + ")");
            System.out.println("   To: " + (receiverId != null ? receiverId + " (" + receiverType + ")" : "ALL " + receiverType + "s"));
            System.out.println("   Message: " + message);
            System.out.println("   Type: " + messageType);

            // Store in database first
            int messageId = storeMessageInDatabase(senderId, senderType, receiverId, receiverType, message, messageType);

            // Send to server with enhanced JSON structure
            JSONObject json = new JSONObject();
            json.put("type", "message");
            json.put("id", messageId);
            json.put("senderId", senderId);
            json.put("senderType", senderType);

            // Clear distinction between direct messages and broadcasts
            if (receiverId != null) {
                json.put("receiverId", receiverId);
                json.put("messageTarget", "direct"); // Added: explicit target type
                System.out.println("💾 Storing as DIRECT message to user: " + receiverId);
            } else {
                json.put("messageTarget", "broadcast"); // Added: explicit target type
                System.out.println("💾 Storing as BROADCAST message to all " + receiverType + "s");
            }

            json.put("receiverType", receiverType);
            json.put("message", message);
            json.put("messageType", messageType);
            json.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            json.put("isRead", false);

            out.println(json.toString());
            System.out.println("✅ Message sent to server successfully");

        } catch (Exception e) {
            System.err.println("❌ Error sending message: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private int storeMessageInDatabase(int senderId, String senderType, Integer receiverId, String receiverType, String message, String messageType) {
        try {
            // Using your existing database connection method
            Connection conn = DatabaseConnection.connect();

            String query = "INSERT INTO chat_messages (sender_id, sender_type, receiver_id, receiver_type, message, message_type, timestamp, is_read) VALUES (?, ?, ?, ?, ?, ?, NOW(), FALSE)";
            PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);

            stmt.setInt(1, senderId);
            stmt.setString(2, senderType);
            if (receiverId != null) {
                stmt.setInt(3, receiverId);
                System.out.println("💾 Database: Storing message for specific user: " + receiverId);
            } else {
                stmt.setNull(3, Types.INTEGER);
                System.out.println("💾 Database: Storing broadcast message (no specific receiver)");
            }
            stmt.setString(4, receiverType);
            stmt.setString(5, message);
            stmt.setString(6, messageType);

            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            int messageId = 0;
            if (rs.next()) {
                messageId = rs.getInt(1);
            }

            rs.close();
            stmt.close();
            conn.close();

            System.out.println("✅ Message stored in database with ID: " + messageId);
            return messageId;

        } catch (SQLException e) {
            System.err.println("❌ Error storing message in database: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public void joinRoom(int userId, String userType) {
        // Modified: Auto-connect if not connected
        if (!connected) {
            if (!connect()) {
                System.err.println("❌ Failed to connect to server, cannot join room");
                return;
            }
        }

        try {
            JSONObject json = new JSONObject();
            json.put("type", "join");
            json.put("userId", userId);
            json.put("userType", userType);

            out.println(json.toString());
            System.out.println("✅ Joined room as " + userType + " with ID: " + userId);
        } catch (Exception e) {
            System.err.println("❌ Error joining room: " + e.getMessage());
        }
    }

    public int getUnreadMessageCount(int userId, String userType) {
        try {
            // Using your existing database connection method
            Connection conn = DatabaseConnection.connect();

            String query;
            if ("user".equals(userType)) {
                query = "SELECT COUNT(*) FROM chat_messages WHERE receiver_id = ? AND receiver_type = 'user' AND is_read = FALSE";
            } else {
                query = "SELECT COUNT(*) FROM chat_messages WHERE sender_type = 'user' AND is_read = FALSE";
            }

            PreparedStatement stmt = conn.prepareStatement(query);
            if ("user".equals(userType)) {
                stmt.setInt(1, userId);
            }

            ResultSet rs = stmt.executeQuery();
            int count = 0;
            if (rs.next()) {
                count = rs.getInt(1);
            }

            rs.close();
            stmt.close();
            conn.close();

            return count;

        } catch (SQLException e) {
            System.err.println("❌ Error getting unread message count: " + e.getMessage());
            return 0;
        }
    }

    public void markMessageAsRead(int messageId) {
        try {
            // Using your existing database connection method
            Connection conn = DatabaseConnection.connect();

            String query = "UPDATE chat_messages SET is_read = TRUE WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, messageId);
            stmt.executeUpdate();

            stmt.close();
            conn.close();

            // Notify listeners
            for (ChatMessageListener listener : listeners) {
                Platform.runLater(() -> listener.onMessageStatusUpdate(messageId, true));
            }

        } catch (SQLException e) {
            System.err.println("❌ Error marking message as read: " + e.getMessage());
        }
    }

    public List<ChatMessage> getChatHistory(int userId, String userType) {
        List<ChatMessage> history = new ArrayList<>();

        try {
            // Using your existing database connection method
            Connection conn = DatabaseConnection.connect();

            String query;
            PreparedStatement stmt;

            if ("user".equals(userType)) {
                // Get messages for this specific user
                query = "SELECT * FROM chat_messages WHERE " +
                        "(sender_id = ? AND sender_type = 'user') OR " +
                        "(receiver_id = ? AND receiver_type = 'user') OR " +
                        "(receiver_id IS NULL AND receiver_type = 'user') " +
                        "ORDER BY timestamp ASC";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, userId);
                stmt.setInt(2, userId);
            } else {
                // Admin gets all messages
                query = "SELECT * FROM chat_messages ORDER BY timestamp ASC";
                stmt = conn.prepareStatement(query);
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                ChatMessage message = new ChatMessage(
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

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException e) {
            System.err.println("❌ Error getting chat history: " + e.getMessage());
        }

        return history;
    }

    // Added: Method to get available users (for compatibility with AdminChatController)
    public List<User> getAvailableUsers() {
        List<User> users = new ArrayList<>();

        try {
            Connection conn = DatabaseConnection.connect();

            // Try different possible queries to find users
            String[] possibleQueries = {
                    "SELECT id, username, user_type FROM users WHERE user_type IN ('user', 'admin') ORDER BY username",
                    "SELECT id, username, role as user_type FROM users WHERE role IN ('user', 'admin') ORDER BY username",
                    "SELECT id, name as username, user_type FROM users WHERE user_type IN ('user', 'admin') ORDER BY name",
                    "SELECT id, username, 'user' as user_type FROM users ORDER BY username"
            };

            for (String query : possibleQueries) {
                try {
                    PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        users.add(new User(
                                rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("user_type")
                        ));
                    }

                    rs.close();
                    stmt.close();
                    break; // Success, exit loop

                } catch (SQLException e) {
                    // Try next query
                    continue;
                }
            }

            conn.close();

        } catch (SQLException e) {
            System.err.println("❌ Error loading users: " + e.getMessage());
        }

        return users;
    }

    public boolean isConnected() {
        return connected;
    }

    public void addMessageListener(ChatMessageListener listener) {
        listeners.add(listener);
    }

    public void removeMessageListener(ChatMessageListener listener) {
        listeners.remove(listener);
    }

    public void disconnect() {
        try {
            connected = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            System.err.println("❌ Error disconnecting: " + e.getMessage());
        }
    }

    // Inner classes - unchanged
    public interface ChatMessageListener {
        void onNewMessage(ChatMessage message);
        void onMessageStatusUpdate(int messageId, boolean isRead);
    }

    public static class ChatMessage {
        private int id;
        private int senderId;
        private String senderType;
        private Integer receiverId;
        private String receiverType;
        private String message;
        private String messageType;
        private String timestamp;
        private boolean isRead;

        public ChatMessage(int id, int senderId, String senderType, Integer receiverId, String receiverType, String message, String messageType, String timestamp, boolean isRead) {
            this.id = id;
            this.senderId = senderId;
            this.senderType = senderType;
            this.receiverId = receiverId;
            this.receiverType = receiverType;
            this.message = message;
            this.messageType = messageType;
            this.timestamp = timestamp;
            this.isRead = isRead;
        }

        // Getters and setters - unchanged
        public int getId() { return id; }
        public int getSenderId() { return senderId; }
        public String getSenderType() { return senderType; }
        public Integer getReceiverId() { return receiverId; }
        public String getReceiverType() { return receiverType; }
        public String getMessage() { return message; }
        public String getMessageType() { return messageType; }
        public String getTimestamp() { return timestamp; }
        public boolean isRead() { return isRead; }
        public void setRead(boolean read) { this.isRead = read; }
    }

    // Added: User class for compatibility
    public static class User {
        private int id;
        private String username;
        private String userType;

        public User(int id, String username, String userType) {
            this.id = id;
            this.username = username;
            this.userType = userType;
        }

        public int getId() { return id; }
        public String getUsername() { return username; }
        public String getUserType() { return userType; }

        @Override
        public String toString() {
            return username + " (" + userType + ")";
        }
    }
}
