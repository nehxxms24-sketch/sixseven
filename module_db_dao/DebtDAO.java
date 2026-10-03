package module_db_dao;

import module_core_logic.Debt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MODULE 1 – DB/DAO Layer (Member 1)
 * DebtDAO: Data Access Object for DEBTS_LOANS table in Oracle DB.
 */
public class DebtDAO {

    public boolean insertDebt(Debt debt) {
        String sql = "INSERT INTO DEBTS_LOANS (person_name, debt_type, principal_amount, interest_rate, due_date, status, notes) " +
                     "VALUES (?, ?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, debt.getPersonName());
            pstmt.setString(2, debt.getDebtType());
            pstmt.setDouble(3, debt.getPrincipalAmount());
            pstmt.setDouble(4, debt.getInterestRate());
            pstmt.setString(5, debt.getDueDate());
            pstmt.setString(6, debt.getStatus() != null ? debt.getStatus() : "PENDING");
            pstmt.setString(7, debt.getNotes() != null ? debt.getNotes() : "");

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DebtDAO] Error inserting debt: " + e.getMessage());
            return false;
        }
    }

    public List<Debt> getAllDebts() {
        List<Debt> list = new ArrayList<>();
        String sql = "SELECT debt_id, person_name, debt_type, principal_amount, interest_rate, " +
                     "TO_CHAR(due_date, 'YYYY-MM-DD') AS fmt_due_date, status, notes " +
                     "FROM DEBTS_LOANS ORDER BY due_date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Debt d = new Debt();
                d.setId(rs.getInt("debt_id"));
                d.setPersonName(rs.getString("person_name"));
                d.setDebtType(rs.getString("debt_type"));
                d.setPrincipalAmount(rs.getDouble("principal_amount"));
                d.setInterestRate(rs.getDouble("interest_rate"));
                d.setDueDate(rs.getString("fmt_due_date"));
                d.setStatus(rs.getString("status"));
                d.setNotes(rs.getString("notes"));
                list.add(d);
            }
        } catch (SQLException e) {
            System.err.println("[DebtDAO] Error fetching debts: " + e.getMessage());
        }
        return list;
    }

    public boolean updateDebtStatus(int debtId, String status) {
        String sql = "UPDATE DEBTS_LOANS SET status = ? WHERE debt_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, debtId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DebtDAO] Error updating debt status: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteDebt(int debtId) {
        String sql = "DELETE FROM DEBTS_LOANS WHERE debt_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, debtId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DebtDAO] Error deleting debt: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns the sum of principal_amount for a specific debt_type, month, and year
     * based on due_date. Only counts non-REPAID debts.
     * If DB is unavailable or no matching rows, returns 0.0.
     *
     * @param debtType "LENT" or "BORROWED"
     * @param month    calendar month 1–12
     * @param year     4-digit calendar year
     */
    public double getDebtSumByTypeAndMonth(String debtType, int month, int year) {
        String sql = "SELECT NVL(SUM(principal_amount), 0) AS total FROM DEBTS_LOANS " +
                     "WHERE debt_type = ? AND status <> 'REPAID' " +
                     "  AND EXTRACT(MONTH FROM due_date) = ? " +
                     "  AND EXTRACT(YEAR  FROM due_date) = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, debtType);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("[DebtDAO] getDebtSumByTypeAndMonth error: " + e.getMessage());
        }
        return 0.0;
    }

    /**
     * Returns the sum of principal_amount for a specific debt_type and year based on due_date.
     * Only counts non-REPAID debts.
     *
     * @param debtType "LENT" or "BORROWED"
     * @param year     4-digit calendar year
     */
    public double getDebtSumByTypeAndYear(String debtType, int year) {
        String sql = "SELECT NVL(SUM(principal_amount), 0) AS total FROM DEBTS_LOANS " +
                     "WHERE debt_type = ? AND status <> 'REPAID' " +
                     "  AND EXTRACT(YEAR FROM due_date) = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, debtType);
            pstmt.setInt(2, year);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.err.println("[DebtDAO] getDebtSumByTypeAndYear error: " + e.getMessage());
        }
        return 0.0;
    }
}

