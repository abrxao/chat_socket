package mychat.client.gui.components;

import mychat.client.gui.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MessageBubble extends JPanel {

    public MessageBubble(String text, boolean isMe) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBackground(Theme.CHAT_AREA);
        setBorder(new EmptyBorder(Theme.px(6), 0, Theme.px(6), 0));

        JTextArea bubble = new JTextArea(text);
        bubble.setWrapStyleWord(true);
        bubble.setLineWrap(true);
        bubble.setEditable(false);
        bubble.setFont(Theme.getFontRegular(1.0f));
        bubble.setForeground(Theme.TEXT_PRIMARY);
        bubble.setBackground(isMe ? Theme.ACCENT_BLUE : Theme.BUBBLE_GRAY);
        bubble.setBorder(new EmptyBorder(Theme.px(12), Theme.px(18), Theme.px(12), Theme.px(18)));

        // Dynamic Height Calculation
        int maxWidth = Theme.rem(20);
        bubble.setSize(new Dimension(maxWidth, Integer.MAX_VALUE));
        Dimension calcSize = bubble.getPreferredSize();
        int finalWidth = Math.min(calcSize.width, maxWidth);

        Dimension finalDim = new Dimension(finalWidth, calcSize.height);
        bubble.setMaximumSize(finalDim);
        bubble.setPreferredSize(finalDim);

        if (isMe) {
            add(Box.createHorizontalGlue());
            add(bubble);
        } else {
            add(bubble);
            add(Box.createHorizontalGlue());
        }
    }
}