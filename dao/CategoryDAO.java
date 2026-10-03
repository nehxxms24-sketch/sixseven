package dao;

import config.DatabaseConnection;
import model.Category;
import model.PaymentMode;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * CategoryDAO – Data-Access Object for CATEGORIES and PAYMENT_MODES.
 *
 * All methods use PreparedStatements to prevent SQL injection.
 * Every method opens a fresh connection via try-with-resources,
 * guaranteeing the connection is always closed regardless of exceptions.
 */
public class CategoryDAO {

    // ══════════════════════════════════════════════════════════════════════════
    //  CATEGORY CRUD
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Returns all categories ordered by type then name.
     */
    public List<Category> getAllCategories() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, category_name, category_type " +
                     "FROM CATEGORIES ORDER BY category_type, category_name";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name"),
                        rs.getString("category_type")));
            }
        }
        return list;
    }

    /**
     * Returns only EXPENSE-type categories.
     */
    public List<Category> getExpenseCategories() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, category_name, category_type " +
                     "FROM CATEGORIES WHERE category_type = 'EXPENSE' " +
                     "ORDER BY category_name";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name"),
                        rs.getString("category_type")));
            }
        }
        return list;
    }

    /**
     * Inserts a new category and returns the generated category_id.
     *
     * @param name unique name (max 50 chars)
     * @param type "EXPENSE" or "INCOME"
     * @return the auto-generated PK
     */
    public int addCategory(String name, String type) throws SQLException {
        String sql = "INSERT INTO CATEGORIES (category_name, category_type) VALUES (?, ?)";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, new String[]{"CATEGORY_ID"})) {

            ps.setString(1, name.trim());
            ps.setString(2, type.toUpperCase());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Failed to retrieve generated category_id");
    }

    /**
     * Deletes a category by PK (cascades to EXPENSES via FK ON DELETE CASCADE).
     */
    public void deleteCategory(int categoryId) throws SQLException {
        String sql = "DELETE FROM CATEGORIES WHERE category_id = ?";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, categoryId);
            ps.executeUpdate();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PAYMENT MODE QUERIES
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Returns all payment modes ordered by mode_id.
     */
    public List<PaymentMode> getAllPaymentModes() throws SQLException {
        List<PaymentMode> list = new ArrayList<>();
        String sql = "SELECT mode_id, mode_name FROM PAYMENT_MODES ORDER BY mode_id";

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new PaymentMode(
                        rs.getInt("mode_id"),
                        rs.getString("mode_name")));
            }
        }
        return list;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  AGGREGATION – used by TrendsChartPanel
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Returns category-wise total spending for EXPENSE-type categories,
     * ordered descending by total.  Optionally filtered by date range.
     *
     * @param startDate inclusive start (null = no lower bound)
     * @param endDate   inclusive end   (null = no upper bound)
     * @return list of Object[]{String categoryName, Double total}
     */
    public List<Object[]> getCategoryTotals(Date startDate, Date endDate) throws SQLException {
        List<Object[]> result = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT c.category_name, SUM(e.amount) AS total " +
            "FROM EXPENSES e JOIN CATEGORIES c ON e.category_id = c.category_id " +
            "WHERE c.category_type = 'EXPENSE' ");

        if (startDate != null) sql.append("AND e.expense_date >= ? ");
        if (endDate   != null) sql.append("AND e.expense_date <= ? ");
        sql.append("GROUP BY c.category_name ORDER BY total DESC");

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int idx = 1;
            if (startDate != null) ps.setDate(idx++, startDate);
            if (endDate   != null) ps.setDate(idx,   endDate);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[]{
                        rs.getString("category_name"),
                        rs.getDouble("total")
                    });
                }
            }
        }
        return result;
    }

    /**
     * Returns the overall total of all EXPENSE entries optionally within a date range.
     */
    public double getTotalExpenses(Date startDate, Date endDate) throws SQLException {
        StringBuilder sql = new StringBuilder(
            "SELECT NVL(SUM(e.amount), 0) AS total " +
            "FROM EXPENSES e JOIN CATEGORIES c ON e.category_id = c.category_id " +
            "WHERE c.category_type = 'EXPENSE' ");

        if (startDate != null) sql.append("AND e.expense_date >= ? ");
        if (endDate   != null) sql.append("AND e.expense_date <= ? ");

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int idx = 1;
            if (startDate != null) ps.setDate(idx++, startDate);
            if (endDate   != null) ps.setDate(idx,   endDate);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        }
        return 0.0;
    }
}
