package com.taskreminder.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Password hashing with SHA-256 + random salt.
 * No external libraries needed.
 */
public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {}

    /** Generate a random 32-byte salt encoded as Base64 */
    public static String generateSalt() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /** Hash password using SHA-256 with the given salt */
    public static String hash(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String input = salt + password;
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /** Verify that a plain-text password matches a stored hash + salt */
    public static boolean verify(String plainPassword, String storedHash, String storedSalt) {
        String computed = hash(plainPassword, storedSalt);
        return computed.equals(storedHash);
    }
}
