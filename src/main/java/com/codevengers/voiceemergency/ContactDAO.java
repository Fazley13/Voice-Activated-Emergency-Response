package com.codevengers.voiceemergency;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ContactDAO {

    public static List<Contact> getAllContacts() {
        List<Contact> contactList = new ArrayList<>();

        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT * FROM contacts";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("name");
                String phone = rs.getString("phone");
                String email = rs.getString("email");

                Contact contact = new Contact(name, phone, email);
                contactList.add(contact);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return contactList;
    }
}
