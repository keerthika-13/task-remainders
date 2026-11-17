package com.taskreminder.notification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Mock SMS provider — prints to console instead of sending real SMS.
 * Replace this class (or add a new implementation) to plug in a real
 * provider like Twilio, AWS SNS, or MSG91 later.
 */
public class MockSMSNotificationService implements SMSNotificationService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void send(String to, String subject, String message) {
        sendSMS(to, message);
    }

    @Override
    public void sendSMS(String mobileNumber, String message) {
        System.out.println();
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║           SMS SENT (MOCK)            ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.println("║ To      : " + padRight(mobileNumber, 27) + "║");
        System.out.println("║ Time    : " + padRight(LocalDateTime.now().format(FMT), 27) + "║");
        System.out.println("╠══════════════════════════════════════╣");
        // Word-wrap message at 38 chars
        for (String line : wrapText("Msg: " + message, 38)) {
            System.out.println("║ " + padRight(line, 38) + " ║");
        }
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();
    }

    private String padRight(String s, int width) {
        if (s == null) s = "";
        return s.length() >= width ? s.substring(0, width) : s + " ".repeat(width - s.length());
    }

    private java.util.List<String> wrapText(String text, int width) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        while (text.length() > width) {
            int cut = text.lastIndexOf(' ', width);
            if (cut <= 0) cut = width;
            lines.add(text.substring(0, cut));
            text = text.substring(cut).trim();
        }
        lines.add(text);
        return lines;
    }
}
