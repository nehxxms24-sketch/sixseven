package module_db_dao;

import module_core_logic.Expense;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MODULE 1 – DB/DAO Layer
 * ExpenseDAO: Data Access Object for EXPENSES table in Oracle DB.
 * Uses PreparedStatements for all SQL queries to prevent SQL injection.
 * Supports soft-deletes (is_deleted column), trash bin recovery, hard deletes, and favorites.
 */
public class ExpenseDAO {

    public boolean insertExpense(Expense expense) {
        String sql = "INSERT INTO EXPENSES (amount, category_id, payment_mode_id, expense_date, notes, is_deleted, is_starred) " +
                     "VALUES (?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?, 0, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, expense.getAmount());
            pstmt.setInt(2, expense.getCategoryId());
            pstmt.setInt(3, expense.getPaymentModeId());
            pstmt.setString(4, expense.getExpenseDate());
            pstmt.setString(5, expense.getNotes() != null ? expense.getNotes() : "");
            pstmt.setInt(6, expense.isStarred() ? 1 : 0);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] Error inserting expense: " + e.getMessage());
            return false;
        }
    }

    public List<Expense> getAllActiveExpenses() {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT e.expense_id, e.amount, e.category_id, c.category_name, " +
                     "e.payment_mode_id, p.mode_name, TO_CHAR(e.expense_date, 'YYYY-MM-DD') AS exp_date, " +
                     "e.notes, NVL(e.is_deleted, 0) AS is_deleted, NVL(e.is_starred, 0) AS is_starred " +
                     "FROM EXPENSES e " +
                     "LEFT JOIN CATEGORIES c ON e.category_id = c.category_id " +
                     "LEFT JOIN PAYMENT_MODES p ON e.payment_mode_id = p.payment_mode_id " +
                     "WHERE NVL(e.is_deleted, 0) = 0 " +
                     "ORDER BY e.expense_date DESC, e.expense_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Expense exp = new Expense();
                exp.setId(rs.getInt("expense_id"));
                exp.setAmount(rs.getDouble("amount"));
                exp.setCategoryId(rs.getInt("category_id"));
                exp.setCategoryName(rs.getString("category_name"));
                exp.setPaymentModeId(rs.getInt("payment_mode_id"));
                exp.setPaymentModeName(rs.getString("mode_name"));
                exp.setExpenseDate(rs.getString("exp_date"));
                exp.setNotes(rs.getString("notes"));
                exp.setDeleted(rs.getInt("is_deleted") == 1);
                exp.setStarred(rs.getInt("is_starred") == 1);
                list.add(exp);
            }
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] Error fetching expenses: " + e.getMessage());
        }
        return list;
    }

    public List<Expense> getDeletedExpenses() {
        List<Expense> list = new ArrayList<>();
        String sql = "SELECT e.expense_id, e.amount, e.category_id, c.category_name, " +
                     "e.payment_mode_id, p.mode_name, TO_CHAR(e.expense_date, 'YYYY-MM-DD') AS exp_date, " +
                     "e.notes, NVL(e.is_deleted, 0) AS is_deleted, NVL(e.is_starred, 0) AS is_starred " +
                     "FROM EXPENSES e " +
                     "LEFT JOIN CATEGORIES c ON e.category_id = c.category_id " +
                     "LEFT JOIN PAYMENT_MODES p ON e.payment_mode_id = p.payment_mode_id " +
                     "WHERE NVL(e.is_deleted, 0) = 1 " +
                     "ORDER BY e.expense_date DESC, e.expense_id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Expense exp = new Expense();
                exp.setId(rs.getInt("expense_id"));
                exp.setAmount(rs.getDouble("amount"));
                exp.setCategoryId(rs.getInt("category_id"));
                exp.setCategoryName(rs.getString("category_name"));
                exp.setPaymentModeId(rs.getInt("payment_mode_id"));
                exp.setPaymentModeName(rs.getString("mode_name"));
                exp.setExpenseDate(rs.getString("exp_date"));
                exp.setNotes(rs.getString("notes"));
                exp.setDeleted(true);
                exp.setStarred(rs.getInt("is_starred") == 1);
                list.add(exp);
            }
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] Error fetching deleted expenses: " + e.getMessage());
        }
        return list;
    }

    public boolean updateExpense(Expense expense) {
        String sql = "UPDATE EXPENSES SET amount = ?, category_id = ?, payment_mode_id = ?, " +
                     "expense_date = TO_DATE(?, 'YYYY-MM-DD'), notes = ?, is_starred = ? " +
                     "WHERE expense_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, expense.getAmount());
            pstmt.setInt(2, expense.getCategoryId());
            pstmt.setInt(3, expense.getPaymentModeId());
            pstmt.setString(4, expense.getExpenseDate());
            pstmt.setString(5, expense.getNotes());
            pstmt.setInt(6, expense.isStarred() ? 1 : 0);
            pstmt.setInt(7, expense.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] Error updating expense: " + e.getMessage());
            return false;
        }
    }

    public boolean softDeleteExpense(int expenseId) {
        String sql = "UPDATE EXPENSES SET is_deleted = 1 WHERE expense_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, expenseId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] Error soft-deleting expense: " + e.getMessage());
            return false;
        }
    }

    public boolean restoreExpense(int expenseId) {
        String sql = "UPDATE EXPENSES SET is_deleted = 0 WHERE expense_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, expenseId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] Error restoring expense: " + e.getMessage());
            return false;
        }
    }

    public boolean hardDeleteExpense(int expenseId) {
        String sql = "DELETE FROM EXPENSES WHERE expense_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, expenseId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] Error hard-deleting expense: " + e.getMessage());
            return false;
        }
    }

    public boolean toggleStarred(int expenseId, boolean isStarred) {
        String sql = "UPDATE EXPENSES SET is_starred = ? WHERE expense_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, isStarred ? 1 : 0);
            pstmt.setInt(2, expenseId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] Error toggling star: " + e.getMessage());
            return false;
        }
    }

    public Map<String, Double> getCategoryTotals() {
        Map<String, Double> totals = new HashMap<>();
        String sql = "SELECT NVL(c.category_name, 'Uncategorized') AS cat_name, SUM(e.amount) AS total " +
                     "FROM EXPENSES e " +
                     "LEFT JOIN CATEGORIES c ON e.category_id = c.category_id " +
                     "WHERE NVL(e.is_deleted, 0) = 0 " +
                     "GROUP BY c.category_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                totals.put(rs.getString("cat_name"), rs.getDouble("total"));
            }
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] Error getting category totals: " + e.getMessage());
        }
        return totals;
    }

    public double getTotalSpent() {
        String sql = "SELECT SUM(amount) AS total FROM EXPENSES WHERE NVL(is_deleted, 0) = 0";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) return rs.getDouble("total");
        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] Error calculating total spent: " + e.getMessage());
        }
        return 0.0;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  seedCompleteHistory() — Oracle DB seed with explicit COMMIT
    // ══════════════════════════════════════════════════════════════════════

    /**
     * Seeds comprehensive multi-year expense history into Oracle DB.
     *
     * Guard: checks COUNT where year=2026 AND month < 10.
     *        If > 0, skips silently (idempotent).
     * Uses ROWNUM=1 subqueries to resolve category_id and payment_mode_id by name.
     * Sets is_deleted=0 and is_starred=0 for all rows.
     * Calls conn.commit() explicitly after the batch — the fix for silent skips.
     * Safe to call every startup; no-ops when DB is offline.
     */
    public static void seedCompleteHistory() {
        if (!DatabaseConnection.isDatabaseAvailable()) {
            System.out.println("[ExpenseDAO] seedCompleteHistory: DB offline — skipping.");
            return;
        }

        final String GUARD_SQL =
            "SELECT COUNT(*) FROM EXPENSES " +
            "WHERE EXTRACT(YEAR FROM expense_date) = 2026 " +
            "  AND EXTRACT(MONTH FROM expense_date) < 10";

        final String INSERT_SQL =
            "INSERT INTO EXPENSES " +
            "  (amount, expense_date, category_id, payment_mode_id, notes, is_deleted, is_starred) " +
            "VALUES " +
            "  (?, TO_DATE(?, 'YYYY-MM-DD'), " +
            "   (SELECT category_id    FROM CATEGORIES   WHERE UPPER(category_name) LIKE UPPER(?) AND ROWNUM=1), " +
            "   (SELECT payment_mode_id FROM PAYMENT_MODES WHERE UPPER(mode_name)   LIKE UPPER(?) AND ROWNUM=1), " +
            "   ?, 0, 0)";

        // Each row: { amount, date, category-LIKE, mode-LIKE, notes }
        Object[][] data = {
            // 2026 Jan – Sep  (18 records, 2 per month)
            { 1450.00, "2026-01-12", "Food%",     "UPI%",          "New Year Dinner & Groceries" },
            { 2100.00, "2026-01-25", "Bills%",    "Net Banking%",  "WiFi & Electricity"          },
            {  650.00, "2026-02-08", "Travel%",   "Cash%",         "Metro & Cab"                 },
            { 1800.00, "2026-02-14", "Food%",     "Credit Card%",  "Cafe Outing"                 },
            { 3200.00, "2026-03-05", "Shopping%", "UPI%",          "Books & Semester Notes"      },
            {  900.00, "2026-03-22", "Health%",   "Debit Card%",   "Pharmacy & Vitamins"         },
            { 1750.00, "2026-04-10", "Bills%",    "Net Banking%",  "Water & Gas Bill"            },
            { 1200.00, "2026-04-28", "Travel%",   "UPI%",          "Weekend Trip Bus"            },
            {  850.00, "2026-05-15", "Food%",     "UPI%",          "Team Snacks & Ice Cream"     },
            { 2600.00, "2026-05-30", "Shopping%", "Credit Card%",  "Summer Clothes"              },
            { 1100.00, "2026-06-11", "Health%",   "Cash%",         "Routine Dental Check"        },
            { 1950.00, "2026-06-24", "Bills%",    "UPI%",          "AC Power Bill"               },
            {  750.00, "2026-07-09", "Food%",     "Debit Card%",   "Monsoon Street Food"         },
            {  400.00, "2026-07-21", "Travel%",   "Cash%",         "Fuel"                        },
            { 2300.00, "2026-08-14", "Shopping%", "UPI%",          "Independence Day Sale"       },
            { 1600.00, "2026-08-29", "Bills%",    "Net Banking%",  "Mobile Postpaid"             },
            { 1350.00, "2026-09-12", "Food%",     "UPI%",          "Family Lunch"                },
            { 1500.00, "2026-09-25", "Travel%",   "Credit Card%",  "Train Reservation"           },
            // 2024 records (3)
            { 3500.00, "2024-03-15", "Bills%",    "Net Banking%",  "Annual Maintenance"          },
            { 4200.00, "2024-07-20", "Travel%",   "Credit Card%",  "Vacation Travel"             },
            { 2900.00, "2024-11-10", "Shopping%", "UPI%",          "Festive Shopping"            },
            // 2025 records (4)
            { 1250.00, "2025-02-18", "Food%",     "UPI%",          "Dinner Outing"               },
            { 2400.00, "2025-06-25", "Bills%",    "Net Banking%",  "Summer Power Charges"        },
            { 3100.00, "2025-09-14", "Shopping%", "Debit Card%",   "Gadget Accessories"          },
            { 1500.00, "2025-11-28", "Health%",   "Cash%",         "Health Insurance Check"      },
        };

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // ← manual commit mode

            // Guard check
            int existing = 0;
            try (PreparedStatement chk = conn.prepareStatement(GUARD_SQL);
                 ResultSet rs = chk.executeQuery()) {
                if (rs.next()) existing = rs.getInt(1);
            }

            if (existing > 0) {
                System.out.println("[ExpenseDAO] seedCompleteHistory: " + existing +
                        " rows for 2026 Jan-Sep already present — skipping.");
                return; // autoCommit restored in finally
            }

            System.out.println("[ExpenseDAO] seedCompleteHistory: seeding " + data.length + " records...");

            int inserted = 0;
            try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL)) {
                for (Object[] row : data) {
                    ps.setDouble(1, (Double)  row[0]);
                    ps.setString(2, (String)  row[1]);
                    ps.setString(3, (String)  row[2]);
                    ps.setString(4, (String)  row[3]);
                    ps.setString(5, (String)  row[4]);
                    ps.addBatch();
                }
                int[] results = ps.executeBatch();
                for (int r : results) if (r > 0) inserted++;
            }

            conn.commit(); // ← explicit COMMIT — guaranteed persistence
            System.out.println("[ExpenseDAO] seedCompleteHistory: COMMITTED " + inserted + " rows.");

        } catch (SQLException e) {
            System.err.println("[ExpenseDAO] seedCompleteHistory FAILED: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
        } finally {
            try {
                if (conn != null) { conn.setAutoCommit(true); conn.close(); }
            } catch (SQLException ignored) {}
        }
    }
}
