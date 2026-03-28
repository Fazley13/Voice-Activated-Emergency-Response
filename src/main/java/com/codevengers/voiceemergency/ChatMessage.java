package com.codevengers.voiceemergency;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage {
    private int id; // Changed from messageId to id
    private String senderId; // Changed to String
    private String senderName; // Added senderName
    private String senderType; // "user" or "admin"
    private String receiverId; // Changed to String, null for broadcast to all admins
    private String receiverType; // "admin" or "user"
    private String content; // Changed from message to content
    private String messageType; // "normal", "emergency", "silent_emergency", "text"
    private String timestamp; // Changed to String for easier database handling
    private boolean isRead;
    private boolean isDelivered;
    private boolean isEmergency; // Added isEmergency field

    // Constructors
    public ChatMessage() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.isRead = false;
        this.isDelivered = false;
        this.isEmergency = false;
    }

    public ChatMessage(String senderId, String senderName, String senderType, String receiverId,
                       String content, boolean isEmergency, String messageType) {
        this();
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderType = senderType;
        this.receiverId = receiverId;
        this.content = content;
        this.messageType = messageType;
        this.isEmergency = isEmergency;
    }

    // Constructor for JavaChatService compatibility
    public ChatMessage(int id, int senderId, String senderType, Integer receiverId,
                       String receiverType, String message, String messageType,
                       String timestamp, boolean isRead) {
        this.id = id;
        this.senderId = String.valueOf(senderId);
        this.senderName = senderType.equals("admin") ? "Admin" : "User " + senderId;
        this.senderType = senderType;
        this.receiverId = receiverId != null ? String.valueOf(receiverId) : null;
        this.receiverType = receiverType;
        this.content = message;
        this.messageType = messageType;
        this.timestamp = timestamp;
        this.isRead = isRead;
        this.isEmergency = "emergency".equals(messageType) || "silent_emergency".equals(messageType);
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName != null ? senderName : (senderType.equals("admin") ? "Admin" : "User " + senderId); }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderType() { return senderType; }
    public void setSenderType(String senderType) { this.senderType = senderType; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getReceiverType() { return receiverType; }
    public void setReceiverType(String receiverType) { this.receiverType = receiverType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    // For compatibility with existing code
    public String getMessage() { return content; }
    public void setMessage(String message) { this.content = message; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) {
        this.messageType = messageType;
        this.isEmergency = "emergency".equals(messageType) || "silent_emergency".equals(messageType);
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public boolean isDelivered() { return isDelivered; }
    public void setDelivered(boolean delivered) { isDelivered = delivered; }

    public boolean isEmergency() { return isEmergency; }
    public void setEmergency(boolean emergency) { isEmergency = emergency; }

    public String getFormattedTimestamp() {
        try {
            LocalDateTime dateTime = LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
        } catch (Exception e) {
            return timestamp;
        }
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", getFormattedTimestamp(), getSenderName(), content);
    }
}
