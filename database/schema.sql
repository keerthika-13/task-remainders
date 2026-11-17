-- Task Reminder - Full Database Schema
-- Drop old schema and recreate
DROP DATABASE IF EXISTS task_remainder;
CREATE DATABASE task_remainder CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE task_remainder;

-- ─────────────────────────────────────────
-- USERS
-- ─────────────────────────────────────────
CREATE TABLE users (
    id              INT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL,
    email           VARCHAR(100) NOT NULL,
    mobile          VARCHAR(20),
    password_hash   VARCHAR(255) NOT NULL,
    salt            VARCHAR(64)  NOT NULL,
    telegram_chat_id VARCHAR(50),
    session_token   VARCHAR(255),
    token_expires   DATETIME,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE  KEY uq_users_email         (email),
    INDEX         idx_users_session    (session_token)
);

-- ─────────────────────────────────────────
-- TASKS
-- ─────────────────────────────────────────
CREATE TABLE tasks (
    id          INT          NOT NULL AUTO_INCREMENT,
    user_id     INT          NOT NULL,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    task_date   DATE,
    task_time   TIME,
    priority    ENUM('Low','Medium','High','Urgent')                               NOT NULL DEFAULT 'Medium',
    category    ENUM('Personal','Work','Study','Health','Finance','Shopping','Other') NOT NULL DEFAULT 'Personal',
    status      ENUM('Pending','Completed','Overdue','Cancelled')                  NOT NULL DEFAULT 'Pending',
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY fk_tasks_user (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_tasks_user   (user_id),
    INDEX idx_tasks_status (status),
    INDEX idx_tasks_date   (task_date)
);

-- ─────────────────────────────────────────
-- REMINDERS
-- ─────────────────────────────────────────
CREATE TABLE reminders (
    id            INT  NOT NULL AUTO_INCREMENT,
    task_id       INT  NOT NULL,
    user_id       INT  NOT NULL,
    remind_at     DATETIME NOT NULL,
    reminder_type VARCHAR(50) NOT NULL DEFAULT 'standard',
    status        ENUM('Pending','Sent','Failed') NOT NULL DEFAULT 'Pending',
    sent_at       DATETIME,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY fk_reminders_task (task_id) REFERENCES tasks(id)  ON DELETE CASCADE,
    FOREIGN KEY fk_reminders_user (user_id) REFERENCES users(id)  ON DELETE CASCADE,
    INDEX idx_reminders_remind_at (remind_at),
    INDEX idx_reminders_status    (status),
    INDEX idx_reminders_user      (user_id)
);

-- ─────────────────────────────────────────
-- NOTIFICATIONS
-- ─────────────────────────────────────────
CREATE TABLE notifications (
    id          INT          NOT NULL AUTO_INCREMENT,
    user_id     INT          NOT NULL,
    task_id     INT,
    reminder_id INT,
    title       VARCHAR(200) NOT NULL,
    message     TEXT         NOT NULL,
    type        VARCHAR(50)  NOT NULL DEFAULT 'reminder',
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY fk_notifications_user     (user_id)     REFERENCES users(id)     ON DELETE CASCADE,
    FOREIGN KEY fk_notifications_task     (task_id)     REFERENCES tasks(id)     ON DELETE SET NULL,
    FOREIGN KEY fk_notifications_reminder (reminder_id) REFERENCES reminders(id) ON DELETE SET NULL,
    INDEX idx_notifications_user    (user_id),
    INDEX idx_notifications_is_read (is_read)
);
