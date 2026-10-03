package module_ui;

import module_core_logic.Expense;
import module_core_logic.ExpenseService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.List;

/**
 * TrendsChartPanel — Simplified "View By" drill-down.
 *
 * Control bar:
 *   View By: [ Year-wise (All Months) | Month-wise (Daily) ]
 *   Year Selector (always active, defaults to current year)
 *   Month Selector (only enabled in Month-wise mode)
 *
 * Year-wise: 12-bar chart Jan–Dec, totals for that year.
 * Month-wise: N-bar chart day-1 to day-N, totals per day for that month/year.
 *
 * Period comparison cards: Total Spent | Total Lent | Total Borrowed
 * (filtered strictly by the selected period).
 */
public class TrendsChartPanel extends JPanel {

    private final ExpenseService expenseService;

    // ── Controls ─────────────────────────────────────────────────────────────
    private JComboBox<String> cbViewBy;
    private JComboBox<String> cbYear;
    private JComboBox<String> cbMonth;

    // ── Summary labels ────────────────────────────────────────────────────────
    private JLabel lblTotalSpent;
    private JLabel lblTotalLent;
    private JLabel lblTotalBorrowed;

    // ── Chart canvas container ────────────────────────────────────────────────
    private JPanel chartCanvasPanel;

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final String VIEW_YEARLY  = "Year-wise (All Months)";
    private static final String VIEW_MONTHLY = "Month-wise (Daily)";

    private static final String[] VIEW_OPTIONS = { VIEW_YEARLY, VIEW_MONTHLY };

    private static final String[] MONTH_NAMES = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    private static final String[] MONTH_SHORT = {
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    };

    // Stone-grey bar palette
    private static final Color[] BAR_COLORS = {
        new Color(0x4A, 0x4D, 0x4A),
        new Color(0x4E, 0x53, 0x4E),
        new Color(0x54, 0x58, 0x54),
        new Color(0x60, 0x65, 0x60),
        new Color(0x3A, 0x3C, 0x3A),
        new Color(0x70, 0x75, 0x70),
        new Color(0x2B, 0x2D, 0x2B),
        new Color(0x80, 0x85, 0x80)
    };

