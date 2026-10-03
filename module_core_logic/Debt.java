package module_core_logic;

/**
 * MODULE 2 – Business Logic & Core Models (Member 2)
 * Debt domain model representing borrowed and lent amounts.
 * Simple Interest Calculation: Total Due = Principal + (Principal * Rate / 100).
 */
public class Debt {

    private int id;
    private String personName;
    private String debtType; // "BORROWED" or "LENT"
    private double principalAmount;
    private double interestRate; // e.g. 5.0 for 5%
    private String dueDate; // Formatted YYYY-MM-DD
    private String status; // "PENDING", "REPAID", "OVERDUE"
    private String notes;

    public Debt() {}

    public Debt(int id, String personName, String debtType, double principalAmount,
                double interestRate, String dueDate, String status, String notes) {
        this.id = id;
        this.personName = personName;
        this.debtType = debtType;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.dueDate = dueDate;
        this.status = status;
        this.notes = notes;
    }

    public double getTotalDue() {
        return principalAmount + (principalAmount * interestRate / 100.0);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public String getDebtType() {
        return debtType;
    }

    public void setDebtType(String debtType) {
        this.debtType = debtType;
    }

    public double getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(double principalAmount) {
        this.principalAmount = principalAmount;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
