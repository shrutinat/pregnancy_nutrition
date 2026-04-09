package com.pregnancy.tracker.service;

import com.pregnancy.tracker.dao.DailyLogDao;
import com.pregnancy.tracker.dao.DoctorUpdateDao;
import com.pregnancy.tracker.model.NutritionTarget;
import com.pregnancy.tracker.util.CSVLoader;

import java.time.LocalDate;
import java.util.*;

/**
 * ProgressTracker service.
 * Tracks daily nutrient completion, weekly weight gain,
 * trimester progress, and baby growth milestones.
 */
public class ProgressTracker {

    private final DailyLogDao dailyLogDao;
    private final DoctorUpdateDao doctorUpdateDao;
    private List<String[]> babyGrowthData;

    public ProgressTracker() {
        this.dailyLogDao = new DailyLogDao();
        this.doctorUpdateDao = new DoctorUpdateDao();
        loadBabyGrowthData();
    }

    /** Load baby growth milestones from CSV */
    private void loadBabyGrowthData() {
        babyGrowthData = CSVLoader.loadBabyGrowth("/data/baby_growth.csv");
    }

    /**
     * Get daily nutrient completion percentages.
     * @param userId user ID
     * @param target daily nutrition targets
     * @return map of nutrient name → completion percentage
     */
    public Map<String, Double> getDailyCompletion(int userId, NutritionTarget target) {
        double[] consumed = dailyLogDao.getConsumedNutrients(userId, LocalDate.now());
        Map<String, Double> completion = new LinkedHashMap<>();

        completion.put("Calories", NutritionCalculator.calculateCompletion(consumed[0], target.getCalories()));
        completion.put("Protein", NutritionCalculator.calculateCompletion(consumed[1], target.getProtein()));
        completion.put("Iron", NutritionCalculator.calculateCompletion(consumed[2], target.getIron()));
        completion.put("Calcium", NutritionCalculator.calculateCompletion(consumed[3], target.getCalcium()));

        return completion;
    }

    /**
     * Get overall daily completion percentage (average of all nutrients).
     * @param userId user ID
     * @param target nutrition targets
     * @return average completion percentage
     */
    public double getOverallCompletion(int userId, NutritionTarget target) {
        Map<String, Double> completion = getDailyCompletion(userId, target);
        return completion.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    /**
     * Get weekly nutrient averages for the past 7 days.
     * @param userId user ID
     * @param target nutrition targets
     * @return map of date → completion map
     */
    public Map<LocalDate, Map<String, Double>> getWeeklyProgress(int userId, NutritionTarget target) {
        Map<LocalDate, Map<String, Double>> weeklyData = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            double[] consumed = dailyLogDao.getConsumedNutrients(userId, date);

            Map<String, Double> dayCompletion = new LinkedHashMap<>();
            dayCompletion.put("Calories", NutritionCalculator.calculateCompletion(consumed[0], target.getCalories()));
            dayCompletion.put("Protein", NutritionCalculator.calculateCompletion(consumed[1], target.getProtein()));
            dayCompletion.put("Iron", NutritionCalculator.calculateCompletion(consumed[2], target.getIron()));
            dayCompletion.put("Calcium", NutritionCalculator.calculateCompletion(consumed[3], target.getCalcium()));

            weeklyData.put(date, dayCompletion);
        }

        return weeklyData;
    }

    /**
     * Get weight gain history for charts.
     * @param userId user ID
     * @return list of [date_string, weight] pairs
     */
    public List<String[]> getWeightHistory(int userId) {
        return doctorUpdateDao.getWeightHistoryWithDates(userId);
    }

    /**
     * Log a weight entry.
     * @param userId user ID
     * @param weight current weight
     * @return true if successful
     */
    public boolean logWeight(int userId, double weight) {
        return doctorUpdateDao.logWeight(userId, LocalDate.now(), weight);
    }

    /**
     * Get baby growth milestone for the current week.
     * @param week current pregnancy week
     * @return String array [week, length_cm, weight_g, development] or null
     */
    public String[] getBabyGrowthMilestone(int week) {
        if (babyGrowthData == null) return null;
        for (String[] row : babyGrowthData) {
            try {
                if (Integer.parseInt(row[0]) == week) {
                    return row;
                }
            } catch (NumberFormatException e) {
                // Skip invalid rows
            }
        }
        return null;
    }

    /**
     * Get all baby growth milestones (for charts).
     * @return list of growth data
     */
    public List<String[]> getAllBabyGrowthData() {
        return babyGrowthData != null ? babyGrowthData : new ArrayList<>();
    }

    /**
     * Get consumed nutrient values for today (raw amounts, not percentages).
     * @param userId user ID
     * @return double array [calories, protein, iron, calcium]
     */
    public double[] getTodayConsumed(int userId) {
        return dailyLogDao.getConsumedNutrients(userId, LocalDate.now());
    }
}
