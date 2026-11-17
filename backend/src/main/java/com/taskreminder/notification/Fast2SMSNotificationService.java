package com.taskreminder.notification;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;

/**
 * Real SMS delivery using Fast2SMS (India).
 *
 * FREE to use — sign up at https://www.fast2sms.com
 * After signup → Dashboard → Dev API → copy your API key
 * Paste it in application.properties: sms.fast2sms.apikey=YOUR_KEY
 */
public class Fast2SMSNotificationService implements SMSNotificationService {

    private static final String API_URL = "https://www.fast2sms.com/dev/bulkV2";
    private final String apiKey;
    private final HttpClient httpClient;

    public Fast2SMSNotificationService() {
        this.apiKey = loadApiKey();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    private String loadApiKey() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in != null) props.load(in);
        } catch (IOException e) {
            System.err.println("[Fast2SMS] Could not load application.properties");
        }
        String key = System.getenv().getOrDefault(
                "FAST2SMS_API_KEY",
                props.getProperty("sms.fast2sms.apikey", "")
        );
        if (key.isBlank() || key.equals("YOUR_FAST2SMS_API_KEY_HERE")) {
            System.err.println("[Fast2SMS] ⚠ API key not configured! Set sms.fast2sms.apikey in application.properties");
        }
        return key;
    }

    @Override
    public void send(String to, String subject, String message) {
        sendSMS(to, message);
    }

    @Override
    public void sendSMS(String mobileNumber, String message) {
        if (apiKey.isBlank() || apiKey.equals("YOUR_FAST2SMS_API_KEY_HERE")) {
            System.err.println("[Fast2SMS] Cannot send SMS — API key missing.");
            printFallback(mobileNumber, message);
            return;
        }

        // Strip country code for Fast2SMS (it expects 10-digit Indian numbers)
        String number = sanitizeNumber(mobileNumber);

        try {
            // Build JSON body
            String jsonBody = "{"
                    + "\"route\":\"q\","
                    + "\"message\":\"" + escapeJson(message) + "\","
                    + "\"language\":\"english\","
                    + "\"flash\":0,"
                    + "\"numbers\":\"" + number + "\""
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(15))
                    .header("authorization", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println();
            System.out.println("╔══════════════════════════════════════╗");
            System.out.println("║         SMS SENT (Fast2SMS)          ║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║ To      : " + padRight(mobileNumber, 27) + "║");
            System.out.println("║ Status  : HTTP " + padRight(String.valueOf(response.statusCode()), 23) + "║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║ Response: " + padRight(truncate(response.body(), 27), 27) + "║");
            System.out.println("╚══════════════════════════════════════╝");
            System.out.println();

            if (response.statusCode() != 200) {
                System.err.println("[Fast2SMS] Non-200 response: " + response.body());
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("[Fast2SMS] SMS delivery failed: " + e.getMessage());
            printFallback(mobileNumber, message);
        }
    }

    // Strip +91 or 91 prefix — Fast2SMS needs 10-digit number
    private String sanitizeNumber(String number) {
        if (number == null) return "";
        number = number.trim().replaceAll("[\\s\\-()]", "");
        if (number.startsWith("+91")) return number.substring(3);
        if (number.startsWith("91") && number.length() == 12) return number.substring(2);
        return number;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }

    private String padRight(String s, int w) {
        if (s == null) s = "";
        return s.length() >= w ? s.substring(0, w) : s + " ".repeat(w - s.length());
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }

    private void printFallback(String number, String message) {
        System.out.println("[Fast2SMS] FALLBACK — Would have sent to " + number + ": " + message);
    }
}
