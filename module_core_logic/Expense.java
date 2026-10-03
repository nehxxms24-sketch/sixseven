package module_core_logic;

/**
 * MODULE 2 – Business Logic & Core Models
 * Expense model with support for soft-deletes (isDeleted) and favorites (isStarred).
 */
public class Expense {
    private int id;
    private double amount;
    private int categoryId;
    private String categoryName;
    private int paymentModeId;
    private String paymentModeName;
    private String expenseDate; // Formatted YYYY-MM-DD
    private String notes;
    private boolean isDeleted;
    private boolean isStarred;

    public Expense() {}

    public Expense(int id, double amount, int categoryId, String categoryName,
                   int paymentModeId, String paymentModeName, String expenseDate,
                   String notes, boolean isDeleted, boolean isStarred) {
        this.id = id;
        this.amount = amount;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.paymentModeId = paymentModeId;
        this.paymentModeName = paymentModeName;
        this.expenseDate = expenseDate;
        this.notes = notes;
        this.isDeleted = isDeleted;
        this.isStarred = isStarred;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
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

    public int getPaymentModeId() {
        return paymentModeId;
    }

    public void setPaymentModeId(int paymentModeId) {
        this.paymentModeId = paymentModeId;
    }

    public String getPaymentModeName() {
        return paymentModeName;
    }

    public void setPaymentModeName(String paymentModeName) {
        this.paymentModeName = paymentModeName;
    }

    public String getExpenseDate() {
        return expenseDate;
    }

    public void setExpenseDate(String expenseDate) {
        this.expenseDate = expenseDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void setStarred(boolean starred) {
        isStarred = starred;
    }
}
