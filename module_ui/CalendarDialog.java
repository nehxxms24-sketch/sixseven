package module_ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * MODULE 3 – Swing & AWT UI Layer
 * CalendarDialog (Phase 2): Custom date picker dialog.
 * Features Month/Year navigation, day selection grid (1-31), and YYYY-MM-DD formatting.
 */
public class CalendarDialog extends JDialog {

    private YearMonth currentYearMonth;
    private final Consumer<String> onDateSelectedCallback;

    private JLabel lblMonthYear;
    private JPanel dayGridPanel;

    public CalendarDialog(Window owner, String initialDate, Consumer<String> onDateSelectedCallback) {
        super(owner, "📅 Choose Date", ModalityType.APPLICATION_MODAL);
        this.onDateSelectedCallback = onDateSelectedCallback;

        LocalDate initDate;
        try {
            initDate = LocalDate.parse(initialDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            initDate = LocalDate.now();
        }
        this.currentYearMonth = YearMonth.from(initDate);

        setSize(320, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(0, 8));
        getContentPane().setBackground(AquaTheme.CARD_BG);

        // Header Navigation: [<] [Month Year] [>]
        JPanel navPanel = new JPanel(new BorderLayout());
        navPanel.setBackground(AquaTheme.GRADIENT_BOTTOM);
        navPanel.setBorder(AquaTheme.padding(8, 10, 8, 10));

        JButton btnPrev = new JButton("◀");
        btnPrev.setFont(AquaTheme.FONT_ARIAL_BUTTON);
        btnPrev.setForeground(Color.WHITE);
        btnPrev.setFocusPainted(false);
        btnPrev.setContentAreaFilled(false);
        btnPrev.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPrev.addActionListener(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendar();
        });

        lblMonthYear = new JLabel("", SwingConstants.CENTER);
        lblMonthYear.setFont(AquaTheme.FONT_ARIAL_HEADER);
        lblMonthYear.setForeground(Color.WHITE);

        JButton btnNext = new JButton("▶");
        btnNext.setFont(AquaTheme.FONT_ARIAL_BUTTON);
        btnNext.setForeground(Color.WHITE);
        btnNext.setFocusPainted(false);
        btnNext.setContentAreaFilled(false);
        btnNext.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNext.addActionListener(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendar();
        });

        navPanel.add(btnPrev, BorderLayout.WEST);
        navPanel.add(lblMonthYear, BorderLayout.CENTER);
        navPanel.add(btnNext, BorderLayout.EAST);

        add(navPanel, BorderLayout.NORTH);

        // Days Header Panel (Sun, Mon, Tue...)
        JPanel headerDaysPanel = new JPanel(new GridLayout(1, 7, 2, 2));
        headerDaysPanel.setBackground(AquaTheme.CARD_BG);
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (String dayName : dayNames) {
            JLabel lbl = new JLabel(dayName, SwingConstants.CENTER);
            lbl.setFont(AquaTheme.FONT_ARIAL_LABEL);
            lbl.setForeground(AquaTheme.TEXT_MUTED);
            headerDaysPanel.add(lbl);
        }

        // Center Wrapper
        JPanel centerPanel = new JPanel(new BorderLayout(0, 4));
        centerPanel.setBackground(AquaTheme.CARD_BG);
        centerPanel.setBorder(AquaTheme.padding(0, 8, 8, 8));
        centerPanel.add(headerDaysPanel, BorderLayout.NORTH);

        dayGridPanel = new JPanel(new GridLayout(6, 7, 3, 3));
        dayGridPanel.setBackground(AquaTheme.CARD_BG);
        centerPanel.add(dayGridPanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        updateCalendar();
    }

    private void updateCalendar() {
        lblMonthYear.setText(currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        dayGridPanel.removeAll();

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Sunday = 0
        int daysInMonth = currentYearMonth.lengthOfMonth();

        // Empty lead labels
        for (int i = 0; i < dayOfWeek; i++) {
            dayGridPanel.add(new JLabel(""));
        }

        LocalDate today = LocalDate.now();

        // Days 1..daysInMonth
        for (int day = 1; day <= daysInMonth; day++) {
            final int dayNum = day;
            LocalDate date = currentYearMonth.atDay(dayNum);

            JButton dayBtn = new JButton(String.valueOf(dayNum));
            dayBtn.setFont(AquaTheme.FONT_ARIAL_SUB);
            dayBtn.setFocusPainted(false);
            dayBtn.setMargin(new Insets(2, 2, 2, 2));
            dayBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

            if (date.equals(today)) {
                dayBtn.setBackground(AquaTheme.BUTTON_CYAN);
                dayBtn.setForeground(Color.WHITE);
                dayBtn.setFont(AquaTheme.FONT_ARIAL_BUTTON);
            } else {
                dayBtn.setBackground(new Color(245, 250, 252));
                dayBtn.setForeground(AquaTheme.TEXT_DARK);
            }

            dayBtn.addActionListener(e -> {
                String formattedDate = String.format("%04d-%02d-%02d",
                        currentYearMonth.getYear(), currentYearMonth.getMonthValue(), dayNum);
                if (onDateSelectedCallback != null) {
                    onDateSelectedCallback.accept(formattedDate);
                }
                dispose();
            });

            dayGridPanel.add(dayBtn);
        }

        // Fill remaining slots
        int totalCells = dayOfWeek + daysInMonth;
        for (int i = totalCells; i < 42; i++) {
            dayGridPanel.add(new JLabel(""));
        }

        dayGridPanel.revalidate();
        dayGridPanel.repaint();
    }
}
