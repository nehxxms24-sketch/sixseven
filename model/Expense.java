package model;

import java.sql.Date;

/**
 * Expense – maps to the EXPENSES table (3NF).
 *
 * Uses java.sql.Date for direct JDBC compatibility.
 */
public class Expense {

    private int    expenseId;
    private double amount;
    private Date   expenseDate;
    private int    categoryId;
    private String categoryName;   // joined from CATEGORIES (read-only display)
    private int    modeId;
    private String modeName;       // joined from PAYMENT_MODES (read-only display)
    private String notes;

    // ── Constructors ─────────────────────────────────────────────────────────

    public Expense() {}

    /** Constructor used when reading from DB (includes joined display fields). */
    public Expense(int expenseId, double amount, Date expenseDate,
                   int categoryId, String categoryName,
                   int modeId,     String modeName,
                   String notes) {
        this.expenseId    = expenseId;
        this.amount       = amount;
        this.expenseDate  = expenseDate;
        this.categoryId   = categoryId;
        this.categoryName = categoryName;
        this.modeId       = modeId;
        this.modeName     = modeName;
        this.notes        = notes;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

    public int getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(int expenseId) {
        this.expenseId = expenseId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Date getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(Date expenseDate) {
        this.expenseDate = expenseDate;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getModeId() {
        return modeId;
    }

    public void setModeId(int modeId) {
        this.modeId = modeId;
    }

    public String getModeName() {
        return modeName;
    }

    public void setModeName(String modeName) {
        this.modeName = modeName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return String.format("Expense[id=%d, ₹%.2f, %s, %s, %s]",
                expenseId, amount, expenseDate, categoryName, modeName);
    }
}
