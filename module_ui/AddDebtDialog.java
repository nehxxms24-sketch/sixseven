package module_ui;

import module_core_logic.Debt;
import module_core_logic.ExpenseService;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/**
 * MODULE 3 – Swing & AWT UI Layer (Member 3)
 * AddDebtDialog (Phase 4): Modal dialog for recording a new debt or loan entry.
 */
public class AddDebtDialog extends JDialog {

    private final ExpenseService expenseService;
    private final Runnable onDebtSavedCallback;

    private JTextField txtPersonName;
    private JComboBox<String> cbDebtType;
    private JTextField txtPrincipal;
    private JTextField txtInterestRate;
    private JTextField txtDueDate;
    private JTextField txtNotes;
    private JLabel lblStatus;

    public AddDebtDialog(Window owner, ExpenseService expenseService, Runnable onDebtSavedCallback) {
        super(owner, "🤝 Add Debt / Loan Entry", ModalityType.APPLICATION_MODAL);
        this.expenseService = expenseService;
        this.onDebtSavedCallback = onDebtSavedCallback;

        setSize(420, 540);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());
        getContentPane().setBackground(AquaTheme.CARD_BG);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(AquaTheme.GRADIENT_TOP);
        headerPanel.setBorder(AquaTheme.padding(12, 16, 12, 16));

        JLabel titleLbl = new JLabel("🤝 Record New Debt or Loan", SwingConstants.LEFT);
        titleLbl.setFont(AquaTheme.FONT_ARIAL_HEADER);
        titleLbl.setForeground(Color.WHITE);

        headerPanel.add(titleLbl, BorderLayout.WEST);
        add(headerPanel, BorderLayout.NORTH);

        // Form Container Panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(AquaTheme.CARD_BG);
        formPanel.setBorder(AquaTheme.padding(16, 20, 16, 20));

        // Person Name Field
        formPanel.add(createLabel("Person Name"));
        txtPersonName = new JTextField();
        txtPersonName.setFont(AquaTheme.FONT_ARIAL_FIELD);
        txtPersonName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        formPanel.add(txtPersonName);
        formPanel.add(Box.createVerticalStrut(10));

        // Debt Type Dropdown
        formPanel.add(createLabel("Debt Type"));
        cbDebtType = new JComboBox<>(new String[]{"BORROWED", "LENT"});
        cbDebtType.setFont(AquaTheme.FONT_ARIAL_FIELD);
        cbDebtType.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        formPanel.add(cbDebtType);
        formPanel.add(Box.createVerticalStrut(10));

        // Principal Amount Field
        formPanel.add(createLabel("Principal Amount (₹)"));
        txtPrincipal = new JTextField();
        txtPrincipal.setFont(AquaTheme.FONT_ARIAL_FIELD);
        txtPrincipal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        formPanel.add(txtPrincipal);
        formPanel.add(Box.createVerticalStrut(10));

        // Interest Rate Field
        formPanel.add(createLabel("Interest Rate (%)"));
        txtInterestRate = new JTextField("0.0");
        txtInterestRate.setFont(AquaTheme.FONT_ARIAL_FIELD);
        txtInterestRate.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        formPanel.add(txtInterestRate);
        formPanel.add(Box.createVerticalStrut(10));

        // Due Date Row
        formPanel.add(createLabel("Due Date (YYYY-MM-DD)"));
        JPanel dateRow = new JPanel(new BorderLayout(8, 0));
        dateRow.setBackground(AquaTheme.CARD_BG);
        dateRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        txtDueDate = new JTextField(LocalDate.now().plusDays(7).toString());
        txtDueDate.setFont(AquaTheme.FONT_ARIAL_FIELD);

        JButton btnPicker = new JButton("📅 Choose Date");
        btnPicker.setFont(AquaTheme.FONT_ARIAL_BUTTON);
        btnPicker.setForeground(AquaTheme.GRADIENT_BOTTOM);
        btnPicker.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPicker.addActionListener(e -> {
            CalendarDialog cal = new CalendarDialog(this, txtDueDate.getText().trim(), selectedDate -> {
                txtDueDate.setText(selectedDate);
            });
            cal.setVisible(true);
        });

        dateRow.add(txtDueDate, BorderLayout.CENTER);
        dateRow.add(btnPicker, BorderLayout.EAST);
        formPanel.add(dateRow);
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

        // Action Buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actionPanel.setBackground(AquaTheme.CARD_BG);
        actionPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 235, 240)));

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(AquaTheme.FONT_ARIAL_BUTTON);
        btnCancel.setForeground(AquaTheme.TEXT_MUTED);
        btnCancel.addActionListener(e -> dispose());

        JButton btnSave = AquaTheme.createCyanButton("Save Entry");
        btnSave.addActionListener(e -> saveDebt());

        actionPanel.add(btnCancel);
        actionPanel.add(btnSave);

        add(actionPanel, BorderLayout.SOUTH);
    }

    private JLabel createLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(AquaTheme.FONT_ARIAL_LABEL);
        lbl.setForeground(AquaTheme.TEXT_DARK);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private void saveDebt() {
        lblStatus.setText(" ");
        String name = txtPersonName.getText().trim();
        String type = (String) cbDebtType.getSelectedItem();
        String principalStr = txtPrincipal.getText().trim();
        String rateStr = txtInterestRate.getText().trim();
        String dueDateStr = txtDueDate.getText().trim();
        String notesStr = txtNotes.getText().trim();

        if (name.isEmpty() || principalStr.isEmpty() || dueDateStr.isEmpty()) {
            lblStatus.setText("Please fill all required fields!");
            return;
        }

        double principal, rate;
        try {
            principal = Double.parseDouble(principalStr);
            rate = Double.parseDouble(rateStr);
            if (principal <= 0 || rate < 0) {
                lblStatus.setText("Principal must be > 0 and rate >= 0!");
                return;
            }
        } catch (NumberFormatException e) {
            lblStatus.setText("Invalid numeric format!");
            return;
        }

        Debt debt = new Debt();
        debt.setPersonName(name);
        debt.setDebtType(type);
        debt.setPrincipalAmount(principal);
        debt.setInterestRate(rate);
        debt.setDueDate(dueDateStr);
        debt.setStatus("PENDING");
        debt.setNotes(notesStr);

        boolean success = expenseService.addDebt(debt);
        if (success) {
            if (onDebtSavedCallback != null) {
                onDebtSavedCallback.run();
            }
            dispose();
        } else {
            lblStatus.setText("Failed to save debt entry.");
        }
    }
}
