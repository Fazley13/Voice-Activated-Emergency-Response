package com.codevengers.voiceemergency;

import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.scene.control.Alert;

public class EmergencySocketServer {
    private static final int PORT = 8888;
    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private ExecutorService threadPool;

    public EmergencySocketServer() {
        threadPool = Executors.newFixedThreadPool(10);
    }

    /**
     * Start the emergency alert server
     */
    public void startServer() {
        try {
            serverSocket = new ServerSocket(PORT);
            isRunning = true;
            System.out.println("🚨 Emergency Alert Server started on port " + PORT);

            while (isRunning) {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(new EmergencyAlertHandler(clientSocket));
            }
        } catch (IOException e) {
            if (isRunning) {
                System.err.println("❌ Server error: " + e.getMessage());
            }
        }
    }

    /**
     * Stop the server
     */
    public void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            threadPool.shutdown();
            System.out.println("✅ Emergency Alert Server stopped");
        } catch (IOException e) {
            System.err.println("❌ Error stopping server: " + e.getMessage());
        }
    }

    /**
     * Handle incoming emergency alerts
     */
    private static class EmergencyAlertHandler implements Runnable {
        private Socket clientSocket;

        public EmergencyAlertHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(
                         clientSocket.getOutputStream(), true)) {

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (inputLine.startsWith("EMERGENCY_ALERT:")) {
                        String alertMessage = inputLine.substring("EMERGENCY_ALERT:".length());
                        handleEmergencyAlert(alertMessage);
                        out.println("ALERT_RECEIVED");
                    }
                }
            } catch (IOException e) {
                System.err.println("❌ Error handling client: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("❌ Error closing client socket: " + e.getMessage());
                }
            }
        }

        private void handleEmergencyAlert(String alertMessage) {
            System.out.println("🚨 EMERGENCY ALERT RECEIVED: " + alertMessage);

            // Show JavaFX alert
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("🚨 EMERGENCY ALERT RECEIVED 🚨");
                alert.setHeaderText("Emergency Situation Detected");
                alert.setContentText(alertMessage);

                alert.getDialogPane().setStyle(
                        "-fx-background-color: #ffebee;" +
                                "-fx-border-color: #f44336;" +
                                "-fx-border-width: 3px;"
                );

                alert.show();
            });

            // Here you could also:
            // - Forward to emergency services
            // - Log to database
            // - Send to monitoring systems
        }
    }

    public static void main(String[] args) {
        EmergencySocketServer server = new EmergencySocketServer();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::stopServer));

        server.startServer();
    }
}
