package com.codevengers.voiceemergency;

import java.time.LocalDateTime;

public class Emergency {
    private int id;
    private int userId;
    private String username;
    private String emergencyType;
    private String location;
    private String status;
    private LocalDateTime timestamp;
    private String audioFileName;
    private String description;
    private String triggerMethod; // "voice", "panic_button", "manual"

    // Constructors
    public Emergency() {}

    public Emergency(int userId, String username, String emergencyType, String location,
                     String triggerMethod, String description) {
        this.userId = userId;
        this.username = username;
        this.emergencyType = emergencyType;
        this.location = location;
        this.triggerMethod = triggerMethod;
        this.description = description;
        this.status = "ACTIVE";
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmergencyType() { return emergencyType; }
    public void setEmergencyType(String emergencyType) { this.emergencyType = emergencyType; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getAudioFileName() { return audioFileName; }
    public void setAudioFileName(String audioFileName) { this.audioFileName = audioFileName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTriggerMethod() { return triggerMethod; }
    public void setTriggerMethod(String triggerMethod) { this.triggerMethod = triggerMethod; }
}
