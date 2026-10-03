package module_ui;

import module_core_logic.ExpenseService;

import javax.swing.*;
import java.awt.*;

/**
 * MODULE 3 – Swing & AWT UI Layer
 * ExpenseManagerFrame: Main Dashboard Frame of Personal Expense Manager.
 * Features KPI status cards, connection status badge, JTabbedPane navigation,
 * and synchronized updates across entry forms, tables, and analytics.
 */
public class ExpenseManagerFrame extends JFrame {

    private final ExpenseService expenseService;

    // Components needing dynamic refresh
    private JLabel lblDbStatusBadge;
    private JLabel lblKpiTotalSpent;
    private JLabel lblKpiCount;
    private ExpenseFormPanel formPanel;
    private ExpenseTablePanel tablePanel;
    private AnalyticsPanel analyticsPanel;

    public ExpenseManagerFrame() {
        this.expenseService = new ExpenseService();

        setTitle("Personal Expense Manager – Aquatic Edition");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        // Root Container
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AquaTheme.OFF_WHITE);

        // 1. TOP HEADER BAR
        root.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. CENTER TABBED CONTAINER
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(AquaTheme.FONT_SECTION);
        tabbedPane.setBackground(AquaTheme.CARD_BG_ALT);
        tabbedPane.setForeground(AquaTheme.OCEAN_BLUE);

        // Tab 1: Main Dashboard (Form + Table)
        JPanel dashboardTab = new JPanel(new BorderLayout(14, 14));
        dashboardTab.setBackground(AquaTheme.OFF_WHITE);
        dashboardTab.setBorder(AquaTheme.padding(14, 14, 14, 14));

        formPanel = new ExpenseFormPanel(expenseService, this::onDataUpdated);
        formPanel.setPreferredSize(new Dimension(320, 0));

        tablePanel = new ExpenseTablePanel(expenseService, this::onDataUpdated);

        dashboardTab.add(formPanel, BorderLayout.WEST);
        dashboardTab.add(tablePanel, BorderLayout.CENTER);

        // Tab 2: Analytics & Charting
        analyticsPanel = new AnalyticsPanel(expenseService);

        tabbedPane.addTab("💳 Expense Dashboard", dashboardTab);
        tabbedPane.addTab("📊 Analytics & Trends", analyticsPanel);

        // Listen for tab switches to refresh analytics
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                analyticsPanel.refreshAnalytics();
            }
        });

        root.add(tabbedPane, BorderLayout.CENTER);

        // 3. BOTTOM FOOTER STATUS BAR
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(AquaTheme.OCEAN_BLUE);
        statusBar.setPreferredSize(new Dimension(getWidth(), 26));

        JLabel statusTxt = new JLabel("  Personal Expense Manager v2.0 • Java Swing + JDBC + Oracle DB");
        statusTxt.setFont(AquaTheme.FONT_SUBTITLE);
        statusTxt.setForeground(Color.WHITE);

        JLabel userTxt = new JLabel("Logged in as: sixseven  ");
        userTxt.setFont(AquaTheme.FONT_SUBTITLE);
        userTxt.setForeground(AquaTheme.SOFT_MINT);

        statusBar.add(statusTxt, BorderLayout.WEST);
        statusBar.add(userTxt, BorderLayout.EAST);

        root.add(statusBar, BorderLayout.SOUTH);

        setContentPane(root);

        // Initial Data Refresh
        updateKpiCards();
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AquaTheme.OCEAN_BLUE);
        header.setBorder(AquaTheme.padding(14, 20, 14, 20));

        // Title & Badge Panel
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);

        JLabel titleLbl = new JLabel("Personal Expense Manager");
        titleLbl.setFont(AquaTheme.FONT_HEADER_TITLE);
        titleLbl.setForeground(Color.WHITE);

        // DB Status Badge
        boolean isConnected = expenseService.isDbConnected();
        lblDbStatusBadge = new JLabel(isConnected ? "  ● Oracle DB Connected  " : "  ▲ Offline / Fallback Mode  ");
        lblDbStatusBadge.setFont(AquaTheme.FONT_SUBTITLE);
        lblDbStatusBadge.setOpaque(true);
        lblDbStatusBadge.setForeground(Color.WHITE);
        lblDbStatusBadge.setBackground(isConnected ? AquaTheme.SUCCESS_GREEN : new Color(230, 140, 30));
        lblDbStatusBadge.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        titlePanel.add(titleLbl);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(lblDbStatusBadge);

        // KPI Metric Cards Container
        JPanel kpiPanel = new JPanel(new GridLayout(1, 2, 12, 0));
        kpiPanel.setOpaque(false);

        lblKpiTotalSpent = new JLabel("₹0.00", SwingConstants.CENTER);
        lblKpiTotalSpent.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblKpiTotalSpent.setForeground(AquaTheme.OCEAN_BLUE);

        lblKpiCount = new JLabel("0 Recs", SwingConstants.CENTER);
        lblKpiCount.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblKpiCount.setForeground(AquaTheme.OCEAN_BLUE);

        kpiPanel.add(createKpiCard("TOTAL SPENT", lblKpiTotalSpent));
        kpiPanel.add(createKpiCard("TRANSACTIONS", lblKpiCount));

        header.add(titlePanel, BorderLayout.WEST);
        header.add(kpiPanel, BorderLayout.EAST);

        return header;
    }

    private JPanel createKpiCard(String title, JLabel valueLabel) {
        JPanel card = new JPanel(new BorderLayout(0, 2));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AquaTheme.SOFT_MINT, 1, true),
                AquaTheme.padding(6, 14, 6, 14)
        ));

        JLabel tLbl = new JLabel(title, SwingConstants.CENTER);
        tLbl.setFont(AquaTheme.FONT_LABEL);
        tLbl.setForeground(AquaTheme.TEXT_MUTED);

        card.add(tLbl, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void onDataUpdated() {
        updateKpiCards();
        tablePanel.refreshTableData();
        analyticsPanel.refreshAnalytics();
    }

    private void updateKpiCards() {
        double total = expenseService.getTotalSpent();
        int count = expenseService.getActiveExpenses().size();

        lblKpiTotalSpent.setText(String.format("₹%.2f", total));
        lblKpiCount.setText(count + " Active");
    }
}
