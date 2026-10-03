package module_ui;

import module_core_logic.Category;
import module_core_logic.Expense;
import module_core_logic.ExpenseService;
import module_core_logic.PaymentMode;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * MODULE 3 – Swing & AWT UI Layer
 * EditExpenseDialog (Phase 3): Modal dialog for editing an existing expense record.
 * Prefills selected record data and updates Oracle DB via ExpenseService.updateExpense().
 */
public class EditExpenseDialog extends JDialog {

    private final ExpenseService expenseService;
    private final Expense targetExpense;
    private final Runnable onExpenseUpdatedCallback;

    private JTextField txtDate;
    private JTextField txtAmount;
    private JComboBox<Category> cbCategory;
    private JComboBox<PaymentMode> cbPaymentMode;
    private JTextField txtNotes;
    private JCheckBox chkStarred;
    private JLabel lblStatus;

    public EditExpenseDialog(Window owner, ExpenseService expenseService, Expense targetExpense, Runnable onExpenseUpdatedCallback) {
        super(owner, "✏️ Edit Expense #" + targetExpense.getId(), ModalityType.APPLICATION_MODAL);
        this.expenseService = expenseService;
        this.targetExpense = targetExpense;
        this.onExpenseUpdatedCallback = onExpenseUpdatedCallback;

        setSize(420, 520);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(AquaTheme.CARD_BG);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AquaTheme.GRADIENT_BOTTOM);
        headerPanel.setBorder(AquaTheme.padding(12, 16, 12, 16));

        JLabel titleLbl = new JLabel("✏️ Modify Expense #" + targetExpense.getId(), SwingConstants.LEFT);
        titleLbl.setFont(AquaTheme.FONT_ARIAL_HEADER);
        titleLbl.setForeground(Color.WHITE);

        headerPanel.add(titleLbl, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(AquaTheme.CARD_BG);
        formPanel.setBorder(AquaTheme.padding(16, 20, 16, 20));

        // Date Row
        formPanel.add(createLabel("Date (YYYY-MM-DD)"));
        JPanel dateRow = new JPanel(new BorderLayout(8, 0));
        dateRow.setBackground(AquaTheme.CARD_BG);
        dateRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        txtDate = new JTextField(targetExpense.getExpenseDate());
        txtDate.setFont(AquaTheme.FONT_ARIAL_FIELD);

        JButton btnPicker = new JButton("📅 Choose Date");
        btnPicker.setFont(AquaTheme.FONT_ARIAL_BUTTON);
        btnPicker.setForeground(AquaTheme.GRADIENT_BOTTOM);
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
        txtAmount = new JTextField(String.format("%.2f", targetExpense.getAmount()));
        txtAmount.setFont(AquaTheme.FONT_ARIAL_FIELD);
        txtAmount.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        formPanel.add(txtAmount);
        formPanel.add(Box.createVerticalStrut(10));

        // Category Dropdown
        formPanel.add(createLabel("Category"));
        cbCategory = new JComboBox<>();
        cbCategory.setFont(AquaTheme.FONT_ARIAL_FIELD);
        cbCategory.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        formPanel.add(cbCategory);
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
        txtNotes = new JTextField(targetExpense.getNotes() != null ? targetExpense.getNotes() : "");
        txtNotes.setFont(AquaTheme.FONT_ARIAL_FIELD);
        txtNotes.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        formPanel.add(txtNotes);
        formPanel.add(Box.createVerticalStrut(10));

        // Favorite Checkbox
        chkStarred = new JCheckBox("⭐ Mark as Favorite (Starred)");
        chkStarred.setFont(AquaTheme.FONT_ARIAL_LABEL);
        chkStarred.setForeground(AquaTheme.TEXT_DARK);
        chkStarred.setBackground(AquaTheme.CARD_BG);
        chkStarred.setSelected(targetExpense.isStarred());
        chkStarred.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(chkStarred);
        formPanel.add(Box.createVerticalStrut(10));

        // Status Feedback
        lblStatus = new JLabel(" ", SwingConstants.LEFT);
        lblStatus.setFont(AquaTheme.FONT_ARIAL_SUB);
        lblStatus.setForeground(AquaTheme.DANGER_RED);
        formPanel.add(lblStatus);

        add(formPanel, BorderLayout.CENTER);

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setBackground(AquaTheme.CARD_BG);
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 235, 240)));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(AquaTheme.FONT_ARIAL_BUTTON);
        btnCancel.setForeground(AquaTheme.TEXT_MUTED);
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = AquaTheme.createCyanButton("Update Changes");
        btnSave.addActionListener(e -> updateExpense());

        actionPanel.add(btnCancel);
        actionPanel.add(btnSave);

        add(actionPanel, BorderLayout.SOUTH);

        populateAndPrefillDropdowns();
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AquaTheme.FONT_ARIAL_LABEL);
        lbl.setForeground(AquaTheme.TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void populateAndPrefillDropdowns() {
        cbCategory.removeAllItems();
        List<Category> categories = expenseService.getCategories();
        Category selectedCat = null;
        for (Category c : categories) {
            cbCategory.addItem(c);
            if (c.getId() == targetExpense.getCategoryId() || c.getName().equalsIgnoreCase(targetExpense.getCategoryName())) {
                selectedCat = c;
            }
        }
        if (selectedCat != null) cbCategory.setSelectedItem(selectedCat);

        cbPaymentMode.removeAllItems();
        List<PaymentMode> modes = expenseService.getPaymentModes();
        PaymentMode selectedMode = null;
        for (PaymentMode m : modes) {
            cbPaymentMode.addItem(m);
            if (m.getId() == targetExpense.getPaymentModeId() || m.getName().equalsIgnoreCase(targetExpense.getPaymentModeName())) {
                selectedMode = m;
            }
        }
        if (selectedMode != null) cbPaymentMode.setSelectedItem(selectedMode);
    }

    private void updateExpense() {
        lblStatus.setText(" ");
        String dateStr = txtDate.getText().trim();
        String amountStr = txtAmount.getText().trim();
        Category catObj = (Category) cbCategory.getSelectedItem();
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

        targetExpense.setExpenseDate(dateStr);
        targetExpense.setAmount(amount);
        targetExpense.setCategoryId(catObj.getId());
        targetExpense.setCategoryName(catObj.getName());
        targetExpense.setPaymentModeId(modeObj.getId());
        targetExpense.setPaymentModeName(modeObj.getName());
        targetExpense.setNotes(notesStr);
        targetExpense.setStarred(chkStarred.isSelected());

        boolean success = expenseService.updateExpense(targetExpense);
        if (success) {
            if (onExpenseUpdatedCallback != null) {
                onExpenseUpdatedCallback.run();
            }
            dispose();
        } else {
            lblStatus.setText("Failed to update expense record.");
        }
    }
}
