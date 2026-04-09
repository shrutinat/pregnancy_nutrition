package com.pregnancy.tracker.dao;

import com.pregnancy.tracker.model.FoodItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for FoodItem entity.
 * Handles CRUD operations and CSV bulk import for the food_items table.
 */
public class FoodItemDao {

    private final DatabaseManager dbManager;

    public FoodItemDao() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Insert a single food item.
     * @param food FoodItem to insert
     * @return generated ID or -1 on failure
     */
    public int insert(FoodItem food) {
        String sql = """
            INSERT INTO food_items (name, category, calories, protein, iron, calcium, serving_size, serving_unit)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, food.getName());
            pstmt.setString(2, food.getCategory());
            pstmt.setDouble(3, food.getCalories());
            pstmt.setDouble(4, food.getProtein());
            pstmt.setDouble(5, food.getIron());
            pstmt.setDouble(6, food.getCalcium());
            pstmt.setDouble(7, food.getServingSize());
            pstmt.setString(8, food.getServingUnit());

            pstmt.executeUpdate();
            ResultSet keys = pstmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("[FoodItemDao] Insert error: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Bulk insert food items from a parsed CSV list.
     * Skips items that already exist (by name).
     * @param foods list of FoodItem objects to insert
     * @return count of items inserted
     */
    public int bulkInsert(List<FoodItem> foods) {
        int count = 0;
        for (FoodItem food : foods) {
            if (findByName(food.getName()) == null) {
                if (insert(food) > 0) count++;
            }
        }
        System.out.println("[FoodItemDao] Bulk inserted " + count + " food items.");
        return count;
    }

    /**
     * Find a food item by name.
     */
    public FoodItem findByName(String name) {
        String sql = "SELECT * FROM food_items WHERE name = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            System.err.println("[FoodItemDao] FindByName error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Find a food item by ID.
     */
    public FoodItem findById(int id) {
        String sql = "SELECT * FROM food_items WHERE id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            System.err.println("[FoodItemDao] FindById error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get all food items from the database.
     */
    public List<FoodItem> findAll() {
        List<FoodItem> foods = new ArrayList<>();
        String sql = "SELECT * FROM food_items ORDER BY name";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                foods.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[FoodItemDao] FindAll error: " + e.getMessage());
        }
        return foods;
    }

    /**
     * Search food items by name (partial match).
     * @param query search query
     * @return matching food items
     */
    public List<FoodItem> searchByName(String query) {
        List<FoodItem> foods = new ArrayList<>();
        String sql = "SELECT * FROM food_items WHERE LOWER(name) LIKE ? ORDER BY name";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + query.toLowerCase() + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                foods.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[FoodItemDao] Search error: " + e.getMessage());
        }
        return foods;
    }

    /**
     * Get food items sorted by a specific nutrient (highest first).
     * Useful for food suggestions.
     * @param nutrient column name (calories, protein, iron, calcium)
     * @param limit max results
     * @return top food items by nutrient
     */
    public List<FoodItem> getTopByNutrient(String nutrient, int limit) {
        List<FoodItem> foods = new ArrayList<>();
        // Whitelist allowed column names to prevent SQL injection
        if (!List.of("calories", "protein", "iron", "calcium").contains(nutrient)) {
            return foods;
        }
        String sql = "SELECT * FROM food_items ORDER BY " + nutrient + " DESC LIMIT ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                foods.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("[FoodItemDao] GetTopByNutrient error: " + e.getMessage());
        }
        return foods;
    }

    /**
     * Get the total count of food items in the database.
     */
    public int getCount() {
        String sql = "SELECT COUNT(*) FROM food_items";
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[FoodItemDao] Count error: " + e.getMessage());
        }
        return 0;
    }

    /** Map ResultSet to FoodItem */
    private FoodItem mapResultSet(ResultSet rs) throws SQLException {
        FoodItem food = new FoodItem();
        food.setId(rs.getInt("id"));
        food.setName(rs.getString("name"));
        food.setCategory(rs.getString("category"));
        food.setCalories(rs.getDouble("calories"));
        food.setProtein(rs.getDouble("protein"));
        food.setIron(rs.getDouble("iron"));
        food.setCalcium(rs.getDouble("calcium"));
        food.setServingSize(rs.getDouble("serving_size"));
        food.setServingUnit(rs.getString("serving_unit"));
        return food;
    }
}
