package module_ui;

import module_core_logic.Category;
import module_core_logic.Expense;
import module_core_logic.ExpenseService;
import module_core_logic.PaymentMode;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * MODULE 3 – Swing & AWT UI Layer
 * AddExpenseDialog: Modal dialog for adding new expenses styled in Money Palette.
 */
public class AddExpenseDialog extends JDialog {

    private final ExpenseService expenseService;
    private final Runnable onExpenseSavedCallback;

    private JTextField txtDate;
    private JTextField txtAmount;
    private JComboBox<Object> cbCategory;
    private JTextField txtCustomCategory;
    private JPanel customCatPanel;
    private JComboBox<PaymentMode> cbPaymentMode;
    private JTextField txtNotes;
    private JLabel lblStatus;

    private static final String OTHER_CUSTOM = "Others (Custom)...";

    public AddExpenseDialog(Window owner, ExpenseService expenseService, Runnable onExpenseSavedCallback) {
        super(owner, "+ Add Expense", ModalityType.APPLICATION_MODAL);
        this.expenseService = expenseService;
        this.onExpenseSavedCallback = onExpenseSavedCallback;

        setSize(420, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(AquaTheme.PURE_WHITE);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AquaTheme.DEEP_FOREST_GREEN);
        headerPanel.setBorder(AquaTheme.padding(12, 16, 12, 16));

        JLabel titleLbl = new JLabel("💳 Record New Expense", SwingConstants.LEFT);
        titleLbl.setFont(AquaTheme.FONT_ARIAL_HEADER);
        titleLbl.setForeground(Color.WHITE);

        headerPanel.add(titleLbl, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Form Container Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(AquaTheme.PURE_WHITE);
        formPanel.setBorder(AquaTheme.padding(16, 20, 16, 20));

        // Date Selection Row
        formPanel.add(createLabel("Date (YYYY-MM-DD)"));
        JPanel dateRow = new JPanel(new BorderLayout(8, 0));
        dateRow.setBackground(AquaTheme.PURE_WHITE);
        dateRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        txtDate = new JTextField(LocalDate.now().toString());
        txtDate.setFont(AquaTheme.FONT_ARIAL_FIELD);

        JButton btnPicker = new JButton("📅 Choose Date");
        btnPicker.setFont(AquaTheme.FONT_ARIAL_BUTTON);
        btnPicker.setForeground(AquaTheme.DEEP_FOREST_GREEN);
        btnPicker.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPicker.addActionListener(e -> {
            CalendarDialog cal = new CalendarDialog(this, txtDate.getText().trim(), selectedDate -> {
                txtDate.setText(selectedDate);
            });
            cal.setVisible(true);
        });

        dateRow.add(txtDate, BorderLayout.CENTER);
        dateRow.add(btnPicker, BorderLayout.EAST);
        formPanel.add(dateRow);
        formPanel.add(Box.createVerticalStrut(10));

        // Amount Field
        formPanel.add(createLabel("Amount (₹)"));
        txtAmount = new JTextField();
        txtAmount.setFont(AquaTheme.FONT_ARIAL_FIELD);
        txtAmount.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        formPanel.add(txtAmount);
        formPanel.add(Box.createVerticalStrut(10));

        // Category Dropdown
        formPanel.add(createLabel("Category"));
        cbCategory = new JComboBox<>();
        cbCategory.setFont(AquaTheme.FONT_ARIAL_FIELD);
        cbCategory.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cbCategory.addActionListener(e -> onCategorySelectionChanged());
        formPanel.add(cbCategory);
        formPanel.add(Box.createVerticalStrut(6));

        // Custom Category Reveal Container
        customCatPanel = new JPanel(new BorderLayout(5, 0));
        customCatPanel.setBackground(AquaTheme.PURE_WHITE);
        customCatPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        customCatPanel.setVisible(false);

        JLabel customLbl = new JLabel("New Name: ");
        customLbl.setFont(AquaTheme.FONT_ARIAL_LABEL);
        txtCustomCategory = new JTextField();
        txtCustomCategory.setFont(AquaTheme.FONT_ARIAL_FIELD);

        customCatPanel.add(customLbl, BorderLayout.WEST);
        customCatPanel.add(txtCustomCategory, BorderLayout.CENTER);

        formPanel.add(customCatPanel);
        formPanel.add(Box.createVerticalStrut(10));

        // Payment Mode Dropdown
        formPanel.add(createLabel("Payment Mode"));
        cbPaymentMode = new JComboBox<>();
        cbPaymentMode.setFont(AquaTheme.FONT_ARIAL_FIELD);
        cbPaymentMode.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        formPanel.add(cbPaymentMode);
        formPanel.add(Box.createVerticalStrut(10));

        // Notes / Remarks Field
        formPanel.add(createLabel("Notes / Remarks"));
        txtNotes = new JTextField();
        txtNotes.setFont(AquaTheme.FONT_ARIAL_FIELD);
        txtNotes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        formPanel.add(txtNotes);
        formPanel.add(Box.createVerticalStrut(10));

        // Status Feedback
        lblStatus = new JLabel(" ", SwingConstants.LEFT);
        lblStatus.setFont(AquaTheme.FONT_ARIAL_SUB);
        lblStatus.setForeground(AquaTheme.DANGER_RED);
        formPanel.add(lblStatus);

        add(formPanel, BorderLayout.CENTER);

        // Bottom Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setBackground(AquaTheme.PURE_WHITE);
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, AquaTheme.VINTAGE_CREAM));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(AquaTheme.FONT_ARIAL_BUTTON);
        btnCancel.setForeground(AquaTheme.SAGE_GREEN);
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = AquaTheme.createCyanButton("Save Expense");
        btnSave.addActionListener(e -> saveExpense());

