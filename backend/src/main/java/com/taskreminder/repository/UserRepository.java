package com.taskreminder.repository;

import com.taskreminder.config.DatabaseConfig;
import com.taskreminder.model.User;

import java.sql.*;
import java.time.LocalDateTime;

public class UserRepository {

    public User findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ? LIMIT 1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public User findBySessionToken(String token) throws SQLException {
        String sql = "SELECT * FROM users WHERE session_token = ? AND token_expires > NOW() LIMIT 1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public User findById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ? LIMIT 1";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public User create(User user) throws SQLException {
        String sql = "INSERT INTO users (name, email, mobile, password_hash, salt) VALUES (?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getMobile());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getSalt());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getInt(1));
            }
        }
        return user;
    }

    public void saveSessionToken(int userId, String token, LocalDateTime expires) throws SQLException {
        String sql = "UPDATE users SET session_token = ?, token_expires = ? WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setTimestamp(2, Timestamp.valueOf(expires));
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    public void clearSessionToken(int userId) throws SQLException {
        String sql = "UPDATE users SET session_token = NULL, token_expires = NULL WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    public void updateProfile(User user) throws SQLException {
        String sql = "UPDATE users SET name = ?, mobile = ?, telegram_chat_id = ? WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getMobile());
            ps.setString(3, user.getTelegramChatId());
            ps.setInt(4, user.getId());
            ps.executeUpdate();
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setName(rs.getString("name"));
        u.setEmail(rs.getString("email"));
        u.setMobile(rs.getString("mobile"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setSalt(rs.getString("salt"));
        u.setSessionToken(rs.getString("session_token"));
        u.setTelegramChatId(rs.getString("telegram_chat_id"));
        Timestamp te = rs.getTimestamp("token_expires");
        if (te != null) u.setTokenExpires(te.toLocalDateTime());
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) u.setCreatedAt(ca.toLocalDateTime());
        return u;
    }
}
