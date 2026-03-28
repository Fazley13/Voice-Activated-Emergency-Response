package com.codevengers.voiceemergency;

public class EmailSetupGuide {

    public static void main(String[] args) {
        printDetailedSetupGuide();
    }

    public static void printDetailedSetupGuide() {
        System.out.println("📧 QUICKRESCUE EMAIL SETUP GUIDE");
        System.out.println("=================================");
        System.out.println();

        System.out.println("🎯 GOAL: Configure email notifications for emergency alerts");
        System.out.println();

        System.out.println("📋 STEP 1: Prepare Your Gmail Account");
        System.out.println("--------------------------------------");
        System.out.println("1. 📧 Use an existing Gmail account OR create a new one");
        System.out.println("   • Recommended: Create a dedicated account like 'yourname.emergency@gmail.com'");
        System.out.println("   • This keeps emergency emails separate from personal emails");
        System.out.println();

        System.out.println("2. 🔐 Enable 2-Step Verification:");
        System.out.println("   a) Go to myaccount.google.com");
        System.out.println("   b) Click 'Security' in the left menu");
        System.out.println("   c) Find '2-Step Verification' and click 'Get started'");
        System.out.println("   d) Follow the setup process (use your phone number)");
        System.out.println("   e) ✅ Verify it's enabled (should show 'On')");
        System.out.println();

        System.out.println("📋 STEP 2: Generate App Password");
        System.out.println("---------------------------------");
        System.out.println("1. 🔑 In Google Account Security settings:");
        System.out.println("   a) Find 'App passwords' (may need to scroll down)");
        System.out.println("   b) Click 'App passwords'");
        System.out.println("   c) You may need to sign in again");
        System.out.println();

        System.out.println("2. 🏷️ Generate password:");
        System.out.println("   a) Select app: 'Mail'");
        System.out.println("   b) Select device: 'Other (Custom name)'");
        System.out.println("   c) Enter name: 'QuickRescue Emergency System'");
        System.out.println("   d) Click 'Generate'");
        System.out.println();

        System.out.println("3. 📝 Copy the 16-character password:");
        System.out.println("   • It looks like: 'abcd efgh ijkl mnop'");
        System.out.println("   • ⚠️ IMPORTANT: Copy it immediately - you won't see it again!");
        System.out.println("   • Remove spaces when using: 'abcdefghijklmnop'");
        System.out.println();

        System.out.println("📋 STEP 3: Configure EmailService.java");
        System.out.println("---------------------------------------");
        System.out.println("1. 📂 Open your project in IntelliJ IDEA");
        System.out.println("2. 📁 Navigate to: backend/src/main/java/EmailService.java");
        System.out.println("3. 🔧 Find these lines (around line 12-20):");
        System.out.println();
        System.out.println("   private static final String EMAIL_USERNAME = \"your-gmail@gmail.com\";");
        System.out.println("   private static final String EMAIL_PASSWORD = \"your-16-character-app-password\";");
        System.out.println("   private static final String FROM_EMAIL = \"your-gmail@gmail.com\";");
        System.out.println();
        System.out.println("4. ✏️ Replace with your actual information:");
        System.out.println("   • EMAIL_USERNAME: Your Gmail address");
        System.out.println("   • EMAIL_PASSWORD: The 16-character app password (no spaces)");
        System.out.println("   • FROM_EMAIL: Same as EMAIL_USERNAME");
        System.out.println();

        System.out.println("📋 STEP 4: Test Your Configuration");
        System.out.println("-----------------------------------");
        System.out.println("1. 💾 Save EmailService.java");
        System.out.println("2. 🔨 Rebuild your project");
        System.out.println("3. ▶️ Run EmailConfigurationTest:");
        System.out.println("   java com.codevengers.voiceemergency.EmailConfigurationTest");
        System.out.println("4. 📧 Send a test email to yourself");
        System.out.println("5. ✅ Verify you receive the test email");
        System.out.println();

        System.out.println("📋 STEP 5: Add Emergency Contacts with Emails");
        System.out.println("----------------------------------------------");
        System.out.println("1. 🚀 Run your QuickRescue application");
        System.out.println("2. 👤 Login as a user");
        System.out.println("3. 📞 Go to Emergency Contacts");
        System.out.println("4. ➕ Add contacts with email addresses");
        System.out.println("5. 🧪 Use 'Test Email' button to verify each contact");
        System.out.println();

        System.out.println("🎯 EXAMPLE CONFIGURATION:");
        System.out.println("=========================");
        System.out.println("EMAIL_USERNAME = \"john.emergency@gmail.com\"");
        System.out.println("EMAIL_PASSWORD = \"abcdefghijklmnop\"  // Your 16-char app password");
        System.out.println("FROM_EMAIL = \"john.emergency@gmail.com\"");
        System.out.println();

        System.out.println("🚨 TROUBLESHOOTING:");
        System.out.println("===================");
        System.out.println("❌ \"Authentication failed\":");
        System.out.println("   • Double-check your Gmail address");
        System.out.println("   • Verify app password is correct (no spaces)");
        System.out.println("   • Ensure 2-Step Verification is enabled");
        System.out.println();

        System.out.println("❌ \"Connection timeout\":");
        System.out.println("   • Check your internet connection");
        System.out.println("   • Try disabling VPN if using one");
        System.out.println("   • Check firewall settings");
        System.out.println();

        System.out.println("❌ \"Test email not received\":");
        System.out.println("   • Check spam/junk folder");
        System.out.println("   • Wait a few minutes (email can be delayed)");
        System.out.println("   • Try sending to a different email address");
        System.out.println();

        System.out.println("✅ SUCCESS INDICATORS:");
        System.out.println("======================");
        System.out.println("• EmailConfigurationTest shows 'Email configuration appears valid'");
        System.out.println("• Test emails are received successfully");
        System.out.println("• Emergency contacts can receive test emails");
        System.out.println("• Beautiful HTML emergency emails with maps and instructions");
        System.out.println();

        System.out.println("🎉 Once configured, your emergency system will:");
        System.out.println("===============================================");
        System.out.println("📧 Send beautiful HTML emails to emergency contacts");
        System.out.println("📍 Include clickable Google Maps location links");
        System.out.println("🆘 Provide clear instructions on what to do");
        System.out.println("⚠️ Mark emails as high priority for urgent delivery");
        System.out.println("📱 Include all emergency details (time, type, description)");
        System.out.println("🔄 Work alongside SMS notifications for maximum reach");
        System.out.println();

        System.out.println("💡 Need help? Check the console output for detailed error messages!");
    }
}