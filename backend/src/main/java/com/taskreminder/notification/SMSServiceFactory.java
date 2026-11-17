package com.taskreminder.notification;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Factory that picks the right notification service based on application.properties.
 *
 * sms.provider=telegram  → TelegramNotificationService (free forever ✅)
 * sms.provider=fast2sms  → Fast2SMSNotificationService (paid credits)
 * sms.provider=mock      → MockSMSNotificationService  (console only)
 */
public class SMSServiceFactory {

    private SMSServiceFactory() {}

    public static SMSNotificationService create() {
        String provider = loadProvider();
        System.out.println("[SMSServiceFactory] Notification provider: " + provider);

        return switch (provider.toLowerCase()) {
            case "telegram"  -> new TelegramNotificationService();
            case "fast2sms"  -> new Fast2SMSNotificationService();
            default          -> new MockSMSNotificationService();
        };
    }

    private static String loadProvider() {
        Properties props = new Properties();
        try (InputStream in = SMSServiceFactory.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in != null) props.load(in);
        } catch (IOException e) {
            System.err.println("[SMSServiceFactory] Could not load properties.");
        }
        return props.getProperty("sms.provider", "telegram");
    }
}
