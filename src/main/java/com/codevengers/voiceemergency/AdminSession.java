package com.codevengers.voiceemergency;

public class AdminSession {
    private static int currentAdminId = -1;
    private static String currentAdminUsername = null;
    private static String currentAdminEmail = null;
    private static String currentAdminDepartment = null;
    private static boolean loggedIn = false;

    public static void setCurrentAdmin(int adminId, String username, String email, String department) {
        currentAdminId = adminId;
        currentAdminUsername = username;
        currentAdminEmail = email;
        currentAdminDepartment = department;
        loggedIn = true;

        System.out.println("✅ AdminSession.setCurrentAdmin() called:");
        System.out.println("   Admin ID: " + adminId);
        System.out.println("   Username: " + username);
        System.out.println("   Email: " + email);
        System.out.println("   Department: " + department);
    }

    public static void logout() {
        currentAdminId = -1;
        currentAdminUsername = null;
        currentAdminEmail = null;
        currentAdminDepartment = null;
        loggedIn = false;

        System.out.println("🔓 AdminSession.logout() called - session cleared");
    }

    public static boolean isLoggedIn() {
        return loggedIn && currentAdminId != -1 && currentAdminUsername != null;
    }

    public static int getCurrentAdminId() {
        return currentAdminId;
    }

    public static String getCurrentAdminUsername() {
        return currentAdminUsername;
    }

    public static String getCurrentAdminEmail() {
        return currentAdminEmail;
    }

    public static String getCurrentAdminDepartment() {
        return currentAdminDepartment != null ? currentAdminDepartment : "Emergency Services";
    }

    // Debug method
    public static void printSessionInfo() {
        System.out.println("=== ADMIN SESSION INFO ===");
        System.out.println("Logged In: " + loggedIn);
        System.out.println("Admin ID: " + currentAdminId);
        System.out.println("Username: " + currentAdminUsername);
        System.out.println("Email: " + currentAdminEmail);
        System.out.println("Department: " + currentAdminDepartment);
        System.out.println("==========================");
    }
}
