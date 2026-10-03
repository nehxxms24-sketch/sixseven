package module_ui;

import module_core_logic.ExpenseService;

import javax.swing.*;
import java.awt.*;

/**
 * MainDashboardFrame — Pure Solid Stone Grey Theme container.
 * Panels:
 *   DASHBOARD    -> DashboardPanel (metrics + 5-7 recent transactions snapshot + link to Transactions)
 *   TRANSACTIONS -> TransactionsPanel (month-wise/year-wise/category/search filter bar + full action bar)
 *   ANALYTICS    -> TrendsChartPanel (month-wise/year-wise comparison & sorting)
 *   DEBTS & LOANS-> DebtManagementPanel
 *   TRASH        -> DeletedHistoryPanel (dedicated trash bin)
 */
public class MainDashboardFrame extends JFrame {

    private final ExpenseService expenseService;

    private CardLayout cardLayout;
    private JPanel mainContentPanel;
    private SidebarPanel sidebar;

    private DashboardPanel dashboardPanel;
    private TransactionsPanel transactionsPanel;
    private TrendsChartPanel trendsChartPanel;
    private DebtManagementPanel debtPanel;
    private DeletedHistoryPanel deletedHistoryPanel;

    public MainDashboardFrame() {
        this.expenseService = new ExpenseService();

        setTitle("SixSeven – Personal Expense System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 750);
        setMinimumSize(new Dimension(980, 640));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(AquaTheme.LIGHT_WASH);

        // ── LEFT SIDEBAR ─────────────────────────────────────────────
        sidebar = new SidebarPanel();
        sidebar.setTabChangeListener(this::switchView);
        root.add(sidebar, BorderLayout.WEST);

        // ── CENTER CONTENT (CardLayout) ───────────────────────────────
        cardLayout = new CardLayout();
        mainContentPanel = new JPanel(cardLayout);
        mainContentPanel.setBackground(AquaTheme.LIGHT_WASH);

        dashboardPanel = new DashboardPanel(
                expenseService,
                this::openAddExpenseDialog,
                () -> sidebar.selectTabByName("Transactions")
        );

        transactionsPanel   = new TransactionsPanel(expenseService, this::refreshAllMetrics);
        trendsChartPanel    = new TrendsChartPanel(expenseService);
        debtPanel           = new DebtManagementPanel(expenseService, this::refreshAllMetrics);
        deletedHistoryPanel = new DeletedHistoryPanel(expenseService, this::refreshAllMetrics);

        mainContentPanel.add(dashboardPanel,      "Dashboard");
        mainContentPanel.add(transactionsPanel,   "Transactions");
        mainContentPanel.add(trendsChartPanel,    "Analytics");
        mainContentPanel.add(debtPanel,           "Debts & Loans");  // single canonical card key
        mainContentPanel.add(deletedHistoryPanel, "Trash");

        root.add(mainContentPanel, BorderLayout.CENTER);
        setContentPane(root);

        refreshAllMetrics();
    }

    private void openAddExpenseDialog() {
        AddExpenseDialog dialog = new AddExpenseDialog(this, expenseService, this::refreshAllMetrics);
        dialog.setVisible(true);
    }

    private void switchView(String tabName) {
        // Sidebar sends exact TABS[] strings — map "Debts & Loans" to canonical card key
        String cardKey = tabName.equals("Debts") ? "Debts & Loans" : tabName;
        cardLayout.show(mainContentPanel, cardKey);
        switch (tabName) {
            case "Dashboard"    -> dashboardPanel.refreshData();
            case "Transactions" -> transactionsPanel.refreshTableData();
            case "Analytics"    -> trendsChartPanel.refreshAnalytics();
            case "Debts & Loans", "Debts" -> debtPanel.refreshTableData();
            case "Trash"        -> deletedHistoryPanel.refreshTableData();
        }
    }

    public void refreshAllMetrics() {
        if (dashboardPanel != null)      dashboardPanel.refreshData();
        if (transactionsPanel != null)   transactionsPanel.refreshTableData();
        if (trendsChartPanel != null)    trendsChartPanel.refreshAnalytics();
        if (debtPanel != null)           debtPanel.refreshTableData();
        if (deletedHistoryPanel != null) deletedHistoryPanel.refreshTableData();
    }
}
