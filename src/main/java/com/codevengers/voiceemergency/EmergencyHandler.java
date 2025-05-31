package com.codevengers.voiceemergency;


import java.util.Scanner;

public class EmergencyHandler {

    public static boolean isEmergencyTriggered = false; // ✅ Add this line

    public static void triggerEmergency() {
        if (isEmergencyTriggered) {
            System.out.println("⚠️ Emergency already triggered.");
            return;
        }

        isEmergencyTriggered = true; // Mark emergency as triggered

        // 1. Play beep
        AlertSound.playAlert();

        // 2. Start recording
        AudioRecorder.startRecording();

        // 3. Get location
        String location = LocationFetcher.getLocationLink();
        System.out.println("Location: " + location);

        // 4. Send emergency message
        String emergencyContact = "01700000000"; // Temporary contact
        MessageSender.send(emergencyContact, "Emergency! Location: " + location);

        // 5. Wait for user input to stop
        Scanner scanner = new Scanner(System.in);
        System.out.println("Press 'S' to stop recording...");
        while (true) {
            String input = scanner.nextLine();
            if (input.equalsIgnoreCase("S")) {
                AudioRecorder.stopRecording();
                break;
            }
        }

        // Exit the application after stopping
        javafx.application.Platform.exit();
    }
}
