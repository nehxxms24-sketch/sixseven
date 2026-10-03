package module_core_logic;

import module_db_dao.CategoryDAO;
import module_db_dao.DatabaseConnection;
import module_db_dao.DebtDAO;
import module_db_dao.ExpenseDAO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;

/**
 * MODULE 2 – Business Logic & Core Models (Member 2)
 * ExpenseService: Central service coordinator with Phase 4 Debts & Loans integration.
 */
public class ExpenseService {

    private final CategoryDAO categoryDAO;
    private final ExpenseDAO expenseDAO;
    private final DebtDAO debtDAO;

    // In-memory fallbacks when DB is unavailable
    private final List<Category> fallbackCategories;
    private final List<PaymentMode> fallbackPaymentModes;
    private final List<Expense> inMemoryExpenses;
    private final List<Debt> inMemoryDebts;

    private int inMemoryIdCounter = 100;
    private int inMemoryCatCounter = 50;
    private int inMemoryDebtCounter = 20;

    public ExpenseService() {
        this.categoryDAO = new CategoryDAO();
        this.expenseDAO = new ExpenseDAO();
        this.debtDAO = new DebtDAO();
        this.inMemoryExpenses = new ArrayList<>();
        this.inMemoryDebts = new ArrayList<>();

        this.fallbackCategories = new ArrayList<>();
        String[] defaultCats = {
            "Food & Dining", "Travel & Commute", "Bills & Utilities",
            "Shopping", "Entertainment", "Health", "Education", "Groceries", "Debt Repayment"
        };
        for (int i = 0; i < defaultCats.length; i++) {
            fallbackCategories.add(new Category(i + 1, defaultCats[i], "EXPENSE"));
        }

        this.fallbackPaymentModes = new ArrayList<>();
        String[] defaultModes = {"UPI", "Cash", "Debit Card", "Credit Card", "Net Banking"};
        for (int i = 0; i < defaultModes.length; i++) {
            fallbackPaymentModes.add(new PaymentMode(i + 1, defaultModes[i]));
        }

        populateSampleInMemoryData();
    }

    private void populateSampleInMemoryData() {
        // Seed 3 recent records for today's month so dashboard metrics work on first launch
        inMemoryExpenses.add(new Expense(1, 450.00,  1, "Food & Dining",     1, "UPI",
                LocalDate.now().toString(),              "Lunch with team",      false, true));
        inMemoryExpenses.add(new Expense(2, 1200.00, 3, "Bills & Utilities",  5, "Net Banking",
                LocalDate.now().minusDays(1).toString(), "Electricity Bill",     false, false));
        inMemoryExpenses.add(new Expense(3, 150.00,  2, "Travel & Commute",   2, "Cash",
                LocalDate.now().minusDays(2).toString(), "Cab fare",             false, false));

        inMemoryDebts.add(new Debt(1, "Rahul Sharma", "BORROWED", 2500.00, 2.5,
                LocalDate.now().plusDays(7).toString(),  "PENDING",  "Emergency cash"));
        inMemoryDebts.add(new Debt(2, "Ananya Roy",   "LENT",     1500.00, 0.0,
                LocalDate.now().minusDays(2).toString(), "OVERDUE",  "Textbook purchase"));
    }

