package com.pregnancy.tracker.dao;

import com.pregnancy.tracker.model.NutritionTarget;

import java.sql.*;

/**
 * Data Access Object for NutritionTarget entity.
 * Handles CRUD operations for nutrition_targets table.
 */
public class NutritionTargetDao {

    private final DatabaseManager dbManager;

    public NutritionTargetDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Insert or update nutrition targets for a user's trimester.
     * If targets exist for the user/trimester combo, updates them.
     * @param target NutritionTarget to save
     * @return true if operation was successful
     */
    public boolean saveOrUpdate(NutritionTarget target) {
        NutritionTarget existing = findByUserAndTrimester(target.getUserId(), target.getTrimester());
        if (existing != null) {
            target.setId(existing.getId());
            return update(target);
        }
        return insert(target);
    }

    /**
     * Insert a new nutrition target record.
     */
    private boolean insert(NutritionTarget target) {
        String sql = """
            INSERT INTO nutrition_targets
            (user_id, trimester, calories, protein, iron, calcium,
             vitamin_a, vitamin_c, vitamin_d, folic_acid, water_intake, is_doctor_modified)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            setTargetParams(pstmt, target);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NutritionTargetDao] Insert error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Update existing nutrition targets.
     */
    private boolean update(NutritionTarget target) {
        String sql = """
            UPDATE nutrition_targets SET
            calories=?, protein=?, iron=?, calcium=?,
            vitamin_a=?, vitamin_c=?, vitamin_d=?, folic_acid=?,
            water_intake=?, is_doctor_modified=?
            WHERE id=?
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, target.getCalories());
            pstmt.setDouble(2, target.getProtein());
            pstmt.setDouble(3, target.getIron());
            pstmt.setDouble(4, target.getCalcium());
            pstmt.setDouble(5, target.getVitaminA());
            pstmt.setDouble(6, target.getVitaminC());
            pstmt.setDouble(7, target.getVitaminD());
            pstmt.setDouble(8, target.getFolicAcid());
            pstmt.setDouble(9, target.getWaterIntake());
            pstmt.setInt(10, target.isDoctorModified() ? 1 : 0);
            pstmt.setInt(11, target.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[NutritionTargetDao] Update error: " + e.getMessage());
        }
        return false;
    }

    /**
     * Find nutrition targets for a specific user and trimester.
     * @param userId the user's ID
     * @param trimester the trimester number (1, 2, or 3)
     * @return NutritionTarget or null if not found
     */
    public NutritionTarget findByUserAndTrimester(int userId, int trimester) {
        String sql = "SELECT * FROM nutrition_targets WHERE user_id = ? AND trimester = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, trimester);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return mapResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("[NutritionTargetDao] Find error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get nutrition targets for a user. If none exist in DB, return WHO defaults.
     */
    public NutritionTarget getTargetsForUser(int userId, int trimester) {
        NutritionTarget target = findByUserAndTrimester(userId, trimester);
        if (target == null) {
            target = NutritionTarget.getDefaults(trimester);
            target.setUserId(userId);
            saveOrUpdate(target);
        }
        return target;
    }

    /** Set PreparedStatement parameters for insert */
    private void setTargetParams(PreparedStatement pstmt, NutritionTarget t) throws SQLException {
        pstmt.setInt(1, t.getUserId());
        pstmt.setInt(2, t.getTrimester());
        pstmt.setDouble(3, t.getCalories());
        pstmt.setDouble(4, t.getProtein());
        pstmt.setDouble(5, t.getIron());
        pstmt.setDouble(6, t.getCalcium());
        pstmt.setDouble(7, t.getVitaminA());
        pstmt.setDouble(8, t.getVitaminC());
        pstmt.setDouble(9, t.getVitaminD());
        pstmt.setDouble(10, t.getFolicAcid());
        pstmt.setDouble(11, t.getWaterIntake());
        pstmt.setInt(12, t.isDoctorModified() ? 1 : 0);
    }

    /** Map ResultSet to NutritionTarget */
    private NutritionTarget mapResultSet(ResultSet rs) throws SQLException {
        NutritionTarget t = new NutritionTarget();
        t.setId(rs.getInt("id"));
        t.setUserId(rs.getInt("user_id"));
        t.setTrimester(rs.getInt("trimester"));
        t.setCalories(rs.getDouble("calories"));
        t.setProtein(rs.getDouble("protein"));
        t.setIron(rs.getDouble("iron"));
        t.setCalcium(rs.getDouble("calcium"));
        t.setVitaminA(rs.getDouble("vitamin_a"));
        t.setVitaminC(rs.getDouble("vitamin_c"));
        t.setVitaminD(rs.getDouble("vitamin_d"));
        t.setFolicAcid(rs.getDouble("folic_acid"));
        t.setWaterIntake(rs.getDouble("water_intake"));
        t.setDoctorModified(rs.getInt("is_doctor_modified") == 1);
        return t;
    }
}
