package module_ui;

import module_core_logic.Expense;
import module_core_logic.ExpenseService;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DeletedHistoryPanel — Dedicated "Trash Bin" view.
 * Accessible via its own Sidebar tab ("Trash").
 * Shows only soft-deleted expenses.
 * Actions: Restore Selected | Permanently Delete
 */
public class DeletedHistoryPanel extends JPanel {

    private final ExpenseService expenseService;
    private final Runnable onDataChangedCallback;

    private JTable table;
    private DefaultTableModel tableModel;
    private List<Expense> currentDeletedExpenses;

    public DeletedHistoryPanel(ExpenseService expenseService, Runnable onDataChangedCallback) {
        this.expenseService = expenseService;
        this.onDataChangedCallback = onDataChangedCallback;
        this.currentDeletedExpenses = new ArrayList<>();

        setBackground(AquaTheme.CRISP_WHITE);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AquaTheme.SUBTLE_BORDER, 1, true),
                AquaTheme.padding(14, 14, 14, 14)));

        // ── Header ──────────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(AquaTheme.CRISP_WHITE);

        JLabel titleLbl = new JLabel("Deleted History (Trash Bin)", SwingConstants.LEFT);
        titleLbl.setFont(AquaTheme.FONT_HEADER_TITLE);
        titleLbl.setForeground(AquaTheme.DEEP_SLATE);
        titleLbl.setBorder(AquaTheme.padding(0, 0, 8, 0));

        JLabel subLbl = new JLabel("Soft-deleted expenses are listed here. Restore or permanently remove them.", SwingConstants.LEFT);
        subLbl.setFont(AquaTheme.FONT_ARIAL_SUB);
        subLbl.setForeground(AquaTheme.HOVER_SLATE);

        JPanel titleBlock = new JPanel();
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        titleBlock.setOpaque(false);
        titleBlock.add(titleLbl);
        titleBlock.add(subLbl);
        header.add(titleBlock, BorderLayout.WEST);

        // ── Toolbar ─────────────────────────────────────────────────
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        toolbar.setBackground(AquaTheme.CRISP_WHITE);

        JButton btnRestore = AquaTheme.createCyanButton("Restore Selected");
        btnRestore.addActionListener(e -> restoreSelected());

        JButton btnHardDelete = AquaTheme.createDangerButton("Permanently Delete");
        btnHardDelete.addActionListener(e -> hardDeleteSelected());

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setFont(AquaTheme.FONT_ARIAL_BUTTON);
        btnRefresh.setForeground(AquaTheme.HOVER_SLATE);
        btnRefresh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> refreshTableData());

        toolbar.add(btnRestore);
        toolbar.add(btnHardDelete);
        toolbar.add(btnRefresh);

        JPanel topBlock = new JPanel();
        topBlock.setLayout(new BoxLayout(topBlock, BoxLayout.Y_AXIS));
        topBlock.setBackground(AquaTheme.CRISP_WHITE);
        topBlock.add(header);
        topBlock.add(Box.createVerticalStrut(8));
        topBlock.add(toolbar);

        add(topBlock, BorderLayout.NORTH);

        // ── Table ────────────────────────────────────────────────────
        String[] cols = {"ID", "Fav", "Date", "Category", "Payment Mode", "Amount (Rs.)", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel);
        table.setFont(AquaTheme.FONT_ARIAL_FIELD);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(0xE0, 0xE4, 0xDF));

        JTableHeader tHeader = table.getTableHeader();
        tHeader.setFont(AquaTheme.FONT_ARIAL_HEADER);
        tHeader.setBackground(AquaTheme.STONE_PRIMARY);
        tHeader.setForeground(Color.WHITE);
        tHeader.setPreferredSize(new Dimension(tHeader.getWidth(), 36));

        // Row renderer — deleted rows tinted red-ish
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                                                           boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, sel, foc, row, col);
                if (sel) {
                    c.setBackground(new Color(0xFE, 0xE5, 0xE5));
                    c.setForeground(AquaTheme.TEXT_DARK);
                } else {
                    c.setBackground(row % 2 == 0 ? new Color(0xFF, 0xF8, 0xF8) : AquaTheme.CRISP_WHITE);
                    c.setForeground(AquaTheme.TEXT_DARK);
                }
                setHorizontalAlignment(col == 0 || col == 1 || col == 2 || col == 5
                        ? SwingConstants.CENTER : SwingConstants.LEFT);
                return c;
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(45);
        table.getColumnModel().getColumn(1).setPreferredWidth(45);
        table.getColumnModel().getColumn(2).setPreferredWidth(95);
        table.getColumnModel().getColumn(3).setPreferredWidth(130);
        table.getColumnModel().getColumn(4).setPreferredWidth(110);
        table.getColumnModel().getColumn(5).setPreferredWidth(100);
        table.getColumnModel().getColumn(6).setPreferredWidth(200);

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(AquaTheme.CRISP_WHITE);
        scroll.setBorder(BorderFactory.createLineBorder(AquaTheme.SUBTLE_BORDER));
        add(scroll, BorderLayout.CENTER);

        refreshTableData();
    }

    public void refreshTableData() {
        tableModel.setRowCount(0);
        currentDeletedExpenses = new ArrayList<>(expenseService.getDeletedExpenses());
        for (Expense e : currentDeletedExpenses) {
            tableModel.addRow(new Object[]{
                    e.getId(),
                    e.isStarred() ? "(*)" : "( )",
                    e.getExpenseDate(),
                    e.getCategoryName()    != null ? e.getCategoryName()    : "N/A",
                    e.getPaymentModeName() != null ? e.getPaymentModeName() : "N/A",
                    String.format("%.2f", e.getAmount()),
                    e.getNotes() != null ? e.getNotes() : ""
            });
        }
    }

    private void restoreSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { warn("Please select a record to restore."); return; }
        Expense exp = currentDeletedExpenses.get(row);
        if (expenseService.restoreExpense(exp.getId())) {
            refreshTableData();
            if (onDataChangedCallback != null) onDataChangedCallback.run();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to restore expense.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hardDeleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { warn("Please select a record to permanently delete."); return; }
        Expense exp = currentDeletedExpenses.get(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "PERMANENT DELETE: This action cannot be undone!\nExpense #" + exp.getId() + " will be erased.",
                "Confirm Permanent Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (expenseService.hardDeleteExpense(exp.getId())) {
                refreshTableData();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to permanently delete.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void warn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Nothing Selected", JOptionPane.INFORMATION_MESSAGE);
    }
}
