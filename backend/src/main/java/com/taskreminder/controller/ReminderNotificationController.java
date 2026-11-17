package com.taskreminder.controller;

import com.sun.net.httpserver.HttpExchange;
import com.taskreminder.model.Notification;
import com.taskreminder.model.Reminder;
import com.taskreminder.model.User;
import com.taskreminder.repository.NotificationRepository;
import com.taskreminder.repository.ReminderRepository;
import com.taskreminder.service.AuthService;
import com.taskreminder.util.JsonUtil;
import com.taskreminder.util.ResponseUtil;
import com.taskreminder.util.SessionUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public class ReminderNotificationController {

    private final ReminderRepository     reminderRepo  = new ReminderRepository();
    private final NotificationRepository notifRepo     = new NotificationRepository();
    private final AuthService            authService   = new AuthService();

    private User requireAuth(HttpExchange ex) throws IOException, SQLException {
        String token = SessionUtil.extractToken(ex);
        User user = authService.authenticate(token);
        if (user == null) ResponseUtil.sendError(ex, 401, "Not authenticated");
        return user;
    }

    // GET /api/reminders
    public void listReminders(HttpExchange ex, Map<String, String> p) throws IOException {
        try {
            User user = requireAuth(ex);
            if (user == null) return;
            List<Reminder> reminders = reminderRepo.findByUser(user.getId());
            List<Map<String, Object>> list = reminders.stream().map(r -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id",           r.getId());
                m.put("taskId",       r.getTaskId());
                m.put("remindAt",     r.getRemindAt() != null ? r.getRemindAt().toString() : null);
                m.put("reminderType", r.getReminderType());
                m.put("status",       r.getStatus());
                m.put("sentAt",       r.getSentAt()   != null ? r.getSentAt().toString()   : null);
                return m;
            }).toList();
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("reminders", list);
            ResponseUtil.sendJson(ex, 200, JsonUtil.toJson(resp));
        } catch (SQLException e) {
            ResponseUtil.sendError(ex, 500, "Failed to fetch reminders");
        }
    }

    // GET /api/notifications
    public void listNotifications(HttpExchange ex, Map<String, String> p) throws IOException {
        try {
            User user = requireAuth(ex);
            if (user == null) return;
            List<Notification> notifs = notifRepo.findByUser(user.getId());
            int unread = notifRepo.countUnread(user.getId());
            List<Map<String, Object>> list = notifs.stream().map(n -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id",         n.getId());
                m.put("title",      n.getTitle());
                m.put("message",    n.getMessage());
                m.put("type",       n.getType());
                m.put("isRead",     n.isRead());
                m.put("taskId",     n.getTaskId());
                m.put("reminderId", n.getReminderId());
                m.put("createdAt",  n.getCreatedAt() != null ? n.getCreatedAt().toString() : null);
                return m;
            }).toList();
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("notifications", list);
            resp.put("unreadCount", unread);
            ResponseUtil.sendJson(ex, 200, JsonUtil.toJson(resp));
        } catch (SQLException e) {
            ResponseUtil.sendError(ex, 500, "Failed to fetch notifications");
        }
    }

    // PATCH /api/notifications/read-all
    public void markAllRead(HttpExchange ex, Map<String, String> p) throws IOException {
        try {
            User user = requireAuth(ex);
            if (user == null) return;
            notifRepo.markAllRead(user.getId());
            ResponseUtil.sendOk(ex, "All notifications marked as read");
        } catch (SQLException e) {
            ResponseUtil.sendError(ex, 500, "Failed to update notifications");
        }
    }

    // PATCH /api/notifications/{id}/read
    public void markRead(HttpExchange ex, Map<String, String> p) throws IOException {
        try {
            User user = requireAuth(ex);
            if (user == null) return;
            int id = Integer.parseInt(p.get("id"));
            notifRepo.markRead(id, user.getId());
            ResponseUtil.sendOk(ex, "Notification marked as read");
        } catch (SQLException e) {
            ResponseUtil.sendError(ex, 500, "Failed to update notification");
        }
    }
}
