package com.taskreminder.service;

import com.taskreminder.model.User;
import com.taskreminder.repository.UserRepository;
import com.taskreminder.util.PasswordUtil;
import com.taskreminder.util.SessionUtil;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuthService {

    private final UserRepository userRepo = new UserRepository();

    // ─── Register ───────────────────────────────────────────────────

    public Map<String, Object> register(String name, String email, String mobile,
                                        String password, String confirmPassword) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();

        // Validation
        if (name == null || name.isBlank()) { result.put("error", "Name is required"); return result; }
        if (email == null || !email.contains("@")) { result.put("error", "Valid email is required"); return result; }
        if (mobile == null || mobile.isBlank()) { result.put("error", "Mobile number is required"); return result; }
        if (password == null || password.length() < 6) { result.put("error", "Password must be at least 6 characters"); return result; }
        if (!password.equals(confirmPassword)) { result.put("error", "Passwords do not match"); return result; }

        // Duplicate email check
        if (userRepo.findByEmail(email) != null) {
            result.put("error", "Email already registered");
            return result;
        }

        // Hash password
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hash(password, salt);

        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim().toLowerCase());
        user.setMobile(mobile.trim());
        user.setPasswordHash(hash);
        user.setSalt(salt);

        User created = userRepo.create(user);

        // Auto-login after registration
        String token = SessionUtil.generateToken();
        LocalDateTime expires = LocalDateTime.now().plusDays(7);
        userRepo.saveSessionToken(created.getId(), token, expires);

        result.put("success", true);
        result.put("token", token);
        result.put("user", safeUserMap(created));
        return result;
    }

    // ─── Login ──────────────────────────────────────────────────────

    public Map<String, Object> login(String email, String password) throws SQLException {
        Map<String, Object> result = new LinkedHashMap<>();

        if (email == null || email.isBlank()) { result.put("error", "Email is required"); return result; }
        if (password == null || password.isBlank()) { result.put("error", "Password is required"); return result; }

        User user = userRepo.findByEmail(email.trim().toLowerCase());
        if (user == null || !PasswordUtil.verify(password, user.getPasswordHash(), user.getSalt())) {
            result.put("error", "Invalid email or password");
            return result;
        }

        String token = SessionUtil.generateToken();
        LocalDateTime expires = LocalDateTime.now().plusDays(7);
        userRepo.saveSessionToken(user.getId(), token, expires);

        result.put("success", true);
        result.put("token", token);
        result.put("user", safeUserMap(user));
        return result;
    }

    // ─── Logout ─────────────────────────────────────────────────────

    public void logout(int userId) throws SQLException {
        userRepo.clearSessionToken(userId);
    }

    // ─── Authenticate request ────────────────────────────────────────

    public User authenticate(String token) throws SQLException {
        if (token == null || token.isBlank()) return null;
        return userRepo.findBySessionToken(token);
    }

    // ─── Helpers ────────────────────────────────────────────────────

    private Map<String, Object> safeUserMap(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id",             u.getId());
        m.put("name",           u.getName());
        m.put("email",          u.getEmail());
        m.put("mobile",         u.getMobile() != null ? u.getMobile() : "");
        m.put("telegramChatId", u.getTelegramChatId() != null ? u.getTelegramChatId() : "");
        return m;
    }
}
