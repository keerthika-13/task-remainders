package com.taskreminder.controller;

import com.sun.net.httpserver.HttpExchange;
import com.taskreminder.model.User;
import com.taskreminder.repository.TaskRepository;
import com.taskreminder.service.AuthService;
import com.taskreminder.service.TaskService;
import com.taskreminder.util.JsonUtil;
import com.taskreminder.util.ResponseUtil;
import com.taskreminder.util.SessionUtil;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;

public class DashboardCalendarController {

    private final TaskRepository taskRepo    = new TaskRepository();
    private final TaskService    taskService = new TaskService();
    private final AuthService    authService = new AuthService();

    private User requireAuth(HttpExchange ex) throws IOException, SQLException {
        String token = SessionUtil.extractToken(ex);
        User user = authService.authenticate(token);
        if (user == null) ResponseUtil.sendError(ex, 401, "Not authenticated");
        return user;
    }

    // GET /api/dashboard/summary
    public void summary(HttpExchange ex, Map<String, String> p) throws IOException {
        try {
            User user = requireAuth(ex);
            if (user == null) return;
            int uid = user.getId();

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success",   true);
            resp.put("today",     taskRepo.countByStatus(uid, "Today"));
            resp.put("pending",   taskRepo.countByStatus(uid, "Pending"));
            resp.put("completed", taskRepo.countByStatus(uid, "Completed"));
            resp.put("overdue",   taskRepo.countByStatus(uid, "Overdue"));
            resp.put("cancelled", taskRepo.countByStatus(uid, "Cancelled"));

            var upcoming = taskRepo.findUpcoming(uid);
            resp.put("upcoming", upcoming.stream().map(taskService::taskToMap).toList());

            ResponseUtil.sendJson(ex, 200, JsonUtil.toJson(resp));
        } catch (SQLException e) {
            ResponseUtil.sendError(ex, 500, "Failed to fetch dashboard data");
        }
    }

    // GET /api/calendar?year=2024&month=11
    public void calendar(HttpExchange ex, Map<String, String> p) throws IOException {
        try {
            User user = requireAuth(ex);
            if (user == null) return;

            Map<String, String> q = parseQuery(ex);
            int year  = Integer.parseInt(q.getOrDefault("year",  String.valueOf(java.time.Year.now().getValue())));
            int month = Integer.parseInt(q.getOrDefault("month", String.valueOf(java.time.LocalDate.now().getMonthValue())));

            var tasks = taskRepo.findByMonth(user.getId(), year, month);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("year", year);
            resp.put("month", month);
            resp.put("tasks", tasks.stream().map(taskService::taskToMap).toList());
            ResponseUtil.sendJson(ex, 200, JsonUtil.toJson(resp));
        } catch (Exception e) {
            ResponseUtil.sendError(ex, 500, "Failed to fetch calendar data");
        }
    }

    private Map<String, String> parseQuery(HttpExchange ex) {
        Map<String, String> params = new HashMap<>();
        String query = ex.getRequestURI().getQuery();
        if (query == null) return params;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) params.put(kv[0], kv[1]);
        }
        return params;
    }
}