        actionPanel.add(btnCancel);
        actionPanel.add(btnSave);

        add(actionPanel, BorderLayout.SOUTH);

        populateDropdowns();
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AquaTheme.FONT_ARIAL_LABEL);
        lbl.setForeground(AquaTheme.TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void populateDropdowns() {
        cbCategory.removeAllItems();
        List<Category> categories = expenseService.getCategories();
        for (Category c : categories) {
            cbCategory.addItem(c);
        }
        cbCategory.addItem(OTHER_CUSTOM);

        cbPaymentMode.removeAllItems();
        List<PaymentMode> modes = expenseService.getPaymentModes();
        for (PaymentMode m : modes) {
            cbPaymentMode.addItem(m);
        }
    }

    private void onCategorySelectionChanged() {
        Object selected = cbCategory.getSelectedItem();
        boolean isOther = OTHER_CUSTOM.equals(selected);
        customCatPanel.setVisible(isOther);
        revalidate();
        repaint();
    }

    private void saveExpense() {
        lblStatus.setText(" ");
        String dateStr = txtDate.getText().trim();
        String amountStr = txtAmount.getText().trim();
        Object catObj = cbCategory.getSelectedItem();
        PaymentMode modeObj = (PaymentMode) cbPaymentMode.getSelectedItem();
        String notesStr = txtNotes.getText().trim();

        if (dateStr.isEmpty() || amountStr.isEmpty() || catObj == null || modeObj == null) {
            lblStatus.setText("Please fill all required fields!");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                lblStatus.setText("Amount must be greater than zero!");
                return;
            }
        } catch (NumberFormatException e) {
            lblStatus.setText("Invalid amount format!");
            return;
        }

        Category categoryToUse;
        if (OTHER_CUSTOM.equals(catObj)) {
            String customName = txtCustomCategory.getText().trim();
            if (customName.isEmpty()) {
                lblStatus.setText("Please enter a custom category name!");
                return;
            }
            categoryToUse = expenseService.addCustomCategory(customName);
            if (categoryToUse == null) {
                lblStatus.setText("Failed to save custom category.");
                return;
            }
        } else {
            categoryToUse = (Category) catObj;
        }

        Expense newExp = new Expense();
        newExp.setExpenseDate(dateStr);
        newExp.setAmount(amount);
        newExp.setCategoryId(categoryToUse.getId());
        newExp.setCategoryName(categoryToUse.getName());
        newExp.setPaymentModeId(modeObj.getId());
        newExp.setPaymentModeName(modeObj.getName());
        newExp.setNotes(notesStr);

        boolean success = expenseService.addExpense(newExp);
        if (success) {
            if (onExpenseSavedCallback != null) {
                onExpenseSavedCallback.run();
            }
            dispose();
        } else {
            lblStatus.setText("Failed to save expense to database.");
        }
    }
}
