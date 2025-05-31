package com.codevengers.voiceemergency;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.Duration;
import tray.notification.NotificationType;
import tray.notification.TrayNotification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FancyNotificationDemo extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Step 1: Database connection
        String dbUrl = "jdbc:mysql://localhost:3306/emergency_db";
        String dbUser = "root";
        String dbPass = "";

        int userId = 1; // Change if needed
        List<String> contactNames = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
            String sql = "SELECT contact_name FROM emergency_contacts WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                contactNames.add(rs.getString("contact_name"));
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        // Step 2: Show notification for each contact
        for (String name : contactNames) {
            TrayNotification tray = new TrayNotification();
            tray.setTitle(" Emergency Alert ");
            tray.setMessage("Notifying contact: " + name);
            tray.setNotificationType(NotificationType.ERROR);
            tray.showAndDismiss(Duration.minutes(1));

            try {
                Thread.sleep(1500); // Delay between notifications
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
