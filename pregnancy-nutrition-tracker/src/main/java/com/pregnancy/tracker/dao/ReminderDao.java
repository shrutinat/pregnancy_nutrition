package com.pregnancy.tracker.dao;

import com.pregnancy.tracker.model.Reminder;
import com.pregnancy.tracker.model.Reminder.ReminderType;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Reminder entity.
 * Handles CRUD operations for reminders table.
 */
public class ReminderDao {

    private final DatabaseManager dbManager;

    public ReminderDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Insert a new reminder.
     * @param reminder Reminder to insert
     * @return generated ID or -1 on failure
     */
    public int insert(Reminder reminder) {
        String sql = """
            INSERT INTO reminders (user_id, type, title, description, scheduled_time, is_active, is_recurring, recurrence_pattern)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, reminder.getUserId());
            pstmt.setString(2, reminder.getType().name());
            pstmt.setString(3, reminder.getTitle());
            pstmt.setString(4, reminder.getDescription());
            pstmt.setString(5, reminder.getScheduledTime() != null ?
                    reminder.getScheduledTime().toString() : null);
            pstmt.setInt(6, reminder.isActive() ? 1 : 0);
            pstmt.setInt(7, reminder.isRecurring() ? 1 : 0);
            pstmt.setString(8, reminder.getRecurrencePattern());

            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("[ReminderDao] Insert error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Update an existing reminder.
     */
    public boolean update(Reminder reminder) {
        String sql = """
            UPDATE reminders SET type=?, title=?, description=?, scheduled_time=?,
            is_active=?, is_recurring=?, recurrence_pattern=?
            WHERE id=?
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, reminder.getType().name());
            pstmt.setString(2, reminder.getTitle());
            pstmt.setString(3, reminder.getDescription());
            pstmt.setString(4, reminder.getScheduledTime() != null ?
                    reminder.getScheduledTime().toString() : null);
            pstmt.setInt(5, reminder.isActive() ? 1 : 0);
            pstmt.setInt(6, reminder.isRecurring() ? 1 : 0);
            pstmt.setString(7, reminder.getRecurrencePattern());
            pstmt.setInt(8, reminder.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ReminderDao] Update error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete a reminder by ID.
     */
    public boolean delete(int reminderId) {
        String sql = "DELETE FROM reminders WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reminderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ReminderDao] Delete error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get all reminders for a user.
     */
    public List<Reminder> findByUserId(int userId) {
        List<Reminder> reminders = new ArrayList<>();
        String sql = "SELECT * FROM reminders WHERE user_id = ? ORDER BY scheduled_time";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                reminders.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ReminderDao] FindByUserId error: " + e.getMessage());
        }
        return reminders;
    }

    /**
     * Get all active reminders for a user.
     */
    public List<Reminder> findActiveByUserId(int userId) {
        List<Reminder> reminders = new ArrayList<>();
        String sql = "SELECT * FROM reminders WHERE user_id = ? AND is_active = 1 ORDER BY scheduled_time";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                reminders.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[ReminderDao] FindActive error: " + e.getMessage());
        }
        return reminders;
    }

    /**
     * Toggle the active state of a reminder.
     */
    public boolean toggleActive(int reminderId) {
        String sql = "UPDATE reminders SET is_active = CASE WHEN is_active = 1 THEN 0 ELSE 1 END WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reminderId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ReminderDao] Toggle error: " + e.getMessage());
        }
        return false;
    }

    /** Map ResultSet to Reminder */
    private Reminder mapResultSet(ResultSet rs) throws SQLException {
        Reminder reminder = new Reminder();
        reminder.setId(rs.getInt("id"));
        reminder.setUserId(rs.getInt("user_id"));
        try {
            reminder.setType(ReminderType.valueOf(rs.getString("type")));
        } catch (IllegalArgumentException e) {
            reminder.setType(ReminderType.CUSTOM);
        }
        reminder.setTitle(rs.getString("title"));
        reminder.setDescription(rs.getString("description"));
        String timeStr = rs.getString("scheduled_time");
        if (timeStr != null && !timeStr.isEmpty()) {
            reminder.setScheduledTime(LocalDateTime.parse(timeStr));
        }
        reminder.setActive(rs.getInt("is_active") == 1);
        reminder.setRecurring(rs.getInt("is_recurring") == 1);
        reminder.setRecurrencePattern(rs.getString("recurrence_pattern"));
        return reminder;
    }
}
