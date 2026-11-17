package com.taskreminder.controller;

import com.sun.net.httpserver.HttpExchange;
import com.taskreminder.model.User;
import com.taskreminder.router.Router;
import com.taskreminder.service.AuthService;
import com.taskreminder.util.JsonUtil;
import com.taskreminder.util.ResponseUtil;
import com.taskreminder.util.SessionUtil;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class AuthController {

    private final AuthService authService = new AuthService();

    public void register(HttpExchange ex, Map<String, String> params) throws IOException {
        String body = ResponseUtil.readBody(ex);
        Map<String, String> data = JsonUtil.parseObject(body);

        try {
            Map<String, Object> result = authService.register(
                    data.get("name"),
                    data.get("email"),
                    data.get("mobile"),
                    data.get("password"),
                    data.get("confirmPassword")
            );

            if (result.containsKey("error")) {
                ResponseUtil.sendError(ex, 400, (String) result.get("error"));
            } else {
                ResponseUtil.sendJson(ex, 201, JsonUtil.toJson(result));
            }
        } catch (SQLException e) {
            System.err.println("[AuthController] register error: " + e.getMessage());
            ResponseUtil.sendError(ex, 500, "Server error during registration");
        }
    }

    public void login(HttpExchange ex, Map<String, String> params) throws IOException {
        String body = ResponseUtil.readBody(ex);
        Map<String, String> data = JsonUtil.parseObject(body);

        try {
            Map<String, Object> result = authService.login(data.get("email"), data.get("password"));

            if (result.containsKey("error")) {
                ResponseUtil.sendError(ex, 401, (String) result.get("error"));
            } else {
                ResponseUtil.sendJson(ex, 200, JsonUtil.toJson(result));
            }
        } catch (SQLException e) {
            System.err.println("[AuthController] login error: " + e.getMessage());
            ResponseUtil.sendError(ex, 500, "Server error during login");
        }
    }

    public void logout(HttpExchange ex, Map<String, String> params) throws IOException {
        String token = SessionUtil.extractToken(ex);
        try {
            User user = authService.authenticate(token);
            if (user == null) {
                ResponseUtil.sendError(ex, 401, "Not authenticated");
                return;
            }
            authService.logout(user.getId());
            ResponseUtil.sendOk(ex, "Logged out successfully");
        } catch (SQLException e) {
            ResponseUtil.sendError(ex, 500, "Server error during logout");
        }
    }

    public void updateProfile(HttpExchange ex, Map<String, String> params) throws IOException {
        String token = SessionUtil.extractToken(ex);
        try {
            User user = authService.authenticate(token);
            if (user == null) { ResponseUtil.sendError(ex, 401, "Not authenticated"); return; }
            String body = ResponseUtil.readBody(ex);
            Map<String, String> data = JsonUtil.parseObject(body);
            if (data.containsKey("name") && data.get("name") != null && !data.get("name").isBlank()) {
                user.setName(data.get("name").trim());
            }
            if (data.containsKey("mobile")) {
                String mobile = data.get("mobile");
                user.setMobile(mobile == null ? "" : mobile.trim());
            }
            if (data.containsKey("telegramChatId")) {
                String chatId = data.get("telegramChatId");
                user.setTelegramChatId(chatId == null ? "" : chatId.trim());
            }
            new com.taskreminder.repository.UserRepository().updateProfile(user);
            String json = "{\"success\":true,\"message\":\"Profile updated\",\"user\":{"
                    + "\"id\":" + user.getId()
                    + ",\"name\":\"" + JsonUtil.escape(user.getName()) + "\""
                    + ",\"email\":\"" + JsonUtil.escape(user.getEmail()) + "\""
                    + ",\"mobile\":\"" + JsonUtil.escape(user.getMobile() != null ? user.getMobile() : "") + "\""
                    + ",\"telegramChatId\":\"" + JsonUtil.escape(user.getTelegramChatId() != null ? user.getTelegramChatId() : "") + "\""
                    + "}}";
            ResponseUtil.sendJson(ex, 200, json);
        } catch (java.sql.SQLException e) {
            System.err.println("[AuthController] updateProfile error: " + e.getMessage());
            ResponseUtil.sendError(ex, 500, "Server error: " + e.getMessage());
        }
    }

    public void me(HttpExchange ex, Map<String, String> params) throws IOException {
        String token = SessionUtil.extractToken(ex);
        try {
            User user = authService.authenticate(token);
            if (user == null) {
                ResponseUtil.sendError(ex, 401, "Not authenticated");
                return;
            }
            String json = "{\"id\":" + user.getId()
                    + ",\"name\":\"" + JsonUtil.escape(user.getName()) + "\""
                    + ",\"email\":\"" + JsonUtil.escape(user.getEmail()) + "\""
                    + ",\"mobile\":\"" + JsonUtil.escape(user.getMobile() != null ? user.getMobile() : "") + "\""
                    + ",\"telegramChatId\":\"" + JsonUtil.escape(user.getTelegramChatId() != null ? user.getTelegramChatId() : "") + "\""
                    + "}";
            ResponseUtil.sendJson(ex, 200, json);
        } catch (SQLException e) {
            ResponseUtil.sendError(ex, 500, "Server error");
        }
    }
}
