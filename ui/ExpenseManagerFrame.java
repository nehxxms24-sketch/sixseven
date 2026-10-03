package ui;

import dao.CategoryDAO;
import dao.ExpenseDAO;
import model.Category;
import model.Expense;
import model.PaymentMode;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

/**
 * ExpenseManagerFrame – main window (aquatic blue + off-white theme).
 * Three tabs: Add Expense | History | Trends chart.
 */
public class ExpenseManagerFrame extends JFrame {

    // ── DAOs ────────────────────────────────────────────────────────────────
    private final ExpenseDAO  expenseDAO  = new ExpenseDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    // ── Aquatic Blue + Off-White Palette ─────────────────────────────────────
    private static final Color BG_MAIN      = new Color(0xE8F6FA);   // light aqua off-white
    private static final Color DEEP_BLUE    = new Color(0x03045E);   // deep navy
    private static final Color OCEAN_BLUE   = new Color(0x0077B6);   // primary
    private static final Color AQUA_MID     = new Color(0x0096C7);   // mid aqua
    private static final Color AQUA_BRIGHT  = new Color(0x00B4D8);   // bright aqua
    private static final Color AQUA_LIGHT   = new Color(0xADE8F4);   // pale aqua
    private static final Color OFF_WHITE    = new Color(0xF8FFFE);   // off-white
    private static final Color CARD_BG      = new Color(0xFFFFFF);   // pure white card
    private static final Color HEADER_BG    = new Color(0x023E8A);   // deep header
    private static final Color TEXT_DARK    = new Color(0x03045E);
    private static final Color TEXT_MUTED   = new Color(0x4A6FA5);
    private static final Color SUCCESS      = new Color(0x06D6A0);
    private static final Color DANGER       = new Color(0xEF476F);
    private static final Color ROW_ALT      = new Color(0xE0F7FA);
    private static final Color BORDER_COL   = new Color(0xADE8F4);

    private static final Font FONT_HEADER = new Font("SansSerif", Font.BOLD, 20);
    private static final Font FONT_LABEL  = new Font("SansSerif", Font.BOLD, 12);
    private static final Font FONT_INPUT  = new Font("SansSerif", Font.PLAIN, 13);
    private static final Font FONT_BTN    = new Font("SansSerif", Font.BOLD, 13);

    // ── Form fields (Tab 1) ─────────────────────────────────────────────────
    private JTextField              tfAmount;
    private JTextField              tfDate;
    private JComboBox<Category>     cbCategory;
    private JComboBox<PaymentMode>  cbMode;
    private JTextArea               taNotes;

    // ── History (Tab 2) ─────────────────────────────────────────────────────
    private JTable            historyTable;
    private DefaultTableModel tableModel;
    private JTextField        tfFilterFrom;
    private JTextField        tfFilterTo;

    // ── Trends (Tab 3) ──────────────────────────────────────────────────────
    private TrendsChartPanel trendsPanel;
    private JTextField       tfChartFrom;
    private JTextField       tfChartTo;

