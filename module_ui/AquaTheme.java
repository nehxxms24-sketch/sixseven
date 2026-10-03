package module_ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Stone Grey Theme — Indian ₹500 Note Palette.
 * All accent colours, fonts, and factory helpers are centralised here.
 */
public class AquaTheme {

    // ── Pure Solid Stone Grey Palette (Zero Green Tint) ──────────────
    public static final Color STONE_PRIMARY   = new Color(0x54, 0x58, 0x54); // #545854 – Sidebar Headers & Accents
    public static final Color DEEP_SLATE      = new Color(0x4A, 0x4D, 0x4A); // #4A4D4A – Pure Solid Stone Grey (Sidebar & Login BG)
    public static final Color ACTIVE_TAB_BG   = new Color(0x3A, 0x3C, 0x3A); // #3A3C3A – Darker Neutral Charcoal-Grey (Active / Hover)
    public static final Color LIGHT_WASH      = new Color(0xEC, 0xEE, 0xEA); // #ECEEEA – Main App Background
    public static final Color CRISP_WHITE     = new Color(0xFF, 0xFF, 0xFF); // #FFFFFF – Inner Cards & Tables
    public static final Color SUBTLE_BORDER   = new Color(0xD0, 0xD3, 0xCF); // #D0D3CF – Neutral Border
    public static final Color ACTION_TEAL     = new Color(0x4E, 0x53, 0x4E); // #4E534E – Solid Slate Grey Primary CTAs
    public static final Color HOVER_SLATE     = new Color(0x3A, 0x3C, 0x3A); // #3A3C3A – Hover / Secondary Accent
    public static final Color GOLD            = new Color(0xD4, 0xAF, 0x37); // Gold for Coin Logo
    public static final Color DANGER_RED      = new Color(0xC0, 0x39, 0x2B); // Errors / Delete
    public static final Color SUCCESS_GREEN   = new Color(0x27, 0xAE, 0x60); // DB Connected / Success

    // Backwards-compat aliases used throughout the project
    public static final Color GRADIENT_TOP    = DEEP_SLATE;
    public static final Color GRADIENT_BOTTOM = STONE_PRIMARY;
    public static final Color OCEAN_BLUE      = DEEP_SLATE;
    public static final Color AQUA_CYAN       = ACTION_TEAL;
    public static final Color BUTTON_CYAN     = ACTION_TEAL;
    public static final Color BUTTON_CYAN_HOVER = HOVER_SLATE;
    public static final Color SOFT_MINT       = ACTION_TEAL;
    public static final Color OFF_WHITE       = LIGHT_WASH;
    public static final Color WARM_OFFWHITE   = LIGHT_WASH;
    public static final Color CARD_BG         = CRISP_WHITE;
    public static final Color PURE_WHITE      = CRISP_WHITE;
    public static final Color CARD_BG_ALT     = new Color(0xE4, 0xE7, 0xE3);
    public static final Color VINTAGE_CREAM   = SUBTLE_BORDER;
    public static final Color ACCENT_BLUE     = ACTION_TEAL;
    public static final Color CURRENCY_ORANGE = ACTION_TEAL;
    public static final Color SAGE_GREEN      = HOVER_SLATE;
    public static final Color STEEL_SLATE     = STONE_PRIMARY;
    public static final Color DEEP_FOREST_GREEN = DEEP_SLATE;
    public static final Color TEXT_DARK       = new Color(0x22, 0x24, 0x22); // Near-black body text
    public static final Color TEXT_MUTED      = HOVER_SLATE;

    // ── Typography: ALL UI text = Arial; Brand = Josefin Slab ─────────
    public static final Font FONT_ARIAL_LABEL  = new Font("Arial", Font.BOLD,  12);
    public static final Font FONT_ARIAL_FIELD  = new Font("Arial", Font.PLAIN, 13);
    public static final Font FONT_ARIAL_BUTTON = new Font("Arial", Font.BOLD,  13);
    public static final Font FONT_ARIAL_SUB    = new Font("Arial", Font.PLAIN, 12);
    public static final Font FONT_ARIAL_HEADER = new Font("Arial", Font.BOLD,  16);

    // Aliases
    public static final Font FONT_LABEL        = FONT_ARIAL_LABEL;
    public static final Font FONT_FIELD        = FONT_ARIAL_FIELD;
    public static final Font FONT_BUTTON       = FONT_ARIAL_BUTTON;
    public static final Font FONT_SUBTITLE     = FONT_ARIAL_SUB;
    public static final Font FONT_SECTION      = FONT_ARIAL_HEADER;
    public static final Font FONT_HEADER_TITLE = new Font("Arial", Font.BOLD, 22);

    /** Josefin Slab for "SixSeven" brand title with fallbacks to Rockwell, Georgia, Serif. */
    public static Font getJosefinSlabFont(int style, float size) {
        String[] preferred = {"Josefin Slab", "Rockwell", "Georgia", Font.SERIF};
        String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String want : preferred) {
            for (String have : available) {
                if (have.equalsIgnoreCase(want)) {
                    return new Font(have, style, (int) size);
                }
            }
        }
        return new Font(Font.SERIF, style, (int) size);
    }

    public static Font getTitleFont() {
        return getJosefinSlabFont(Font.BOLD, 30f);
    }

    public static Border padding(int t, int l, int b, int r) { return new EmptyBorder(t, l, b, r); }

    public static JPanel createCardPanel() {
        JPanel p = new JPanel();
        p.setBackground(CRISP_WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SUBTLE_BORDER, 1, true),
                new EmptyBorder(14, 14, 14, 14)));
        return p;
    }

    /** Muted Emerald/Teal-Grey CTA button (#2D6A4F) with white bold Arial text. */
    public static JButton createCyanButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color fill = getModel().isPressed()  ? DEEP_SLATE :
                             getModel().isRollover() ? HOVER_SLATE : ACTION_TEAL;
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_ARIAL_BUTTON);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public static JButton createAquaButton(String text)   { return createCyanButton(text); }

    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? DANGER_RED.brighter() : DANGER_RED);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_ARIAL_BUTTON);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /**
     * Draws a circular gold-embossed coin with a centred ₹ symbol using Graphics2D.
     * Used when coin_logo.png is unavailable.
     */
    public static void drawCoinLogo(Graphics2D g2, int x, int y, int diameter) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Outer shadow
        g2.setColor(new Color(0, 0, 0, 40));
        g2.fillOval(x + 2, y + 2, diameter, diameter);
        // Gold gradient fill
        GradientPaint gp = new GradientPaint(x, y, new Color(0xFF, 0xD7, 0x00),
                                             x + diameter, y + diameter, new Color(0xB8, 0x86, 0x0B));
        g2.setPaint(gp);
        g2.fillOval(x, y, diameter, diameter);
        // Border ring
        g2.setColor(new Color(0x8B, 0x6F, 0x00));
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(x, y, diameter, diameter);
        // Inner ring
        g2.setColor(new Color(0xFF, 0xD7, 0x00, 180));
        g2.setStroke(new BasicStroke(1f));
        g2.drawOval(x + 4, y + 4, diameter - 8, diameter - 8);
        // Rupee symbol
        g2.setColor(new Color(0x5A, 0x3E, 0x00));
        g2.setFont(new Font("Arial", Font.BOLD, diameter / 2));
        FontMetrics fm = g2.getFontMetrics();
        String symbol = "\u20B9";
        int sw = fm.stringWidth(symbol);
        int sh = fm.getAscent();
        g2.drawString(symbol, x + (diameter - sw) / 2, y + (diameter + sh) / 2 - 3);
    }
}
