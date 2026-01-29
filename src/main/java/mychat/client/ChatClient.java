package mychat.client;

import mychat.client.gui.ClientGUI;
import mychat.common.ChatProtocol;
import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class ChatClient {
    private static final String DOWNLOAD_DIR = "downloads";

    private final String serverAddress;
    private final int serverPort;
    private final ClientGUI gui;
    private final String savePath;
    private Socket socket;

    public ChatClient(String serverAddress, int serverPort, ClientGUI gui) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.gui = gui;

        File downloadDir = new File(DOWNLOAD_DIR);
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        this.savePath = downloadDir.getAbsolutePath();
    }

    public void start() {
        try {
            this.socket = new Socket(serverAddress, serverPort);

            gui.appendMessage("[SYSTEM] Connected to " + serverAddress + ":" + serverPort);
            gui.appendMessage("[SYSTEM] Downloads path: " + savePath);

            // Listener Thread
            new Thread(() -> {
                try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                    while (!socket.isClosed()) {
                        ChatProtocol.handleClientResponse(dis, savePath, gui::appendMessage);
                    }
                } catch (IOException e) {
                    gui.appendMessage("[SYSTEM] Connection lost.");
                }
            }).start();

        } catch (IOException e) {
            gui.appendMessage("[ERROR] Connection failed: " + e.getMessage());
        }
    }

    public void sendMessage(String input) {
        if (socket == null || socket.isClosed()) {
            gui.appendMessage("[ERROR] Not connected.");
            return;
        }

        try {
            if (input.startsWith("/file ")) {
                handleFileUpload(input);
            } else {
                ChatProtocol.sendText(socket, input);
            }
        } catch (IOException e) {
            gui.appendMessage("[ERROR] Failed to send: " + e.getMessage());
            disconnect();
        }
    }

    private void handleFileUpload(String input) throws IOException {
        String[] parts = input.split(" ", 2);
        if (parts.length == 2) {
            File file = new File(parts[1]);
            if (file.exists() && file.isFile()) {
                gui.appendMessage("[YOU] Sending file: " + file.getName());
                ChatProtocol.sendFile(socket, file);
            } else {
                gui.appendMessage("[ERROR] File not found: " + parts[1]);
            }
        }
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            // Ignore
        }
    }

    public static void main(String[] args) {
        String address = args.length >= 1 ? args[0] : "127.0.0.1";
        int port = 12345;

        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port.");
            }
        }

        final String finalAddress = address;
        final int finalPort = port;

        SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI();
            ChatClient client = new ChatClient(finalAddress, finalPort, gui);
            gui.setChatClient(client);
            gui.setVisible(true);
            new Thread(client::start).start();
        });
    }
}