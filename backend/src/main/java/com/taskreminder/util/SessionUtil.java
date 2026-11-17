package com.taskreminder.util;

import com.sun.net.httpserver.HttpExchange;

import java.util.UUID;

/**
 * Session token helpers.
 * Tokens are UUID strings stored in the users table.
 */
public final class SessionUtil {

    private SessionUtil() {}

    public static String generateToken() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Extract the session token from the Authorization header.
     * Accepts: "Bearer <token>" or just "<token>"
     */
    public static String extractToken(HttpExchange ex) {
        String auth = ex.getRequestHeaders().getFirst("Authorization");
        if (auth == null || auth.isBlank()) {
            // also try X-Session-Token header
            auth = ex.getRequestHeaders().getFirst("X-Session-Token");
        }
        if (auth == null) return null;
        auth = auth.trim();
        if (auth.toLowerCase().startsWith("bearer ")) {
            return auth.substring(7).trim();
        }
        return auth;
    }
}
