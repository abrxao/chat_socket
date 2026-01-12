package mychat;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.*;

public class ChatServer {
    private static final int PORT = 12345;
    private static Map<Socket, String> clientNames = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        System.out.println("Servidor MVC rodando na porta " + PORT);
        try (ServerSocket server = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = server.accept();
                new Thread(new ClientRunnable(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientRunnable implements Runnable {
        private Socket socket;

        public ClientRunnable(Socket s) {
            this.socket = s;
        }

        @Override
        public void run() {
            try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                // Setup Inicial
                String defaultName = "User-" + socket.getPort();
                clientNames.put(socket, defaultName);
                ChatProtocol.sendDirectMessage(socket, "Bem-vindo! Comandos: /nick [nome] ou /file [caminho]");

                // LOOP PRINCIPAL: O Protocolo assume o controle
                while (true) {
                    ChatProtocol.handleIncomingData(socket, dis, clientNames);
                }
            } catch (IOException e) {
                clientNames.remove(socket);
                System.out.println("Cliente desconectado.");
            }
        }
    }
}