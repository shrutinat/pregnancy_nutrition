package com.pregnancy.tracker.service;

import com.pregnancy.tracker.dao.DailyLogDao;
import com.pregnancy.tracker.dao.FoodItemDao;
import com.pregnancy.tracker.model.FoodItem;
import com.pregnancy.tracker.model.NutritionTarget;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI-based Food Suggestion Engine.
 * Uses rule-based logic to suggest foods based on nutrient deficits.
 * Dynamically queries the food database and ranks suggestions by relevance.
 */
public class FoodSuggestionEngine {

    private final FoodItemDao foodItemDao;
    private final DailyLogDao dailyLogDao;

    public FoodSuggestionEngine() {
        this.foodItemDao = new FoodItemDao();
        this.dailyLogDao = new DailyLogDao();
    }

    /**
     * Represents a food suggestion with a reason.
     */
    public static class Suggestion {
        private final FoodItem food;
        private final String reason;
        private final double relevanceScore;

        public Suggestion(FoodItem food, String reason, double relevanceScore) {
            this.food = food;
            this.reason = reason;
            this.relevanceScore = relevanceScore;
        }

        public FoodItem getFood() { return food; }
        public String getReason() { return reason; }
        public double getRelevanceScore() { return relevanceScore; }
    }

    /**
     * Generate intelligent food suggestions based on current nutrient deficits.
     * Core AI rule: IF nutrient deficit > threshold → suggest foods rich in that nutrient
     *
     * @param userId user ID
     * @param target daily nutrition targets
     * @return list of food suggestions sorted by relevance
     */
    public List<Suggestion> generateSuggestions(int userId, NutritionTarget target) {
        // Get today's consumed nutrients
        double[] consumed = dailyLogDao.getConsumedNutrients(userId, LocalDate.now());

        List<Suggestion> allSuggestions = new ArrayList<>();

        // Calculate deficits for each nutrient
        double calorieDeficit = NutritionCalculator.calculateDeficit(consumed[0], target.getCalories());
        double proteinDeficit = NutritionCalculator.calculateDeficit(consumed[1], target.getProtein());
        double ironDeficit = NutritionCalculator.calculateDeficit(consumed[2], target.getIron());
        double calciumDeficit = NutritionCalculator.calculateDeficit(consumed[3], target.getCalcium());

        // RULE 1: If iron deficit > 30%, suggest iron-rich foods
        if (ironDeficit > 30) {
            List<FoodItem> ironFoods = foodItemDao.getTopByNutrient("iron", 10);
            for (FoodItem food : ironFoods) {
                double score = food.getIron() * (ironDeficit / 100);
                String reason = String.format("Rich in Iron (%.1f mg) — You need %.0f%% more iron today",
                        food.getIron(), ironDeficit);
                allSuggestions.add(new Suggestion(food, reason, score * 3)); // Iron is critical in pregnancy
            }
        }

        // RULE 2: If calcium deficit > 30%, suggest calcium-rich foods
        if (calciumDeficit > 30) {
            List<FoodItem> calciumFoods = foodItemDao.getTopByNutrient("calcium", 10);
            for (FoodItem food : calciumFoods) {
                double score = food.getCalcium() * (calciumDeficit / 100) / 10; // Normalize
                String reason = String.format("Rich in Calcium (%.0f mg) — You need %.0f%% more calcium today",
                        food.getCalcium(), calciumDeficit);
                allSuggestions.add(new Suggestion(food, reason, score * 2));
            }
        }

        // RULE 3: If protein deficit > 30%, suggest protein-rich foods
        if (proteinDeficit > 30) {
            List<FoodItem> proteinFoods = foodItemDao.getTopByNutrient("protein", 10);
            for (FoodItem food : proteinFoods) {
                double score = food.getProtein() * (proteinDeficit / 100);
                String reason = String.format("Rich in Protein (%.1f g) — You need %.0f%% more protein today",
                        food.getProtein(), proteinDeficit);
                allSuggestions.add(new Suggestion(food, reason, score * 2));
            }
        }

        // RULE 4: If calorie deficit > 20%, suggest calorie-dense foods
        if (calorieDeficit > 20) {
            List<FoodItem> calorieFoods = foodItemDao.getTopByNutrient("calories", 10);
            for (FoodItem food : calorieFoods) {
                double score = food.getCalories() * (calorieDeficit / 100) / 100; // Normalize
                String reason = String.format("High in Calories (%.0f kcal) — You need %.0f%% more calories today",
                        food.getCalories(), calorieDeficit);
                allSuggestions.add(new Suggestion(food, reason, score));
            }
        }

        // Deduplicate by food name (keep highest scoring suggestion)
        Map<String, Suggestion> uniqueSuggestions = new LinkedHashMap<>();
        allSuggestions.sort((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()));

        for (Suggestion s : allSuggestions) {
            uniqueSuggestions.putIfAbsent(s.getFood().getName(), s);
        }

        // Return top 8 suggestions
        return uniqueSuggestions.values().stream()
                .limit(8)
                .collect(Collectors.toList());
    }

    /**
     * Get a quick summary of nutrient status.
     * @param userId user ID
     * @param target nutrition targets
     * @return map of nutrient name → status ("Good", "Low", "Critical")
     */
    public Map<String, String> getNutrientStatus(int userId, NutritionTarget target) {
        double[] consumed = dailyLogDao.getConsumedNutrients(userId, LocalDate.now());
        Map<String, String> status = new LinkedHashMap<>();

        status.put("Calories", getStatus(consumed[0], target.getCalories()));
        status.put("Protein", getStatus(consumed[1], target.getProtein()));
        status.put("Iron", getStatus(consumed[2], target.getIron()));
        status.put("Calcium", getStatus(consumed[3], target.getCalcium()));

        return status;
    }

    /** Categorize nutrient intake status */
    private String getStatus(double consumed, double target) {
        if (target <= 0) return "Good";
        double pct = (consumed / target) * 100;
        if (pct >= 80) return "Good";
        if (pct >= 50) return "Low";
        return "Critical";
    }
}
