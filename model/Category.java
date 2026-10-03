package model;

/**
 * Category – maps to the CATEGORIES table (3NF).
 *
 * category_type is either "EXPENSE" or "INCOME" as enforced by the DB CHECK constraint.
 */
public class Category {

    private int    categoryId;
    private String categoryName;
    private String categoryType;   // "EXPENSE" | "INCOME"

    // ── Constructors ─────────────────────────────────────────────────────────

    public Category() {}

    public Category(int categoryId, String categoryName, String categoryType) {
        this.categoryId   = categoryId;
        this.categoryName = categoryName;
        this.categoryType = categoryType;
    }

    // ── Getters / Setters ────────────────────────────────────────────────────

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

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    // ── toString – displayed in JComboBox ────────────────────────────────────

    @Override
    public String toString() {
        return categoryName + " (" + categoryType + ")";
    }
}