    /**
     * Clears the in-memory store and loads 21 diverse records across 2024, 2025, and 2026 (Jan-Sep).
     * Works regardless of whether Oracle DB is connected.
     * Called by the UI "Seed Demo Data" button and also via seedDemoData().
     */
    public void forceSeedMultiYearData() {
        inMemoryExpenses.clear();
        inMemoryIdCounter = 0;

        // helper to add records cleanly
        addMem(3200.00, "Bills & Utilities", 3, "Net Banking", 5, "2024-03-12", "Annual Insurance");
        addMem(4500.00, "Travel & Commute",  2, "Credit Card", 4, "2024-06-20", "Flight Booking");
        addMem(2800.00, "Shopping",          4, "Debit Card",  3, "2024-09-15", "Festival Clothes");
        addMem( 850.00, "Food & Dining",     1, "UPI",         1, "2024-11-05", "Family Dinner");

        addMem(1500.00, "Health",            6, "UPI",         1, "2025-01-10", "Medical Checkup");
        addMem(1850.00, "Food & Dining",     1, "Credit Card", 4, "2025-02-14", "Celebration Dinner");
        addMem(2100.00, "Bills & Utilities", 3, "Net Banking", 5, "2025-04-05", "Summer Electricity");
        addMem( 650.00, "Travel & Commute",  2, "Cash",        2, "2025-05-18", "Train Ticket");
        addMem(3400.00, "Shopping",          4, "UPI",         1, "2025-07-22", "Laptop Accessories");
        addMem( 420.00, "Food & Dining",     1, "UPI",         1, "2025-08-30", "Weekend Cafe");
        addMem(1950.00, "Bills & Utilities", 3, "Net Banking", 5, "2025-10-12", "Internet & Water");
        addMem(2200.00, "Entertainment",     5, "Credit Card", 4, "2025-12-25", "Year-End Party");

        addMem( 920.00, "Food & Dining",     1, "UPI",         1, "2026-01-08", "Grocery Restock");
        addMem( 300.00, "Travel & Commute",  2, "Cash",        2, "2026-02-19", "Fuel");
        addMem(4000.00, "Education",         7, "Net Banking", 5, "2026-03-11", "Semester Material");
        addMem(1650.00, "Bills & Utilities", 3, "UPI",         1, "2026-04-14", "Mobile Postpaid & DTH");
        addMem(1100.00, "Food & Dining",     1, "Debit Card",  3, "2026-05-20", "Team Lunch");
        addMem( 800.00, "Health",            6, "Cash",        2, "2026-06-16", "Dental Consultation");
        addMem(2750.00, "Shopping",          4, "UPI",         1, "2026-07-25", "Monsoon Gear");
        addMem( 550.00, "Travel & Commute",  2, "UPI",         1, "2026-08-10", "Cab Rides");
        addMem(1400.00, "Bills & Utilities", 3, "Net Banking", 5, "2026-09-28", "Monthly Power Bill");

        // Keep today's records so current-month metrics are non-zero
        addMem( 450.00, "Food & Dining",     1, "UPI",         1, LocalDate.now().toString(),              "Lunch with team");
        addMem(1200.00, "Bills & Utilities", 3, "Net Banking", 5, LocalDate.now().minusDays(1).toString(), "Electricity Bill Oct");
        addMem( 150.00, "Travel & Commute",  2, "Cash",        2, LocalDate.now().minusDays(2).toString(), "Cab fare");

        System.out.println("[ExpenseService] forceSeedMultiYearData: loaded " + inMemoryExpenses.size() + " records.");
    }

    /** Convenience alias for the UI button. */
    public void seedDemoData() {
        forceSeedMultiYearData();
    }

    /** Internal helper: appends an Expense to inMemoryExpenses. */
    private void addMem(double amount, String catName, int catId,
                        String modeName, int modeId, String date, String notes) {
        inMemoryExpenses.add(new Expense(
                ++inMemoryIdCounter, amount, catId, catName,
                modeId, modeName, date, notes, false, false));
    }


    public boolean isDbConnected() {
        return DatabaseConnection.isDatabaseAvailable();
    }

    public List<Category> getCategories() {
        if (isDbConnected()) {
            try {
                List<Category> dbCats = categoryDAO.getAllCategories();
                if (dbCats != null && !dbCats.isEmpty()) {
                    return dbCats;
                }
            } catch (SQLException e) {
                System.err.println("[ExpenseService] Error fetching categories: " + e.getMessage());
            }
        }
        return fallbackCategories;
    }

    public Category addCustomCategory(String categoryName) {
        String trimmed = categoryName.trim();
        if (trimmed.isEmpty()) return null;

        for (Category c : getCategories()) {
            if (c.getName().equalsIgnoreCase(trimmed)) {
                return c;
            }
        }

        if (isDbConnected()) {
            try {
                int newId = categoryDAO.addCategory(trimmed, "EXPENSE");
                return new Category(newId, trimmed, "EXPENSE");
            } catch (SQLException e) {
                System.err.println("[ExpenseService] Failed to insert custom category: " + e.getMessage());
            }
        }

        Category cat = new Category(++inMemoryCatCounter, trimmed, "EXPENSE");
        fallbackCategories.add(cat);
        return cat;
    }

    public List<PaymentMode> getPaymentModes() {
        if (isDbConnected()) {
            try {
                List<PaymentMode> dbModes = categoryDAO.getAllPaymentModes();
                if (dbModes != null && !dbModes.isEmpty()) {
                    return dbModes;
                }
            } catch (SQLException e) {
                System.err.println("[ExpenseService] Error fetching modes: " + e.getMessage());
            }
        }
        return fallbackPaymentModes;
    }

    public List<Expense> getActiveExpenses() {
        if (isDbConnected()) {
            return expenseDAO.getAllActiveExpenses();
        }
        List<Expense> active = new ArrayList<>();
        for (Expense e : inMemoryExpenses) {
            if (!e.isDeleted()) {
                active.add(e);
            }
        }
        return active;
    }

