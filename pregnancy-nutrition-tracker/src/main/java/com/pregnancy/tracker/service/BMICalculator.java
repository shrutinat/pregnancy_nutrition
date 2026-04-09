package com.pregnancy.tracker.service;

/**
 * BMI Calculator service.
 * Provides BMI calculation and classification for pregnant women.
 */
public class BMICalculator {

    /**
     * Calculate BMI from weight and height.
     * @param weightKg weight in kilograms
     * @param heightCm height in centimeters
     * @return BMI value rounded to 1 decimal place
     */
    public static double calculate(double weightKg, double heightCm) {
        if (weightKg <= 0 || heightCm <= 0) {
            throw new IllegalArgumentException("Weight and height must be positive values.");
        }
        double heightM = heightCm / 100.0;
        return Math.round((weightKg / (heightM * heightM)) * 10.0) / 10.0;
    }

    /**
     * Classify BMI into WHO categories.
     * @param bmi calculated BMI value
     * @return classification string
     */
    public static String classify(double bmi) {
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal weight";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    /**
     * Get recommended weight gain during pregnancy based on pre-pregnancy BMI.
     * Based on IOM (Institute of Medicine) guidelines.
     * @param bmi pre-pregnancy BMI
     * @return recommended weight gain range as "min-max kg"
     */
    public static String getRecommendedWeightGain(double bmi) {
        if (bmi < 18.5) return "12.5 - 18.0 kg";
        if (bmi < 25.0) return "11.5 - 16.0 kg";
        if (bmi < 30.0) return "7.0 - 11.5 kg";
        return "5.0 - 9.0 kg";
    }

    /**
     * Get recommended weekly weight gain for 2nd and 3rd trimesters.
     * @param bmi pre-pregnancy BMI
     * @return recommended weekly gain in kg
     */
    public static double getRecommendedWeeklyGain(double bmi) {
        if (bmi < 18.5) return 0.51;
        if (bmi < 25.0) return 0.42;
        if (bmi < 30.0) return 0.28;
        return 0.22;
    }

    /**
     * Calculate base calorie needs using Harris-Benedict equation.
     * For women: BMR = 655 + (9.6 × weight in kg) + (1.8 × height in cm) − (4.7 × age)
     * @param weightKg weight in kilograms
     * @param heightCm height in centimeters
     * @param age age in years
     * @return basal metabolic rate in kcal
     */
    public static double calculateBMR(double weightKg, double heightCm, int age) {
        return 655 + (9.6 * weightKg) + (1.8 * heightCm) - (4.7 * age);
    }
}
