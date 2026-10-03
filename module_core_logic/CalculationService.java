package module_core_logic;

import java.util.List;

/**
 * MODULE 2 – Business Logic & Core Models (Member 2)
 * CalculationService: Financial metrics helper routines.
 */
public class CalculationService {

    public static double calculateSimpleInterest(double principal, double ratePercent) {
        return principal * (ratePercent / 100.0);
    }

    public static double calculateTotalDue(double principal, double ratePercent) {
        return principal + calculateSimpleInterest(principal, ratePercent);
    }

    public static double calculateTotalExpenses(List<Expense> expenses) {
        if (expenses == null) return 0.0;
        double sum = 0.0;
        for (Expense e : expenses) {
            if (!e.isDeleted()) {
                sum += e.getAmount();
            }
        }
        return sum;
    }
}