    public List<Expense> getDeletedExpenses() {
        if (isDbConnected()) {
            return expenseDAO.getDeletedExpenses();
        }
        List<Expense> deleted = new ArrayList<>();
        for (Expense e : inMemoryExpenses) {
            if (e.isDeleted()) {
                deleted.add(e);
            }
        }
        return deleted;
    }

    public boolean addExpense(Expense expense) {
        if (isDbConnected()) {
            return expenseDAO.insertExpense(expense);
        } else {
            expense.setId(++inMemoryIdCounter);
            inMemoryExpenses.add(0, expense);
            return true;
        }
    }

    public boolean updateExpense(Expense expense) {
        if (isDbConnected()) {
            return expenseDAO.updateExpense(expense);
        } else {
            for (int i = 0; i < inMemoryExpenses.size(); i++) {
                if (inMemoryExpenses.get(i).getId() == expense.getId()) {
                    inMemoryExpenses.set(i, expense);
                    return true;
                }
            }
            return false;
        }
    }

    public boolean deleteExpense(int expenseId) {
        if (isDbConnected()) {
            return expenseDAO.softDeleteExpense(expenseId);
        } else {
            for (Expense e : inMemoryExpenses) {
                if (e.getId() == expenseId) {
                    e.setDeleted(true);
                    return true;
                }
            }
            return false;
        }
    }

    public boolean restoreExpense(int expenseId) {
        if (isDbConnected()) {
            return expenseDAO.restoreExpense(expenseId);
        } else {
            for (Expense e : inMemoryExpenses) {
                if (e.getId() == expenseId) {
                    e.setDeleted(false);
                    return true;
                }
            }
            return false;
        }
    }

    public boolean hardDeleteExpense(int expenseId) {
        if (isDbConnected()) {
            return expenseDAO.hardDeleteExpense(expenseId);
        } else {
            return inMemoryExpenses.removeIf(e -> e.getId() == expenseId);
        }
    }

