package com.taskreminder.notification;

/**
 * Base interface for all notification services.
 */
public interface NotificationService {
    void send(String to, String subject, String message);
}
