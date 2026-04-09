package com.pregnancy.tracker.model;

import java.time.LocalDate;

/**
 * DailyLog entity representing a food consumption entry.
 * Tracks what food was eaten, quantity, and whether it was consumed.
 */
public class DailyLog {

    private int id;
    private int userId;
    private LocalDate logDate;
    private int foodItemId;
    private String foodName;     // denormalized for display convenience
    private double quantity;     // in grams or ml
    private boolean consumed;

    // Calculated nutrient values (not stored, computed at runtime)
    private double calories;
    private double protein;
    private double iron;
    private double calcium;

    /** Default constructor */
    public DailyLog() {}

    /** Constructor for creating a new log entry */
    public DailyLog(int userId, LocalDate logDate, int foodItemId, double quantity) {
        this.userId = userId;
        this.logDate = logDate;
        this.foodItemId = foodItemId;
        this.quantity = quantity;
        this.consumed = false;
    }

    // ==================== Getters & Setters ====================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public LocalDate getLogDate() { return logDate; }
    public void setLogDate(LocalDate logDate) { this.logDate = logDate; }

    public int getFoodItemId() { return foodItemId; }
    public void setFoodItemId(int foodItemId) { this.foodItemId = foodItemId; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public boolean isConsumed() { return consumed; }
    public void setConsumed(boolean consumed) { this.consumed = consumed; }

    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }

    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }

    public double getIron() { return iron; }
    public void setIron(double iron) { this.iron = iron; }

    public double getCalcium() { return calcium; }
    public void setCalcium(double calcium) { this.calcium = calcium; }

    @Override
    public String toString() {
        return "DailyLog{food='" + foodName + "', quantity=" + quantity +
               ", consumed=" + consumed + ", date=" + logDate + "}";
    }
}