    public boolean toggleStar(int expenseId, boolean currentStarStatus) {
        boolean newStatus = !currentStarStatus;
        if (isDbConnected()) {
            return expenseDAO.toggleStarred(expenseId, newStatus);
        } else {
            for (Expense e : inMemoryExpenses) {
                if (e.getId() == expenseId) {
                    e.setStarred(newStatus);
                    return true;
                }
            }
            return false;
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  DEBTS & LOANS (Phase 4)
    // ══════════════════════════════════════════════════════════════════

    public List<Debt> getDebts() {
        if (isDbConnected()) {
            List<Debt> list = debtDAO.getAllDebts();
            if (list != null && !list.isEmpty()) {
                updateDebtOverdueStatuses(list);
                return list;
            }
        }
        updateDebtOverdueStatuses(inMemoryDebts);
        return inMemoryDebts;
    }

    private void updateDebtOverdueStatuses(List<Debt> debts) {
        LocalDate today = LocalDate.now();
        for (Debt d : debts) {
            if ("PENDING".equals(d.getStatus()) && d.getDueDate() != null) {
                try {
                    LocalDate due = LocalDate.parse(d.getDueDate());
                    if (due.isBefore(today)) {
                        d.setStatus("OVERDUE");
                        if (isDbConnected()) {
                            debtDAO.updateDebtStatus(d.getId(), "OVERDUE");
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    public boolean addDebt(Debt debt) {
        if (isDbConnected()) {
            return debtDAO.insertDebt(debt);
        } else {
            debt.setId(++inMemoryDebtCounter);
            inMemoryDebts.add(0, debt);
            return true;
        }
    }

    public boolean markDebtAsRepaid(Debt debt, boolean logAsExpense) {
        debt.setStatus("REPAID");
        boolean updated = isDbConnected() ? debtDAO.updateDebtStatus(debt.getId(), "REPAID") : true;

        if (updated && logAsExpense) {
            // Find or create "Debt Repayment" category
            Category debtCat = addCustomCategory("Debt Repayment");

            Expense exp = new Expense();
            exp.setAmount(debt.getTotalDue());
            exp.setExpenseDate(LocalDate.now().toString());
            exp.setCategoryId(debtCat != null ? debtCat.getId() : 1);
            exp.setCategoryName(debtCat != null ? debtCat.getName() : "Debt Repayment");
            exp.setPaymentModeId(1); // UPI default
            exp.setPaymentModeName("UPI");
            exp.setNotes("Repayment for " + debt.getDebtType() + " (" + debt.getPersonName() + ")");

            addExpense(exp);
        }

        return updated;
    }

    public Map<String, Double> getCategoryTotals() {
        if (isDbConnected()) {
            return expenseDAO.getCategoryTotals();
        }
        Map<String, Double> totals = new HashMap<>();
        for (Expense e : inMemoryExpenses) {
            if (!e.isDeleted()) {
                String cat = e.getCategoryName() != null ? e.getCategoryName() : "Other";
                totals.put(cat, totals.getOrDefault(cat, 0.0) + e.getAmount());
            }
        }
        return totals;
    }

    public double getThisMonthSpent() {
        LocalDate now = LocalDate.now();
        String currentYearMonth = String.format("%04d-%02d", now.getYear(), now.getMonthValue());

        List<Expense> active = getActiveExpenses();
        double sum = 0.0;
        for (Expense e : active) {
            if (e.getExpenseDate() != null && e.getExpenseDate().startsWith(currentYearMonth)) {
                sum += e.getAmount();
            }
        }
        return sum;
    }

    public int getThisMonthTransactionCount() {
        LocalDate now = LocalDate.now();
        String currentYearMonth = String.format("%04d-%02d", now.getYear(), now.getMonthValue());

        List<Expense> active = getActiveExpenses();
        int count = 0;
        for (Expense e : active) {
            if (e.getExpenseDate() != null && e.getExpenseDate().startsWith(currentYearMonth)) {
                count++;
            }
        }
        return count;
    }

    public double getTotalSpent() {
        List<Expense> expenses = getActiveExpenses();
        double sum = 0;
        for (Expense e : expenses) {
            sum += e.getAmount();
        }
        return sum;
    }

    /**
     * Returns the total amount LENT in the given month (1–12) and year.
     * Filters by due_date on the DB path; by Debt.dueDate on the in-memory path.
     * Excludes REPAID debts.
     */
    public double getDebtLentForPeriod(int month, int year) {
        if (isDbConnected()) {
            return debtDAO.getDebtSumByTypeAndMonth("LENT", month, year);
        }
        double sum = 0.0;
        for (Debt d : inMemoryDebts) {
            if ("LENT".equalsIgnoreCase(d.getDebtType()) && !"REPAID".equalsIgnoreCase(d.getStatus())
                    && matchesPeriod(d.getDueDate(), month, year)) {
                sum += d.getPrincipalAmount();
            }
        }
        return sum;
    }

    /**
     * Returns the total amount BORROWED in the given month (1–12) and year.
     */
    public double getDebtBorrowedForPeriod(int month, int year) {
        if (isDbConnected()) {
            return debtDAO.getDebtSumByTypeAndMonth("BORROWED", month, year);
        }
        double sum = 0.0;
        for (Debt d : inMemoryDebts) {
            if ("BORROWED".equalsIgnoreCase(d.getDebtType()) && !"REPAID".equalsIgnoreCase(d.getStatus())
                    && matchesPeriod(d.getDueDate(), month, year)) {
                sum += d.getPrincipalAmount();
            }
        }
        return sum;
    }

    /**
     * Returns the total amount LENT in the given year (all months).
     */
    public double getDebtLentForYear(int year) {
        if (isDbConnected()) {
            return debtDAO.getDebtSumByTypeAndYear("LENT", year);
        }
        double sum = 0.0;
        for (Debt d : inMemoryDebts) {
            if ("LENT".equalsIgnoreCase(d.getDebtType()) && !"REPAID".equalsIgnoreCase(d.getStatus())
                    && matchesYear(d.getDueDate(), year)) {
                sum += d.getPrincipalAmount();
            }
        }
        return sum;
    }

    /**
     * Returns the total amount BORROWED in the given year (all months).
     */
    public double getDebtBorrowedForYear(int year) {
        if (isDbConnected()) {
            return debtDAO.getDebtSumByTypeAndYear("BORROWED", year);
        }
        double sum = 0.0;
        for (Debt d : inMemoryDebts) {
            if ("BORROWED".equalsIgnoreCase(d.getDebtType()) && !"REPAID".equalsIgnoreCase(d.getStatus())
                    && matchesYear(d.getDueDate(), year)) {
                sum += d.getPrincipalAmount();
            }
        }
        return sum;
    }

    private boolean matchesPeriod(String dateStr, int month, int year) {
        if (dateStr == null) return false;
        try {
            LocalDate d = LocalDate.parse(dateStr);
            return d.getMonthValue() == month && d.getYear() == year;
        } catch (Exception e) { return false; }
    }

    private boolean matchesYear(String dateStr, int year) {
        if (dateStr == null) return false;
        try { return LocalDate.parse(dateStr).getYear() == year; }
        catch (Exception e) { return false; }
    }
}

