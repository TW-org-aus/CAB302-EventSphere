package com.eventsphere.app.Database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class Database {
    private static Connection instance = null;
    private static final String connection_path = "jdbc:sqlite:database.db";
    private Database() {
        try {


            instance = DriverManager.getConnection(connection_path);

                try (Statement stmt = instance.createStatement()) {
                    stmt.execute("PRAGMA foreign_keys = ON;");
                    stmt.execute("PRAGMA journal_mode = WAL;");
                }


        }catch (SQLException sqlEx) {
            throw new RuntimeException("Failed to connect to database", sqlEx);
        }



        }


        public static Connection DBConnect(){
        if(instance == null){

            new Database();


        }
        return instance;
        }
    }

