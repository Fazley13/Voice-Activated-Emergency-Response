package com.codevengers.voiceemergency;

public class MessageSender {

    public static void send(String phoneNumber, String message) {
        try {
            // Simulate sending SMS/message
            System.out.println("📱 Sending message to " + phoneNumber + ":");
            System.out.println("   Message: " + message);

            // In a real implementation, you would integrate with an SMS service like:
            // - Twilio
            // - AWS SNS
            // - Local SMS gateway

            // For now, we'll just log the message
            Thread.sleep(500); // Simulate network delay
            System.out.println("✅ Message sent successfully to " + phoneNumber);

        } catch (Exception e) {
            System.err.println("❌ Error sending message to " + phoneNumber + ": " + e.getMessage());
        }
    }
}
