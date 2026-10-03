import module_db_dao.ExpenseDAO;
import module_ui.LoginFrame;

import javax.swing.SwingUtilities;

/**
 * Personal Expense Manager – Entry Point.
 * Launches LoginFrame under Swing Event Dispatch Thread (EDT).
 *
 * Startup sequence:
 *   1. Background thread: ExpenseDAO.seedCompleteHistory() — seeds Oracle DB if connected.
 *   2. EDT: LoginFrame displayed.
 *
 * Compilation & Execution (Windows):
 *   build.bat
 * Or manually:
 *   javac -cp ".;lib\ojdbc8.jar" -d out module_db_dao\*.java module_core_logic\*.java module_ui\*.java Main.java
 *   java  -cp "out;lib\ojdbc8.jar" Main
 */
public class Main {
    public static void main(String[] args) {

        // ── Background DB seed (non-blocking, won't delay UI startup) ──────────
        Thread seedThread = new Thread(() -> {
            try {
                ExpenseDAO.seedCompleteHistory();
            } catch (Exception e) {
                System.err.println("[Main] seedCompleteHistory error: " + e.getMessage());
            }
        }, "db-seed-thread");
        seedThread.setDaemon(true); // won't prevent JVM exit
        seedThread.start();

        // ── Launch UI on Swing EDT ─────────────────────────────────────────────
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
