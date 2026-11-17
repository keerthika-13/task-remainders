package com.taskreminder.notification;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Properties;

/**
 * Sends task reminders via Telegram Bot API — 100% FREE forever.
 *
 * SETUP (one-time, 2 minutes):
 * ─────────────────────────────
 * 1. Open Telegram → search @BotFather → send /newbot
 * 2. Follow the steps, copy your Bot Token
 * 3. Paste token in application.properties: telegram.bot.token=YOUR_TOKEN
 * 4. Open your bot in Telegram → send it any message (e.g. /start)
 * 5. Visit: https://api.telegram.org/bot{YOUR_TOKEN}/getUpdates
 *    Copy the "id" value from "chat" — that's your Chat ID
 * 6. In the app → Profile page → paste your Chat ID → Save
 * 7. Done! You'll receive real Telegram messages on your phone.
 */
public class TelegramNotificationService implements SMSNotificationService {

    private static final String API_BASE = "https://api.telegram.org/bot";

    private final String botToken;
    private final HttpClient httpClient;

    public TelegramNotificationService() {
        this.botToken = loadBotToken();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    private String loadBotToken() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in != null) props.load(in);
        } catch (IOException e) {
            System.err.println("[Telegram] Could not load application.properties");
        }
        String env = System.getenv("TELEGRAM_BOT_TOKEN");
        String token = (env != null && !env.isBlank())
                ? env.trim()
                : props.getProperty("telegram.bot.token", "").trim();
        if (token.isBlank() || token.equals("YOUR_TELEGRAM_BOT_TOKEN_HERE")) {
            System.err.println("[Telegram] ⚠ Bot token not configured. Set TELEGRAM_BOT_TOKEN env var or telegram.bot.token in application.properties");
        } else {
            System.out.println("[Telegram] Bot token loaded.");
        }
        return token;
    }

    @Override
    public void send(String to, String subject, String message) {
        sendSMS(to, message);
    }

    /**
     * @param chatId  Telegram chat_id of the user (stored in users.telegram_chat_id)
     * @param message The reminder message text
     */
    @Override
    public void sendSMS(String chatId, String message) {
        if (botToken.isBlank() || botToken.equals("YOUR_TELEGRAM_BOT_TOKEN_HERE")) {
            System.err.println("[Telegram] Cannot send — bot token missing.");
            return;
        }
        if (chatId == null || chatId.isBlank()) {
            System.err.println("[Telegram] Cannot send — user has no Telegram Chat ID set.");
            return;
        }

        try {
            // Format message with Telegram HTML
            String formattedText = "⏰ <b>Task Reminder</b>\n\n" + escapeHtml(message)
                    + "\n\n<i>— Task Reminder App</i>";

            String jsonBody = "{"
                    + "\"chat_id\":\"" + chatId + "\","
                    + "\"text\":\"" + escapeJson(formattedText) + "\","
                    + "\"parse_mode\":\"HTML\""
                    + "}";

            String url = API_BASE + botToken + "/sendMessage";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println();
            System.out.println("╔══════════════════════════════════════╗");
            System.out.println("║      TELEGRAM MESSAGE SENT ✅         ║");
            System.out.println("╠══════════════════════════════════════╣");
            System.out.println("║ Chat ID : " + padRight(chatId, 27)  + "║");
            System.out.println("║ Status  : HTTP " + padRight(String.valueOf(response.statusCode()), 23) + "║");
            System.out.println("╠══════════════════════════════════════╣");

            for (String line : wrapText("Msg: " + message, 38)) {
                System.out.println("║ " + padRight(line, 38) + " ║");
            }
            System.out.println("╚══════════════════════════════════════╝");
            System.out.println();

            if (response.statusCode() != 200) {
                System.err.println("[Telegram] Error response: " + response.body());
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("[Telegram] Delivery failed: " + e.getMessage());
        }
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
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
