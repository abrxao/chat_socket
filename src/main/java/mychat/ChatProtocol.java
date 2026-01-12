package mychat;

import java.io.*;
import java.net.Socket;
import java.util.Map;

public class ChatProtocol {
    public static final byte TYPE_TEXT = 0;
    public static final byte TYPE_FILE = 1;

    // Método utilitário para envio rápido de mensagens de sistema
    public static void sendDirectMessage(Socket socket, String text) throws IOException {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeByte(TYPE_TEXT);
        dos.writeUTF(text);
        dos.flush();
    }

    // O CÉREBRO DO SERVIDOR: Decide o fluxo
    public static void handleIncomingData(Socket sender, DataInputStream dis, Map<Socket, String> clients)
            throws IOException {
        byte type = dis.readByte(); // 1. Identifica o pacote

        if (type == TYPE_TEXT) {
            // Delega para o TextHandler
            String rawMsg = dis.readUTF();
            String formattedMsg = TextHandler.processMessage(sender, rawMsg, clients);

            // Se o Handler retornou algo, fazemos o broadcast
            if (formattedMsg != null) {
                broadcastText(formattedMsg, sender, clients);
            }
        } else if (type == TYPE_FILE) {
            // Delega para o FileHandler
            FileHandler.relayFile(dis, sender, clients);
        }
    }

    // O CÉREBRO DO CLIENTE
    public static void handleClientResponse(DataInputStream dis, String saveDir) throws IOException {
        byte type = dis.readByte();

        if (type == TYPE_TEXT) {
            System.out.println("\n" + dis.readUTF());
            System.out.print("> ");
        } else if (type == TYPE_FILE) {
            FileHandler.saveFile(dis, saveDir);
            System.out.print("> ");
        }
    }

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

    // No ChatProtocol.java
    public static void sendFile(Socket socket, File file) throws IOException {
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        // Chama o Model especialista
        FileHandler.sendFile(dos, file);
    }

    // Wrapper para manter consistência de nomes, se preferir usar 'sendText'
    public static void sendText(Socket socket, String text) throws IOException {
        sendDirectMessage(socket, text);
    }
}