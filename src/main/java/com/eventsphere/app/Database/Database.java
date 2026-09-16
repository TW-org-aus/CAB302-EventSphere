package com.eventsphere.app.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class Database {

    private static String DB_FILE = "database.db";
    private static String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private static Connection instance;

    private Database() { }


     //Foreign key enforcement and WAL journalling are set on the connection
    public static Connection DBConnect() {
        if (instance == null) {
            instance = openConnection();
        }
        return instance;
    }

    // A new connection, separate from the shared instance, for batch pulls from ticketmaster and for seeding if needed
    public static Connection openConnection() {
        try {
            Connection connection = DriverManager.getConnection(DB_URL);
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
                stmt.execute("PRAGMA journal_mode = WAL;");
                // With two connections writing, wait up to 5s for the lock instead of failing with SQLITE_BUSY.
                stmt.execute("PRAGMA busy_timeout = 5000;");
            }
            return connection;
        } catch (SQLException sqlEx) {
            throw new RuntimeException("Failed to connect to " + DB_FILE, sqlEx);
        }
    }

    public static void close() {
        if (instance != null) {
            try {
                instance.close();
            } catch (SQLException sqlEx) {
                throw new RuntimeException("Failed to close connection to " + DB_FILE, sqlEx);
            } finally {
                instance = null;
            }
        }
    }
}