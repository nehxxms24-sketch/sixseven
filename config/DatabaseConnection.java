package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection – Singleton utility for Oracle JDBC connections.
 *
 * Callers should use try-with-resources on the Connection returned by
 * {@link #getConnection()} so that connections are always closed properly.
 *
 * Configuration:
 *   Edit the four constants below to match your Oracle instance.
 */
public class DatabaseConnection {

    // ── Oracle connection parameters ─────────────────────────────────────────
    private static final String HOST     = "localhost";
    private static final String PORT     = "1521";
    private static final String SID      = "XE";           // or SERVICE_NAME
    private static final String USERNAME = "system";        // Oracle username
    private static final String PASSWORD = "12345";         // Oracle password
    // ─────────────────────────────────────────────────────────────────────────

    private static final String URL =
            "jdbc:oracle:thin:@" + HOST + ":" + PORT + ":" + SID;

    // Private constructor – utility class, not instantiable
    private DatabaseConnection() {}

    /**
     * Returns a fresh JDBC {@link Connection} to the Oracle database.
     * The caller is responsible for closing the connection (use try-with-resources).
     *
     * @return a live {@link Connection}
     * @throws SQLException if the driver is missing or credentials are wrong
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "ojdbc8.jar not found on classpath. Add it with -cp ojdbc8.jar", e);
        }
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /**
     * Quick connectivity test – prints OK or the error message.
     * Run with: java -cp .;ojdbc8.jar config.DatabaseConnection
     */
    public static void main(String[] args) {
        try (Connection c = getConnection()) {
            System.out.println("✔  Oracle connection successful: " + c.getMetaData().getURL());
        } catch (SQLException e) {
            System.err.println("✘  Connection failed: " + e.getMessage());
        }
    }
}
