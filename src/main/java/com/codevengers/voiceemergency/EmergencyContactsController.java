package com.codevengers.voiceemergency;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmergencyContactsController {

    @FXML private Label titleLabel;
    @FXML private VBox contactBox;
    @FXML private Button closeButton;

    private String userEmail = "test@example.com"; // default

    public void setUserEmail(String email) {
        this.userEmail = email;
    }

    public void initialize() {
        closeButton.setOnAction(e -> closeButton.getScene().getWindow().hide());
    }

    public void loadContacts() {
        List<String> contacts = fetchContacts(userEmail);

        if (contacts.isEmpty()) {
            contactBox.getChildren().add(new Label("No emergency contacts found."));
        } else {
            int count = 1;
            for (String contact : contacts) {
                contactBox.getChildren().add(new Label(count++ + ". " + contact));
            }
        }
    }

    private List<String> fetchContacts(String email) {
        List<String> contactList = new ArrayList<>();
        String url = "jdbc:mysql://localhost:3306/emergency_db";
        String user = "root";
        String password = "";

        String query = "SELECT name, phone FROM emergency_contacts WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                String phone = rs.getString("phone");
                contactList.add("Name: " + name + ", Phone: " + phone);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return contactList;
    }
}
