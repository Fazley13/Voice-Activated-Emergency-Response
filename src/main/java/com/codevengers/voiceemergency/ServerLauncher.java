package com.codevengers.voiceemergency;

public class ServerLauncher {
    private static Thread serverThread;
    private static JavaSocketServer server;
    private static boolean serverRunning = false;

    public static synchronized void startServer() {
        if (!serverRunning) {
            try {
                System.out.println("🚀 Starting chat server...");

                server = new JavaSocketServer();
                serverThread = new Thread(() -> {
                    server.start();
                });

                serverThread.setDaemon(true);
                serverThread.start();

                // Wait a moment for server to start
                Thread.sleep(2000);

                serverRunning = true;
                System.out.println("✅ Chat server started successfully");

            } catch (Exception e) {
                System.err.println("❌ Failed to start server: " + e.getMessage());
                serverRunning = false;
            }
        }
    }

    public static synchronized void stopServer() {
        if (serverRunning && server != null) {
            try {
                server.stop();
                serverRunning = false;
                System.out.println("✅ Chat server stopped");
            } catch (Exception e) {
                System.err.println("❌ Error stopping server: " + e.getMessage());
            }
        }
    }

    public static boolean isServerRunning() {
        return serverRunning;
    }
}