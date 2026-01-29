package mychat.client.gui;

import mychat.client.ChatClient;
import mychat.client.gui.components.*;
import javax.swing.*;
import java.awt.*;

public class ClientGUI extends JFrame {
    private final ChatViewport chatViewport;
    private final InputPanel inputPanel;
    private ChatClient chatClient;

    public ClientGUI() {
        super("Chat Client - TP1");

        // Initial Window Setup
        setSize(Theme.rem(28), Theme.rem(40));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_DARK);
        setLayout(new BorderLayout());

        // 1. Chat Area
        chatViewport = new ChatViewport();
        add(chatViewport, BorderLayout.CENTER);

        // 2. Input Area
        inputPanel = new InputPanel(text -> {
            if (chatClient != null) {
                // Optimistic UI update
                appendMessage("[YOU] " + text);
                chatClient.sendMessage(text);
            }
        });
        add(inputPanel, BorderLayout.SOUTH);
    }

    public void setChatClient(ChatClient client) {
        this.chatClient = client;
    }

    /**
     * Parses the incoming raw message and decides which component to create.
     */
    public void appendMessage(String message) {
        if (message == null)
            return;

        SwingUtilities.invokeLater(() -> {
            boolean isSystem = message.startsWith("[SYSTEM]") ||
                    message.startsWith("[ERROR]") ||
                    message.startsWith("[FILE]");

            boolean isMe = message.startsWith("[YOU]") || message.startsWith("[you]");

            if (isSystem) {
                // Remove prefix for cleaner look if desired, or keep it.
                chatViewport.addComponent(new SystemMessage(message));
            } else {
                // Remove prefix "[User]:" if you want just the text in the bubble
                // For now, we pass the full message or parse it.
                chatViewport.addComponent(new MessageBubble(message, isMe));
            }
        });
    }

    // Main for visual testing without server
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI();
            gui.setVisible(true);
            gui.appendMessage("[SYSTEM] GUI Layout Test Mode");
            gui.appendMessage("Hello world from the new modular architecture!");
            gui.appendMessage("[YOU] It looks much cleaner code-wise.");
        });
    }
}