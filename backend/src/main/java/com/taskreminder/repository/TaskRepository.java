package com.taskreminder.repository;

import com.taskreminder.config.DatabaseConfig;
import com.taskreminder.model.Task;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class TaskRepository {

    public List<Task> findByUser(int userId, String status, String category, String priority, String search) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM tasks WHERE user_id = ?");
        List<Object> args = new ArrayList<>();
        args.add(userId);

        if (status != null && !status.isBlank()) { sql.append(" AND status = ?"); args.add(status); }
        if (category != null && !category.isBlank()) { sql.append(" AND category = ?"); args.add(category); }
        if (priority != null && !priority.isBlank()) { sql.append(" AND priority = ?"); args.add(priority); }
        if (search != null && !search.isBlank()) {
            sql.append(" AND (title LIKE ? OR description LIKE ?)");
            args.add("%" + search + "%");
            args.add("%" + search + "%");
        }
        sql.append(" ORDER BY task_date ASC, task_time ASC, id DESC");

        List<Task> tasks = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < args.size(); i++) ps.setObject(i + 1, args.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tasks.add(mapRow(rs));
            }
        }
        return tasks;
    }

    public Task findById(int id, int userId) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE id = ? AND user_id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public Task create(Task task) throws SQLException {
        String sql = "INSERT INTO tasks (user_id, title, description, task_date, task_time, priority, category, status) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, task.getUserId());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getDescription());
            ps.setDate(4, task.getTaskDate() != null ? Date.valueOf(task.getTaskDate()) : null);
            ps.setTime(5, task.getTaskTime() != null ? Time.valueOf(task.getTaskTime()) : null);
            ps.setString(6, task.getPriority() != null ? task.getPriority() : "Medium");
            ps.setString(7, task.getCategory() != null ? task.getCategory() : "Personal");
            ps.setString(8, "Pending");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) task.setId(keys.getInt(1));
            }
        }
        return task;
    }

    public boolean update(Task task) throws SQLException {
        String sql = "UPDATE tasks SET title=?, description=?, task_date=?, task_time=?, priority=?, category=?, status=? WHERE id=? AND user_id=?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, task.getTitle());
            ps.setString(2, task.getDescription());
            ps.setDate(3, task.getTaskDate() != null ? Date.valueOf(task.getTaskDate()) : null);
            ps.setTime(4, task.getTaskTime() != null ? Time.valueOf(task.getTaskTime()) : null);
            ps.setString(5, task.getPriority());
            ps.setString(6, task.getCategory());
            ps.setString(7, task.getStatus());
            ps.setInt(8, task.getId());
            ps.setInt(9, task.getUserId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(int id, int userId, String status) throws SQLException {
        String sql = "UPDATE tasks SET status = ? WHERE id = ? AND user_id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.setInt(3, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean delete(int id, int userId) throws SQLException {
        String sql = "DELETE FROM tasks WHERE id = ? AND user_id = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    /** Mark tasks as Overdue if their date/time has passed and they are still Pending */
    public void markOverdueTasks() throws SQLException {
        String sql = "UPDATE tasks SET status = 'Overdue' " +
                "WHERE status = 'Pending' " +
                "AND task_date IS NOT NULL " +
                "AND CONCAT(task_date, ' ', IFNULL(task_time, '23:59:59')) < NOW()";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            int rows = ps.executeUpdate();
            if (rows > 0) System.out.println("[TaskRepository] Marked " + rows + " task(s) as Overdue.");
        }
    }

    // Dashboard summary counts
    public int countByStatus(int userId, String status) throws SQLException {
        String sql = status.equals("Today")
                ? "SELECT COUNT(*) FROM tasks WHERE user_id = ? AND task_date = CURDATE()"
                : "SELECT COUNT(*) FROM tasks WHERE user_id = ? AND status = ?";
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            if (!status.equals("Today")) ps.setString(2, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public List<Task> findUpcoming(int userId) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE user_id = ? AND status = 'Pending' AND task_date > CURDATE() ORDER BY task_date ASC, task_time ASC LIMIT 5";
        List<Task> tasks = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tasks.add(mapRow(rs));
            }
        }
        return tasks;
    }

    public List<Task> findByDate(int userId, String date) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE user_id = ? AND task_date = ? ORDER BY task_time ASC";
        List<Task> tasks = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tasks.add(mapRow(rs));
            }
        }
        return tasks;
    }

    public List<Task> findByMonth(int userId, int year, int month) throws SQLException {
        String sql = "SELECT * FROM tasks WHERE user_id = ? AND YEAR(task_date) = ? AND MONTH(task_date) = ? ORDER BY task_date ASC, task_time ASC";
        List<Task> tasks = new ArrayList<>();
        try (Connection c = DatabaseConfig.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, year);
            ps.setInt(3, month);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tasks.add(mapRow(rs));
            }
        }
        return tasks;
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        Task t = new Task();
        t.setId(rs.getInt("id"));
        t.setUserId(rs.getInt("user_id"));
        t.setTitle(rs.getString("title"));
        t.setDescription(rs.getString("description"));
        Date d = rs.getDate("task_date");
        if (d != null) t.setTaskDate(d.toLocalDate());
        Time time = rs.getTime("task_time");
        if (time != null) t.setTaskTime(time.toLocalTime());
        t.setPriority(rs.getString("priority"));
        t.setCategory(rs.getString("category"));
        t.setStatus(rs.getString("status"));
        Timestamp ca = rs.getTimestamp("created_at");
        if (ca != null) t.setCreatedAt(ca.toLocalDateTime());
        Timestamp ua = rs.getTimestamp("updated_at");
        if (ua != null) t.setUpdatedAt(ua.toLocalDateTime());
        return t;
    }
}
