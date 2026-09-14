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
            try {
                instance = DriverManager.getConnection(DB_URL);
                try (Statement stmt = instance.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                    stmt.execute("PRAGMA journal_mode = WAL;");
                }
            } catch (SQLException sqlEx) {
                throw new RuntimeException("Failed to connect to " + DB_FILE, sqlEx);
            }
        }
        return instance;
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