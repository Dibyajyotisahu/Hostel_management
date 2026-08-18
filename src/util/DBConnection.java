package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton wrapper around a single shared JDBC Connection to MySQL.
 * Ensures the whole application reuses one connection instead of
 * opening a new one for every DAO call.
 */
public final class DBConnection {

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/hostel_management?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "Dibya@1234"; // change this

    private static Connection instance;

    private DBConnection() {
        // prevent instantiation
    }

    public static synchronized Connection getConnection() {
        try {
            if (instance == null || instance.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                instance = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC Driver not found. Add mysql-connector-j to classpath.", e);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database: " + e.getMessage(), e);
        }
        return instance;
    }

    public static synchronized void close() {
        try {
            if (instance != null && !instance.isClosed()) {
                instance.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}
