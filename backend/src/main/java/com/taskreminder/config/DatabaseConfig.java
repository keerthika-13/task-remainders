package com.taskreminder.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConfig {

    private static final String URL;
    private static final String USERNAME;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = DatabaseConfig.class
                .getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            System.err.println("[DatabaseConfig] Could not load application.properties: " + e.getMessage());
        }

        URL      = System.getenv().getOrDefault("DB_URL",      props.getProperty("db.url",      "jdbc:mysql://localhost:3306/task_remainder?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"));
        USERNAME = System.getenv().getOrDefault("DB_USERNAME", props.getProperty("db.username", "root"));
        PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", props.getProperty("db.password", ""));
    }

    private DatabaseConfig() {}

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found", e);
        }
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    /** Quick connectivity check used at startup */
    public static boolean testConnection() {
        try (Connection c = getConnection()) {
            return c.isValid(2);
        } catch (SQLException e) {
            System.err.println("[DatabaseConfig] DB connection failed: " + e.getMessage());
            return false;
        }
    }
}