    // ── Constructor ───────────────────────────────────────────────────────────
    public TrendsChartPanel(ExpenseService expenseService) {
        this.expenseService = expenseService;

        setBackground(AquaTheme.CRISP_WHITE);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AquaTheme.SUBTLE_BORDER, 1, true),
                AquaTheme.padding(16, 16, 16, 16)
        ));

        // ── PAGE HEADER ───────────────────────────────────────────────────────
        JLabel titleLbl = new JLabel("Trends & Analytics", SwingConstants.LEFT);
        titleLbl.setFont(AquaTheme.FONT_HEADER_TITLE);
        titleLbl.setForeground(AquaTheme.DEEP_SLATE);
        titleLbl.setBorder(AquaTheme.padding(0, 0, 8, 0));

        // ── CONTROL BAR ───────────────────────────────────────────────────────
        JPanel controlBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        controlBar.setBackground(AquaTheme.LIGHT_WASH);
        controlBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AquaTheme.SUBTLE_BORDER, 1, true),
                AquaTheme.padding(6, 12, 6, 12)
        ));

        // View By selector
        JLabel viewByLbl = new JLabel("View By:");
        viewByLbl.setFont(AquaTheme.FONT_ARIAL_LABEL);
        cbViewBy = new JComboBox<>(VIEW_OPTIONS);
        cbViewBy.setFont(AquaTheme.FONT_ARIAL_FIELD);

        // Year selector – dynamically built, current year down to 2020, no future years
        int currentYear = LocalDate.now().getYear();
        String[] years = MonthYearPickerDialog.buildYearArray(); // e.g. ["2026","2025",...,"2020"]
        JLabel yearLbl = new JLabel("Year:");
        yearLbl.setFont(AquaTheme.FONT_ARIAL_LABEL);
        cbYear = new JComboBox<>(years);
        cbYear.setFont(AquaTheme.FONT_ARIAL_FIELD);
        cbYear.setSelectedItem(String.valueOf(currentYear)); // always default to now

        // Month selector – only relevant for Month-wise
        JLabel monthLbl = new JLabel("Month:");
        monthLbl.setFont(AquaTheme.FONT_ARIAL_LABEL);
        cbMonth = new JComboBox<>(MONTH_NAMES);
        cbMonth.setFont(AquaTheme.FONT_ARIAL_FIELD);
        cbMonth.setSelectedIndex(LocalDate.now().getMonthValue() - 1); // default current month

        controlBar.add(viewByLbl);
        controlBar.add(cbViewBy);
        controlBar.add(yearLbl);
        controlBar.add(cbYear);
        controlBar.add(monthLbl);
        controlBar.add(cbMonth);

        // Sync month enabled/disabled
        cbViewBy.addActionListener(e -> {
            boolean isMonthly = VIEW_MONTHLY.equals(cbViewBy.getSelectedItem());
            cbMonth.setEnabled(isMonthly);
            monthLbl.setEnabled(isMonthly);
            updateAnalyticsView();
        });
        cbYear.addActionListener(e -> updateAnalyticsView());
        cbMonth.addActionListener(e -> updateAnalyticsView());

        // Calendar picker button
        JButton btnPickDate = new JButton("[Cal] Pick Month/Year");
        btnPickDate.setFont(AquaTheme.FONT_ARIAL_BUTTON);
        btnPickDate.setForeground(AquaTheme.DEEP_SLATE);
        btnPickDate.setFocusPainted(false);
        btnPickDate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPickDate.addActionListener(e -> {
            int curMonth = cbMonth.getSelectedIndex() + 1;
            int curYear;
            try { curYear = Integer.parseInt((String) cbYear.getSelectedItem()); }
            catch (Exception ex) { curYear = LocalDate.now().getYear(); }
            MonthYearPickerDialog picker = new MonthYearPickerDialog(
                    SwingUtilities.getWindowAncestor(TrendsChartPanel.this), curMonth, curYear,
                    (pickedMonth, pickedYear) -> {
                        cbViewBy.setSelectedItem(VIEW_MONTHLY);
                        cbMonth.setSelectedIndex(pickedMonth - 1);
                        cbYear.setSelectedItem(String.valueOf(pickedYear));
                        cbMonth.setEnabled(true);
                        updateAnalyticsView();
                    }
            );
            picker.setVisible(true);
        });
        controlBar.add(btnPickDate);

        // Initially disable month for year-wise view
        cbMonth.setEnabled(false);
        monthLbl.setEnabled(false);

        // Assemble top panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBackground(AquaTheme.CRISP_WHITE);
        topPanel.add(titleLbl);
        topPanel.add(Box.createVerticalStrut(6));
        topPanel.add(controlBar);
        add(topPanel, BorderLayout.NORTH);

        // ── CHART CANVAS ──────────────────────────────────────────────────────
        chartCanvasPanel = new JPanel(new BorderLayout());
        chartCanvasPanel.setBackground(AquaTheme.CRISP_WHITE);
        add(chartCanvasPanel, BorderLayout.CENTER);

        // ── PERIOD COMPARISON CARDS ───────────────────────────────────────────
        JPanel summaryRow = new JPanel(new GridLayout(1, 3, 14, 0));
        summaryRow.setBackground(AquaTheme.LIGHT_WASH);
        summaryRow.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AquaTheme.SUBTLE_BORDER, 1, true),
                AquaTheme.padding(10, 14, 10, 14)
        ));

        lblTotalSpent = new JLabel("Rs.0.00", SwingConstants.CENTER);
        lblTotalSpent.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotalSpent.setForeground(AquaTheme.ACTION_TEAL);

        lblTotalLent = new JLabel("Rs.0.00", SwingConstants.CENTER);
        lblTotalLent.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotalLent.setForeground(new Color(0x27, 0xAE, 0x60));

        lblTotalBorrowed = new JLabel("Rs.0.00", SwingConstants.CENTER);
        lblTotalBorrowed.setFont(new Font("Arial", Font.BOLD, 16));
        lblTotalBorrowed.setForeground(new Color(0xC0, 0x39, 0x2B));

        summaryRow.add(createMiniCard("TOTAL SPENT",    lblTotalSpent));
        summaryRow.add(createMiniCard("TOTAL LENT",     lblTotalLent));
        summaryRow.add(createMiniCard("TOTAL BORROWED", lblTotalBorrowed));

        add(summaryRow, BorderLayout.SOUTH);

        // Initial render
        updateAnalyticsView();
    }

    // ── Public refresh hook ───────────────────────────────────────────────────
    public void refreshAnalytics() {
        updateAnalyticsView();
    }

    // ── Core update logic ─────────────────────────────────────────────────────
    private void updateAnalyticsView() {
        chartCanvasPanel.removeAll();

        String mode   = (String) cbViewBy.getSelectedItem();
        int    year   = Integer.parseInt((String) cbYear.getSelectedItem());
        int    month  = cbMonth.getSelectedIndex() + 1; // 1-12

        List<Expense> active = expenseService.getActiveExpenses();

        if (VIEW_YEARLY.equals(mode)) {
            // ── Year-wise: 12 monthly totals ──────────────────────────────────
            double[] monthlyTotals = new double[12];
            double   periodSpent   = 0.0;

            for (Expense e : active) {
                if (e.getExpenseDate() != null) {
                    try {
                        LocalDate d = LocalDate.parse(e.getExpenseDate());
                        if (d.getYear() == year) {
                            monthlyTotals[d.getMonthValue() - 1] += e.getAmount();
                            periodSpent += e.getAmount();
                        }
                    } catch (Exception ignored) {}
                }
            }

            lblTotalSpent.setText(String.format("Rs.%.2f", periodSpent));
            lblTotalLent.setText(String.format("Rs.%.2f",
                    expenseService.getDebtLentForYear(year)));
            lblTotalBorrowed.setText(String.format("Rs.%.2f",
                    expenseService.getDebtBorrowedForYear(year)));

            chartCanvasPanel.add(createYearlyCanvas(monthlyTotals, year), BorderLayout.CENTER);

        } else {
            // ── Month-wise: daily totals for that month ───────────────────────
            YearMonth ym       = YearMonth.of(year, month);
            int       daysInMonth = ym.lengthOfMonth();
            double[]  dailyTotals = new double[daysInMonth];
            double    periodSpent = 0.0;

            for (Expense e : active) {
                if (e.getExpenseDate() != null) {
                    try {
                        LocalDate d = LocalDate.parse(e.getExpenseDate());
                        if (d.getYear() == year && d.getMonthValue() == month) {
                            dailyTotals[d.getDayOfMonth() - 1] += e.getAmount();
                            periodSpent += e.getAmount();
                        }
                    } catch (Exception ignored) {}
                }
            }

            lblTotalSpent.setText(String.format("Rs.%.2f", periodSpent));
            lblTotalLent.setText(String.format("Rs.%.2f",
                    expenseService.getDebtLentForPeriod(month, year)));
            lblTotalBorrowed.setText(String.format("Rs.%.2f",
                    expenseService.getDebtBorrowedForPeriod(month, year)));

            String subtitle = MONTH_NAMES[month - 1] + " " + year;
            chartCanvasPanel.add(createDailyCanvas(dailyTotals, subtitle), BorderLayout.CENTER);
        }

        chartCanvasPanel.revalidate();
        chartCanvasPanel.repaint();
    }

    // ── Year-wise canvas: 12 bars, labelled Jan–Dec ───────────────────────────
    private JPanel createYearlyCanvas(double[] monthlyTotals, int year) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int W = getWidth(), H = getHeight();
                if (W <= 0 || H <= 0) { g2.dispose(); return; }

                double maxVal = 1.0;
                for (double v : monthlyTotals) if (v > maxVal) maxVal = v;

                int pad = 58, bottomPad = 72;
                int cW = W - 2 * pad, cH = H - pad - bottomPad;
                int n  = 12;

                drawAxes(g2, pad, bottomPad, W, H, cW, cH, maxVal);

                int barW  = Math.max(18, (cW - (n + 1) * 10) / n);
                int barGap = Math.max(6,  (cW - n * barW)    / (n + 1));

                for (int i = 0; i < 12; i++) {
                    double amt = monthlyTotals[i];
                    int bH = (int) (amt / maxVal * cH);
                    int x  = pad + barGap + i * (barW + barGap);
                    int y  = H - bottomPad - bH;

                    Color c = BAR_COLORS[i % BAR_COLORS.length];
                    drawBar(g2, x, y, barW, bH, c);

                    // Amount label above bar
                    if (amt > 0) {
                        g2.setColor(AquaTheme.TEXT_DARK);
                        g2.setFont(new Font("Arial", Font.BOLD, 9));
                        String vStr = String.format("%.0f", amt);
                        int sw = g2.getFontMetrics().stringWidth(vStr);
                        g2.drawString(vStr, x + (barW - sw) / 2, Math.max(pad + 10, y - 3));
                    }

                    // X-axis label (month abbreviation)
                    g2.setFont(new Font("Arial", Font.BOLD, 10));
                    g2.setColor(AquaTheme.STONE_PRIMARY);
                    String lbl = MONTH_SHORT[i];
                    int lw = g2.getFontMetrics().stringWidth(lbl);
                    g2.drawString(lbl, x + (barW - lw) / 2, H - bottomPad + 16);
                }

                // Chart title
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.setColor(AquaTheme.DEEP_SLATE);
                String title = "Monthly Spending Trend — " + year;
                g2.drawString(title, pad + 4, pad - 6);

                g2.dispose();
            }
        };
    }

    // ── Month-wise canvas: day-1 to day-N bars ────────────────────────────────
    private JPanel createDailyCanvas(double[] dailyTotals, String subtitle) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int W = getWidth(), H = getHeight();
                if (W <= 0 || H <= 0) { g2.dispose(); return; }

                double maxVal = 1.0;
                for (double v : dailyTotals) if (v > maxVal) maxVal = v;

                int pad = 58, bottomPad = 72;
                int cW = W - 2 * pad, cH = H - pad - bottomPad;
                int n  = dailyTotals.length;

                drawAxes(g2, pad, bottomPad, W, H, cW, cH, maxVal);

                int barW  = Math.max(10, (cW - (n + 1) * 4) / n);
                int barGap = Math.max(3,  (cW - n * barW)   / (n + 1));

                for (int i = 0; i < n; i++) {
                    double amt = dailyTotals[i];
                    int bH = (int) (amt / maxVal * cH);
                    int x  = pad + barGap + i * (barW + barGap);
                    int y  = H - bottomPad - bH;

                    Color c = (amt > 0) ? BAR_COLORS[i % BAR_COLORS.length]
                                        : new Color(0xD8, 0xD8, 0xD8);
                    drawBar(g2, x, y, barW, Math.max(bH, 2), c);

                    // Amount label only if there's space and the bar has a value
                    if (amt > 0 && barW >= 14) {
                        g2.setColor(AquaTheme.TEXT_DARK);
                        g2.setFont(new Font("Arial", Font.BOLD, 8));
                        String vStr = String.format("%.0f", amt);
                        int sw = g2.getFontMetrics().stringWidth(vStr);
                        if (sw <= barW) {
                            g2.drawString(vStr, x + (barW - sw) / 2, Math.max(pad + 10, y - 2));
                        }
                    }

                    // X-axis day label — print every day if bar is wide enough, else every 5th
                    int dayNum = i + 1;
                    boolean printLabel = (barW >= 14) || (dayNum == 1) || (dayNum % 5 == 0) || (dayNum == n);
                    if (printLabel) {
                        g2.setFont(new Font("Arial", Font.PLAIN, 9));
                        g2.setColor(AquaTheme.STONE_PRIMARY);
                        String lbl = String.valueOf(dayNum);
                        int lw = g2.getFontMetrics().stringWidth(lbl);
                        g2.drawString(lbl, x + (barW - lw) / 2, H - bottomPad + 14);
                    }
                }

                // Chart title
                g2.setFont(new Font("Arial", Font.BOLD, 12));
                g2.setColor(AquaTheme.DEEP_SLATE);
                String title = "Daily Spending Trend — " + subtitle;
                g2.drawString(title, pad + 4, pad - 6);

                g2.dispose();
            }
        };
    }

    // ── Shared: draw axes & Y-grid ────────────────────────────────────────────
    private void drawAxes(Graphics2D g2, int pad, int bottomPad,
                          int W, int H, int cW, int cH, double maxVal) {
        // Axis lines
        g2.setColor(AquaTheme.STONE_PRIMARY);
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawLine(pad, H - bottomPad, W - pad, H - bottomPad); // X
        g2.drawLine(pad, pad,           pad,      H - bottomPad); // Y

        // Y-axis grid + labels (4 divisions)
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        for (int i = 0; i <= 4; i++) {
            int y = H - bottomPad - i * cH / 4;
            double val = maxVal / 4.0 * i;
            g2.setColor(AquaTheme.HOVER_SLATE);
            String yLbl = val >= 1000
                    ? String.format("%.1fk", val / 1000.0)
                    : String.format("%.0f", val);
            g2.drawString(yLbl, 4, y + 4);
            if (i > 0) {
                g2.setColor(new Color(0xD0, 0xD3, 0xCF, 130));
                g2.setStroke(new BasicStroke(0.8f));
                g2.drawLine(pad, y, W - pad, y);
                g2.setStroke(new BasicStroke(1.5f));
            }
        }
    }

    // ── Shared: draw a gradient-filled rounded bar ─────────────────────────────
    private void drawBar(Graphics2D g2, int x, int y, int barW, int bH, Color c) {
        if (bH <= 0) return;
        GradientPaint gp = new GradientPaint(x, y, c.brighter(), x, y + bH, c.darker());
        g2.setPaint(gp);
        g2.fillRoundRect(x, y, barW, bH, 5, 5);
        g2.setColor(c.darker());
        g2.setStroke(new BasicStroke(0.8f));
        g2.drawRoundRect(x, y, barW, bH, 5, 5);
    }

    // ── Utility: mini summary card ─────────────────────────────────────────────
    private JPanel createMiniCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 2));
        card.setBackground(AquaTheme.CRISP_WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AquaTheme.SUBTLE_BORDER, 1, true),
                AquaTheme.padding(6, 10, 6, 10)));
        JLabel titleLbl = new JLabel(title, SwingConstants.CENTER);
        titleLbl.setFont(AquaTheme.FONT_ARIAL_LABEL);
        titleLbl.setForeground(AquaTheme.HOVER_SLATE);
        card.add(titleLbl,  BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }
}
