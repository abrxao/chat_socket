package mychat.client.gui;

import java.awt.*;

public class Theme {
    // Modern Dark Palette
    public static final Color BG_DARK = new Color(28, 28, 30);
    public static final Color CHAT_AREA = new Color(35, 35, 38);
    public static final Color ACCENT_BLUE = new Color(10, 132, 255);
    public static final Color BUBBLE_GRAY = new Color(58, 58, 60);
    public static final Color INPUT_BG = new Color(48, 48, 50);
    public static final Color TEXT_PRIMARY = new Color(240, 240, 240);
    public static final Color TEXT_SYSTEM = new Color(110, 110, 110);
    public static final Color BORDER_GRAY = new Color(80, 80, 80);

    // Scaling Logic
    private static final int BASE_FONT_SIZE = 20;
    private static float SCALE_FACTOR;

    static {
        int screenRes = Toolkit.getDefaultToolkit().getScreenResolution();
        float calculatedScale = (float) screenRes / 64.0f;
        // Force minimum scale of 1.1x for readability
        SCALE_FACTOR = Math.max(calculatedScale, 1.1f);
        System.out.println("Theme Initialized | Scale: " + SCALE_FACTOR);
    }

    public static int rem(float value) {
        return Math.round(BASE_FONT_SIZE * value * SCALE_FACTOR);
    }

    public static int px(int value) {
        return Math.round(value * SCALE_FACTOR);
    }

    public static Font getFontRegular(float remSize) {
        return new Font("Segoe UI", Font.PLAIN, rem(remSize));
    }

    public static Font getFontBold(float remSize) {
        return new Font("Segoe UI", Font.BOLD, rem(remSize));
    }

    public static Font getFontMonospace(float remSize) {
        return new Font("Consolas", Font.BOLD, rem(remSize));
    }
}