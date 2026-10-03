package module_ui;

import module_core_logic.Debt;
import module_core_logic.ExpenseService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * MODULE 3 – Swing & AWT UI Layer
 * DebtManagementPanel: Debts & Loans overview dashboard styled in Money Palette.
 * Features Overview Cards (Total Borrowed ₹, Total Lent ₹, Net Balance),
 * color-coded status highlights, and "+ Add Debt/Loan" modal launcher.
 */
public class DebtManagementPanel extends JPanel {

    private final ExpenseService expenseService;
    private final Runnable onDataChangedCallback;

    private JLabel lblTotalBorrowed;
    private JLabel lblTotalLent;
    private JLabel lblNetBalance;

    private JTable table;
    private DefaultTableModel tableModel;
    private List<Debt> currentDisplayedDebts;

    public DebtManagementPanel(ExpenseService expenseService, Runnable onDataChangedCallback) {
        this.expenseService = expenseService;
        this.onDataChangedCallback = onDataChangedCallback;
        this.currentDisplayedDebts = new ArrayList<>();

        setBackground(AquaTheme.PURE_WHITE);
        setLayout(new BorderLayout(0, 12));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AquaTheme.VINTAGE_CREAM, 1, true),
                AquaTheme.padding(16, 16, 16, 16)
        ));

        // 1. TOP SECTION: OVERVIEW CARDS & TOOLBAR
        JPanel topContainer = new JPanel(new BorderLayout(0, 10));
        topContainer.setBackground(AquaTheme.PURE_WHITE);

        // Header Title & Action Buttons Row
        JPanel titleRow = new JPanel(new BorderLayout());
        titleRow.setBackground(AquaTheme.PURE_WHITE);

        JLabel titleLbl = new JLabel("🤝 Debts & Loans Management");
        titleLbl.setFont(AquaTheme.FONT_HEADER_TITLE);
        titleLbl.setForeground(AquaTheme.DEEP_FOREST_GREEN);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnGroup.setBackground(AquaTheme.PURE_WHITE);

        JButton btnAddDebt = AquaTheme.createCyanButton("➕ Add Debt/Loan");
        btnAddDebt.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            AddDebtDialog dialog = new AddDebtDialog(parentWindow, expenseService, this::refreshTableData);
            dialog.setVisible(true);
        });

        JButton btnMarkRepaid = new JButton("✓ Mark as Repaid");
        btnMarkRepaid.setFont(AquaTheme.FONT_ARIAL_BUTTON);
        btnMarkRepaid.setForeground(AquaTheme.SUCCESS_GREEN);
        btnMarkRepaid.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnMarkRepaid.addActionListener(e -> markSelectedRepaid());

        btnGroup.add(btnAddDebt);
        btnGroup.add(btnMarkRepaid);

        titleRow.add(titleLbl, BorderLayout.WEST);
        titleRow.add(btnGroup, BorderLayout.EAST);

        // Overview Cards Container: Total Borrowed (₹), Total Lent (₹), Net Balance
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 14, 0));
        cardsPanel.setBackground(AquaTheme.PURE_WHITE);

        lblTotalBorrowed = new JLabel("₹0.00", SwingConstants.CENTER);
        lblTotalBorrowed.setFont(new Font("Arial", Font.BOLD, 18));
        lblTotalBorrowed.setForeground(AquaTheme.DANGER_RED);

        lblTotalLent = new JLabel("₹0.00", SwingConstants.CENTER);
        lblTotalLent.setFont(new Font("Arial", Font.BOLD, 18));
        lblTotalLent.setForeground(AquaTheme.SUCCESS_GREEN);

        lblNetBalance = new JLabel("₹0.00", SwingConstants.CENTER);
        lblNetBalance.setFont(new Font("Arial", Font.BOLD, 18));
        lblNetBalance.setForeground(AquaTheme.DEEP_FOREST_GREEN);

        cardsPanel.add(createSummaryCard("TOTAL BORROWED (₹)", lblTotalBorrowed, new Color(253, 237, 237)));
        cardsPanel.add(createSummaryCard("TOTAL LENT (₹)", lblTotalLent, new Color(237, 247, 237)));
        cardsPanel.add(createSummaryCard("NET BALANCE (₹)", lblNetBalance, new Color(245, 248, 246)));

        topContainer.add(titleRow, BorderLayout.NORTH);
        topContainer.add(cardsPanel, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // 2. TABLE SETUP
        String[] columns = {"ID", "Person Name", "Type", "Principal (₹)", "Interest (%)", "Total Due (₹)", "Due Date", "Status", "Notes"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(AquaTheme.FONT_ARIAL_FIELD);
        table.setRowHeight(32);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(230, 235, 230));

        JTableHeader header = table.getTableHeader();
        header.setFont(AquaTheme.FONT_ARIAL_HEADER);
        header.setBackground(AquaTheme.DEEP_FOREST_GREEN);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));

        // Renderer with soft red for OVERDUE and soft yellow for DUE SOON
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (row < currentDisplayedDebts.size()) {
                    Debt d = currentDisplayedDebts.get(row);
                    boolean isOverdue = "OVERDUE".equalsIgnoreCase(d.getStatus());
                    boolean isDueSoon = false;

                    if ("PENDING".equalsIgnoreCase(d.getStatus()) && d.getDueDate() != null) {
                        try {
                            LocalDate due = LocalDate.parse(d.getDueDate());
                            LocalDate today = LocalDate.now();
                            if (!due.isBefore(today) && due.isBefore(today.plusDays(7))) {
                                isDueSoon = true;
                            }
                        } catch (Exception ignored) {}
                    }

                    if (isSelected) {
                        c.setBackground(new Color(220, 230, 225));
                        c.setForeground(AquaTheme.TEXT_DARK);
                    } else if ("REPAID".equalsIgnoreCase(d.getStatus())) {
                        c.setBackground(new Color(238, 248, 240));
                        c.setForeground(AquaTheme.TEXT_DARK);
                    } else if (isOverdue) {
                        c.setBackground(new Color(255, 230, 230));
                        c.setForeground(AquaTheme.DANGER_RED);
                    } else if (isDueSoon) {
                        c.setBackground(new Color(255, 250, 220));
                        c.setForeground(AquaTheme.TEXT_DARK);
                    } else {
                        c.setBackground(row % 2 == 0 ? Color.WHITE : AquaTheme.WARM_OFFWHITE);
                        c.setForeground(AquaTheme.TEXT_DARK);
                    }
                }

                if (column == 0 || column == 2 || column == 3 || column == 4 || column == 5 || column == 6 || column == 7) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(AquaTheme.VINTAGE_CREAM));

        add(scrollPane, BorderLayout.CENTER);

        refreshTableData();
    }

    private JPanel createSummaryCard(String titleText, JLabel valueLabel, Color bg) {
        JPanel card = new JPanel(new BorderLayout(0, 4));
        card.setBackground(bg);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AquaTheme.VINTAGE_CREAM, 1, true),
                AquaTheme.padding(10, 16, 10, 16)
        ));

        JLabel title = new JLabel(titleText, SwingConstants.CENTER);
        title.setFont(AquaTheme.FONT_ARIAL_LABEL);
        title.setForeground(AquaTheme.SAGE_GREEN);

        card.add(title, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    public void refreshTableData() {
        tableModel.setRowCount(0);
        currentDisplayedDebts.clear();

        double totalBorrowed = 0.0;
        double totalLent = 0.0;

        List<Debt> debts = expenseService.getDebts();
        for (Debt d : debts) {
            currentDisplayedDebts.add(d);

            double due = d.getTotalDue();
            if (!"REPAID".equalsIgnoreCase(d.getStatus())) {
                if ("BORROWED".equalsIgnoreCase(d.getDebtType())) {
                    totalBorrowed += due;
                } else if ("LENT".equalsIgnoreCase(d.getDebtType())) {
                    totalLent += due;
                }
            }

            tableModel.addRow(new Object[]{
                    d.getId(),
                    d.getPersonName(),
                    d.getDebtType(),
                    String.format("%.2f", d.getPrincipalAmount()),
                    String.format("%.1f%%", d.getInterestRate()),
                    String.format("%.2f", due),
                    d.getDueDate(),
                    d.getStatus(),
                    d.getNotes() != null ? d.getNotes() : ""
            });
        }

        lblTotalBorrowed.setText(String.format("₹%.2f", totalBorrowed));
        lblTotalLent.setText(String.format("₹%.2f", totalLent));
        double net = totalLent - totalBorrowed;
        lblNetBalance.setText(String.format("%s₹%.2f", net >= 0 ? "+" : "", net));
        lblNetBalance.setForeground(net >= 0 ? AquaTheme.SUCCESS_GREEN : AquaTheme.DANGER_RED);
    }

    private void markSelectedRepaid() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a debt record to mark as repaid.",
                    "No Row Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Debt selectedDebt = currentDisplayedDebts.get(selectedRow);

        if ("REPAID".equalsIgnoreCase(selectedDebt.getStatus())) {
            JOptionPane.showMessageDialog(this, "This entry is already marked as REPAID.",
                    "Already Repaid", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int logExpenseOption = JOptionPane.showConfirmDialog(this,
                "Mark debt for " + selectedDebt.getPersonName() + " (Total ₹" + String.format("%.2f", selectedDebt.getTotalDue()) +
                        ") as REPAID?\n\nWould you like to log this repayment as an expense transaction in your expense history?",
                "Confirm Debt Repayment", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (logExpenseOption == JOptionPane.YES_OPTION) {
            expenseService.markDebtAsRepaid(selectedDebt, true);
            refreshTableData();
            if (onDataChangedCallback != null) onDataChangedCallback.run();
        } else if (logExpenseOption == JOptionPane.NO_OPTION) {
            expenseService.markDebtAsRepaid(selectedDebt, false);
            refreshTableData();
            if (onDataChangedCallback != null) onDataChangedCallback.run();
        }
    }
}
