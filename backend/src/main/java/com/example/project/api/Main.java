package com.example.project.api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        server.createContext("/api/health", Main::handleHealth);
        server.createContext("/api/message", Main::handleMessage);
        server.createContext("/api/customers", Main::handleCustomers);
        server.createContext("/", Main::handleRoot);

        server.setExecutor(null);
        server.start();

        System.out.println("Java backend running on http://localhost:8080");
    }

    private static void handleRoot(HttpExchange exchange) throws IOException {
        sendJson(exchange, 200, "{\"message\":\"Backend running\"}");
    }

    private static void handleHealth(HttpExchange exchange) throws IOException {
        sendJson(exchange, 200, "{\"status\":\"ok\",\"service\":\"task-remainder-backend\"}");
    }

    private static void handleMessage(HttpExchange exchange) throws IOException {
        sendJson(exchange, 200, "{\"message\":\"Welcome from the Java backend\"}");
    }

    private static void handleCustomers(HttpExchange exchange) throws IOException {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendOptions(exchange);
            return;
        }

        List<Map<String, String>> customers = new ArrayList<>();

        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT id, name, email, status FROM customers ORDER BY id ASC");
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Map<String, String> customer = new HashMap<>();
                customer.put("id", String.valueOf(resultSet.getInt("id")));
                customer.put("name", resultSet.getString("name"));
                customer.put("email", resultSet.getString("email"));
                customer.put("status", resultSet.getString("status"));
                customers.add(customer);
            }
        } catch (SQLException e) {
            customers.add(Map.of("id", "1", "name", "Sample User", "email", "sample@example.com", "status", "active"));
            customers.add(Map.of("id", "2", "name", "Demo User", "email", "demo@example.com", "status", "inactive"));
            System.out.println("Database unavailable. Falling back to sample data: " + e.getMessage());
        }

        String payload = toJsonArray(customers, "customers");
        sendJson(exchange, 200, payload);
    }

    private static void sendOptions(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.sendResponseHeaders(200, -1);
    }

    private static void sendJson(HttpExchange exchange, int statusCode, String payload) throws IOException {
        byte[] response = payload.getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.sendResponseHeaders(statusCode, response.length);

        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(response);
        }
    }

    private static String toJsonArray(List<Map<String, String>> values, String key) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"").append(key).append("\":[");

        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }

            Map<String, String> item = values.get(i);
            builder.append("{");

            int index = 0;
            for (Map.Entry<String, String> entry : item.entrySet()) {
                if (index > 0) {
                    builder.append(",");
                }
                builder.append("\"")
                        .append(entry.getKey())
                        .append("\":\"")
                        .append(escapeJson(entry.getValue()))
                        .append("\"");
                index++;
            }

            builder.append("}");
        }

        builder.append("]}");
        return builder.toString();
    }

    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
