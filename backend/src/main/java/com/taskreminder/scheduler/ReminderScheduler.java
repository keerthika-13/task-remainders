package com.taskreminder.scheduler;

import com.taskreminder.model.Reminder;
import com.taskreminder.model.Task;
import com.taskreminder.model.Notification;
import com.taskreminder.notification.SMSNotificationService;
import com.taskreminder.notification.SMSServiceFactory;
import com.taskreminder.repository.NotificationRepository;
import com.taskreminder.repository.ReminderRepository;
import com.taskreminder.repository.TaskRepository;
import com.taskreminder.repository.UserRepository;
import com.taskreminder.model.User;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Background scheduler that runs every minute.
 * 1. Marks overdue tasks.
 * 2. Fires pending reminders via MockSMSNotificationService.
 * 3. Creates a notification record for each sent reminder.
 */
public class ReminderScheduler {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    private final ReminderRepository reminderRepo    = new ReminderRepository();
    private final TaskRepository      taskRepo        = new TaskRepository();
    private final UserRepository      userRepo        = new UserRepository();
    private final NotificationRepository notifRepo   = new NotificationRepository();
    private final SMSNotificationService smsService  = SMSServiceFactory.create();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "reminder-scheduler");
        t.setDaemon(true);
        return t;
    });

    public void start() {
        scheduler.scheduleAtFixedRate(this::tick, 0, 60, TimeUnit.SECONDS);
        System.out.println("[ReminderScheduler] Started — checking every 60 seconds.");
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    private void tick() {
        try {
            // 1. Mark overdue tasks
            taskRepo.markOverdueTasks();

            // 2. Fire pending reminders
            List<Reminder> due = reminderRepo.findPendingDue();
            for (Reminder reminder : due) {
                try {
                    Task task = taskRepo.findById(reminder.getTaskId(), reminder.getUserId());
                    User user = userRepo.findById(reminder.getUserId());

                    if (task != null && user != null) {
                        String taskTime = task.getTaskDate() != null
                                ? task.getTaskDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                                  + (task.getTaskTime() != null ? " at " + task.getTaskTime() : "")
                                : "soon";

                        String smsMsg = "Reminder: \"" + task.getTitle() + "\" is due " + taskTime + ".";
                        // Send via configured provider (Telegram / SMS)
                        String recipient = user.getTelegramChatId();
                        boolean usingTelegramChat = recipient != null && !recipient.isBlank();
                        if (!usingTelegramChat) {
                            // fall back to mobile number for SMS providers
                            recipient = user.getMobile();
                        }
                        if (recipient != null && !recipient.isBlank()) {
                            if (smsService instanceof com.taskreminder.notification.TelegramNotificationService && !usingTelegramChat) {
                                System.err.println("[ReminderScheduler] User " + user.getId() + " has no Telegram Chat ID set (mobile number cannot be used for Telegram).");
                            } else {
                                smsService.sendSMS(recipient, smsMsg);
                            }
                        } else {
                            System.err.println("[ReminderScheduler] User " + user.getId() + " has no Telegram Chat ID or mobile number set.");
                        }

                        // Create notification record
                        Notification notif = new Notification();
                        notif.setUserId(user.getId());
                        notif.setTaskId(task.getId());
                        notif.setReminderId(reminder.getId());
                        notif.setTitle("Reminder: " + task.getTitle());
                        notif.setMessage(smsMsg);
                        notif.setType("reminder");
                        notifRepo.create(notif);

                        reminderRepo.markSent(reminder.getId());
                    } else {
                        reminderRepo.markFailed(reminder.getId());
                    }
                } catch (Exception e) {
                    System.err.println("[ReminderScheduler] Failed to send reminder " + reminder.getId() + ": " + e.getMessage());
                    try { reminderRepo.markFailed(reminder.getId()); } catch (Exception ignored) {}
                }
            }
        } catch (Exception e) {
            System.err.println("[ReminderScheduler] tick error: " + e.getMessage());
        }
    }
}
