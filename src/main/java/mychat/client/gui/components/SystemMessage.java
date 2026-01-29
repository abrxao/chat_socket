package mychat.client.gui.components;

import mychat.client.gui.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class SystemMessage extends JPanel {

    public SystemMessage(String text) {
        super(new GridBagLayout());
        setBackground(Theme.CHAT_AREA);
        // Compact vertical spacing
        setBorder(new EmptyBorder(Theme.px(2), Theme.px(40), Theme.px(2), Theme.px(40)));

        JTextPane textPane = new JTextPane();
        textPane.setText(text);
        textPane.setEditable(false);
        textPane.setOpaque(false);
        textPane.setFocusable(false);
        textPane.setMargin(new Insets(0, 0, 0, 0));
        textPane.setFont(Theme.getFontMonospace(0.75f));
        textPane.setForeground(Theme.TEXT_SYSTEM);

        // Center alignment logic
        StyledDocument doc = textPane.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        // Strict sizing
        int maxWidth = Theme.rem(25);
        textPane.setSize(new Dimension(maxWidth, Short.MAX_VALUE));
        int height = textPane.getPreferredSize().height;
        Dimension finalSize = new Dimension(maxWidth, height);

        textPane.setPreferredSize(finalSize);
        textPane.setMaximumSize(finalSize);
        textPane.setMinimumSize(finalSize);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(textPane, gbc);

        // Constraint the panel height
        setMaximumSize(new Dimension(Integer.MAX_VALUE, height + Theme.px(4)));
    }
}