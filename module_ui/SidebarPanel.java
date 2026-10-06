package module_ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * WhatsApp-style vertical sidebar.
 * Tabs: Dashboard | Transactions | Analytics | Debts | Trash
 */
public class SidebarPanel extends JPanel {

    public interface TabChangeListener { void onTabChanged(String tabName); }

    private static final String[] TABS = {"Dashboard", "Transactions", "Categories", "Analytics", "Debts & Loans", "Trash"};

    private int selectedTab = 0;
    private TabChangeListener listener;
    private JButton[] tabButtons;

    public SidebarPanel() {
        setPreferredSize(new Dimension(200, 0));
        setBackground(AquaTheme.DEEP_SLATE);
        setLayout(new BorderLayout());

        // ── Header (logo + title) ──────────────────────────────────────
        JPanel header = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                // Coin logo
                AquaTheme.drawCoinLogo(g2, 10, 12, 44);
                g2.dispose();
            }
        };
        header.setPreferredSize(new Dimension(200, 70));
        header.setBackground(AquaTheme.DEEP_SLATE);
        header.setLayout(new FlowLayout(FlowLayout.LEFT, 60, 18));

        JLabel brandLabel = new JLabel("SixSeven");
        brandLabel.setFont(AquaTheme.getJosefinSlabFont(Font.BOLD, 24f));
        brandLabel.setForeground(AquaTheme.GOLD);
        header.add(brandLabel);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x5A, 0x5D, 0x5A));
        sep.setBackground(new Color(0x5A, 0x5D, 0x5A));

        // ── Navigation buttons ─────────────────────────────────────────
        JPanel navPanel = new JPanel();
        navPanel.setBackground(AquaTheme.DEEP_SLATE);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        tabButtons = new JButton[TABS.length];
        for (int i = 0; i < TABS.length; i++) {
            final int idx = i;
            tabButtons[i] = createTabButton(TABS[i], i);
            tabButtons[i].addActionListener(e -> selectTab(idx));
            navPanel.add(tabButtons[i]);
            navPanel.add(Box.createRigidArea(new Dimension(0, 2)));
        }

        // ── Footer (DB status) ─────────────────────────────────────────
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(AquaTheme.DEEP_SLATE);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        JLabel dbLabel = new JLabel("  Oracle DB Connected", JLabel.LEFT);
        dbLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        dbLabel.setForeground(AquaTheme.SUCCESS_GREEN);
        footer.add(dbLabel, BorderLayout.SOUTH);

        add(header,   BorderLayout.NORTH);
        add(sep,      BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane(navPanel);
        scroll.setBorder(null);
        scroll.setBackground(AquaTheme.DEEP_SLATE);
        scroll.getViewport().setBackground(AquaTheme.DEEP_SLATE);
        add(scroll,   BorderLayout.CENTER);
        add(footer,   BorderLayout.SOUTH);

        selectTab(0);
    }

    private JButton createTabButton(String text, int idx) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (selectedTab == idx) {
                    g2.setColor(AquaTheme.ACTIVE_TAB_BG);
                    g2.fillRoundRect(6, 2, getWidth() - 12, getHeight() - 4, 10, 10);
                    // Left accent bar
                    g2.setColor(AquaTheme.GOLD);
                    g2.fillRoundRect(6, 2, 4, getHeight() - 4, 4, 4);
                } else if (getModel().isRollover()) {
                    g2.setColor(AquaTheme.ACTIVE_TAB_BG);
                    g2.fillRoundRect(6, 2, getWidth() - 12, getHeight() - 4, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Arial", Font.PLAIN, 13));
        btn.setForeground(AquaTheme.LIGHT_WASH);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 10));
        btn.setMaximumSize(new Dimension(200, 46));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void selectTab(int idx) {
        selectedTab = idx;
        for (JButton b : tabButtons) b.repaint();
        if (listener != null) listener.onTabChanged(TABS[idx]);
    }

    public void setTabChangeListener(TabChangeListener l) { this.listener = l; }
    public String getSelectedTab() { return TABS[selectedTab]; }

    public void selectTabByName(String name) {
        for (int i = 0; i < TABS.length; i++) {
            if (TABS[i].equalsIgnoreCase(name)) { selectTab(i); return; }
        }
    }
}
