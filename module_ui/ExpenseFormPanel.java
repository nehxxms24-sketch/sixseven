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
 * ExpenseFormPanel: Input panel for recording new expenses.
 * Uses BoxLayout and GridBagLayout with proper vertical component sizing.
 */
public class ExpenseFormPanel extends JPanel {

    private final ExpenseService expenseService;
    private final Runnable onExpenseAddedCallback;

    private JTextField txtDate;
    private JTextField txtAmount;
    private JComboBox<Category> cbCategory;
    private JComboBox<PaymentMode> cbPaymentMode;
    private JTextField txtNotes;
    private JLabel lblFormStatus;

    public ExpenseFormPanel(ExpenseService expenseService, Runnable onExpenseAddedCallback) {
        this.expenseService = expenseService;
        this.onExpenseAddedCallback = onExpenseAddedCallback;

        setBackground(AquaTheme.CARD_BG);
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(215, 228, 235), 1, true),
                AquaTheme.padding(16, 16, 16, 16)
        ));

        // Form Title
        JLabel titleLbl = new JLabel("➕ Add New Expense", SwingConstants.LEFT);
        titleLbl.setFont(AquaTheme.FONT_SECTION);
        titleLbl.setForeground(AquaTheme.OCEAN_BLUE);
        titleLbl.setBorder(AquaTheme.padding(0, 0, 12, 0));

        add(titleLbl, BorderLayout.NORTH);

        // Center Fields Container
        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setBackground(AquaTheme.CARD_BG);

        // Date Field
        fieldsPanel.add(createLabel("Date (YYYY-MM-DD)"));
        txtDate = new JTextField(LocalDate.now().toString());
        styleField(txtDate);
        fieldsPanel.add(txtDate);
        fieldsPanel.add(Box.createVerticalStrut(10));

        // Amount Field
        fieldsPanel.add(createLabel("Amount (₹)"));
        txtAmount = new JTextField();
        styleField(txtAmount);
        fieldsPanel.add(txtAmount);
        fieldsPanel.add(Box.createVerticalStrut(10));

        // Category Dropdown
        fieldsPanel.add(createLabel("Category"));
        cbCategory = new JComboBox<>();
        styleCombo(cbCategory);
        fieldsPanel.add(cbCategory);
        fieldsPanel.add(Box.createVerticalStrut(10));

        // Payment Mode Dropdown
        fieldsPanel.add(createLabel("Payment Mode"));
        cbPaymentMode = new JComboBox<>();
        styleCombo(cbPaymentMode);
        fieldsPanel.add(cbPaymentMode);
        fieldsPanel.add(Box.createVerticalStrut(10));

        // Notes Field
        fieldsPanel.add(createLabel("Notes / Remarks"));
        txtNotes = new JTextField();
        styleField(txtNotes);
        fieldsPanel.add(txtNotes);
        fieldsPanel.add(Box.createVerticalStrut(12));

        // Status Feedback
        lblFormStatus = new JLabel(" ", SwingConstants.LEFT);
        lblFormStatus.setFont(AquaTheme.FONT_SUBTITLE);
        lblFormStatus.setForeground(AquaTheme.DANGER_RED);
        fieldsPanel.add(lblFormStatus);
        fieldsPanel.add(Box.createVerticalStrut(12));

        // Action Buttons Panel
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setBackground(AquaTheme.CARD_BG);

        JButton btnClear = new JButton("Clear");
        btnClear.setFont(AquaTheme.FONT_BUTTON);
        btnClear.setForeground(AquaTheme.TEXT_MUTED);
        btnClear.setFocusPainted(false);
        btnClear.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClear.addActionListener(e -> clearForm());

        JButton btnSubmit = AquaTheme.createAquaButton("Save Expense");
        btnSubmit.addActionListener(e -> submitExpense());

        btnPanel.add(btnClear);
        btnPanel.add(btnSubmit);

        fieldsPanel.add(btnPanel);

        add(fieldsPanel, BorderLayout.CENTER);

        // Load dropdown items
        loadDropdownData();
    }

    public void loadDropdownData() {
        cbCategory.removeAllItems();
        List<Category> categories = expenseService.getCategories();
        for (Category cat : categories) {
            cbCategory.addItem(cat);
        }

        cbPaymentMode.removeAllItems();
        List<PaymentMode> modes = expenseService.getPaymentModes();
        for (PaymentMode mode : modes) {
            cbPaymentMode.addItem(mode);
        }
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AquaTheme.FONT_LABEL);
        lbl.setForeground(AquaTheme.TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void styleField(JTextField tf) {
        tf.setFont(AquaTheme.FONT_FIELD);
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private <T> void styleCombo(JComboBox<T> combo) {
        combo.setFont(AquaTheme.FONT_FIELD);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        combo.setBackground(Color.WHITE);
    }

    private void submitExpense() {
        lblFormStatus.setText(" ");
        String dateStr = txtDate.getText().trim();
        String amountStr = txtAmount.getText().trim();
        Category selectedCat = (Category) cbCategory.getSelectedItem();
        PaymentMode selectedMode = (PaymentMode) cbPaymentMode.getSelectedItem();
        String notesStr = txtNotes.getText().trim();

        if (dateStr.isEmpty() || amountStr.isEmpty() || selectedCat == null || selectedMode == null) {
            lblFormStatus.setForeground(AquaTheme.DANGER_RED);
            lblFormStatus.setText("Please fill all required fields!");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                lblFormStatus.setForeground(AquaTheme.DANGER_RED);
                lblFormStatus.setText("Amount must be greater than zero!");
                return;
            }
        } catch (NumberFormatException e) {
            lblFormStatus.setForeground(AquaTheme.DANGER_RED);
            lblFormStatus.setText("Invalid amount format!");
            return;
        }

        Expense newExp = new Expense();
        newExp.setExpenseDate(dateStr);
        newExp.setAmount(amount);
        newExp.setCategoryId(selectedCat.getId());
        newExp.setCategoryName(selectedCat.getName());
        newExp.setPaymentModeId(selectedMode.getId());
        newExp.setPaymentModeName(selectedMode.getName());
        newExp.setNotes(notesStr);

        boolean success = expenseService.addExpense(newExp);
        if (success) {
            lblFormStatus.setForeground(AquaTheme.SUCCESS_GREEN);
            lblFormStatus.setText("✓ Expense recorded successfully!");
            txtAmount.setText("");
            txtNotes.setText("");
            if (onExpenseAddedCallback != null) {
                onExpenseAddedCallback.run();
            }
        } else {
            lblFormStatus.setForeground(AquaTheme.DANGER_RED);
            lblFormStatus.setText("Failed to save expense.");
        }
    }

    private void clearForm() {
        txtDate.setText(LocalDate.now().toString());
        txtAmount.setText("");
        txtNotes.setText("");
        lblFormStatus.setText(" ");
        if (cbCategory.getItemCount() > 0) cbCategory.setSelectedIndex(0);
        if (cbPaymentMode.getItemCount() > 0) cbPaymentMode.setSelectedIndex(0);
    }
}
