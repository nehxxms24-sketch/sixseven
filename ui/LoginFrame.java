package ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * LoginFrame – Application login gate.
 * Credentials: username = sixseven, password = 12345
 * Opens ExpenseManagerFrame on success.
 */
public class LoginFrame extends JFrame {

    // ── Aquatic Blue + Off-White Palette ─────────────────────────────────────
    private static final Color BG_MAIN       = new Color(0xE0F4F8);   // off-white aqua
    private static final Color DEEP_BLUE     = new Color(0x03045E);   // deep navy
    private static final Color OCEAN_BLUE    = new Color(0x0077B6);   // primary blue
    private static final Color AQUA_BRIGHT   = new Color(0x00B4D8);   // bright aqua
    private static final Color AQUA_LIGHT    = new Color(0x90E0EF);   // light aqua
    private static final Color OFF_WHITE     = new Color(0xF8FFFE);   // off-white
    private static final Color TEXT_DARK     = new Color(0x03045E);   // dark navy text
    private static final Color TEXT_MUTED    = new Color(0x4A6FA5);   // muted blue
    private static final Color DANGER        = new Color(0xEF476F);   // error red

    private static final Font FONT_TITLE  = new Font("SansSerif", Font.BOLD, 26);
    private static final Font FONT_SUB    = new Font("SansSerif", Font.ITALIC, 13);
    private static final Font FONT_LABEL  = new Font("SansSerif", Font.BOLD, 13);
    private static final Font FONT_INPUT  = new Font("SansSerif", Font.PLAIN, 14);
    private static final Font FONT_BTN    = new Font("SansSerif", Font.BOLD, 14);

    private static final String VALID_USER = "sixseven";
    private static final String VALID_PASS = "12345";

    private JTextField     tfUser;
    private JPasswordField pfPass;
    private JLabel         lblError;

