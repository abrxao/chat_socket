package mychat;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public class TextHandler {

    /**
     * Processa uma mensagem de texto.
     * Retorna NULL se for um comando interno (que não deve ser enviado aos outros).
     * Retorna a String formatada se for uma mensagem para broadcast.
     */
    public static String processMessage(Socket sender, String message, Map<Socket, String> clientNames)
            throws IOException {
        // LÓGICA DE COMANDO (/nick)
        if (message.startsWith("/nick ")) {
            String[] parts = message.split(" ", 2);
            if (parts.length == 2) {
                String oldName = clientNames.get(sender);
                String newName = parts[1].trim();

                if (!newName.isEmpty()) {
                    clientNames.put(sender, newName);
                    // Resposta direta ao usuário (feedback)
                    ChatProtocol.sendDirectMessage(sender, "Apelido alterado para: " + newName);
                    // Retorna mensagem de sistema para broadcast
                    return ">>> " + oldName + " agora é " + newName;
                }
            }
            ChatProtocol.sendDirectMessage(sender, "Erro ao alterar nick.");
            return null; // Não faz broadcast
        }

        // LÓGICA DE MENSAGEM COMUM
        String senderName = clientNames.get(sender);
        return "[" + senderName + "]: " + message;
    }
}