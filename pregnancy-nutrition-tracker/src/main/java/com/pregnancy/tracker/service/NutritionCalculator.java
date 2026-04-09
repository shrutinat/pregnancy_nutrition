package com.pregnancy.tracker.service;

import com.pregnancy.tracker.model.NutritionTarget;
import com.pregnancy.tracker.model.User;

/**
 * NutritionCalculator service.
 * Calculates personalized daily nutrition requirements based on
 * user profile and current trimester.
 */
public class NutritionCalculator {

    /**
     * Calculate personalized nutrition targets for a user.
     * Uses Harris-Benedict equation for base calories and adds trimester adjustments.
     * @param user the user profile
     * @return personalized NutritionTarget
     */
    public static NutritionTarget calculateTargets(User user) {
        int trimester = user.getCurrentTrimester();

        // Get WHO defaults as baseline
        NutritionTarget target = NutritionTarget.getDefaults(trimester);
        target.setUserId(user.getId());

        // Personalize calorie target using Harris-Benedict equation
        double bmr = BMICalculator.calculateBMR(user.getWeight(), user.getHeight(), user.getAge());

        // Activity factor for pregnant women (sedentary to light activity)
        double activityFactor = 1.4;
        double baseCalories = bmr * activityFactor;

        // Add trimester-specific calorie adjustments
        switch (trimester) {
            case 1:
                target.setCalories(baseCalories);
                break;
            case 2:
                target.setCalories(baseCalories + 340);
                break;
            case 3:
                target.setCalories(baseCalories + 450);
                break;
        }

        // Adjust protein based on weight (1.1g per kg body weight)
        double proteinTarget = Math.max(user.getWeight() * 1.1, target.getProtein());
        target.setProtein(Math.round(proteinTarget * 10.0) / 10.0);

        // Adjust water intake based on weight
        double waterTarget = user.getWeight() * 0.035; // 35ml per kg
        if (trimester >= 2) waterTarget += 0.3; // Extra in later trimesters
        target.setWaterIntake(Math.round(waterTarget * 10.0) / 10.0);

        return target;
    }

    /**
     * Calculate deficit percentage for a specific nutrient.
     * @param consumed amount consumed today
     * @param target daily target
     * @return deficit percentage (0 = met, 100 = nothing consumed)
     */
    public static double calculateDeficit(double consumed, double target) {
        if (target <= 0) return 0;
        double deficit = ((target - consumed) / target) * 100;
        return Math.max(deficit, 0); // Can't have negative deficit
    }

    /**
     * Calculate completion percentage for a specific nutrient.
     * @param consumed amount consumed
     * @param target daily target
     * @return completion percentage (capped at 100%)
     */
    public static double calculateCompletion(double consumed, double target) {
        if (target <= 0) return 100;
        return Math.min((consumed / target) * 100, 100);
    }

    /**
     * Determine if a nutrient level is critically low.
     * A nutrient is critical if less than 30% of target consumed by noon,
     * or less than 60% by evening.
     * @param consumed amount consumed
     * @param target daily target
     * @return true if critically low
     */
    public static boolean isCriticallyLow(double consumed, double target) {
        if (target <= 0) return false;
        double percentage = (consumed / target) * 100;
        int hour = java.time.LocalTime.now().getHour();

        if (hour < 12) {
            return percentage < 15; // Less than 15% by noon
        } else if (hour < 18) {
            return percentage < 40; // Less than 40% by evening
        } else {
            return percentage < 60; // Less than 60% by night
        }
    }

    /**
     * Get a risk level assessment based on overall nutrient intake.
     * @param completionPercentage overall nutrient completion %
     * @return risk level: "Good", "Warning", "Critical"
     */
    public static String getRiskLevel(double completionPercentage) {
        if (completionPercentage >= 80) return "Good";
        if (completionPercentage >= 50) return "Warning";
        return "Critical";
    }
}
