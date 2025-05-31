package com.codevengers.voiceemergency;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class AddEmergencyContactController {

    @FXML private Label titleLabel;
    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private Button addBtn;
    @FXML private Label statusLabel;

    private String username = "User";
    private String userEmail = "test@example.com";

    public void setUserInfo(String username, String userEmail) {
        this.username = username;
        this.userEmail = userEmail;
    }

    @FXML
    private void initialize() {
        addBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String phone = phoneField.getText().trim();

            if (name.isEmpty() || phone.isEmpty()) {
                statusLabel.setText("Please fill all fields.");
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/emergency_db", "root", "")) {
                String query = "INSERT INTO emergency_contacts (name, phone, username, user_email) VALUES (?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(query);
                ps.setString(1, name);
                ps.setString(2, phone);
                ps.setString(3, username);
                ps.setString(4, userEmail);

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    statusLabel.setText("Contact added successfully!");
                    nameField.clear();
                    phoneField.clear();
                } else {
                    statusLabel.setText("Failed to add contact.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });
    }
}
