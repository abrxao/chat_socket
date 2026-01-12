package mychat;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    private static final String DOWNLOAD_DIR = "downloads";

    public static void main(String[] args) {
        String serverAddress = "127.0.0.1";
        int serverPort = 12345;

        if (args.length >= 1) {
            serverAddress = args[0];
        }
        if (args.length >= 2) {
            try {
                serverPort = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.err.println("Porta inválida, usando padrão: " + serverPort);
            }
        }
        try {
            Socket socket = new Socket(serverAddress, serverPort);

            // Calculamos o caminho absoluto apenas para mostrar ao usuário onde os arquivos
            // vão parar
            String savePath = new File(DOWNLOAD_DIR).getAbsolutePath();
            System.out.println("Conectado! Arquivos serão salvos em: " + savePath);

            // 1. THREAD DE ESCUTA (Ouvido)
            // Agora delegamos 100% da inteligência para o Protocolo
            Thread listenerThread = new Thread(() -> {
                try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
                    while (true) {
                        // O método handleClientResponse é bloqueante (espera dados chegarem)
                        // e sabe decidir sozinho se imprime texto ou salva arquivo.
                        ChatProtocol.handleClientResponse(dis, savePath);
                    }
                } catch (IOException e) {
                    System.out.println("\nConexão com o servidor encerrada.");
                    System.exit(0);
                }
            });
            listenerThread.start();

            // 2. THREAD DE ENVIO (Fala)
            Scanner sc = new Scanner(System.in);

            while (true) {
                // O prompt visual é gerido aqui e restaurado pelo handleClientResponse
                // quando uma mensagem chega.
                String input = sc.nextLine();

                if (input.startsWith("/file ")) {
                    // Lógica de comando de arquivo
                    String[] parts = input.split(" ", 2);
                    if (parts.length == 2) {
                        File file = new File(parts[1]);
                        if (file.exists() && file.isFile()) {
                            // Chama o wrapper do Protocolo (que chama o FileHandler)
                            // Nota: Certifique-se que o método sendFile existe no ChatProtocol
                            // ou chame FileHandler.sendFile(new DataOutputStream(socket.getOutputStream()),
                            // file);
                            ChatProtocol.sendFile(socket, file);
                        } else {
                            System.out.println("Erro: Arquivo não encontrado ou inválido.");
                            System.out.print("> ");
                        }
                    }
                } else {
                    // Lógica de envio de texto
                    // Usa o método genérico do Protocolo
                    ChatProtocol.sendText(socket, input);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}