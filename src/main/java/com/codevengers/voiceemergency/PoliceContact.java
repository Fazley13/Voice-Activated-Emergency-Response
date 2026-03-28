package com.codevengers.voiceemergency;
public class PoliceContact {
    private String division;
    private String area;
    private String contactNumber;
    private String emergencyType;
    private String description;
    
    // Constructor
    public PoliceContact(String division, String area, String contactNumber, String emergencyType, String description) {
        this.division = division;
        this.area = area;
        this.contactNumber = contactNumber;
        this.emergencyType = emergencyType;
        this.description = description;
    }
    
    // Getters and Setters (JDK 8 compatible)
    public String getDivision() {
        return division;
    }
    
    public void setDivision(String division) {
        this.division = division;
    }
    
    public String getArea() {
        return area;
    }
    
    public void setArea(String area) {
        this.area = area;
    }
    
    public String getContactNumber() {
        return contactNumber;
    }
    
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
    
    public String getEmergencyType() {
        return emergencyType;
    }
    
    public void setEmergencyType(String emergencyType) {
        this.emergencyType = emergencyType;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return area + " - " + contactNumber;
    }
}
