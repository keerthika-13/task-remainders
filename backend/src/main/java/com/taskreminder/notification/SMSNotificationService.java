package com.taskreminder.notification;

/**
 * Interface for SMS reminder notifications.
 */
public interface SMSNotificationService extends NotificationService {
    void sendSMS(String mobileNumber, String message);
}
