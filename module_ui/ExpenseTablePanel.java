package module_ui;

import module_core_logic.Category;
import module_core_logic.Expense;
import module_core_logic.ExpenseService;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * MODULE 3 – Swing & AWT UI Layer
 * ExpenseTablePanel: Active Expenses Table with Search, Category Filter, Edit, Star & Delete.
 * Trash/Deleted History is now in its own dedicated DeletedHistoryPanel tab.
 */
public class ExpenseTablePanel extends JPanel {

    private final ExpenseService expenseService;
    private final Runnable onDataChangedCallback;

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private JComboBox<String> cbCategoryFilter;

    // Action Toolbar Buttons
    private JButton btnEdit;
    private JButton btnStarToggle;
    private JButton btnDelete;
    private JButton btnExport;

    private List<Expense> currentDisplayedExpenses;

    public ExpenseTablePanel(ExpenseService expenseService, Runnable onDataChangedCallback) {
        this.expenseService = expenseService;
        this.onDataChangedCallback = onDataChangedCallback;
        this.currentDisplayedExpenses = new ArrayList<>();

        setBackground(AquaTheme.CRISP_WHITE);
        setLayout(new BorderLayout(0, 10));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(AquaTheme.SUBTLE_BORDER, 1, true),
                AquaTheme.padding(14, 14, 14, 14)
        ));

        // 1. TOP TOOLBAR & FILTER ROW
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setBackground(AquaTheme.CRISP_WHITE);

        // Action Toolbar
        JPanel actionToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        actionToolbar.setBackground(AquaTheme.CRISP_WHITE);

        btnEdit = new JButton("Edit Expense");
        btnEdit.setFont(AquaTheme.FONT_ARIAL_BUTTON);
        btnEdit.setForeground(AquaTheme.TEXT_DARK);
        btnEdit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEdit.addActionListener(e -> openEditDialog());

        btnStarToggle = new JButton("(*) Star / Unstar");
        btnStarToggle.setFont(AquaTheme.FONT_ARIAL_BUTTON);
        btnStarToggle.setForeground(AquaTheme.ACTION_TEAL);
        btnStarToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnStarToggle.addActionListener(e -> toggleStarOnSelected());

        btnDelete = AquaTheme.createDangerButton("Delete Selected");
        btnDelete.addActionListener(e -> handleDeleteSelected());

        btnExport = new JButton("Export CSV");
        btnExport.setFont(AquaTheme.FONT_ARIAL_BUTTON);
        btnExport.setForeground(AquaTheme.HOVER_SLATE);
        btnExport.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExport.addActionListener(e -> exportToCSV());

        actionToolbar.add(btnEdit);
        actionToolbar.add(btnStarToggle);
        actionToolbar.add(btnDelete);
        actionToolbar.add(btnExport);

        // Filter Bar (Search + Category Filter)
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        filterRow.setBackground(AquaTheme.CRISP_WHITE);

        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(AquaTheme.FONT_ARIAL_LABEL);
        txtSearch = new JTextField(12);
        txtSearch.setFont(AquaTheme.FONT_ARIAL_FIELD);
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filterTable(); }
            public void removeUpdate(DocumentEvent e) { filterTable(); }
            public void changedUpdate(DocumentEvent e) { filterTable(); }
        });

        JLabel catLbl = new JLabel("Category:");
        catLbl.setFont(AquaTheme.FONT_ARIAL_LABEL);
        cbCategoryFilter = new JComboBox<>();
        cbCategoryFilter.setFont(AquaTheme.FONT_ARIAL_FIELD);
        cbCategoryFilter.addActionListener(e -> filterTable());

        filterRow.add(searchLbl);
        filterRow.add(txtSearch);
        filterRow.add(catLbl);
        filterRow.add(cbCategoryFilter);

        topContainer.add(actionToolbar);
        topContainer.add(Box.createVerticalStrut(4));
        topContainer.add(filterRow);

        add(topContainer, BorderLayout.NORTH);

        // 2. TABLE SETUP
        String[] columns = {"ID", "Fav", "Date", "Category", "Payment Mode", "Amount (₹)", "Notes"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(AquaTheme.FONT_ARIAL_FIELD);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(230, 235, 230));

        JTableHeader header = table.getTableHeader();
        header.setFont(AquaTheme.FONT_ARIAL_HEADER);
        header.setBackground(AquaTheme.STONE_PRIMARY);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 36));

        // Renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (isSelected) {
                    c.setBackground(new Color(0xD8, 0xE4, 0xDF));
                    c.setForeground(AquaTheme.TEXT_DARK);
                } else {
                    c.setBackground(row % 2 == 0 ? AquaTheme.CRISP_WHITE : AquaTheme.LIGHT_WASH);
                    c.setForeground(AquaTheme.TEXT_DARK);
                }
                setHorizontalAlignment(column == 0 || column == 1 || column == 2 || column == 5
                        ? SwingConstants.CENTER : SwingConstants.LEFT);
                if (column == 1) {
                    setFont(new Font("Arial", Font.BOLD, 13));
                    setForeground("(*)".equals(value) ? AquaTheme.ACTION_TEAL : AquaTheme.HOVER_SLATE);
                }
                return c;
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(45);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(45);  // Fav
        table.getColumnModel().getColumn(2).setPreferredWidth(95);  // Date
        table.getColumnModel().getColumn(3).setPreferredWidth(130); // Category
        table.getColumnModel().getColumn(4).setPreferredWidth(110); // Mode
        table.getColumnModel().getColumn(5).setPreferredWidth(95);  // Amount
        table.getColumnModel().getColumn(6).setPreferredWidth(200); // Notes

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(AquaTheme.CRISP_WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(AquaTheme.SUBTLE_BORDER));

        add(scrollPane, BorderLayout.CENTER);

        refreshTableData();
    }

    public void updateFilterCategories() {
        cbCategoryFilter.removeAllItems();
        cbCategoryFilter.addItem("All Categories");
        for (Category cat : expenseService.getCategories()) {
            cbCategoryFilter.addItem(cat.getName());
        }
    }

    public void refreshTableData() {
        updateFilterCategories();
        filterTable();
    }


    private void filterTable() {
        tableModel.setRowCount(0);
        currentDisplayedExpenses.clear();

        List<Expense> expensesToDisplay = expenseService.getActiveExpenses();

        String searchText = txtSearch.getText().trim().toLowerCase();
        String selectedCategory = (String) cbCategoryFilter.getSelectedItem();

        for (Expense exp : expensesToDisplay) {
            boolean matchesSearch = searchText.isEmpty() ||
                    String.valueOf(exp.getId()).contains(searchText) ||
                    (exp.getNotes() != null && exp.getNotes().toLowerCase().contains(searchText)) ||
                    (exp.getCategoryName() != null && exp.getCategoryName().toLowerCase().contains(searchText)) ||
                    (exp.getPaymentModeName() != null && exp.getPaymentModeName().toLowerCase().contains(searchText));

            boolean matchesCategory = selectedCategory == null ||
                    "All Categories".equals(selectedCategory) ||
                    selectedCategory.equalsIgnoreCase(exp.getCategoryName());

            if (matchesSearch && matchesCategory) {
                currentDisplayedExpenses.add(exp);
                tableModel.addRow(new Object[]{
                        exp.getId(),
                        exp.isStarred() ? "(*)" : "( )",
                        exp.getExpenseDate(),
                        exp.getCategoryName() != null ? exp.getCategoryName() : "N/A",
                        exp.getPaymentModeName() != null ? exp.getPaymentModeName() : "N/A",
                        String.format("%.2f", exp.getAmount()),
                        exp.getNotes() != null ? exp.getNotes() : ""
                });
            }
        }
    }

    private void openEditDialog() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an expense row to edit.",
                    "No Row Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Expense selectedExp = currentDisplayedExpenses.get(selectedRow);
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        EditExpenseDialog editDialog = new EditExpenseDialog(parentWindow, expenseService, selectedExp, () -> {
            filterTable();
            if (onDataChangedCallback != null) onDataChangedCallback.run();
        });
        editDialog.setVisible(true);
    }

    private void toggleStarOnSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an expense row to star/unstar.",
                    "No Row Selected", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Expense selectedExp = currentDisplayedExpenses.get(selectedRow);
        expenseService.toggleStar(selectedExp.getId(), selectedExp.isStarred());
        filterTable();
        if (onDataChangedCallback != null) onDataChangedCallback.run();
    }

    private void handleDeleteSelected() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select an expense record first.",
                    "No Record Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Expense exp = currentDisplayedExpenses.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Move expense #" + exp.getId() + " (Rs." + String.format("%.2f", exp.getAmount()) + ") to Trash?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (expenseService.deleteExpense(exp.getId())) {
                filterTable();
                if (onDataChangedCallback != null) onDataChangedCallback.run();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete expense.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void exportToCSV() {
        if (currentDisplayedExpenses.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records available to export.",
                    "Export CSV", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File("Expenses_Report.csv"));
        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            try (PrintWriter pw = new PrintWriter(fileToSave)) {
                pw.println("ID,Starred,Date,Category,PaymentMode,Amount,Notes");
                for (Expense e : currentDisplayedExpenses) {
                    pw.printf("%d,%s,%s,\"%s\",\"%s\",%.2f,\"%s\"%n",
                            e.getId(),
                            e.isStarred() ? "Yes" : "No",
                            e.getExpenseDate(),
                            e.getCategoryName() != null ? e.getCategoryName() : "",
                            e.getPaymentModeName() != null ? e.getPaymentModeName() : "",
                            e.getAmount(),
                            e.getNotes() != null ? e.getNotes().replace("\"", "\"\"") : ""
                    );
                }
                JOptionPane.showMessageDialog(this, "Successfully exported " + currentDisplayedExpenses.size() +
                        " records to:\n" + fileToSave.getAbsolutePath(), "Export Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error exporting CSV: " + ex.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
