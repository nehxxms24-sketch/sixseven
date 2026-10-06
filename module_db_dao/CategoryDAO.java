package module_db_dao;

import module_core_logic.Category;
import module_core_logic.PaymentMode;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MODULE 1 – DB/DAO Layer
 * CategoryDAO: CRUD + aggregation for CATEGORIES and PAYMENT_MODES.
 */
public class CategoryDAO {

    // ══════════════════════════════════════════════════════════════════
    //  CATEGORIES
    // ══════════════════════════════════════════════════════════════════

    public List<Category> getAllCategories() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT category_id, category_name, category_type " +
                     "FROM CATEGORIES ORDER BY category_type, category_name";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
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

    public int addCategory(String name, String type) throws SQLException {
        String sql = "INSERT INTO CATEGORIES (category_name, category_type) VALUES (?,?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, new String[]{"CATEGORY_ID"})) {
            ps.setString(1, name.trim());
            ps.setString(2, type.toUpperCase());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) return k.getInt(1);
            }
        }
        throw new SQLException("Failed to get generated category_id");
    }

    public boolean updateCategory(int categoryId, String name, String type) throws SQLException {
        String sql = "UPDATE CATEGORIES SET category_name = ?, category_type = ? WHERE category_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, type.toUpperCase());
            ps.setInt(3, categoryId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Deletes only categories that are not referenced by any expense, including trashed expenses. */
    public boolean deleteCategory(int categoryId) throws SQLException {
        String referenceSql = "SELECT COUNT(*) FROM EXPENSES WHERE category_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement referencePs = c.prepareStatement(referenceSql)) {
            referencePs.setInt(1, categoryId);
            try (ResultSet rs = referencePs.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return false;
            }
            String deleteSql = "DELETE FROM CATEGORIES WHERE category_id = ?";
            try (PreparedStatement deletePs = c.prepareStatement(deleteSql)) {
                deletePs.setInt(1, categoryId);
                return deletePs.executeUpdate() > 0;
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  PAYMENT MODES
    // ══════════════════════════════════════════════════════════════════

    public List<PaymentMode> getAllPaymentModes() throws SQLException {
        List<PaymentMode> list = new ArrayList<>();
        String sql = "SELECT payment_mode_id, mode_name FROM PAYMENT_MODES ORDER BY payment_mode_id";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(new PaymentMode(rs.getInt("payment_mode_id"), rs.getString("mode_name")));
            }
        }
        return list;
    }

    // ══════════════════════════════════════════════════════════════════
    //  AGGREGATION – used by TrendsChartPanel
    //  Only counts active (is_deleted=0) EXPENSE-type rows.
    // ══════════════════════════════════════════════════════════════════

    /**
     * Returns category-wise totals for EXPENSE categories.
     * @param startDate null = no lower bound
     * @param endDate   null = no upper bound
     * @return List of Object[]{ String categoryName, Double total }
     */
    public List<Object[]> getCategoryTotals(Date startDate, Date endDate) throws SQLException {
        List<Object[]> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT c.category_name, NVL(SUM(e.amount),0) AS total " +
            "FROM CATEGORIES c LEFT JOIN EXPENSES e " +
            "  ON c.category_id = e.category_id AND e.is_deleted = 0 " +
            "WHERE c.category_type = 'EXPENSE' ");
        if (startDate != null) sql.append("AND e.expense_date >= ? ");
        if (endDate   != null) sql.append("AND e.expense_date <= ? ");
        sql.append("GROUP BY c.category_name HAVING NVL(SUM(e.amount),0) > 0 " +
                   "ORDER BY total DESC");

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            if (startDate != null) ps.setDate(i++, startDate);
            if (endDate   != null) ps.setDate(i,   endDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new Object[]{ rs.getString("category_name"), rs.getDouble("total") });
                }
            }
        }
        return result;
    }
}