    private JLabel lblStatus;
    private final NumberFormat rupeeFormat =
            NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    // ════════════════════════════════════════════════════════════════════════
    public ExpenseManagerFrame() {
        super("💰 Personal Expense Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050, 680);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);

        getContentPane().setBackground(BG_MAIN);
        add(buildRoot());

        loadFormDropdowns();
        loadHistory(null, null);
        setVisible(true);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Root layout
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildRoot() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_MAIN);
        root.add(buildHeader(),    BorderLayout.NORTH);
        root.add(buildTabs(),      BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setBackground(HEADER_BG);
        hdr.setBorder(new EmptyBorder(12, 24, 12, 24));

        JLabel left = new JLabel("💰  Personal Expense Manager");
        left.setFont(FONT_HEADER);
        left.setForeground(OFF_WHITE);

        JLabel right = new JLabel("Track · Analyse · Save");
        right.setFont(new Font("SansSerif", Font.ITALIC, 12));
        right.setForeground(AQUA_LIGHT);

        hdr.add(left,  BorderLayout.WEST);
        hdr.add(right, BorderLayout.EAST);
        return hdr;
    }

    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(BG_MAIN);
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));

        tabs.addTab("➕  Add Expense",  buildAddTab());
        tabs.addTab("📋  History",       buildHistoryTab());
        tabs.addTab("📊  Trends",        buildTrendsTab());

        tabs.addChangeListener(e -> {
            if (tabs.getSelectedIndex() == 2) {
                refreshTrends(parseDateField(tfChartFrom), parseDateField(tfChartTo));
            }
        });
        return tabs;
    }

    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(HEADER_BG);
        bar.setBorder(new EmptyBorder(4, 16, 4, 16));

        lblStatus = new JLabel("Ready");
        lblStatus.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblStatus.setForeground(AQUA_LIGHT);
        bar.add(lblStatus, BorderLayout.WEST);

        JLabel copy = new JLabel("Oracle + Java Swing  |  MVC + DAO Architecture");
        copy.setFont(new Font("SansSerif", Font.PLAIN, 11));
        copy.setForeground(AQUA_LIGHT);
        bar.add(copy, BorderLayout.EAST);
        return bar;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  TAB 1 – Add Expense  (fixed form layout)
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildAddTab() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(BG_MAIN);

        // ── Card panel ────────────────────────────────────────────────────────
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COL, 2, true),
                new EmptyBorder(24, 36, 28, 36)));

        // Card title
        JLabel cardTitle = new JLabel("📝  Enter New Expense");
        cardTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
        cardTitle.setForeground(OCEAN_BLUE);
        cardTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(cardTitle);
        card.add(Box.createVerticalStrut(18));

        // ── Form rows ─────────────────────────────────────────────────────────
        tfAmount   = styledField(22);
        tfAmount.setToolTipText("Positive number, e.g. 250.00");
        tfDate     = styledField(22);
        tfDate.setText(LocalDate.now().toString());
        tfDate.setToolTipText("YYYY-MM-DD");
        cbCategory = styledCombo();
        cbMode     = styledCombo();

        taNotes = new JTextArea(3, 22);
        taNotes.setFont(FONT_INPUT);
        taNotes.setBackground(OFF_WHITE);
        taNotes.setForeground(TEXT_DARK);
        taNotes.setCaretColor(OCEAN_BLUE);
        taNotes.setLineWrap(true);
        taNotes.setWrapStyleWord(true);
        taNotes.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COL, 1, true),
                new EmptyBorder(6, 8, 6, 8)));
        JScrollPane notesSP = new JScrollPane(taNotes);
        notesSP.setBorder(new LineBorder(BORDER_COL, 1, true));
        notesSP.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        card.add(formRow("Amount (₹) *",       tfAmount));
        card.add(Box.createVerticalStrut(10));
        card.add(formRow("Date (YYYY-MM-DD) *", tfDate));
        card.add(Box.createVerticalStrut(10));
        card.add(formRow("Category *",          cbCategory));
        card.add(Box.createVerticalStrut(10));
        card.add(formRow("Payment Mode",        cbMode));
        card.add(Box.createVerticalStrut(10));
        card.add(formRow("Notes",               notesSP));
        card.add(Box.createVerticalStrut(20));

        // ── Buttons ───────────────────────────────────────────────────────────
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnRow.setBackground(CARD_BG);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnSave  = aquaButton("💾  Save Expense", SUCCESS);
        JButton btnClear = aquaButton("🗑  Clear Form",    AQUA_MID);

        btnSave.addActionListener(e  -> handleSaveExpense());
        btnClear.addActionListener(e -> clearForm());

        btnRow.add(btnSave);
        btnRow.add(btnClear);
        card.add(btnRow);

        outer.add(card, new GridBagConstraints());
        return outer;
    }

    /** Builds a horizontal label + field row, both left-aligned. */
    private JPanel formRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(CARD_BG);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(FONT_LABEL);
        lbl.setForeground(TEXT_MUTED);
        lbl.setPreferredSize(new Dimension(160, 30));

        row.add(lbl,   BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  TAB 2 – History
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildHistoryTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        // ── Filter bar ────────────────────────────────────────────────────────
        JPanel filterBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        filterBar.setBackground(CARD_BG);
        filterBar.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COL, 1, true),
                new EmptyBorder(6, 12, 6, 12)));

        filterBar.add(label("From:"));
        tfFilterFrom = styledField(11);
        tfFilterFrom.setToolTipText("YYYY-MM-DD");
        filterBar.add(tfFilterFrom);

        filterBar.add(label("To:"));
        tfFilterTo = styledField(11);
        tfFilterTo.setToolTipText("YYYY-MM-DD");
        filterBar.add(tfFilterTo);

        JButton btnFilter  = aquaButton("🔍 Filter",  OCEAN_BLUE);
        JButton btnReset   = aquaButton("↺ Reset",    AQUA_MID);
        btnFilter.addActionListener(e ->
                loadHistory(parseDateField(tfFilterFrom), parseDateField(tfFilterTo)));
        btnReset.addActionListener(e -> {
            tfFilterFrom.setText(""); tfFilterTo.setText("");
            loadHistory(null, null);
        });
        filterBar.add(btnFilter);
        filterBar.add(btnReset);
        panel.add(filterBar, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────────────
        String[] cols = {"ID", "Date", "Category", "Mode", "Amount (₹)", "Notes"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(tableModel);
        styleTable(historyTable);

        JScrollPane sp = new JScrollPane(historyTable);
        sp.getViewport().setBackground(OFF_WHITE);
        sp.setBorder(new LineBorder(BORDER_COL, 1, true));
        panel.add(sp, BorderLayout.CENTER);

        // ── Action bar ────────────────────────────────────────────────────────
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        actionBar.setBackground(CARD_BG);
        actionBar.setBorder(new LineBorder(BORDER_COL, 1, true));

        JButton btnDelete  = aquaButton("🗑  Delete Selected", DANGER);
        JButton btnRefresh = aquaButton("↺  Refresh",          AQUA_MID);
        btnDelete.addActionListener(e  -> handleDeleteExpense());
        btnRefresh.addActionListener(e ->
                loadHistory(parseDateField(tfFilterFrom), parseDateField(tfFilterTo)));

        actionBar.add(btnDelete);
        actionBar.add(btnRefresh);
        panel.add(actionBar, BorderLayout.SOUTH);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  TAB 3 – Trends
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildTrendsTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(BG_MAIN);
        panel.setBorder(new EmptyBorder(14, 14, 14, 14));

        JPanel controlBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        controlBar.setBackground(CARD_BG);
        controlBar.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COL, 1, true),
                new EmptyBorder(6, 12, 6, 12)));

        controlBar.add(label("From:"));
        tfChartFrom = styledField(11);
        controlBar.add(tfChartFrom);

        controlBar.add(label("To:"));
        tfChartTo = styledField(11);
        controlBar.add(tfChartTo);

        JButton btnLoad = aquaButton("📊 Load Chart", OCEAN_BLUE);
        JButton btnAll  = aquaButton("All Time",       AQUA_MID);
        btnLoad.addActionListener(e ->
                refreshTrends(parseDateField(tfChartFrom), parseDateField(tfChartTo)));
        btnAll.addActionListener(e -> {
            tfChartFrom.setText(""); tfChartTo.setText("");
            refreshTrends(null, null);
        });
        controlBar.add(btnLoad);
        controlBar.add(btnAll);
        panel.add(controlBar, BorderLayout.NORTH);

        trendsPanel = new TrendsChartPanel();
        JScrollPane chartSP = new JScrollPane(trendsPanel);
        chartSP.setBorder(new LineBorder(BORDER_COL, 1, true));
        panel.add(chartSP, BorderLayout.CENTER);
        return panel;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Event handlers
    // ════════════════════════════════════════════════════════════════════════
    private void handleSaveExpense() {
        // Validate amount
        String amtStr = tfAmount.getText().trim();
        if (amtStr.isEmpty()) { showError("Amount is required."); tfAmount.requestFocus(); return; }
        double amount;
        try {
            amount = Double.parseDouble(amtStr);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            showError("Amount must be a positive number (e.g. 250.00).");
            tfAmount.requestFocus(); return;
        }

        // Validate date
        String dateStr = tfDate.getText().trim();
        if (dateStr.isEmpty()) { showError("Date is required."); tfDate.requestFocus(); return; }
        Date expDate;
        try {
            expDate = Date.valueOf(LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE));
        } catch (DateTimeParseException ex) {
            showError("Date must be YYYY-MM-DD format (e.g. 2026-09-15).");
            tfDate.requestFocus(); return;
        }

        // Validate category
        if (cbCategory.getSelectedItem() == null) {
            showError("Please select a category."); cbCategory.requestFocus(); return;
        }
        Category cat   = (Category)    cbCategory.getSelectedItem();
        PaymentMode mode = (PaymentMode) cbMode.getSelectedItem();

        Expense exp = new Expense();
        exp.setAmount(amount);
        exp.setExpenseDate(expDate);
        exp.setCategoryId(cat.getCategoryId());
        if (mode != null) exp.setModeId(mode.getModeId());
        exp.setNotes(taNotes.getText().trim());

        try {
            int id = expenseDAO.addExpense(exp);
            showSuccess("✅  Expense saved! (ID: " + id + ")");
            clearForm();
            loadHistory(null, null);
        } catch (Exception ex) {
            showError("DB Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void handleDeleteExpense() {
        int row = historyTable.getSelectedRow();
        if (row < 0) { showError("Select a row to delete."); return; }
        int expId = (int) tableModel.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete Expense #" + expId + "?  This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (expenseDAO.deleteExpense(expId)) {
                    showSuccess("Expense #" + expId + " deleted.");
                    loadHistory(parseDateField(tfFilterFrom), parseDateField(tfFilterTo));
                } else showError("Record not found.");
            } catch (Exception ex) {
                showError("Delete failed: " + ex.getMessage()); ex.printStackTrace();
            }
        }
    }

    // ════════════════════════════════════════════════════════════════════════
    //  DAO → UI helpers
    // ════════════════════════════════════════════════════════════════════════
    private void loadFormDropdowns() {
        try {
            List<Category>    cats  = categoryDAO.getAllCategories();
            List<PaymentMode> modes = categoryDAO.getAllPaymentModes();

            cbCategory.removeAllItems();
            for (Category c : cats) cbCategory.addItem(c);

            cbMode.removeAllItems();
            for (PaymentMode m : modes) cbMode.addItem(m);

            setStatus("Loaded " + cats.size() + " categories, " + modes.size() + " payment modes.");
        } catch (Exception ex) {
            showError("Could not load dropdowns: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void loadHistory(Date from, Date to) {
        tableModel.setRowCount(0);
        try {
            List<Expense> list = (from == null && to == null)
                    ? expenseDAO.getAllExpenses()
                    : expenseDAO.getExpensesByDateRange(from, to);

            rupeeFormat.setMaximumFractionDigits(2);
            for (Expense e : list) {
                tableModel.addRow(new Object[]{
                    e.getExpenseId(),
                    e.getExpenseDate().toString(),
                    e.getCategoryName(),
                    e.getModeName(),
                    rupeeFormat.format(e.getAmount()),
                    e.getNotes()
                });
            }
            setStatus("History: " + list.size() + " record(s) loaded.");
        } catch (Exception ex) {
            showError("Cannot load history: " + ex.getMessage()); ex.printStackTrace();
        }
    }

    private void refreshTrends(Date from, Date to) {
        if (trendsPanel != null) trendsPanel.refreshData(from, to);
    }

    private void clearForm() {
        tfAmount.setText("");
        tfDate.setText(LocalDate.now().toString());
        if (cbCategory.getItemCount() > 0) cbCategory.setSelectedIndex(0);
        if (cbMode.getItemCount()     > 0) cbMode.setSelectedIndex(0);
        taNotes.setText("");
        tfAmount.requestFocus();
        setStatus("Form cleared.");
    }

    private Date parseDateField(JTextField tf) {
        if (tf == null || tf.getText().trim().isEmpty()) return null;
        try {
            return Date.valueOf(
                    LocalDate.parse(tf.getText().trim(), DateTimeFormatter.ISO_LOCAL_DATE));
        } catch (DateTimeParseException e) { return null; }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
        setStatus("⚠  " + msg);
    }

    private void showSuccess(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
        setStatus(msg);
    }

    private void setStatus(String msg) {
        if (lblStatus != null) lblStatus.setText(msg);
    }

    // ════════════════════════════════════════════════════════════════════════
    //  Component factories
    // ════════════════════════════════════════════════════════════════════════
    private JTextField styledField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setFont(FONT_INPUT);
        tf.setBackground(OFF_WHITE);
        tf.setForeground(TEXT_DARK);
        tf.setCaretColor(OCEAN_BLUE);
        tf.setBorder(new CompoundBorder(
                new LineBorder(BORDER_COL, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    private <T> JComboBox<T> styledCombo() {
        JComboBox<T> cb = new JComboBox<>();
        cb.setFont(FONT_INPUT);
        cb.setBackground(OFF_WHITE);
        cb.setForeground(TEXT_DARK);
        cb.setBorder(new LineBorder(BORDER_COL, 1, true));
        cb.setPreferredSize(new Dimension(240, 34));
        cb.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        return cb;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(TEXT_MUTED);
        return l;
    }

    private JButton aquaButton(String text, Color color) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = getModel().isPressed() ? color.darker()
                        : getModel().isRollover() ? color.brighter() : color;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(FONT_BTN);
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setForeground(TEXT_DARK);
        table.setBackground(OFF_WHITE);
        table.setGridColor(BORDER_COL);
        table.setRowHeight(27);
        table.setSelectionBackground(AQUA_BRIGHT);
        table.setSelectionForeground(Color.WHITE);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(8, 4));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Alternating row colours
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) setBackground(row % 2 == 0 ? OFF_WHITE : ROW_ALT);
                setForeground(TEXT_DARK);
                setBorder(new EmptyBorder(0, 8, 0, 8));
                return this;
            }
        });

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
        header.setBackground(OCEAN_BLUE);
        header.setForeground(Color.WHITE);
        header.setBorder(new LineBorder(AQUA_MID, 1));

        // Column widths
        int[] widths = {50, 100, 140, 110, 110, 260};
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }
}
