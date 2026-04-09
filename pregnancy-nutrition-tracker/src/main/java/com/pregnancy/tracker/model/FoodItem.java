package com.pregnancy.tracker.model;

/**
 * FoodItem entity representing a food from the nutrition database.
 * Each food item contains nutritional values per serving.
 */
public class FoodItem {

    private int id;
    private String name;
    private String category;
    private double calories;
    private double protein;      // grams
    private double iron;         // mg
    private double calcium;      // mg
    private double servingSize;
    private String servingUnit;

    /** Default constructor */
    public FoodItem() {}

    /** Full constructor */
    public FoodItem(int id, String name, String category, double calories,
                    double protein, double iron, double calcium,
                    double servingSize, String servingUnit) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.calories = calories;
        this.protein = protein;
        this.iron = iron;
        this.calcium = calcium;
        this.servingSize = servingSize;
        this.servingUnit = servingUnit;
    }

    /**
     * Calculate nutrients for a given quantity.
     * Nutrients in CSV are per 100g/ml, so scale accordingly.
     * @param quantity amount consumed in grams/ml
     * @return scaled FoodItem with adjusted nutrient values
     */
    public FoodItem getScaledNutrients(double quantity) {
        double factor = quantity / servingSize;
        FoodItem scaled = new FoodItem();
        scaled.setName(this.name);
        scaled.setCalories(this.calories * factor);
        scaled.setProtein(this.protein * factor);
        scaled.setIron(this.iron * factor);
        scaled.setCalcium(this.calcium * factor);
        return scaled;
    }

    // ==================== Getters & Setters ====================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }

    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }

    public double getIron() { return iron; }
    public void setIron(double iron) { this.iron = iron; }

    public double getCalcium() { return calcium; }
    public void setCalcium(double calcium) { this.calcium = calcium; }

    public double getServingSize() { return servingSize; }
    public void setServingSize(double servingSize) { this.servingSize = servingSize; }

    public String getServingUnit() { return servingUnit; }
    public void setServingUnit(String servingUnit) { this.servingUnit = servingUnit; }

    @Override
    public String toString() {
        return name + " (" + calories + " kcal, " + protein + "g protein)";
    }
}
