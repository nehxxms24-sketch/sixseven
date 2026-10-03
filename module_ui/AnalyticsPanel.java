package module_ui;

import module_core_logic.ExpenseService;
import java.awt.BorderLayout;
import javax.swing.JPanel;

/**
 * AnalyticsPanel — Wraps TrendsChartPanel for backward compatibility.
 */
public class AnalyticsPanel extends JPanel {

    private final TrendsChartPanel trendsChartPanel;

    public AnalyticsPanel(ExpenseService expenseService) {
        setLayout(new BorderLayout());
        trendsChartPanel = new TrendsChartPanel(expenseService);
        add(trendsChartPanel, BorderLayout.CENTER);
    }

    public void refreshAnalytics() {
        if (trendsChartPanel != null) {
            trendsChartPanel.refreshAnalytics();
        }
    }
}
