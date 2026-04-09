package com.pregnancy.tracker.util;

import com.pregnancy.tracker.model.FoodItem;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for parsing CSV files.
 * Loads food database and baby growth data from resource files.
 */
public class CSVLoader {

    /**
     * Load food items from the CSV resource file.
     * Expected format: Name,Category,Calories,Protein,Iron,Calcium,ServingSize,ServingUnit
     * @param resourcePath path to CSV resource (e.g., "/data/food_database.csv")
     * @return list of FoodItem objects
     */
    public static List<FoodItem> loadFoodItems(String resourcePath) {
        List<FoodItem> foods = new ArrayList<>();

        try (InputStream is = CSVLoader.class.getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                // Skip header row
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                // Skip empty lines
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    FoodItem food = parseFoodLine(line);
                    if (food != null) {
                        foods.add(food);
                    }
                } catch (Exception e) {
                    System.err.println("[CSVLoader] Error parsing line: " + line + " - " + e.getMessage());
                }
            }

            System.out.println("[CSVLoader] Loaded " + foods.size() + " food items from " + resourcePath);

        } catch (Exception e) {
            System.err.println("[CSVLoader] Error loading CSV: " + e.getMessage());
            e.printStackTrace();
        }

        return foods;
    }

    /**
     * Parse a single CSV line into a FoodItem.
     * Handles comma-separated values with basic validation.
     */
    private static FoodItem parseFoodLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 8) {
            System.err.println("[CSVLoader] Insufficient columns: " + line);
            return null;
        }

        FoodItem food = new FoodItem();
        food.setName(parts[0].trim());
        food.setCategory(parts[1].trim());
        food.setCalories(parseDouble(parts[2]));
        food.setProtein(parseDouble(parts[3]));
        food.setIron(parseDouble(parts[4]));
        food.setCalcium(parseDouble(parts[5]));
        food.setServingSize(parseDouble(parts[6]));
        food.setServingUnit(parts[7].trim());

        return food;
    }

    /**
     * Load baby growth milestones from CSV.
     * Expected format: Week,Length_cm,Weight_g,Development
     * @param resourcePath path to CSV resource
     * @return list of String arrays [week, length, weight, development]
     */
    public static List<String[]> loadBabyGrowth(String resourcePath) {
        List<String[]> milestones = new ArrayList<>();

        try (InputStream is = CSVLoader.class.getResourceAsStream(resourcePath);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",", 4);
                if (parts.length >= 4) {
                    milestones.add(new String[]{
                            parts[0].trim(), parts[1].trim(),
                            parts[2].trim(), parts[3].trim()
                    });
                }
            }

            System.out.println("[CSVLoader] Loaded " + milestones.size() + " baby growth milestones.");

        } catch (Exception e) {
            System.err.println("[CSVLoader] Error loading baby growth CSV: " + e.getMessage());
        }

        return milestones;
    }

    /**
     * Safely parse a double value from string.
     */
    private static double parseDouble(String value) {
        try {
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
