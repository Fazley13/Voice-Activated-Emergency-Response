package com.codevengers.voiceemergency;

public class UserSession {
    private static int currentUserId = -1;
    private static String currentUsername = null;
    private static String currentUserEmail = null;
    private static String currentUserType = null;
    private static boolean loggedIn = false;

    public static void login(int userId, String username, String email, String userType) {
        currentUserId = userId;
        currentUsername = username;
        currentUserEmail = email;
        currentUserType = userType;
        loggedIn = true;

        System.out.println("✅ UserSession.login() called:");
        System.out.println("   User ID: " + userId);
        System.out.println("   Username: " + username);
        System.out.println("   Email: " + email);
        System.out.println("   User Type: " + userType);
        System.out.println("   Logged In: " + loggedIn);
    }

    public static void logout() {
        System.out.println("🔓 UserSession.logout() called - clearing session");
        currentUserId = -1;
        currentUsername = null;
        currentUserEmail = null;
        currentUserType = null;
        loggedIn = false;
        System.out.println("✅ UserSession cleared successfully");
    }

    public static boolean isLoggedIn() {
        boolean sessionValid = loggedIn && currentUserId != -1 && currentUsername != null;
        System.out.println("🔍 UserSession.isLoggedIn() check: " + sessionValid);
        if (!sessionValid) {
            System.out.println("   - loggedIn: " + loggedIn);
            System.out.println("   - currentUserId: " + currentUserId);
            System.out.println("   - currentUsername: " + currentUsername);
        }
        return sessionValid;
    }

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static String getCurrentUsername() {
        if (currentUsername == null || currentUsername.trim().isEmpty()) {
            System.err.println("⚠️ getCurrentUsername() returning null or empty - session may not be set properly");
            return "Unknown User";
        }
        return currentUsername;
    }

    public static String getCurrentUserEmail() {
        return currentUserEmail != null ? currentUserEmail : "unknown@example.com";
    }

    public static String getCurrentUserType() {
        return currentUserType != null ? currentUserType : "user";
    }

    // Debug method
    public static void printSessionInfo() {
        System.out.println("=== USER SESSION INFO ===");
        System.out.println("Logged In: " + loggedIn);
        System.out.println("User ID: " + currentUserId);
        System.out.println("Username: " + currentUsername);
        System.out.println("Email: " + currentUserEmail);
        System.out.println("User Type: " + currentUserType);
        System.out.println("========================");
    }

    // Validation method
    public static boolean validateSession() {
        boolean valid = isLoggedIn();
        if (!valid) {
            System.err.println("❌ Session validation failed!");
            printSessionInfo();
        }
        return valid;
    }
}
