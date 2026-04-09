package com.pregnancy.tracker.model;

import java.time.LocalDateTime;

/**
 * Reminder entity for meal, medicine, and appointment alerts.
 * Supports different reminder types with scheduling capabilities.
 */
public class Reminder {

    /** Enum for different types of reminders */
    public enum ReminderType {
        MEAL("Meal Reminder"),
        MEDICINE("Medicine Reminder"),
        APPOINTMENT("Doctor Appointment"),
        CUSTOM("Custom Reminder");

        private final String displayName;

        ReminderType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() { return displayName; }
    }

    private int id;
    private int userId;
    private ReminderType type;
    private String title;
    private String description;
    private LocalDateTime scheduledTime;
    private boolean isActive;
    private boolean isRecurring;
    private String recurrencePattern;  // DAILY, WEEKLY, etc.

    /** Default constructor */
    public Reminder() {
        this.isActive = true;
        this.isRecurring = false;
    }

    /** Constructor for creating a new reminder */
    public Reminder(int userId, ReminderType type, String title,
                    String description, LocalDateTime scheduledTime) {
        this.userId = userId;
        this.type = type;
        this.title = title;
        this.description = description;
        this.scheduledTime = scheduledTime;
        this.isActive = true;
        this.isRecurring = false;
    }

    /**
     * Check if the reminder is due (within the next 5 minutes).
     * @return true if reminder should trigger
     */
    public boolean isDue() {
        if (!isActive || scheduledTime == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return scheduledTime.isAfter(now.minusMinutes(1)) &&
               scheduledTime.isBefore(now.plusMinutes(5));
    }

    /**
     * Check if the reminder is overdue.
     * @return true if past scheduled time
     */
    public boolean isOverdue() {
        if (scheduledTime == null) return false;
        return LocalDateTime.now().isAfter(scheduledTime);
    }

    // ==================== Getters & Setters ====================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public ReminderType getType() { return type; }
    public void setType(ReminderType type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }

    public String getRecurrencePattern() { return recurrencePattern; }
    public void setRecurrencePattern(String recurrencePattern) {
        this.recurrencePattern = recurrencePattern;
    }

    @Override
    public String toString() {
        return "Reminder{type=" + type + ", title='" + title +
               "', time=" + scheduledTime + ", active=" + isActive + "}";
    }
}
