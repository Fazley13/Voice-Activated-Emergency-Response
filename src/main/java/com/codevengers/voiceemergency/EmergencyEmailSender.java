package com.codevengers.voiceemergency;
import java.sql.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

public class EmergencyEmailSender {

    public static void main(String[] args) {
        String dbUrl = "jdbc:mysql://localhost:3306/emergency_db";
        String dbUser = "root";
        String dbPass = "";

        final String senderEmail = "fazleyrabbi1013@gmail.com";
        final String senderPassword = "rnfp yvji bxoi qrqm";

        int userId = 1; // Replace with dynamic user if needed

        List<String> emailList = new ArrayList<>();

        // Step 1: Fetch Emails from DB
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
            String sql = "SELECT contact_email FROM emergency_contacts WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                emailList.add(rs.getString("contact_email")); // ✅ Use the actual column name

            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Step 2: Send Email to Each Contact
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(senderEmail, senderPassword);
            }
        });

        for (String recipientEmail : emailList) {
            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(senderEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
                message.setSubject("⚠ Emergency Alert ⚠");
                message.setText("This is an emergency notification triggered by your contact. Please check on them immediately.");

                Transport.send(message);
                System.out.println("Email sent to: " + recipientEmail);
            } catch (MessagingException e) {
                System.err.println("Failed to send to: " + recipientEmail);
                e.printStackTrace();
            }
        }
    }
}
