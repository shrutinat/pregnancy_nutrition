package com.pregnancy.tracker.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * User entity representing a pregnant woman's profile.
 * Stores personal health data and pregnancy information.
 */
public class User {

    private int id;
    private String name;
    private String email;
    private String password;
    private int age;
    private double height;   // in cm
    private double weight;   // in kg
    private LocalDate pregnancyStartDate;
    private double bmi;
    private String createdAt;

    /** Default constructor */
    public User() {}

    /** Full constructor */
    public User(int id, String name, String email, String password, int age,
                double height, double weight, LocalDate pregnancyStartDate) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.pregnancyStartDate = pregnancyStartDate;
        this.bmi = calculateBMI();
    }

    /**
     * Calculate BMI using weight (kg) and height (cm).
     * Formula: BMI = weight / (height_in_meters ^ 2)
     * @return calculated BMI value
     */
    public double calculateBMI() {
        if (height <= 0) return 0;
        double heightInMeters = height / 100.0;
        this.bmi = weight / (heightInMeters * heightInMeters);
        return this.bmi;
    }

    /**
     * Get the current pregnancy week based on the start date.
     * @return current week number (1-40)
     */
    public int getCurrentWeek() {
        if (pregnancyStartDate == null) return 0;
        long days = ChronoUnit.DAYS.between(pregnancyStartDate, LocalDate.now());
        int weeks = (int) (days / 7) + 1;
        return Math.min(Math.max(weeks, 1), 40);
    }

    /**
     * Determine the current trimester based on pregnancy week.
     * 1-12 weeks → 1st trimester
     * 13-26 weeks → 2nd trimester
     * 27-40 weeks → 3rd trimester
     * @return trimester number (1, 2, or 3)
     */
    public int getCurrentTrimester() {
        int week = getCurrentWeek();
        if (week <= 12) return 1;
        if (week <= 26) return 2;
        return 3;
    }

    /**
     * Get estimated due date (40 weeks from pregnancy start).
     * @return estimated due date
     */
    public LocalDate getDueDate() {
        if (pregnancyStartDate == null) return null;
        return pregnancyStartDate.plusWeeks(40);
    }

    /**
     * Get days remaining until due date.
     * @return number of days remaining
     */
    public long getDaysRemaining() {
        LocalDate dueDate = getDueDate();
        if (dueDate == null) return 0;
        long days = ChronoUnit.DAYS.between(LocalDate.now(), dueDate);
        return Math.max(days, 0);
    }

    /**
     * Classify BMI into categories.
     * @return BMI category string
     */
    public String getBMICategory() {
        if (bmi <= 0) return "Unknown";
        if (bmi < 18.5) return "Underweight";
        if (bmi < 25.0) return "Normal";
        if (bmi < 30.0) return "Overweight";
        return "Obese";
    }

    // ==================== Getters & Setters ====================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public double getHeight() { return height; }
    public void setHeight(double height) { this.height = height; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public LocalDate getPregnancyStartDate() { return pregnancyStartDate; }
    public void setPregnancyStartDate(LocalDate pregnancyStartDate) {
        this.pregnancyStartDate = pregnancyStartDate;
    }

    public double getBmi() { return bmi; }
    public void setBmi(double bmi) { this.bmi = bmi; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "User{name='" + name + "', age=" + age + ", week=" + getCurrentWeek() +
               ", trimester=" + getCurrentTrimester() + ", bmi=" + String.format("%.1f", bmi) + "}";
    }
}
