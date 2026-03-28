package com.codevengers.voiceemergency;

import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class JavaSocketServer {
    private ServerSocket serverSocket;
    private boolean running = false;
    private List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static final int PORT = 8080;

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            running = true;
            System.out.println("🚀 Chat server started on port " + PORT);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
                System.out.println("✅ New client connected. Total clients: " + clients.size());
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("❌ Server error: " + e.getMessage());
            }
        }
    }

    public void stop() {
        running = false;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
            for (ClientHandler client : clients) {
                client.disconnect();
            }
            clients.clear();
        } catch (IOException e) {
            System.err.println("❌ Error stopping server: " + e.getMessage());
        }
    }

    public void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("❌ Client disconnected. Total clients: " + clients.size());
    }

    public static void main(String[] args) {
        JavaSocketServer server = new JavaSocketServer();

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        server.start();
    }

    // Inner class for handling individual clients
    private static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private JavaSocketServer server;
        private int userId;
        private String userType;

        public ClientHandler(Socket socket, JavaSocketServer server) {
            this.socket = socket;
            this.server = server;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    handleMessage(inputLine);
                }
            } catch (IOException e) {
                System.err.println("❌ Client handler error: " + e.getMessage());
            } finally {
                disconnect();
            }
        }

        private void handleMessage(String jsonMessage) {
            try {
                JSONObject json = new JSONObject(jsonMessage);
                String type = json.getString("type");

                switch (type) {
                    case "join":
                        userId = json.getInt("userId");
                        userType = json.getString("userType");
                        System.out.println("✅ User joined: " + userType + " ID: " + userId);
                        break;

                    case "message":
                        // Broadcast message to all other clients
                        server.broadcastMessage(jsonMessage, this);
                        System.out.println("📨 Message broadcasted from " + userType + " ID: " + userId);
                        break;
                }
            } catch (Exception e) {
                System.err.println("❌ Error handling message: " + e.getMessage());
            }
        }

        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        public void disconnect() {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
                server.removeClient(this);
            } catch (IOException e) {
                System.err.println("❌ Error disconnecting client: " + e.getMessage());
            }
        }
    }
}
