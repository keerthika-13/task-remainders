package com.taskreminder.repository;

import com.taskreminder.config.DatabaseConfig;
import com.taskreminder.model.Reminder;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReminderRepository {

    public Reminder create(Reminder reminder) throws SQLException {
        String sql = "INSERT INTO reminders (task_id, user_id, remind_at, reminder_type) VALUES (?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, reminder.getTaskId());
            ps.setInt(2, reminder.getUserId());
            ps.setTimestamp(3, Timestamp.valueOf(reminder.getRemindAt()));
            ps.setString(4, reminder.getReminderType());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) reminder.setId(keys.getInt(1));
            }
        }
        return reminder;
    }

    public List<Reminder> findPendingDue() throws SQLException {
        String sql = "SELECT * FROM reminders WHERE status = 'Pending' AND remind_at <= NOW()";
        List<Reminder> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<Reminder> findByUser(int userId) throws SQLException {
        String sql = "SELECT r.*, t.title as task_title FROM reminders r JOIN tasks t ON r.task_id = t.id WHERE r.user_id = ? ORDER BY r.remind_at DESC";
        List<Reminder> list = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void markSent(int reminderId) throws SQLException {
        String sql = "UPDATE reminders SET status = 'Sent', sent_at = NOW() WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, reminderId);
            ps.executeUpdate();
        }
    }

    public void markFailed(int reminderId) throws SQLException {
        String sql = "UPDATE reminders SET status = 'Failed' WHERE id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, reminderId);
            ps.executeUpdate();
        }
    }

    public void deleteByTask(int taskId) throws SQLException {
        String sql = "DELETE FROM reminders WHERE task_id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, taskId);
            ps.executeUpdate();
        }
    }

    private Reminder mapRow(ResultSet rs) throws SQLException {
        Reminder r = new Reminder();
        r.setId(rs.getInt("id"));
        r.setTaskId(rs.getInt("task_id"));
        r.setUserId(rs.getInt("user_id"));
        Timestamp ra = rs.getTimestamp("remind_at");
        if (ra != null) r.setRemindAt(ra.toLocalDateTime());
        r.setReminderType(rs.getString("reminder_type"));
        r.setStatus(rs.getString("status"));
        Timestamp sa = rs.getTimestamp("sent_at");
        if (sa != null) r.setSentAt(sa.toLocalDateTime());
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) r.setCreatedAt(ca.toLocalDateTime());
        return r;
    }
}
