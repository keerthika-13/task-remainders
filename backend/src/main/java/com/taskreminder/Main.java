package com.taskreminder;

import com.sun.net.httpserver.HttpServer;
import com.taskreminder.config.DatabaseConfig;
import com.taskreminder.controller.*;
import com.taskreminder.router.Router;
import com.taskreminder.scheduler.ReminderScheduler;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {

    public static void main(String[] args) throws IOException {

        // ── 1. Test DB connection ────────────────────────────────────
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║      Task Reminder - Backend         ║");
        System.out.println("╚══════════════════════════════════════╝");

        boolean dbOk = DatabaseConfig.testConnection();
        System.out.println("[DB] Connection: " + (dbOk ? "✓ Connected" : "✗ Unavailable (check MySQL)"));

        // ── 2. Build controllers ─────────────────────────────────────
        AuthController                  authCtrl    = new AuthController();
        TaskController                  taskCtrl    = new TaskController();
        DashboardCalendarController     dashCtrl    = new DashboardCalendarController();
        ReminderNotificationController  notifCtrl   = new ReminderNotificationController();

        // ── 3. Register routes ───────────────────────────────────────
        Router router = new Router();

        // Health
        router.get("/api/health", (ex, p) -> {
            String status = DatabaseConfig.testConnection() ? "ok" : "db-unavailable";
            com.taskreminder.util.ResponseUtil.sendJson(ex, 200,
                "{\"status\":\"" + status + "\",\"service\":\"task-reminder-backend\"}");
        });

        // Auth
        router.post("/api/auth/register",  authCtrl::register);
        router.post("/api/auth/login",     authCtrl::login);
        router.post("/api/auth/logout",    authCtrl::logout);
        router.get ("/api/auth/me",        authCtrl::me);
        router.put ("/api/auth/profile",   authCtrl::updateProfile);

        // Tasks
        router.get   ("/api/tasks",              taskCtrl::list);
        router.post  ("/api/tasks",              taskCtrl::create);
        router.get   ("/api/tasks/{id}",         taskCtrl::getById);
        router.put   ("/api/tasks/{id}",         taskCtrl::update);
        router.delete("/api/tasks/{id}",         taskCtrl::delete);
        router.patch ("/api/tasks/{id}/complete",taskCtrl::complete);
        router.patch ("/api/tasks/{id}/cancel",  taskCtrl::cancel);

        // Dashboard & Calendar
        router.get("/api/dashboard/summary",     dashCtrl::summary);
        router.get("/api/calendar",              dashCtrl::calendar);

        // Reminders & Notifications
        router.get  ("/api/reminders",                       notifCtrl::listReminders);
        router.get  ("/api/notifications",                   notifCtrl::listNotifications);
        router.patch("/api/notifications/read-all",          notifCtrl::markAllRead);
        router.patch("/api/notifications/{id}/read",         notifCtrl::markRead);

        // ── 4. Start HTTP server ─────────────────────────────────────
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api", router);
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("[Server] Running on http://localhost:8080");
        System.out.println("[API]    Health  → http://localhost:8080/api/health");
        System.out.println("[API]    Auth    → POST /api/auth/register | /api/auth/login");
        System.out.println("[API]    Tasks   → GET/POST/PUT/DELETE /api/tasks");

        // ── 5. Start reminder scheduler ──────────────────────────────
        if (dbOk) {
            ReminderScheduler scheduler = new ReminderScheduler();
            scheduler.start();
        } else {
            System.out.println("[Scheduler] Skipped (no DB connection).");
        }
    }
}
