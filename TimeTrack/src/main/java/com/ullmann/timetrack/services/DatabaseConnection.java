package com.ullmann.timetrack.services;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DBlocation = "/Users/alexu/WIFI-Java/Abschlussprojekt/Database/chinook.db";
    private static final String connString = "jdbc:sqlite:" + DBlocation;
    private static Connection conn = null;
    private DatabaseConnection() {}

    public static Connection getConnection() throws SQLException {
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(connString);
        }
        return conn;
    }

    public static void closeConnection() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }
}
