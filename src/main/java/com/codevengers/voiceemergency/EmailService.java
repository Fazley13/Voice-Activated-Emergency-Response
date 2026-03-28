package com.codevengers.voiceemergency;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EmailService {

    // Email configuration - You can modify these settings
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String EMAIL_USERNAME = "abrarbhuiyan731@gmail.com"; // Replace with your email
    private static final String EMAIL_PASSWORD = "ttek wsxl ngdo mdal\n"; // Replace with your app password
    private static final String FROM_EMAIL = "QuickRescue Emergency System <your-emergency-app@gmail.com>";

    
    public static boolean sendEmergencyNotification(int userId, String username, String emergencyType,
                                                    String location, String description, int emergencyId) {
        try {
            System.out.println("📧 Sending emergency notifications for user: " + username);

            // Get user's emergency contacts
            List<EmergencyContact> contacts = EmergencyContactDAO.getEmergencyContactsByUserId(userId);

            if (contacts.isEmpty()) {
                System.out.println("⚠️ No emergency contacts found for user: " + username);
                return false;
            }

            boolean allSent = true;
            int sentCount = 0;

            for (EmergencyContact contact : contacts) {
                if (contact.getEmail() != null && !contact.getEmail().trim().isEmpty()) {
                    boolean sent = sendEmergencyEmail(contact, username, emergencyType, location, description, emergencyId);
                    if (sent) {
                        sentCount++;
                        System.out.println("✅ Emergency email sent to: " + contact.getName() + " (" + contact.getEmail() + ")");
                    } else {
                        allSent = false;
                        System.err.println("❌ Failed to send emergency email to: " + contact.getName() + " (" + contact.getEmail() + ")");
                    }
                } else {
                    System.out.println("⚠️ Skipping contact " + contact.getName() + " - no email address");
                }
            }

            System.out.println("📊 Emergency notification summary: " + sentCount + "/" + contacts.size() + " emails sent");
            return sentCount > 0; // Return true if at least one email was sent

        } catch (Exception e) {
            System.err.println("❌ Error sending emergency notifications: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Send emergency email to a specific contact
     */
    private static boolean sendEmergencyEmail(EmergencyContact contact, String username, String emergencyType,
                                              String location, String description, int emergencyId) {
        try {
            // Create email session
            Session session = createEmailSession();

            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(contact.getEmail()));

            // Set subject
            String subject = "🚨 EMERGENCY ALERT - " + username + " needs immediate help!";
            message.setSubject(subject);

            // Create email content
            String emailContent = createEmergencyEmailContent(contact, username, emergencyType, location, description, emergencyId);
            message.setContent(emailContent, "text/html; charset=utf-8");

            // Send email
            Transport.send(message);

            return true;

        } catch (Exception e) {
            System.err.println("❌ Error sending email to " + contact.getEmail() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Create emergency email content
     */
    private static String createEmergencyEmailContent(EmergencyContact contact, String username, String emergencyType,
                                                      String location, String description, int emergencyId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'></head><body>");
        html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>");

        // Header
        html.append("<div style='background-color: #e74c3c; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0;'>");
        html.append("<h1 style='margin: 0; font-size: 24px;'>🚨 EMERGENCY ALERT 🚨</h1>");
        html.append("<p style='margin: 10px 0 0 0; font-size: 16px;'>Immediate Attention Required</p>");
        html.append("</div>");

        // Main content
        html.append("<div style='background-color: #f8f9fa; padding: 30px; border: 3px solid #e74c3c;'>");

        html.append("<p style='font-size: 18px; margin-bottom: 20px;'>");
        html.append("Dear <strong>").append(contact.getName()).append("</strong>,");
        html.append("</p>");

        html.append("<p style='font-size: 16px; color: #e74c3c; font-weight: bold; margin-bottom: 20px;'>");
        html.append("This is an automated emergency alert from the QuickRescue Emergency Response System.");
        html.append("</p>");

        // Emergency details
        html.append("<div style='background-color: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; border-left: 5px solid #e74c3c;'>");
        html.append("<h2 style='color: #e74c3c; margin-top: 0;'>Emergency Details</h2>");

        html.append("<table style='width: 100%; border-collapse: collapse;'>");
        html.append("<tr><td style='padding: 8px 0; font-weight: bold; width: 30%;'>Person in Emergency:</td><td style='padding: 8px 0;'>").append(username).append("</td></tr>");
        html.append("<tr><td style='padding: 8px 0; font-weight: bold;'>Emergency Type:</td><td style='padding: 8px 0; color: #e74c3c; font-weight: bold;'>").append(emergencyType).append("</td></tr>");
        html.append("<tr><td style='padding: 8px 0; font-weight: bold;'>Time:</td><td style='padding: 8px 0;'>").append(timestamp).append("</td></tr>");
        html.append("<tr><td style='padding: 8px 0; font-weight: bold;'>Emergency ID:</td><td style='padding: 8px 0;'>#").append(emergencyId).append("</td></tr>");
        html.append("<tr><td style='padding: 8px 0; font-weight: bold;'>Your Relationship:</td><td style='padding: 8px 0;'>").append(contact.getRelationship()).append("</td></tr>");

        if (description != null && !description.trim().isEmpty()) {
            html.append("<tr><td style='padding: 8px 0; font-weight: bold;'>Description:</td><td style='padding: 8px 0;'>").append(description).append("</td></tr>");
        }

        html.append("</table>");
        html.append("</div>");

        // Location section
        if (location != null && !location.trim().isEmpty() && !location.equals("Location unavailable")) {
            html.append("<div style='background-color: #fff3cd; padding: 20px; border-radius: 8px; margin-bottom: 20px; border-left: 5px solid #ffc107;'>");
            html.append("<h3 style='color: #856404; margin-top: 0;'>📍 Location Information</h3>");

            if (location.startsWith("https://")) {
                html.append("<p style='margin-bottom: 15px;'>Click the button below to view the exact location on Google Maps:</p>");
                html.append("<div style='text-align: center; margin: 20px 0;'>");
                html.append("<a href='").append(location).append("' target='_blank' style='");
                html.append("background-color: #4285f4; color: white; padding: 15px 30px; text-decoration: none; ");
                html.append("border-radius: 5px; font-weight: bold; display: inline-block;'>");
                html.append("📍 VIEW LOCATION ON MAP");
                html.append("</a>");
                html.append("</div>");
                html.append("<p style='font-size: 12px; color: #6c757d;'>Location URL: ").append(location).append("</p>");
            } else {
                html.append("<p><strong>Location:</strong> ").append(location).append("</p>");
            }
            html.append("</div>");
        }

        // Action required section
        html.append("<div style='background-color: #d1ecf1; padding: 20px; border-radius: 8px; margin-bottom: 20px; border-left: 5px solid #17a2b8;'>");
        html.append("<h3 style='color: #0c5460; margin-top: 0;'>⚡ Immediate Action Required</h3>");
        html.append("<ul style='margin: 10px 0; padding-left: 20px;'>");

        if (contact.isPrimary()) {
            html.append("<li style='margin-bottom: 8px; color: #e74c3c; font-weight: bold;'>You are the PRIMARY emergency contact</li>");
        }

        html.append("<li style='margin-bottom: 8px;'>Contact ").append(username).append(" immediately</li>");
        html.append("<li style='margin-bottom: 8px;'>If you cannot reach them, contact local emergency services</li>");
        html.append("<li style='margin-bottom: 8px;'>Go to the location if it's safe to do so</li>");
        html.append("<li style='margin-bottom: 8px;'>Keep this email for reference</li>");
        html.append("</ul>");
        html.append("</div>");

        // Emergency numbers section
        html.append("<div style='background-color: #f8d7da; padding: 20px; border-radius: 8px; margin-bottom: 20px; border-left: 5px solid #dc3545;'>");
        html.append("<h3 style='color: #721c24; margin-top: 0;'>🚨 Emergency Numbers</h3>");
        html.append("<ul style='margin: 10px 0; padding-left: 20px;'>");
        html.append("<li style='margin-bottom: 5px;'><strong>Police:</strong> 999</li>");
        html.append("<li style='margin-bottom: 5px;'><strong>Fire Service:</strong> 199</li>");
        html.append("<li style='margin-bottom: 5px;'><strong>Ambulance:</strong> 199</li>");
        html.append("<li style='margin-bottom: 5px;'><strong>National Emergency:</strong> 999</li>");
        html.append("</ul>");
        html.append("</div>");

        // Footer
        html.append("<div style='text-align: center; padding: 20px; background-color: #e9ecef; border-radius: 0 0 10px 10px;'>");
        html.append("<p style='margin: 0; font-size: 12px; color: #6c757d;'>");
        html.append("This is an automated message from QuickRescue Emergency Response System<br>");
        html.append("Emergency ID: #").append(emergencyId).append(" | Time: ").append(timestamp);
        html.append("</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</div>");
        html.append("</body></html>");

        return html.toString();
    }

    /**
     * Send test email to verify email configuration
     */
    public static boolean sendTestEmail(String toEmail, String contactName) {
        try {
            System.out.println("📧 Sending test email to: " + toEmail);

            Session session = createEmailSession();

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("✅ QuickRescue Emergency System - Test Email");

            String testContent = createTestEmailContent(contactName);
            message.setContent(testContent, "text/html; charset=utf-8");

            Transport.send(message);

            System.out.println("✅ Test email sent successfully to: " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error sending test email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create test email content
     */
    private static String createTestEmailContent(String contactName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'></head><body>");
        html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>");

        html.append("<div style='background-color: #28a745; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0;'>");
        html.append("<h1 style='margin: 0; font-size: 24px;'>✅ Test Email Successful</h1>");
        html.append("</div>");

        html.append("<div style='background-color: #f8f9fa; padding: 30px; border: 3px solid #28a745;'>");
        html.append("<p style='font-size: 18px;'>Dear <strong>").append(contactName).append("</strong>,</p>");
        html.append("<p>This is a test email from the QuickRescue Emergency Response System.</p>");
        html.append("<p><strong>✅ Your email configuration is working correctly!</strong></p>");
        html.append("<p>In case of a real emergency, you will receive detailed information including:</p>");
        html.append("<ul>");
        html.append("<li>Emergency type and description</li>");
        html.append("<li>Exact location with Google Maps link</li>");
        html.append("<li>Time and emergency ID</li>");
        html.append("<li>Action steps to take</li>");
        html.append("</ul>");
        html.append("<p style='color: #6c757d; font-size: 12px;'>Test sent at: ").append(timestamp).append("</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body></html>");

        return html.toString();
    }

    /**
     * Create email session with SMTP configuration
     */
    private static Session createEmailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
            }
        });
    }

    /**
     * Send emergency resolution notification
     */
    public static boolean sendEmergencyResolvedNotification(int userId, String username, int emergencyId) {
        try {
            System.out.println("📧 Sending emergency resolved notifications for user: " + username);

            List<EmergencyContact> contacts = EmergencyContactDAO.getEmergencyContactsByUserId(userId);

            if (contacts.isEmpty()) {
                return false;
            }

            boolean allSent = true;
            int sentCount = 0;

            for (EmergencyContact contact : contacts) {
                if (contact.getEmail() != null && !contact.getEmail().trim().isEmpty()) {
                    boolean sent = sendResolvedEmail(contact, username, emergencyId);
                    if (sent) {
                        sentCount++;
                    } else {
                        allSent = false;
                    }
                }
            }

            System.out.println("📊 Emergency resolved notification summary: " + sentCount + "/" + contacts.size() + " emails sent");
            return sentCount > 0;

        } catch (Exception e) {
            System.err.println("❌ Error sending emergency resolved notifications: " + e.getMessage());
            return false;
        }
    }

    /**
     * Send emergency resolved email
     */
    private static boolean sendResolvedEmail(EmergencyContact contact, String username, int emergencyId) {
        try {
            Session session = createEmailSession();

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(contact.getEmail()));
            message.setSubject("✅ Emergency Resolved - " + username + " is now safe");

            String content = createResolvedEmailContent(contact, username, emergencyId);
            message.setContent(content, "text/html; charset=utf-8");

            Transport.send(message);
            return true;

        } catch (Exception e) {
            System.err.println("❌ Error sending resolved email to " + contact.getEmail() + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Create emergency resolved email content
     */
    private static String createResolvedEmailContent(EmergencyContact contact, String username, int emergencyId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><meta charset='UTF-8'></head><body>");
        html.append("<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>");

        html.append("<div style='background-color: #28a745; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0;'>");
        html.append("<h1 style='margin: 0; font-size: 24px;'>✅ Emergency Resolved</h1>");
        html.append("</div>");

        html.append("<div style='background-color: #f8f9fa; padding: 30px; border: 3px solid #28a745;'>");
        html.append("<p style='font-size: 18px;'>Dear <strong>").append(contact.getName()).append("</strong>,</p>");
        html.append("<p style='font-size: 16px; color: #28a745; font-weight: bold;'>");
        html.append("Good news! The emergency situation involving <strong>").append(username).append("</strong> has been resolved.");
        html.append("</p>");
        html.append("<p>Emergency ID #").append(emergencyId).append(" has been marked as resolved at ").append(timestamp).append(".</p>");
        html.append("<p>Thank you for your quick response and concern.</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body></html>");

        return html.toString();
    }
}