package module_ui;

import module_core_logic.Expense;
import module_core_logic.ExpenseService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.Random;

/**
 * DashboardPanel — Differentiated Dashboard View.
 * • Casual peer greeting (random one-liner pool, no long philosophical quote)
 * • Orange offline badge removed entirely
 * • Dynamic recent-transaction count title
 * • Single "View All Transactions ->" link at top-right of table section
 */
public class DashboardPanel extends JPanel {

    private final ExpenseService expenseService;
    private final Runnable onAddExpenseCallback;
    private final Runnable onViewAllTransactionsCallback;

    private JLabel lblGreeting;
    private JLabel lblMonthSpent;
    private JLabel lblMonthCount;

    private JLabel sectionTitle;
    private JTable recentTable;
    private DefaultTableModel tableModel;

    // ── Casual one-liner greeting pool (plain text only – no emoji to avoid square box glyphs) ──
    private static final String[] GREETINGS = {
        "Welcome blud, track your cash.",
        "Hello dudee! Let's manage the bag.",
        "Lessgooo! Stay on top of your spends.",
        "Sup! Ready to save some bread?",
        "Yo! Time to check those numbers.",
        "Let's get it! Budget check incoming."
    };

    public DashboardPanel(ExpenseService expenseService,
                          Runnable onAddExpenseCallback,
                          Runnable onViewAllTransactionsCallback) {
        this.expenseService = expenseService;
        this.onAddExpenseCallback = onAddExpenseCallback;
        this.onViewAllTransactionsCallback = onViewAllTransactionsCallback;

        setBackground(AquaTheme.LIGHT_WASH);
        setLayout(new BorderLayout(0, 14));
        setBorder(AquaTheme.padding(16, 16, 16, 16));

        // ── 1. WELCOME HEADER CARD & METRICS ─────────────────────────────────
        JPanel headerCard = AquaTheme.createCardPanel();
        headerCard.setLayout(new BorderLayout(16, 0));

        // Left: casual greeting only (no badge, no philosophical quote)
        JPanel leftInfo = new JPanel();
        leftInfo.setLayout(new BoxLayout(leftInfo, BoxLayout.Y_AXIS));
        leftInfo.setOpaque(false);

        lblGreeting = new JLabel(randomGreeting());
        lblGreeting.setFont(new Font("Arial", Font.BOLD, 20));
        lblGreeting.setForeground(AquaTheme.DEEP_SLATE);

        leftInfo.add(lblGreeting);

        // Right: metric cards + CTA
        JPanel metricsContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        metricsContainer.setOpaque(false);

        lblMonthSpent = new JLabel("Rs.0.00", SwingConstants.CENTER);
        lblMonthSpent.setFont(new Font("Arial", Font.BOLD, 18));
        lblMonthSpent.setForeground(AquaTheme.ACTION_TEAL);

        lblMonthCount = new JLabel("0 Transactions", SwingConstants.CENTER);
        lblMonthCount.setFont(new Font("Arial", Font.BOLD, 18));
        lblMonthCount.setForeground(AquaTheme.DEEP_SLATE);

        JPanel cardSpent = createMetricCard("THIS MONTH SPENT", lblMonthSpent);
        JPanel cardCount = createMetricCard("Month-to-Date Count", lblMonthCount);

        JButton btnAdd = AquaTheme.createCyanButton("+ Add Expense");
        btnAdd.setPreferredSize(new Dimension(140, 42));
        btnAdd.addActionListener(e -> {
            if (onAddExpenseCallback != null) onAddExpenseCallback.run();
        });

        metricsContainer.add(cardSpent);
        metricsContainer.add(cardCount);
        metricsContainer.add(btnAdd);

        headerCard.add(leftInfo, BorderLayout.WEST);
        headerCard.add(metricsContainer, BorderLayout.EAST);
        add(headerCard, BorderLayout.NORTH);

        // ── 2. RECENT TRANSACTIONS SNAPSHOT ──────────────────────────────────
        JPanel snapshotCard = AquaTheme.createCardPanel();
        snapshotCard.setLayout(new BorderLayout(0, 10));

        JPanel snapshotHeader = new JPanel(new BorderLayout());
        snapshotHeader.setOpaque(false);

        sectionTitle = new JLabel("Recent Transactions", SwingConstants.LEFT);
        sectionTitle.setFont(AquaTheme.FONT_ARIAL_HEADER);
        sectionTitle.setForeground(AquaTheme.DEEP_SLATE);
        snapshotHeader.add(sectionTitle, BorderLayout.WEST);

        JButton btnViewAll = new JButton("View All Transactions ->");
        btnViewAll.setFont(AquaTheme.FONT_ARIAL_BUTTON);
        btnViewAll.setForeground(AquaTheme.ACTION_TEAL);
        btnViewAll.setFocusPainted(false);
        btnViewAll.setContentAreaFilled(false);
        btnViewAll.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        btnViewAll.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnViewAll.addActionListener(e -> {
            if (onViewAllTransactionsCallback != null) onViewAllTransactionsCallback.run();
        });
        snapshotHeader.add(btnViewAll, BorderLayout.EAST);
        snapshotCard.add(snapshotHeader, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Fav", "Date", "Category", "Payment Mode", "Amount (₹)", "Notes"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        recentTable = new JTable(tableModel);
        recentTable.setFont(AquaTheme.FONT_ARIAL_FIELD);
        recentTable.setRowHeight(32);
        recentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recentTable.setGridColor(new Color(230, 235, 230));

        JTableHeader header = recentTable.getTableHeader();
        header.setFont(AquaTheme.FONT_ARIAL_HEADER);
        header.setBackground(AquaTheme.STONE_PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 34));

        recentTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(
                        tbl, value, isSelected, hasFocus, row, col);
                if (isSelected) {
                    c.setBackground(new Color(0xD8, 0xE4, 0xDF));
                    c.setForeground(AquaTheme.TEXT_DARK);
                } else {
                    c.setBackground(row % 2 == 0 ? AquaTheme.CRISP_WHITE : AquaTheme.LIGHT_WASH);
                    c.setForeground(AquaTheme.TEXT_DARK);
                }
                setHorizontalAlignment(col == 0 || col == 1 || col == 2 || col == 5
                        ? SwingConstants.CENTER : SwingConstants.LEFT);
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(recentTable);
        scrollPane.getViewport().setBackground(AquaTheme.CRISP_WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(AquaTheme.SUBTLE_BORDER));
        snapshotCard.add(scrollPane, BorderLayout.CENTER);
        // No bottom button — table expands to fill the card naturally

        add(snapshotCard, BorderLayout.CENTER);

        refreshData();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static String randomGreeting() {
        return GREETINGS[new Random().nextInt(GREETINGS.length)];
    }

    private JPanel createMetricCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 2));
        card.setBackground(AquaTheme.CRISP_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AquaTheme.SUBTLE_BORDER, 1, true),
                AquaTheme.padding(6, 14, 6, 14)));
        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(AquaTheme.FONT_ARIAL_LABEL);
        titleLbl.setForeground(AquaTheme.HOVER_SLATE);
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    // ── Public refresh ────────────────────────────────────────────────────────
    public void refreshData() {
        double monthSpent = expenseService.getThisMonthSpent();
        int    monthCount = expenseService.getThisMonthTransactionCount();

        lblMonthSpent.setText(String.format("Rs.%.2f", monthSpent));
        lblMonthCount.setText(monthCount + " MTD");

        // Latest 5 active expenses
        tableModel.setRowCount(0);
        List<Expense> active = expenseService.getActiveExpenses();
        int limit = Math.min(5, active.size());
        for (int i = 0; i < limit; i++) {
            Expense exp = active.get(i);
            tableModel.addRow(new Object[]{
                    exp.getId(),
                    exp.isStarred() ? "(*)" : "( )",
                    exp.getExpenseDate(),
                    exp.getCategoryName()    != null ? exp.getCategoryName()    : "N/A",
                    exp.getPaymentModeName() != null ? exp.getPaymentModeName() : "N/A",
                    String.format("%.2f", exp.getAmount()),
                    exp.getNotes()           != null ? exp.getNotes()           : ""
            });
        }

        // Dynamic section title
        if (sectionTitle != null) {
            sectionTitle.setText(limit == 0
                    ? "Recent Transactions (None)"
                    : "Recent Transactions (" + limit + ")");
        }
    }
}
