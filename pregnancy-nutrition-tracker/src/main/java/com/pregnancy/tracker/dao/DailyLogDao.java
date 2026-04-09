package com.pregnancy.tracker.dao;

import com.pregnancy.tracker.model.DailyLog;
import com.pregnancy.tracker.model.FoodItem;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for DailyLog entity.
 * Handles daily food consumption tracking operations.
 */
public class DailyLogDao {

    private final DatabaseManager dbManager;
    private final FoodItemDao foodItemDao;

    public DailyLogDao() {
        this.dbManager = DatabaseManager.getInstance();
        this.foodItemDao = new FoodItemDao();
    }

    /**
     * Insert a new daily food log entry.
     * @param log DailyLog to insert
     * @return generated ID or -1 on failure
     */
    public int insert(DailyLog log) {
        String sql = """
            INSERT INTO daily_logs (user_id, log_date, food_item_id, quantity, consumed)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, log.getUserId());
            pstmt.setString(2, log.getLogDate().toString());
            pstmt.setInt(3, log.getFoodItemId());
            pstmt.setDouble(4, log.getQuantity());
            pstmt.setInt(5, log.isConsumed() ? 1 : 0);

            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) {
            System.err.println("[DailyLogDao] Insert error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Update the consumed status of a log entry.
     * @param logId log entry ID
     * @param consumed whether the food was consumed
     * @return true if update was successful
     */
    public boolean updateConsumedStatus(int logId, boolean consumed) {
        String sql = "UPDATE daily_logs SET consumed = ? WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, consumed ? 1 : 0);
            pstmt.setInt(2, logId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DailyLogDao] UpdateConsumed error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete a daily log entry.
     */
    public boolean delete(int logId) {
        String sql = "DELETE FROM daily_logs WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, logId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[DailyLogDao] Delete error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get all food logs for a user on a specific date.
     * Includes calculated nutrient values based on food and quantity.
     * @param userId user ID
     * @param date the date to query
     * @return list of daily logs with nutrient calculations
     */
    public List<DailyLog> getLogsForDate(int userId, LocalDate date) {
        List<DailyLog> logs = new ArrayList<>();
        String sql = """
            SELECT dl.*, fi.name as food_name, fi.calories, fi.protein, fi.iron, fi.calcium, fi.serving_size
            FROM daily_logs dl
            JOIN food_items fi ON dl.food_item_id = fi.id
            WHERE dl.user_id = ? AND dl.log_date = ?
            ORDER BY dl.id
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, date.toString());

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                DailyLog log = mapResultSet(rs);
                // Calculate nutrient values based on quantity
                double factor = log.getQuantity() / rs.getDouble("serving_size");
                log.setCalories(rs.getDouble("calories") * factor);
                log.setProtein(rs.getDouble("protein") * factor);
                log.setIron(rs.getDouble("iron") * factor);
                log.setCalcium(rs.getDouble("calcium") * factor);
                logs.add(log);
            }
        } catch (SQLException e) {
            System.err.println("[DailyLogDao] GetLogsForDate error: " + e.getMessage());
        }
        return logs;
    }

    /**
     * Get total consumed nutrients for a user on a specific date.
     * Only counts entries marked as consumed.
     * @return double array: [calories, protein, iron, calcium]
     */
    public double[] getConsumedNutrients(int userId, LocalDate date) {
        double[] totals = new double[4]; // calories, protein, iron, calcium
        String sql = """
            SELECT
                SUM(fi.calories * dl.quantity / fi.serving_size) as total_calories,
                SUM(fi.protein * dl.quantity / fi.serving_size) as total_protein,
                SUM(fi.iron * dl.quantity / fi.serving_size) as total_iron,
                SUM(fi.calcium * dl.quantity / fi.serving_size) as total_calcium
            FROM daily_logs dl
            JOIN food_items fi ON dl.food_item_id = fi.id
            WHERE dl.user_id = ? AND dl.log_date = ? AND dl.consumed = 1
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, date.toString());

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                totals[0] = rs.getDouble("total_calories");
                totals[1] = rs.getDouble("total_protein");
                totals[2] = rs.getDouble("total_iron");
                totals[3] = rs.getDouble("total_calcium");
            }
        } catch (SQLException e) {
            System.err.println("[DailyLogDao] GetConsumedNutrients error: " + e.getMessage());
        }
        return totals;
    }

    /**
     * Get logs for a date range (for weekly progress tracking).
     */
    public List<DailyLog> getLogsForDateRange(int userId, LocalDate startDate, LocalDate endDate) {
        List<DailyLog> logs = new ArrayList<>();
        String sql = """
            SELECT dl.*, fi.name as food_name, fi.calories, fi.protein, fi.iron, fi.calcium, fi.serving_size
            FROM daily_logs dl
            JOIN food_items fi ON dl.food_item_id = fi.id
            WHERE dl.user_id = ? AND dl.log_date BETWEEN ? AND ?
            ORDER BY dl.log_date, dl.id
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setString(2, startDate.toString());
            pstmt.setString(3, endDate.toString());

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                DailyLog log = mapResultSet(rs);
                double factor = log.getQuantity() / rs.getDouble("serving_size");
                log.setCalories(rs.getDouble("calories") * factor);
                log.setProtein(rs.getDouble("protein") * factor);
                log.setIron(rs.getDouble("iron") * factor);
                log.setCalcium(rs.getDouble("calcium") * factor);
                logs.add(log);
            }
        } catch (SQLException e) {
            System.err.println("[DailyLogDao] GetLogsForDateRange error: " + e.getMessage());
        }
        return logs;
    }

    /** Map ResultSet to DailyLog */
    private DailyLog mapResultSet(ResultSet rs) throws SQLException {
        DailyLog log = new DailyLog();
        log.setId(rs.getInt("id"));
        log.setUserId(rs.getInt("user_id"));
        log.setLogDate(LocalDate.parse(rs.getString("log_date")));
        log.setFoodItemId(rs.getInt("food_item_id"));
        log.setFoodName(rs.getString("food_name"));
        log.setQuantity(rs.getDouble("quantity"));
        log.setConsumed(rs.getInt("consumed") == 1);
        return log;
    }
}
