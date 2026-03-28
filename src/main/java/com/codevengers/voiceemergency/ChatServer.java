package com.codevengers.voiceemergency;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    private static final int PORT = 8080;
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private Map<String, ClientHandler> connectedClients = new ConcurrentHashMap<>();
    private Connection dbConnection;

    public ChatServer() {
        try {
            dbConnection = DBConnection.getConnection();
            initializeChatTables();
        } catch (SQLException e) {
            System.err.println("❌ Failed to initialize database: " + e.getMessage());
        }
    }

    private void initializeChatTables() throws SQLException {
        // Fixed: Use MariaDB syntax instead of SQLite
        String createMessagesTable = "CREATE TABLE IF NOT EXISTS chat_messages (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "sender_id INT NOT NULL, " +
                "sender_type VARCHAR(50) NOT NULL, " +
                "receiver_id INT NULL, " +
                "receiver_type VARCHAR(50) NOT NULL, " +
                "message TEXT NOT NULL, " +
                "message_type VARCHAR(50) NOT NULL DEFAULT 'text', " +
                "timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "is_read BOOLEAN NOT NULL DEFAULT FALSE" +
                ")";

        String createUsersTable = "CREATE TABLE IF NOT EXISTS chat_users (" +
                "id VARCHAR(50) PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "user_type VARCHAR(50) NOT NULL, " +
                "last_seen DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                "is_online BOOLEAN DEFAULT FALSE" +
                ")";

        Statement stmt = dbConnection.createStatement();

        try {
            stmt.execute(createMessagesTable);
            System.out.println("✅ chat_messages table created/verified");

            stmt.execute(createUsersTable);
            System.out.println("✅ chat_users table created/verified");

            // Create indexes for better performance
            try {
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_sender ON chat_messages(sender_id, sender_type)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_receiver ON chat_messages(receiver_id, receiver_type)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_timestamp ON chat_messages(timestamp)");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_unread ON chat_messages(is_read)");
                System.out.println("✅ Database indexes created/verified");
            } catch (SQLException e) {
                System.out.println("⚠️ Indexes may already exist: " + e.getMessage());
            }

        } finally {
            stmt.close();
        }

        System.out.println("✅ Chat database tables initialized successfully");
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println("🚀 Chat Server started on port " + PORT);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("❌ Server error: " + e.getMessage());
            }
        }
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            if (dbConnection != null) {
                dbConnection.close();
            }
        } catch (IOException | SQLException e) {
            System.err.println("❌ Error stopping server: " + e.getMessage());
        }
    }

    public synchronized void addClient(String userId, ClientHandler handler) {
        connectedClients.put(userId, handler);
        updateUserOnlineStatus(userId, true);
        System.out.println("✅ Client connected: " + userId + " (Total: " + connectedClients.size() + ")");
    }

    public synchronized void removeClient(String userId) {
        connectedClients.remove(userId);
        updateUserOnlineStatus(userId, false);
        System.out.println("❌ Client disconnected: " + userId + " (Total: " + connectedClients.size() + ")");
    }

    private void updateUserOnlineStatus(String userId, boolean isOnline) {
        try {
            String sql = "INSERT INTO chat_users (id, name, user_type, is_online, last_seen) VALUES (?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE is_online = ?, last_seen = ?";
            PreparedStatement stmt = dbConnection.prepareStatement(sql);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            stmt.setString(1, userId);
            stmt.setString(2, "User " + userId);
            stmt.setString(3, "user");
            stmt.setBoolean(4, isOnline);
            stmt.setString(5, timestamp);
            stmt.setBoolean(6, isOnline);
            stmt.setString(7, timestamp);

            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Error updating user status: " + e.getMessage());
        }
    }

    public synchronized void broadcastMessage(ChatMessage message, String senderUserId) {
        try {
            // Save message to database
            saveMessageToDatabase(message);

            // Send to specific receiver if specified
            if (message.getReceiverId() != null && !message.getReceiverId().isEmpty()) {
                ClientHandler receiver = connectedClients.get(message.getReceiverId());
                if (receiver != null) {
                    receiver.sendMessage(message);
                }
            } else {
                // Broadcast to all admins if no specific receiver
                if ("user".equals(message.getSenderType())) {
                    connectedClients.values().stream()
                            .filter(client -> "admin".equals(client.getUserType()))
                            .forEach(client -> client.sendMessage(message));
                }
            }

            // Send back to sender for confirmation
            ClientHandler sender = connectedClients.get(senderUserId);
            if (sender != null) {
                sender.sendMessage(message);
            }

        } catch (Exception e) {
            System.err.println("❌ Error broadcasting message: " + e.getMessage());
        }
    }

    private void saveMessageToDatabase(ChatMessage message) throws SQLException {
        String sql = "INSERT INTO chat_messages (sender_id, sender_type, receiver_id, receiver_type, message, message_type, is_read) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement stmt = dbConnection.prepareStatement(sql);
        stmt.setString(1, message.getSenderId());
        stmt.setString(2, message.getSenderType());
        stmt.setString(3, message.getReceiverId());
        stmt.setString(4, message.getReceiverType());
        stmt.setString(5, message.getContent());
        stmt.setString(6, message.getMessageType());
        stmt.setBoolean(7, false);

        stmt.executeUpdate();
        stmt.close();
    }

    public List<ChatMessage> getChatHistory(String userId, String userType) {
        List<ChatMessage> messages = new ArrayList<>();
        try {
            String sql;
            PreparedStatement stmt;

            if ("admin".equals(userType)) {
                // Admin sees all messages
                sql = "SELECT * FROM chat_messages ORDER BY timestamp DESC LIMIT 100";
                stmt = dbConnection.prepareStatement(sql);
            } else {
                // User sees only their messages and messages to them
                sql = "SELECT * FROM chat_messages " +
                        "WHERE sender_id = ? OR receiver_id = ? OR receiver_id IS NULL " +
                        "ORDER BY timestamp DESC LIMIT 100";
                stmt = dbConnection.prepareStatement(sql);
                stmt.setString(1, userId);
                stmt.setString(2, userId);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ChatMessage message = new ChatMessage(
                        rs.getString("sender_id"),
                        "User " + rs.getString("sender_id"), // sender name
                        rs.getString("sender_type"),
                        rs.getString("receiver_id"),
                        rs.getString("message"),
                        "emergency".equals(rs.getString("message_type")) || "silent_emergency".equals(rs.getString("message_type")),
                        rs.getString("message_type")
                );
                message.setId(rs.getInt("id"));
                message.setTimestamp(rs.getString("timestamp"));
                message.setRead(rs.getBoolean("is_read"));
                messages.add(message);
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Error getting chat history: " + e.getMessage());
        }
        return messages;
    }

    public int getUnreadMessageCount(String userId, String userType) {
        try {
            String sql;
            PreparedStatement stmt;

            if ("admin".equals(userType)) {
                sql = "SELECT COUNT(*) FROM chat_messages WHERE sender_type = 'user' AND is_read = FALSE";
                stmt = dbConnection.prepareStatement(sql);
            } else {
                sql = "SELECT COUNT(*) FROM chat_messages WHERE receiver_id = ? AND is_read = FALSE";
                stmt = dbConnection.prepareStatement(sql);
                stmt.setString(1, userId);
            }

            ResultSet rs = stmt.executeQuery();
            int count = rs.next() ? rs.getInt(1) : 0;
            rs.close();
            stmt.close();
            return count;
        } catch (SQLException e) {
            System.err.println("❌ Error getting unread count: " + e.getMessage());
            return 0;
        }
    }

    public void markMessagesAsRead(String userId, String userType) {
        try {
            String sql;
            PreparedStatement stmt;

            if ("admin".equals(userType)) {
                sql = "UPDATE chat_messages SET is_read = TRUE WHERE sender_type = 'user'";
                stmt = dbConnection.prepareStatement(sql);
            } else {
                sql = "UPDATE chat_messages SET is_read = TRUE WHERE receiver_id = ?";
                stmt = dbConnection.prepareStatement(sql);
                stmt.setString(1, userId);
            }

            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("❌ Error marking messages as read: " + e.getMessage());
        }
    }

    // Inner class for handling individual client connections
    public static class ClientHandler implements Runnable {
        private Socket socket;
        private ChatServer server;
        private PrintWriter out;
        private BufferedReader in;
        private String userId;
        private String userName;
        private String userType;

        public ClientHandler(Socket socket, ChatServer server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Read user authentication
                String authLine = in.readLine();
                if (authLine != null && authLine.startsWith("AUTH:")) {
                    String[] authParts = authLine.substring(5).split(":");
                    if (authParts.length >= 3) {
                        userId = authParts[0];
                        userName = authParts[1];
                        userType = authParts[2];

                        server.addClient(userId, this);

                        // Send chat history
                        sendChatHistory();

                        // Listen for messages
                        String inputLine;
                        while ((inputLine = in.readLine()) != null) {
                            handleMessage(inputLine);
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("❌ Client handler error: " + e.getMessage());
            } finally {
                cleanup();
            }
        }

        private void sendChatHistory() {
            List<ChatMessage> history = server.getChatHistory(userId, userType);
            for (ChatMessage message : history) {
                sendMessage(message);
            }
        }

        private void handleMessage(String messageData) {
            try {
                // Parse message format: TYPE:senderId:senderName:senderType:receiverId:content:isEmergency:messageType
                String[] parts = messageData.split(":", 8);
                if (parts.length >= 7 && "MESSAGE".equals(parts[0])) {
                    ChatMessage message = new ChatMessage(
                            parts[1], // senderId
                            parts[2], // senderName
                            parts[3], // senderType
                            parts[4].isEmpty() ? null : parts[4], // receiverId
                            parts[6], // content
                            Boolean.parseBoolean(parts[7]), // isEmergency
                            parts.length > 8 ? parts[8] : "text" // messageType
                    );

                    server.broadcastMessage(message, userId);
                } else if ("MARK_READ".equals(parts[0])) {
                    server.markMessagesAsRead(userId, userType);
                }
            } catch (Exception e) {
                System.err.println("❌ Error handling message: " + e.getMessage());
            }
        }

        public void sendMessage(ChatMessage message) {
            if (out != null) {
                String messageStr = String.format("MESSAGE:%s:%s:%s:%s:%s:%s:%s:%b:%s:%s",
                        message.getId(),
                        message.getSenderId(),
                        message.getSenderName(),
                        message.getSenderType(),
                        message.getReceiverId() != null ? message.getReceiverId() : "",
                        message.getContent(),
                        message.getTimestamp(),
                        message.isEmergency(),
                        message.getMessageType(),
                        message.isRead()
                );
                out.println(messageStr);
            }
        }

        public String getUserType() {
            return userType;
        }

        private void cleanup() {
            try {
                if (userId != null) {
                    server.removeClient(userId);
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("❌ Error cleaning up client: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        ChatServer server = new ChatServer();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        server.start();
    }
}