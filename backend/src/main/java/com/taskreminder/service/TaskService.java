package com.taskreminder.service;

import com.taskreminder.model.Task;
import com.taskreminder.model.Reminder;
import com.taskreminder.repository.ReminderRepository;
import com.taskreminder.repository.TaskRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class TaskService {

    private final TaskRepository     taskRepo     = new TaskRepository();
    private final ReminderRepository reminderRepo = new ReminderRepository();

    // ─── List ────────────────────────────────────────────────────────

    public List<Map<String, Object>> listTasks(int userId, String status,
                                                String category, String priority,
                                                String search) throws SQLException {
        List<Task> tasks = taskRepo.findByUser(userId, status, category, priority, search);
        return tasks.stream().map(this::taskToMap).toList();
    }

    // ─── Create ──────────────────────────────────────────────────────

    public Map<String, Object> createTask(int userId, Map<String, String> data) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();

        String title = data.get("title");
        if (title == null || title.isBlank()) {
            result.put("error", "Title is required");
            return result;
        }

        Task task = new Task();
        task.setUserId(userId);
        task.setTitle(title.trim());
        task.setDescription(data.getOrDefault("description", ""));
        task.setPriority(data.getOrDefault("priority", "Medium"));
        task.setCategory(data.getOrDefault("category", "Personal"));

        String dateStr = data.get("taskDate");
        if (dateStr != null && !dateStr.isBlank()) task.setTaskDate(LocalDate.parse(dateStr));

        String timeStr = data.get("taskTime");
        if (timeStr != null && !timeStr.isBlank()) task.setTaskTime(LocalTime.parse(timeStr));

        Task created = taskRepo.create(task);

        // Handle reminder
        String reminderOffset = data.get("reminder");
        if (reminderOffset != null && !reminderOffset.isBlank() && !reminderOffset.equalsIgnoreCase("none")) {
            createReminderForTask(created, userId, reminderOffset, data.get("customReminder"));
        }

        result.put("success", true);
        result.put("task", taskToMap(created));
        return result;
    }

    // ─── Update ──────────────────────────────────────────────────────

    public Map<String, Object> updateTask(int taskId, int userId, Map<String, String> data) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();

        Task task = taskRepo.findById(taskId, userId);
        if (task == null) {
            result.put("error", "Task not found");
            return result;
        }

        if (data.containsKey("title"))       task.setTitle(data.get("title"));
        if (data.containsKey("description")) task.setDescription(data.get("description"));
        if (data.containsKey("priority"))    task.setPriority(data.get("priority"));
        if (data.containsKey("category"))    task.setCategory(data.get("category"));
        if (data.containsKey("status"))      task.setStatus(data.get("status"));

        String dateStr = data.get("taskDate");
        if (dateStr != null) task.setTaskDate(dateStr.isBlank() ? null : LocalDate.parse(dateStr));

        String timeStr = data.get("taskTime");
        if (timeStr != null) task.setTaskTime(timeStr.isBlank() ? null : LocalTime.parse(timeStr));

        taskRepo.update(task);

        result.put("success", true);
        result.put("task", taskToMap(task));
        return result;
    }

    // ─── Delete ──────────────────────────────────────────────────────

    public Map<String, Object> deleteTask(int taskId, int userId) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        boolean deleted = taskRepo.delete(taskId, userId);
        if (deleted) {
            result.put("success", true);
            result.put("message", "Task deleted");
        } else {
            result.put("error", "Task not found");
        }
        return result;
    }

    // ─── Status change ───────────────────────────────────────────────

    public Map<String, Object> changeStatus(int taskId, int userId, String status) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();
        boolean updated = taskRepo.updateStatus(taskId, userId, status);
        if (updated) {
            result.put("success", true);
            result.put("message", "Task status updated to " + status);
        } else {
            result.put("error", "Task not found");
        }
        return result;
    }

    // ─── Reminder helper ─────────────────────────────────────────────

    private void createReminderForTask(Task task, int userId, String offset, String customDateTime) throws SQLException {
        if (task.getTaskDate() == null) return;

        LocalDateTime taskDateTime = LocalDateTime.of(
                task.getTaskDate(),
                task.getTaskTime() != null ? task.getTaskTime() : LocalTime.of(9, 0)
        );

        LocalDateTime remindAt = switch (offset) {
            case "5 min before"   -> taskDateTime.minusMinutes(5);
            case "10 min before"  -> taskDateTime.minusMinutes(10);
            case "15 min before"  -> taskDateTime.minusMinutes(15);
            case "30 min before"  -> taskDateTime.minusMinutes(30);
            case "1 hour before"  -> taskDateTime.minusHours(1);
            case "2 hours before" -> taskDateTime.minusHours(2);
            case "1 day before"   -> taskDateTime.minusDays(1);
            case "custom"         -> {
                if (customDateTime == null || customDateTime.isBlank()) {
                    yield taskDateTime.minusMinutes(30);
                }
                try {
                    yield LocalDateTime.parse(customDateTime);
                } catch (Exception e) {
                    // datetime-local may omit seconds
                    yield LocalDateTime.parse(customDateTime + ":00");
                }
            }
            default               -> taskDateTime.minusMinutes(30);
        };

        Reminder reminder = new Reminder();
        reminder.setTaskId(task.getId());
        reminder.setUserId(userId);
        reminder.setRemindAt(remindAt);
        reminder.setReminderType(offset);
        reminderRepo.create(reminder);
    }

    // ─── Map conversion ──────────────────────────────────────────────

    public Map<String, Object> taskToMap(Task t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",          t.getId());
        m.put("title",       t.getTitle());
        m.put("description", t.getDescription());
        m.put("taskDate",    t.getTaskDate()   != null ? t.getTaskDate().toString()   : null);
        m.put("taskTime",    t.getTaskTime()   != null ? t.getTaskTime().toString()   : null);
        m.put("priority",    t.getPriority());
        m.put("category",    t.getCategory());
        m.put("status",      t.getStatus());
        m.put("createdAt",   t.getCreatedAt()  != null ? t.getCreatedAt().toString()  : null);
        return m;
    }
}
