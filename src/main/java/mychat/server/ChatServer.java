package mychat.server;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

import mychat.common.ChatProtocol;

public class ChatServer {
    private static final int PORT = 12345;
    private static Map<Socket, String> clientNames = new ConcurrentHashMap<>();
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        System.out.println("Chat Server started on port " + PORT);

        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = server.accept();
                logConsole("New connection from: " + socket.getInetAddress().getHostAddress());

                // Spawn a new thread for each connected client (ClientHandler requirement)
                new Thread(new ClientRunnable(socket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Synchronized method to log communication details into 'comm.log'.
     * Records: Client name, IP, Start time, and Session duration.
     */
    private static synchronized void writeLog(String name, String ip, long start, long end) {
        long duration = end - start; 
        String startTimeStr = SDF.format(new Date(start));

        String logLine = String.format("Client: %s | IP: %s | Start: %s | Duration: %d ms%n",
                name, ip, startTimeStr, duration);

        try (FileWriter fw = new FileWriter("comm.log", true); 
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(logLine);
        } catch (IOException e) {
            System.err.println("Logging error: " + e.getMessage());
        }
    }

    /**
     * Prints formatted logs to the console with a timestamp.
     */
    public static void logConsole(String message) {
        System.out.println("[" + SDF.format(new Date()) + "] " + message);
    }

    /**
     * Broadcasts a system notification to all connected clients.
     */
    public static void broadcastSystemMessage(String message) {
        for (Socket s : clientNames.keySet()) {
            try {
                ChatProtocol.sendDirectMessage(s, "[SYSTEM] " + message);
            } catch (Exception e) {
                // Ignore failed deliveries; cleanup is handled in ClientRunnable's finally block
            }
        }
    }

    /**
     * Internal class handling individual client logic (TP1 Architecture).
     */
    private static class ClientRunnable implements Runnable {
        private Socket socket;

        public ClientRunnable(Socket s) {
            this.socket = s;
        }

        @Override
        public void run() {
            long startTime = System.currentTimeMillis();
            String clientName = "Unknown";

            try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                // Initial Setup
                clientName = "User-" + socket.getPort(); 
                clientNames.put(socket, clientName);

                ChatProtocol.sendDirectMessage(socket,
                        "Welcome! Available commands: /nick [name], /file [path]");

                ChatServer.broadcastSystemMessage(clientName + " joined the room.");

                // Main communication loop
                while (true) {
                    ChatProtocol.handleIncomingData(socket, dis, clientNames);

                    // Sync name in case it was changed via /nick command
                    if (clientNames.containsKey(socket)) {
                        clientName = clientNames.get(socket);
                    }
                }
            } catch (EOFException | SocketException e) {
                // Connection closed normally or lost
            } catch (IOException e) {
                System.err.println("Communication error with " + clientName + ": " + e.getMessage());
            } finally {
                // Termination cleanup and logging (Critical for TP1 requirements)
                clientNames.remove(socket);
                
                ChatServer.logConsole("Client disconnected: " + clientName);
                ChatServer.broadcastSystemMessage(clientName + " left the chat.");

                long endTime = System.currentTimeMillis();
                ChatServer.writeLog(clientName, socket.getInetAddress().getHostAddress(), startTime, endTime);

                try {
                    if (socket != null && !socket.isClosed()) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}