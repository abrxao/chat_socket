package mychat.common;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public class TextHandler {

    /**
     * Processes a text message.
     * Returns NULL if it is an internal command.
     * Returns the formatted String if it is a message intended for broadcast.
     */
    public static String processMessage(Socket sender, String message, Map<Socket, String> clientNames)
            throws IOException {

        // COMMAND LOGIC (/nick)
        if (message.startsWith("/nick ")) {
            String[] parts = message.split(" ", 2);
            if (parts.length == 2) {
                String oldName = clientNames.get(sender);
                String newName = parts[1].trim();

                if (!newName.isEmpty()) {
                    clientNames.put(sender, newName);

                    // Direct feedback to the user (Tagged as SYSTEM for centralized GUI display)
                    ChatProtocol.sendDirectMessage(sender, "[SYSTEM] Your nickname changed to \"" + newName + "\"");

                    // Return system message for broadcast (Tagged as SYSTEM for all users)
                    return "[SYSTEM] \"" + oldName + "\" is now \"" + newName + "\"";
                }
            }
            ChatProtocol.sendDirectMessage(sender, "[SYSTEM] Error changing nickname.");
            return null;
        }

        // STANDARD MESSAGE LOGIC
        String senderName = clientNames.get(sender);
        return "[" + senderName + "]: " + message;
    }
}