package dao;

import config.DatabaseConnection;
import model.Expense;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ExpenseDAO – Data-Access Object for the EXPENSES table.
 *
 * All DB operations use PreparedStatements (no string concatenation in SQL).
 * Connections are obtained per-method and closed via try-with-resources.
 */
public class ExpenseDAO {

    // ── shared JOIN query fragment ────────────────────────────────────────────
    private static final String SELECT_WITH_JOINS =
        "SELECT e.expense_id, e.amount, e.expense_date, " +
        "       e.category_id, c.category_name, " +
        "       e.mode_id,     NVL(p.mode_name, 'N/A') AS mode_name, " +
        "       NVL(e.notes, '') AS notes " +
        "FROM EXPENSES e " +
        "JOIN CATEGORIES c ON e.category_id = c.category_id " +
        "LEFT JOIN PAYMENT_MODES p ON e.mode_id = p.mode_id ";

    // ══════════════════════════════════════════════════════════════════════════
    //  CREATE
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Inserts a new expense record and returns the auto-generated expense_id.
     *
     * @param expense Expense object (expenseId is ignored; set by DB IDENTITY)
     * @return the generated expense_id
     * @throws SQLException on DB error or constraint violation
     */
    public int addExpense(Expense expense) throws SQLException {
        String sql = "INSERT INTO EXPENSES (amount, expense_date, category_id, mode_id, notes) " +
                     "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"EXPENSE_ID"})) {

            ps.setDouble(1, expense.getAmount());
            ps.setDate  (2, expense.getExpenseDate());
            ps.setInt   (3, expense.getCategoryId());

            if (expense.getModeId() > 0) {
                ps.setInt(4, expense.getModeId());
            } else {
                ps.setNull(4, Types.NUMERIC);
            }

            String notes = expense.getNotes();
            if (notes != null && !notes.isBlank()) {
                ps.setString(5, notes.trim());
            } else {
                ps.setNull(5, Types.VARCHAR);
            }

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Failed to retrieve generated expense_id");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  READ – full history (most recent first)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Returns all expenses, newest first.
     */
    public List<Expense> getAllExpenses() throws SQLException {
        String sql = SELECT_WITH_JOINS + "ORDER BY e.expense_date DESC, e.expense_id DESC";
        return executeExpenseQuery(sql, null, null);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  READ – date-range filter
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Returns expenses whose expense_date is within [startDate, endDate].
     * Either bound may be null (meaning unbounded).
     *
     * @param startDate inclusive lower bound (may be null)
     * @param endDate   inclusive upper bound (may be null)
     */
    public List<Expense> getExpensesByDateRange(Date startDate, Date endDate)
            throws SQLException {

        StringBuilder sql = new StringBuilder(SELECT_WITH_JOINS).append("WHERE 1=1 ");
        if (startDate != null) sql.append("AND e.expense_date >= ? ");
        if (endDate   != null) sql.append("AND e.expense_date <= ? ");
        sql.append("ORDER BY e.expense_date DESC, e.expense_id DESC");

        return executeExpenseQuery(sql.toString(), startDate, endDate);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  READ – single record
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Fetches a single expense by PK. Returns null if not found.
     */
    public Expense getExpenseById(int expenseId) throws SQLException {
        String sql = SELECT_WITH_JOINS + "WHERE e.expense_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, expenseId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UPDATE
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Updates all mutable fields of an existing expense.
     *
     * @param expense must have a valid expenseId
     * @return true if a row was updated
     */
    public boolean updateExpense(Expense expense) throws SQLException {
        String sql = "UPDATE EXPENSES SET amount = ?, expense_date = ?, " +
                     "category_id = ?, mode_id = ?, notes = ? " +
                     "WHERE expense_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, expense.getAmount());
            ps.setDate  (2, expense.getExpenseDate());
            ps.setInt   (3, expense.getCategoryId());

            if (expense.getModeId() > 0) {
                ps.setInt(4, expense.getModeId());
            } else {
                ps.setNull(4, Types.NUMERIC);
            }

            String notes = expense.getNotes();
            if (notes != null && !notes.isBlank()) {
                ps.setString(5, notes.trim());
            } else {
                ps.setNull(5, Types.VARCHAR);
            }

            ps.setInt(6, expense.getExpenseId());
            return ps.executeUpdate() > 0;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  DELETE
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Deletes an expense record by PK.
     *
     * @return true if a row was deleted
     */
    public boolean deleteExpense(int expenseId) throws SQLException {
        String sql = "DELETE FROM EXPENSES WHERE expense_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, expenseId);
            return ps.executeUpdate() > 0;
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Executes a parameterised expense query and maps every ResultSet row to
     * an {@link Expense}.  startDate / endDate are bound in order if non-null.
     */
    private List<Expense> executeExpenseQuery(String sql, Date startDate, Date endDate)
            throws SQLException {

        List<Expense> list = new ArrayList<>();

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int idx = 1;
            if (startDate != null) ps.setDate(idx++, startDate);
            if (endDate   != null) ps.setDate(idx,   endDate);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Maps the current ResultSet row to an {@link Expense} object. */
    private Expense mapRow(ResultSet rs) throws SQLException {
        return new Expense(
                rs.getInt   ("expense_id"),
                rs.getDouble("amount"),
                rs.getDate  ("expense_date"),
                rs.getInt   ("category_id"),
                rs.getString("category_name"),
                rs.getInt   ("mode_id"),
                rs.getString("mode_name"),
                rs.getString("notes"));
    }
}
