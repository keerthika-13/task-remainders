package com.taskreminder.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Task {
    private int id;
    private int userId;
    private String title;
    private String description;
    private LocalDate taskDate;
    private LocalTime taskTime;
    private String priority;   // Low | Medium | High | Urgent
    private String category;   // Personal | Work | Study | Health | Finance | Shopping | Other
    private String status;     // Pending | Completed | Overdue | Cancelled
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Task() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getTaskDate() { return taskDate; }
    public void setTaskDate(LocalDate taskDate) { this.taskDate = taskDate; }

    public LocalTime getTaskTime() { return taskTime; }
    public void setTaskTime(LocalTime taskTime) { this.taskTime = taskTime; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
