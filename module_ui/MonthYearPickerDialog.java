package module_ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * MonthYearPickerDialog — lightweight modal that lets the user pick any
 * Month + Year and fires a callback with (month 1-12, year).
 *
 * Displayed when the user clicks the "Pick Month/Year" calendar button
 * in TransactionsPanel or TrendsChartPanel.
 */
public class MonthYearPickerDialog extends JDialog {

    private static final String[] MONTH_NAMES = {
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    };

    public MonthYearPickerDialog(Window owner, int initialMonth, int initialYear,
                                 BiConsumer<Integer, Integer> onPicked) {
        super(owner, "Pick Month & Year", ModalityType.APPLICATION_MODAL);
        setSize(300, 220);
        setLocationRelativeTo(owner);
        setResizable(false);

        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(AquaTheme.CRISP_WHITE);
        panel.setBorder(AquaTheme.padding(16, 20, 16, 20));

        // ── Header ────────────────────────────────────────────────────────────
        JLabel title = new JLabel("Select Month & Year", SwingConstants.CENTER);
        title.setFont(AquaTheme.FONT_ARIAL_HEADER);
        title.setForeground(AquaTheme.DEEP_SLATE);
        panel.add(title, BorderLayout.NORTH);

        // ── Controls ──────────────────────────────────────────────────────────
        JPanel controls = new JPanel(new GridLayout(2, 2, 10, 10));
        controls.setBackground(AquaTheme.CRISP_WHITE);

        JLabel monthLbl = new JLabel("Month:", SwingConstants.LEFT);
        monthLbl.setFont(AquaTheme.FONT_ARIAL_LABEL);

        JComboBox<String> cbMonth = new JComboBox<>(MONTH_NAMES);
        cbMonth.setFont(AquaTheme.FONT_ARIAL_FIELD);
        cbMonth.setSelectedIndex(Math.max(0, Math.min(11, initialMonth - 1)));

        JLabel yearLbl = new JLabel("Year:", SwingConstants.LEFT);
        yearLbl.setFont(AquaTheme.FONT_ARIAL_LABEL);

        JComboBox<String> cbYear = new JComboBox<>(buildYearArray());
        cbYear.setFont(AquaTheme.FONT_ARIAL_FIELD);
        cbYear.setSelectedItem(String.valueOf(initialYear));

        controls.add(monthLbl);  controls.add(cbMonth);
        controls.add(yearLbl);   controls.add(cbYear);
        panel.add(controls, BorderLayout.CENTER);

        // ── Buttons ───────────────────────────────────────────────────────────
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setBackground(AquaTheme.CRISP_WHITE);

        JButton btnCancel = new JButton("Cancel");
        btnCancel.setFont(AquaTheme.FONT_ARIAL_BUTTON);
        btnCancel.addActionListener(e -> dispose());

        JButton btnOK = AquaTheme.createCyanButton("Apply");
        btnOK.addActionListener(e -> {
            int month = cbMonth.getSelectedIndex() + 1;
            int year  = Integer.parseInt((String) cbYear.getSelectedItem());
            if (onPicked != null) onPicked.accept(month, year);
            dispose();
        });

        btnRow.add(btnCancel);
        btnRow.add(btnOK);
        panel.add(btnRow, BorderLayout.SOUTH);

        setContentPane(panel);
    }

    /**
     * Builds a String[] from current year down to 2020.
     * No future years are included.
     */
    public static String[] buildYearArray() {
        int current = LocalDate.now().getYear();
        int oldest  = 2020;
        String[] arr = new String[current - oldest + 1];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = String.valueOf(current - i);
        }
        return arr;
    }
}
