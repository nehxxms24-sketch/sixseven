package module_ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * LoginFrame — Stone Grey / ₹500 Note Theme.
 * Authentication screen for "SixSeven" with:
 * - Deep Slate → Stone Primary gradient background
 * - Coin logo via Graphics2D in the card header
 * - Cursive "SixSeven" brand title
 * - Muted Emerald CTA login button
 */
public class LoginFrame extends JFrame {

    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JLabel lblStatus;
    private Point mouseClickPoint;

    public LoginFrame() {
        setTitle("SixSeven");
        setUndecorated(true);
        setSize(480, 560);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // ── Background: Deep Slate → Stone Grey gradient ─────────────
        JPanel mainPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, AquaTheme.DEEP_SLATE, 0, getHeight(), AquaTheme.STONE_PRIMARY);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        mainPanel.setLayout(new BorderLayout());

        // ── Draggable header bar ──────────────────────────────────────
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);
        headerBar.setPreferredSize(new Dimension(480, 36));

        JButton closeBtn = new JButton("x");
        closeBtn.setFont(new Font("Arial", Font.BOLD, 13));
        closeBtn.setForeground(new Color(0xCC, 0xCC, 0xCC));
        closeBtn.setFocusPainted(false);
        closeBtn.setContentAreaFilled(false);
        closeBtn.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 16));
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addActionListener(e -> System.exit(0));
        closeBtn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { closeBtn.setForeground(AquaTheme.DANGER_RED); }
            public void mouseExited(MouseEvent e)  { closeBtn.setForeground(new Color(0xCC, 0xCC, 0xCC)); }
        });

        headerBar.add(closeBtn, BorderLayout.EAST);
        headerBar.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) { mouseClickPoint = e.getPoint(); }
        });
        headerBar.addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                Point cur = e.getLocationOnScreen();
                setLocation(cur.x - mouseClickPoint.x, cur.y - mouseClickPoint.y);
            }
        });
        mainPanel.add(headerBar, BorderLayout.NORTH);

        // ── White card ───────────────────────────────────────────────
        JPanel cardPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Shadow
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(4, 4, getWidth() - 8, getHeight() - 8, 18, 18);
                // Card body
                g2.setColor(AquaTheme.CRISP_WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, 18, 18);
                // Subtle border
                g2.setColor(AquaTheme.SUBTLE_BORDER);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth() - 6, getHeight() - 6, 18, 18);
                g2.dispose();
            }
        };
        cardPanel.setOpaque(false);
        cardPanel.setPreferredSize(new Dimension(420, 480));
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(AquaTheme.padding(28, 38, 28, 38));

        // ── Logo row: coin + brand title ─────────────────────────────
        JPanel logoRow = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                AquaTheme.drawCoinLogo(g2, 0, (getHeight() - 48) / 2, 48);
                g2.dispose();
            }
        };
        logoRow.setOpaque(false);
        logoRow.setLayout(new FlowLayout(FlowLayout.CENTER, 14, 4));
        logoRow.setMaximumSize(new Dimension(380, 60));

        // Spacer for coin
        JPanel coinSpacer = new JPanel();
        coinSpacer.setOpaque(false);
        coinSpacer.setPreferredSize(new Dimension(52, 52));

        JLabel logoLbl = new JLabel("SixSeven");
        logoLbl.setFont(AquaTheme.getJosefinSlabFont(Font.BOLD, 30f));
        logoLbl.setForeground(AquaTheme.DEEP_SLATE);

        logoRow.add(coinSpacer);
        logoRow.add(logoLbl);
        logoRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("Personal Expense System", SwingConstants.CENTER);
        subLbl.setFont(AquaTheme.FONT_ARIAL_SUB);
        subLbl.setForeground(AquaTheme.HOVER_SLATE);
        subLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Separator ─────────────────────────────────────────────────
        JSeparator sep = new JSeparator();
        sep.setForeground(AquaTheme.SUBTLE_BORDER);
        sep.setBackground(AquaTheme.SUBTLE_BORDER);
        sep.setMaximumSize(new Dimension(380, 1));

        // ── Form fields ───────────────────────────────────────────────
        JLabel uLabel = new JLabel("Username");
        uLabel.setFont(AquaTheme.FONT_ARIAL_LABEL);
        uLabel.setForeground(AquaTheme.TEXT_DARK);
        uLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtUsername = new JTextField();
        txtUsername.setFont(AquaTheme.FONT_ARIAL_FIELD);
        txtUsername.setMaximumSize(new Dimension(380, 40));
        txtUsername.setBorder(new CompoundBorder(
                new LineBorder(AquaTheme.SUBTLE_BORDER, 1, true),
                new EmptyBorder(7, 11, 7, 11)));

        JLabel pLabel = new JLabel("Password");
        pLabel.setFont(AquaTheme.FONT_ARIAL_LABEL);
        pLabel.setForeground(AquaTheme.TEXT_DARK);
        pLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtPassword = new JPasswordField();
        txtPassword.setFont(AquaTheme.FONT_ARIAL_FIELD);
        txtPassword.setMaximumSize(new Dimension(380, 40));
        txtPassword.setBorder(new CompoundBorder(
                new LineBorder(AquaTheme.SUBTLE_BORDER, 1, true),
                new EmptyBorder(7, 11, 7, 11)));

        lblStatus = new JLabel(" ", SwingConstants.CENTER);
        lblStatus.setFont(AquaTheme.FONT_ARIAL_SUB);
        lblStatus.setForeground(AquaTheme.DANGER_RED);
        lblStatus.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnLogin = AquaTheme.createCyanButton("LOGIN");
        btnLogin.setMaximumSize(new Dimension(380, 44));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> attemptLogin());

        txtPassword.addActionListener(e -> attemptLogin());
        txtUsername.addActionListener(e -> attemptLogin());

        JLabel hintLbl = new JLabel("Hint: sixseven / 12345", SwingConstants.CENTER);
        hintLbl.setFont(new Font("Arial", Font.ITALIC, 11));
        hintLbl.setForeground(new Color(0xA0, 0xA8, 0xA4));
        hintLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Assemble ─────────────────────────────────────────────────
        cardPanel.add(Box.createVerticalStrut(6));
        cardPanel.add(logoRow);
        cardPanel.add(subLbl);
        cardPanel.add(Box.createVerticalStrut(14));
        cardPanel.add(sep);
        cardPanel.add(Box.createVerticalStrut(18));
        cardPanel.add(uLabel);
        cardPanel.add(Box.createVerticalStrut(6));
        cardPanel.add(txtUsername);
        cardPanel.add(Box.createVerticalStrut(14));
        cardPanel.add(pLabel);
        cardPanel.add(Box.createVerticalStrut(6));
        cardPanel.add(txtPassword);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(lblStatus);
        cardPanel.add(Box.createVerticalStrut(14));
        cardPanel.add(btnLogin);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(hintLbl);

        JPanel outerWrapper = new JPanel(new GridBagLayout());
        outerWrapper.setOpaque(false);
        outerWrapper.add(cardPanel);
        mainPanel.add(outerWrapper, BorderLayout.CENTER);

        setContentPane(mainPanel);
    }

    private void attemptLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setForeground(AquaTheme.DANGER_RED);
            lblStatus.setText("Please enter both username and password.");
            shakeWindow();
            return;
        }

        if ("sixseven".equalsIgnoreCase(username) && "12345".equals(password)) {
            lblStatus.setForeground(AquaTheme.SUCCESS_GREEN);
            lblStatus.setText("Access Granted! Loading dashboard...");
            Timer timer = new Timer(450, e -> {
                dispose();
                SwingUtilities.invokeLater(() -> new MainDashboardFrame().setVisible(true));
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            lblStatus.setForeground(AquaTheme.DANGER_RED);
            lblStatus.setText("Invalid credentials. Please try again.");
            shakeWindow();
        }
    }

    private void shakeWindow() {
        final Point original = getLocation();
        new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    setLocation(original.x + 9, original.y);
                    Thread.sleep(28);
                    setLocation(original.x - 9, original.y);
                    Thread.sleep(28);
                }
                setLocation(original);
            } catch (InterruptedException ignored) {}
        }).start();
    }
}
