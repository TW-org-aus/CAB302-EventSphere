import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Singleton SQLite connection manager.
 *
 * Production code calls Database.DBConnect() to get the shared connection
 * to the real database.db file. Tests do NOT use this class — they
 * construct DAOs directly with a Connection to jdbc:sqlite::memory:,
 * which keeps every test isolated and avoids contaminating database.db.
 */
public class Database {

    private static final String DB_FILE = "database.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private static Connection connection;

    private Database() { }

    /**
     * Returns the shared singleton Connection to database.db,
     * opening it on first call. Enables SQLite foreign key
     * enforcement on the connection.
     */
    public static synchronized Connection DBConnect() {
        if (connection == null) {
            try {
                connection = DriverManager.getConnection(DB_URL);
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                }
            } catch (SQLException e) {
                throw new RuntimeException("Failed to connect to " + DB_FILE, e);
            }
        }
        return connection;
    }

    /**
     * Closes the shared connection, if open. Call on application shutdown.
     */
    public static synchronized void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to close connection", e);
            } finally {
                connection = null;
            }
        }
    }
}