package module_core_logic;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * MODULE 2 – Business Logic & Core Models (Member 2)
 * ValidationUtil: Input validation routines for date, numeric, and text strings.
 */
public class ValidationUtil {

    public static boolean isValidPositiveDouble(String input) {
        if (input == null || input.trim().isEmpty()) return false;
        try {
            double val = Double.parseDouble(input.trim());
            return val > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidNonNegativeDouble(String input) {
        if (input == null || input.trim().isEmpty()) return false;
        try {
            double val = Double.parseDouble(input.trim());
            return val >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean isValidDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return false;
        try {
            LocalDate.parse(dateStr.trim());
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
