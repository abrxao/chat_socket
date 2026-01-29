package mychat.common;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.function.Consumer;

public class ChatProtocol {
    public static final byte TYPE_TEXT = 0;
    public static final byte TYPE_FILE = 1;

    /**
     * Sends a text message directly to a specific socket.
     */
    public static void sendDirectMessage(Socket socket, String text) throws IOException {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeByte(TYPE_TEXT);
        dos.writeUTF(text);
        dos.flush();
    }

    /**
     * Handles responses from the server on the client side.
     * Reads the packet type and triggers the appropriate callback for the GUI.
     */
    public static void handleClientResponse(DataInputStream dis, String savePath, Consumer<String> callback)
            throws IOException {

        // Packet header: Identifies if the incoming data is text or a file
        byte type = dis.readByte();

        if (type == TYPE_TEXT) {
            String message = dis.readUTF();
            callback.accept(message);
        } else if (type == TYPE_FILE) {
            try {
                // Delegates file downloading to the FileHandler model
                String fileName = FileHandler.receiveFile(dis, savePath);
                callback.accept("[FILE] Received: " + fileName);
            } catch (IOException e) {
                callback.accept("[ERROR] Failed to download file.");
                throw e; 
            }
        }
    }

    /**
     * Server-side logic to process incoming data from clients.
     * Routes the payload to either TextHandler or FileHandler based on packet type.
     */
    public static void handleIncomingData(Socket sender, DataInputStream dis, Map<Socket, String> clients)
            throws IOException {
        
        byte type = dis.readByte(); 
        
        if (type == TYPE_TEXT) {
            String rawMsg = dis.readUTF();
            String formattedMsg = TextHandler.processMessage(sender, rawMsg, clients);

            if (formattedMsg != null) {
                broadcastText(formattedMsg, sender, clients);
            }
        } else if (type == TYPE_FILE) {
            FileHandler.relayFile(dis, sender, clients);
        }
    }

    /**
     * Sends a text message to all connected clients except the sender.
     */
    private static void broadcastText(String msg, Socket sender, Map<Socket, String> clients) {
        for (Socket s : clients.keySet()) {
            if (s != sender) {
                try {
                    sendDirectMessage(s, msg);
                } catch (IOException e) {
                    clients.remove(s);
                }
            }
        }
    }

    /**
     * Initiates a file transfer from the client to the server.
     */
    public static void sendFile(Socket socket, File file) throws IOException {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        FileHandler.sendFile(dos, file);
    }

    /**
     * Wrapper for sendDirectMessage to maintain naming consistency.
     */
    public static void sendText(Socket socket, String text) throws IOException {
        sendDirectMessage(socket, text);
    }
}