    public LoginFrame() {
        super("Expense Manager – Login");
        setUndecorated(true);                   // frameless window
        setSize(440, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setBackground(new Color(0, 0, 0, 0));   // transparent root
        add(buildPanel());
        setVisible(true);
    }

    // ── Main panel ────────────────────────────────────────────────────────────
    private JPanel buildPanel() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                // gradient background
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(0xCAF0F8),
                        getWidth(), getHeight(), new Color(0x90E0EF));
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                g2.dispose();
            }
        };
        root.setOpaque(false);
        root.setBorder(new EmptyBorder(0, 0, 0, 0));

        root.add(buildHeader(),  BorderLayout.NORTH);
        root.add(buildCard(),    BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        // Allow dragging frameless window
        addDragSupport(root);
        return root;
    }

    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new GridBagLayout());
        hdr.setOpaque(false);
        hdr.setBorder(new EmptyBorder(36, 0, 10, 0));

        JLabel icon = new JLabel("💰", SwingConstants.CENTER);
        icon.setFont(new Font("SansSerif", Font.PLAIN, 52));

        JLabel title = new JLabel("Expense Manager", SwingConstants.CENTER);
        title.setFont(FONT_TITLE);
        title.setForeground(DEEP_BLUE);

        JLabel sub = new JLabel("Personal Finance Tracker", SwingConstants.CENTER);
        sub.setFont(FONT_SUB);
        sub.setForeground(OCEAN_BLUE);

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.gridy = 0; g.insets = new Insets(0,0,4,0);
        hdr.add(icon, g);
        g.gridy = 1; hdr.add(title, g);
        g.gridy = 2; g.insets = new Insets(4,0,0,0);
        hdr.add(sub, g);
        return hdr;
    }

    private JPanel buildCard() {
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 210));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.setColor(new Color(0x00B4D8));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(28, 36, 28, 36));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(8, 0, 8, 0);

        // ── Username label ────────────────────────────────────────────────────
        g.gridy = 0;
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(FONT_LABEL);
        lblUser.setForeground(TEXT_DARK);
        card.add(lblUser, g);

        // ── Username field ────────────────────────────────────────────────────
        g.gridy = 1; g.insets = new Insets(0, 0, 14, 0);
        tfUser = styledField(22);
        card.add(tfUser, g);

        // ── Password label ────────────────────────────────────────────────────
        g.gridy = 2; g.insets = new Insets(8, 0, 8, 0);
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(FONT_LABEL);
        lblPass.setForeground(TEXT_DARK);
        card.add(lblPass, g);

        // ── Password field ────────────────────────────────────────────────────
        g.gridy = 3; g.insets = new Insets(0, 0, 8, 0);
        pfPass = new JPasswordField(22);
        pfPass.setFont(FONT_INPUT);
        pfPass.setBackground(OFF_WHITE);
        pfPass.setForeground(TEXT_DARK);
        pfPass.setCaretColor(OCEAN_BLUE);
        pfPass.setBorder(new CompoundBorder(
                new LineBorder(AQUA_BRIGHT, 2, true),
                new EmptyBorder(8, 12, 8, 12)));
        card.add(pfPass, g);

        // ── Error label ───────────────────────────────────────────────────────
        g.gridy = 4; g.insets = new Insets(0, 0, 4, 0);
        lblError = new JLabel(" ");
        lblError.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblError.setForeground(DANGER);
        lblError.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblError, g);

        // ── Login button ──────────────────────────────────────────────────────
        g.gridy = 5; g.insets = new Insets(10, 0, 0, 0);
        JButton btnLogin = loginButton();
        btnLogin.addActionListener(e -> attemptLogin());
        card.add(btnLogin, g);

        // Enter key triggers login
        pfPass.addActionListener(e -> attemptLogin());
        tfUser.addActionListener(e -> pfPass.requestFocus());

        // Wrap in margin panel
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(new EmptyBorder(10, 30, 20, 30));
        wrapper.add(card, new GridBagConstraints());
        return wrapper;
    }

    private JPanel buildFooter() {
        JPanel foot = new JPanel(new FlowLayout(FlowLayout.CENTER));
        foot.setOpaque(false);
        foot.setBorder(new EmptyBorder(0, 0, 16, 0));

        // Close button
        JLabel closeBtn = new JLabel("✕  Exit");
        closeBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
        closeBtn.setForeground(TEXT_MUTED);
        closeBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { System.exit(0); }
            @Override public void mouseEntered(MouseEvent e) { closeBtn.setForeground(DANGER); }
            @Override public void mouseExited(MouseEvent e)  { closeBtn.setForeground(TEXT_MUTED); }
        });
        foot.add(closeBtn);
        return foot;
    }

    // ── Login logic ───────────────────────────────────────────────────────────
    private void attemptLogin() {
        String user = tfUser.getText().trim();
        String pass = new String(pfPass.getPassword());

        if (user.equals(VALID_USER) && pass.equals(VALID_PASS)) {
            dispose();
            SwingUtilities.invokeLater(ExpenseManagerFrame::new);
        } else {
            lblError.setText("❌  Invalid username or password.");
            pfPass.setText("");
            pfPass.requestFocus();

            // Shake animation
            Point p = getLocationOnScreen();
            Timer t = new Timer(40, null);
            final int[] step = {0};
            final int[] dx = {-8, 8, -6, 6, -4, 4, -2, 2, 0};
            t.addActionListener(e -> {
                if (step[0] < dx.length) {
                    setLocation(p.x + dx[step[0]], p.y);
                    step[0]++;
                } else {
                    ((Timer)e.getSource()).stop();
                    setLocation(p);
                }
            });
            t.start();
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private JTextField styledField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(FONT_INPUT);
        tf.setBackground(OFF_WHITE);
        tf.setForeground(TEXT_DARK);
        tf.setCaretColor(OCEAN_BLUE);
        tf.setBorder(new CompoundBorder(
                new LineBorder(AQUA_BRIGHT, 2, true),
                new EmptyBorder(8, 12, 8, 12)));
        return tf;
    }

    private JButton loginButton() {
        JButton btn = new JButton("  Sign In  →") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp;
                if (getModel().isPressed()) {
                    gp = new GradientPaint(0,0,DEEP_BLUE,getWidth(),0,OCEAN_BLUE);
                } else if (getModel().isRollover()) {
                    gp = new GradientPaint(0,0,AQUA_BRIGHT,getWidth(),0,OCEAN_BLUE);
                } else {
                    gp = new GradientPaint(0,0,OCEAN_BLUE,getWidth(),0,AQUA_BRIGHT);
                }
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(OFF_WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(12, 0, 12, 0));
        return btn;
    }

    /** Makes an undecorated window draggable. */
    private void addDragSupport(JComponent comp) {
        final Point[] drag = {null};
        comp.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { drag[0] = e.getPoint(); }
        });
        comp.addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - drag[0].x,
                            loc.y + e.getY() - drag[0].y);
            }
        });
    }
}
