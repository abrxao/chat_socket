package mychat.client.gui.components;

import mychat.client.gui.Theme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ChatViewport extends JScrollPane {
    private final JPanel contentPanel;

    public ChatViewport() {
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Theme.CHAT_AREA);
        contentPanel.setBorder(new EmptyBorder(Theme.px(15), Theme.px(15), Theme.px(15), Theme.px(15)));

        setViewportView(contentPanel);
        setBorder(null);
        getVerticalScrollBar().setUnitIncrement(Theme.px(20));
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    public void addComponent(JComponent component) {
        contentPanel.add(component);
        contentPanel.revalidate();
        contentPanel.repaint();
        scrollToBottom();
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
}