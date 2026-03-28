package com.codevengers.voiceemergency;

public class EmergencyContactTest {

    public static void main(String[] args) {
        System.out.println("=== EMERGENCY CONTACT SYSTEM TEST (COMPLETE CLEANUP) ===");

        // Test 1: Complete Database Cleanup
        System.out.println("\n1. Complete Database Cleanup:");
        EmergencyContactDatabaseCleanup.cleanupEmergencyContactsTable();

        // Test 2: Database Connection
        System.out.println("\n2. Testing Database Connection:");
        DatabaseConnectionTester.testConnection();

        // Test 3: Table Initialization (should work perfectly now)
        System.out.println("\n3. Testing Table Initialization:");
        EmergencyContactDAO.initializeEmergencyContactTable();

        // Test 4: User Session Simulation
        System.out.println("\n4. Simulating User Session:");
        UserSession.login(1, "Sayma Sultana", "sayma@example.com", "user");
        UserSession.printSessionInfo();

        // Test 5: Add Test Contact (should work perfectly now)
        System.out.println("\n5. Testing Add Contact:");
        EmergencyContact testContact = new EmergencyContact(
                1,
                "Test Mother",
                "01700000001",
                "mother@example.com",
                "Family Member",
                true
        );

        try {
            int contactId = EmergencyContactDAO.addEmergencyContact(testContact);
            if (contactId > 0) {
                System.out.println("✅ Test contact added with ID: " + contactId);

                // Test 6: Retrieve Contacts
                System.out.println("\n6. Testing Retrieve Contacts:");
                var contacts = EmergencyContactDAO.getEmergencyContactsByUserId(1);
                System.out.println("✅ Found " + contacts.size() + " contacts");

                for (EmergencyContact contact : contacts) {
                    System.out.println("   - " + contact.getName() + " (" + contact.getPhoneNumber() + ") - Primary: " + contact.isPrimary());
                }

                // Test 7: Update Contact
                System.out.println("\n7. Testing Update Contact:");
                testContact.setId(contactId);
                testContact.setName("Updated Mother");
                testContact.setPhoneNumber("01700000002");
                boolean updateSuccess = EmergencyContactDAO.updateEmergencyContact(testContact);
                System.out.println("✅ Update result: " + updateSuccess);

                // Test 8: Add Second Contact (non-primary)
                System.out.println("\n8. Testing Add Second Contact:");
                EmergencyContact secondContact = new EmergencyContact(
                        1,
                        "Test Father",
                        "01700000003",
                        "father@example.com",
                        "Family Member",
                        false
                );

                int secondContactId = EmergencyContactDAO.addEmergencyContact(secondContact);
                if (secondContactId > 0) {
                    System.out.println("✅ Second contact added with ID: " + secondContactId);

                    // Test 9: Get Primary Contact
                    System.out.println("\n9. Testing Get Primary Contact:");
                    EmergencyContact primaryContact = EmergencyContactDAO.getPrimaryContact(1);
                    if (primaryContact != null) {
                        System.out.println("✅ Primary contact: " + primaryContact.getName() + " (" + primaryContact.getPhoneNumber() + ")");
                    } else {
                        System.err.println("❌ No primary contact found");
                    }

                    // Test 10: Delete Contacts
                    System.out.println("\n10. Testing Delete Contacts:");
                    boolean deleteFirst = EmergencyContactDAO.deleteEmergencyContact(contactId, 1);
                    boolean deleteSecond = EmergencyContactDAO.deleteEmergencyContact(secondContactId, 1);
                    System.out.println("✅ Delete results: First=" + deleteFirst + ", Second=" + deleteSecond);

                } else {
                    System.err.println("❌ Failed to add second contact");
                }

            } else {
                System.err.println("❌ Failed to add test contact");
            }
        } catch (Exception e) {
            System.err.println("❌ Test failed with exception: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n=== TEST COMPLETE ===");
        System.out.println("🎯 If all tests passed, your Emergency Contacts feature is ready to use!");
    }
}