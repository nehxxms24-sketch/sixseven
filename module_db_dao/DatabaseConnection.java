package module_db_dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * MODULE 1 – DB/DAO Layer
 * DatabaseConnection: Singleton utility for Oracle JDBC.
 * Fails gracefully – never throws unchecked exceptions that freeze the UI.
 */
public class DatabaseConnection {

    // ── Configure these to match your Oracle instance ──────────────────
    private static final String HOST     = "localhost";
    private static final String PORT     = "1521";
    private static final String SID      = "XE";
    private static final String USERNAME = "system";
    private static final String PASSWORD = "12345";
    // ──────────────────────────────────────────────────────────────────

    private static final String URL =
            "jdbc:oracle:thin:@" + HOST + ":" + PORT + ":" + SID;

    private static boolean driverLoaded = false;

    private DatabaseConnection() {}

    /** Pre-loads the JDBC driver once. Returns false if driver jar is missing. */
    public static boolean loadDriver() {
        if (driverLoaded) return true;
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            driverLoaded = true;
            return true;
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] ojdbc8.jar not on classpath: " + e.getMessage());
            return false;
        }
    }

    /**
     * Returns a new JDBC Connection. Caller must close it (use try-with-resources).
     * Throws SQLException so callers can handle gracefully.
     */
    public static Connection getConnection() throws SQLException {
        if (!loadDriver()) {
            throw new SQLException("Oracle JDBC driver not found. Place ojdbc8.jar in lib/.");
        }
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.err.println("[DatabaseConnection] Cannot connect: " + e.getMessage());
            throw e;
        }
    }

    /** @return true if a test connection can be opened and closed. */
    public static boolean isAvailable() {
        try (Connection c = getConnection()) {
            return c != null && !c.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    /** Alias method for boolean db availability check */
    public static boolean isDatabaseAvailable() {
        return isAvailable();
    }
}
