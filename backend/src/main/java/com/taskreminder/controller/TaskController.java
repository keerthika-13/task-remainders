package com.taskreminder.controller;

import com.sun.net.httpserver.HttpExchange;
import com.taskreminder.model.User;
import com.taskreminder.service.AuthService;
import com.taskreminder.service.TaskService;
import com.taskreminder.util.JsonUtil;
import com.taskreminder.util.ResponseUtil;
import com.taskreminder.util.SessionUtil;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;
import java.util.*;

public class TaskController {

    private final TaskService taskService = new TaskService();
    private final AuthService authService = new AuthService();

    private User requireAuth(HttpExchange ex) throws IOException, SQLException {
        String token = SessionUtil.extractToken(ex);
        User user = authService.authenticate(token);
        if (user == null) {
            ResponseUtil.sendError(ex, 401, "Not authenticated");
        }
        return user;
    }

    private Map<String, String> parseQuery(HttpExchange ex) {
        Map<String, String> params = new HashMap<>();
        URI uri = ex.getRequestURI();
        String query = uri.getQuery();
        if (query == null) return params;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) params.put(kv[0], java.net.URLDecoder.decode(kv[1], java.nio.charset.StandardCharsets.UTF_8));
            else if (kv.length == 1) params.put(kv[0], "");
        }
        return params;
    }

    public void list(HttpExchange ex, Map<String, String> pathParams) throws IOException {
        try {
            User user = requireAuth(ex);
            if (user == null) return;
            Map<String, String> q = parseQuery(ex);
            List<Map<String, Object>> tasks = taskService.listTasks(
                    user.getId(), q.get("status"), q.get("category"), q.get("priority"), q.get("search"));
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("tasks", tasks);
            resp.put("total", tasks.size());
            ResponseUtil.sendJson(ex, 200, JsonUtil.toJson(resp));
        } catch (SQLException e) {
            ResponseUtil.sendError(ex, 500, "Failed to fetch tasks");
        }
    }

    public void getById(HttpExchange ex, Map<String, String> pathParams) throws IOException {
        try {
            User user = requireAuth(ex);
            if (user == null) return;
            int id = Integer.parseInt(pathParams.get("id"));
            var task = new com.taskreminder.repository.TaskRepository().findById(id, user.getId());
            if (task == null) { ResponseUtil.sendError(ex, 404, "Task not found"); return; }
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("task", taskService.taskToMap(task));
            ResponseUtil.sendJson(ex, 200, JsonUtil.toJson(resp));
        } catch (SQLException e) {
            ResponseUtil.sendError(ex, 500, "Failed to fetch task");
        }
    }

    public void create(HttpExchange ex, Map<String, String> pathParams) throws IOException {
        try {
            User user = requireAuth(ex);
            if (user == null) return;
            String body = ResponseUtil.readBody(ex);
            Map<String, String> data = JsonUtil.parseObject(body);
            Map<String, Object> result = taskService.createTask(user.getId(), data);
            if (result.containsKey("error")) {
                ResponseUtil.sendError(ex, 400, (String) result.get("error"));
            } else {
                ResponseUtil.sendJson(ex, 201, JsonUtil.toJson(result));
            }
        } catch (SQLException e) {
            ResponseUtil.sendError(ex, 500, "Failed to create task");
        }
    }

    public void update(HttpExchange ex, Map<String, String> pathParams) throws IOException {
        try {
            User user = requireAuth(ex);
            if (user == null) return;
            int id = Integer.parseInt(pathParams.get("id"));
            String body = ResponseUtil.readBody(ex);
            Map<String, String> data = JsonUtil.parseObject(body);
            Map<String, Object> result = taskService.updateTask(id, user.getId(), data);
            if (result.containsKey("error")) {
                ResponseUtil.sendError(ex, 404, (String) result.get("error"));
            } else {
                ResponseUtil.sendJson(ex, 200, JsonUtil.toJson(result));
            }
        } catch (SQLException e) {
            ResponseUtil.sendError(ex, 500, "Failed to update task");
        }
    }

    public void delete(HttpExchange ex, Map<String, String> pathParams) throws IOException {
        try {
            User user = requireAuth(ex);
            if (user == null) return;
            int id = Integer.parseInt(pathParams.get("id"));
            Map<String, Object> result = taskService.deleteTask(id, user.getId());
            if (result.containsKey("error")) {
                ResponseUtil.sendError(ex, 404, (String) result.get("error"));
            } else {
                ResponseUtil.sendJson(ex, 200, JsonUtil.toJson(result));
            }
        } catch (SQLException e) {
            ResponseUtil.sendError(ex, 500, "Failed to delete task");
        }
    }

    public void complete(HttpExchange ex, Map<String, String> pathParams) throws IOException {
        patchStatus(ex, pathParams, "Completed");
    }

    public void cancel(HttpExchange ex, Map<String, String> pathParams) throws IOException {
        patchStatus(ex, pathParams, "Cancelled");
    }

    private void patchStatus(HttpExchange ex, Map<String, String> pathParams, String status) throws IOException {
        try {
            User user = requireAuth(ex);
            if (user == null) return;
            int id = Integer.parseInt(pathParams.get("id"));
            Map<String, Object> result = taskService.changeStatus(id, user.getId(), status);
            if (result.containsKey("error")) {
                ResponseUtil.sendError(ex, 404, (String) result.get("error"));
            } else {
                ResponseUtil.sendJson(ex, 200, JsonUtil.toJson(result));
            }
        } catch (SQLException e) {
            ResponseUtil.sendError(ex, 500, "Failed to update status");
        }
    }
}
