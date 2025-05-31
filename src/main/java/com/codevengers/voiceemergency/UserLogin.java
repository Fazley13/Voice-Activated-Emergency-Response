package com.codevengers.voiceemergency;



import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;

public class UserLogin extends Application {

    @Override
    public void start(Stage stage) {
        Label emailLabel = new Label("Email:");
        TextField emailField = new TextField();

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();

        Button loginButton = new Button("Login");
        Label resultLabel = new Label();

        loginButton.setOnAction((ActionEvent e) -> {
            String email = emailField.getText();
            String password = passwordField.getText();

            if (validateLogin(email, password)) {
                resultLabel.setText("Login successful!");
                stage.close(); // Close login window

                // Open dashboard
                UserDashboard dashboard = new UserDashboard(email);
                try {
                    dashboard.start(new Stage());
                } catch (Exception ex) {
                    Logger.getLogger(UserLogin.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                resultLabel.setText("Invalid username or password. Try again.");
            }
        });

        VBox root = new VBox(10, emailLabel, emailField, passwordLabel, passwordField, loginButton, resultLabel);
        root.setPadding(new Insets(20));
        Scene scene = new Scene(root, 300, 250);

        stage.setTitle("User Login");
        stage.setScene(scene);
        stage.show();
    }

    private boolean validateLogin(String email, String password) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/emergency_db", "root", "");
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE email = ? AND password = ?");
            stmt.setString(1, email);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();
            return rs.next(); // If a row exists, login is valid

        } catch (SQLException e) {
            System.out.println("DB Error: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
