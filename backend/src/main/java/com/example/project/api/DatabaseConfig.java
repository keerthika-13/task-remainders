package com.example.project.api;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConfig {
    private DatabaseConfig() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found", e);
        }

        String url = System.getenv().getOrDefault(
                "DB_URL",
                "jdbc:mysql://localhost:3306/task_remainder?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        );
        String username = System.getenv().getOrDefault("DB_USERNAME", "root");
        String password = System.getenv().getOrDefault("DB_PASSWORD", "password");

        return DriverManager.getConnection(url, username, password);
    }
}
