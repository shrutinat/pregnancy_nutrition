package com.pregnancy.tracker.dao;

import com.pregnancy.tracker.model.DoctorUpdate;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for DoctorUpdate entity.
 * Handles CRUD operations for doctor_updates table.
 */
public class DoctorUpdateDao {

    private final DatabaseManager dbManager;

    public DoctorUpdateDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Insert a new doctor update.
     * @param update DoctorUpdate to insert
     * @return generated ID or -1 on failure
     */
    public int insert(DoctorUpdate update) {
        String sql = """
            INSERT INTO doctor_updates (user_id, update_date, doctor_name, notes, risk_conditions, updated_targets)
            VALUES (?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, update.getUserId());
            pstmt.setString(2, update.getUpdateDate().toString());
            pstmt.setString(3, update.getDoctorName());
            pstmt.setString(4, update.getNotes());
            pstmt.setString(5, update.getRiskConditions());
            pstmt.setString(6, update.getUpdatedTargets());

            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DoctorUpdateDao] Insert error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Get all doctor updates for a user, most recent first.
     */
    public List<DoctorUpdate> findByUserId(int userId) {
        List<DoctorUpdate> updates = new ArrayList<>();
        String sql = "SELECT * FROM doctor_updates WHERE user_id = ? ORDER BY update_date DESC";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                updates.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[DoctorUpdateDao] FindByUserId error: " + e.getMessage());
        }
        return updates;
    }

    /**
     * Get the most recent doctor update for a user.
     */
    public DoctorUpdate findLatestByUserId(int userId) {
        String sql = "SELECT * FROM doctor_updates WHERE user_id = ? ORDER BY update_date DESC LIMIT 1";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            System.err.println("[DoctorUpdateDao] FindLatest error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Delete a doctor update.
     */
    public boolean delete(int updateId) {
        String sql = "DELETE FROM doctor_updates WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, updateId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DoctorUpdateDao] Delete error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Check if there are any active risk conditions for a user.
     * @return latest risk conditions string or null
     */
    public String getActiveRiskConditions(int userId) {
        DoctorUpdate latest = findLatestByUserId(userId);
        if (latest != null && latest.hasRiskConditions()) {
            return latest.getRiskConditions();
        }
        return null;
    }

    /**
     * Log a weight entry for tracking.
     */
    public boolean logWeight(int userId, LocalDate date, double weight) {
        String sql = "INSERT INTO weight_logs (user_id, log_date, weight) VALUES (?, ?, ?)";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, date.toString());
            pstmt.setDouble(3, weight);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DoctorUpdateDao] LogWeight error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get weight history for a user.
     * @return list of [date, weight] pairs
     */
    public List<double[]> getWeightHistory(int userId) {
        List<double[]> history = new ArrayList<>();
        String sql = "SELECT log_date, weight FROM weight_logs WHERE user_id = ? ORDER BY log_date";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                // Store as epoch day and weight for charting
                LocalDate date = LocalDate.parse(rs.getString("log_date"));
                history.add(new double[]{date.toEpochDay(), rs.getDouble("weight")});
            }
        } catch (SQLException e) {
            System.err.println("[DoctorUpdateDao] GetWeightHistory error: " + e.getMessage());
        }
        return history;
    }

    /**
     * Get weight log dates for a user (for display).
     */
    public List<String[]> getWeightHistoryWithDates(int userId) {
        List<String[]> history = new ArrayList<>();
        String sql = "SELECT log_date, weight FROM weight_logs WHERE user_id = ? ORDER BY log_date";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                history.add(new String[]{rs.getString("log_date"), String.valueOf(rs.getDouble("weight"))});
            }
        } catch (SQLException e) {
            System.err.println("[DoctorUpdateDao] GetWeightHistoryDates error: " + e.getMessage());
        }
        return history;
    }

    /** Map ResultSet to DoctorUpdate */
    private DoctorUpdate mapResultSet(ResultSet rs) throws SQLException {
        DoctorUpdate update = new DoctorUpdate();
        update.setId(rs.getInt("id"));
        update.setUserId(rs.getInt("user_id"));
        update.setUpdateDate(LocalDate.parse(rs.getString("update_date")));
        update.setDoctorName(rs.getString("doctor_name"));
        update.setNotes(rs.getString("notes"));
        update.setRiskConditions(rs.getString("risk_conditions"));
        update.setUpdatedTargets(rs.getString("updated_targets"));
        return update;
    }
}
