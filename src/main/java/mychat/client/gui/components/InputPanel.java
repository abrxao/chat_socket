package mychat.client.gui.components;

import mychat.client.gui.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

public class InputPanel extends JPanel {
    private final JTextField inputField;
    private final JButton sendButton;

    public InputPanel(Consumer<String> onSend) {
        super(new BorderLayout(Theme.px(12), 0));
        setBackground(Theme.BG_DARK);
        setBorder(new EmptyBorder(Theme.px(15), Theme.px(15), Theme.px(15), Theme.px(15)));

        // Input Field Styling
        inputField = new JTextField();
        inputField.setBackground(Theme.INPUT_BG);
        inputField.setForeground(Theme.TEXT_PRIMARY);
        inputField.setCaretColor(Color.WHITE);
        inputField.setFont(Theme.getFontRegular(1.0f));
        inputField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_GRAY, 0),
                BorderFactory.createEmptyBorder(Theme.px(12), Theme.px(15), Theme.px(12), Theme.px(15))));

        // Button Styling
        sendButton = new JButton("➤");
        sendButton.setBackground(Theme.ACCENT_BLUE);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFont(Theme.getFontBold(1.3f));
        sendButton.setFocusPainted(false);
        sendButton.setBorderPainted(false);
        sendButton.setPreferredSize(new Dimension(Theme.px(60), Theme.px(50)));
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        add(inputField, BorderLayout.CENTER);
        add(sendButton, BorderLayout.EAST);

        // Events
        ActionListener action = e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                onSend.accept(text);
                inputField.setText("");
            }
        };

        sendButton.addActionListener(action);
        inputField.addActionListener(action);
    }
}