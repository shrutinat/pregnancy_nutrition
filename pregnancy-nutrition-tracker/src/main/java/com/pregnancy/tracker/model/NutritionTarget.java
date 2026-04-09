package com.pregnancy.tracker.model;

/**
 * NutritionTarget entity storing daily nutritional requirements
 * per trimester for a pregnant woman.
 * Values are based on WHO recommendations.
 */
public class NutritionTarget {

    private int id;
    private int userId;
    private int trimester;
    private double calories;
    private double protein;      // in grams
    private double iron;         // in mg
    private double calcium;      // in mg
    private double vitaminA;     // in mcg
    private double vitaminC;     // in mg
    private double vitaminD;     // in mcg
    private double folicAcid;    // in mcg
    private double waterIntake;  // in liters
    private boolean isDoctorModified;

    /** Default constructor */
    public NutritionTarget() {}

    /**
     * Create default nutrition targets based on trimester.
     * Uses WHO-recommended values for pregnant women.
     * @param trimester current trimester (1, 2, or 3)
     * @return NutritionTarget with default values
     */
    public static NutritionTarget getDefaults(int trimester) {
        NutritionTarget target = new NutritionTarget();
        target.setTrimester(trimester);

        // Base calorie needs for average pregnant woman (~2000 kcal)
        double baseCalories = 2000;
        switch (trimester) {
            case 1:
                target.setCalories(baseCalories);         // No extra in 1st trimester
                target.setProtein(46);                     // grams
                target.setIron(27);                        // mg (WHO recommendation)
                target.setCalcium(1000);                   // mg
                target.setVitaminA(770);                   // mcg
                target.setVitaminC(85);                    // mg
                target.setVitaminD(15);                    // mcg
                target.setFolicAcid(600);                  // mcg
                target.setWaterIntake(2.3);                // liters
                break;
            case 2:
                target.setCalories(baseCalories + 340);    // +340 kcal in 2nd trimester
                target.setProtein(71);
                target.setIron(27);
                target.setCalcium(1000);
                target.setVitaminA(770);
                target.setVitaminC(85);
                target.setVitaminD(15);
                target.setFolicAcid(600);
                target.setWaterIntake(2.6);
                break;
            case 3:
                target.setCalories(baseCalories + 450);    // +450 kcal in 3rd trimester
                target.setProtein(71);
                target.setIron(27);
                target.setCalcium(1300);
                target.setVitaminA(770);
                target.setVitaminC(85);
                target.setVitaminD(15);
                target.setFolicAcid(600);
                target.setWaterIntake(3.0);
                break;
            default:
                target.setCalories(baseCalories);
                target.setProtein(46);
                target.setIron(18);
                target.setCalcium(1000);
                target.setVitaminA(700);
                target.setVitaminC(75);
                target.setVitaminD(15);
                target.setFolicAcid(400);
                target.setWaterIntake(2.0);
        }
        return target;
    }

    // ==================== Getters & Setters ====================

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTrimester() { return trimester; }
    public void setTrimester(int trimester) { this.trimester = trimester; }

    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }

    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }

    public double getIron() { return iron; }
    public void setIron(double iron) { this.iron = iron; }

    public double getCalcium() { return calcium; }
    public void setCalcium(double calcium) { this.calcium = calcium; }

    public double getVitaminA() { return vitaminA; }
    public void setVitaminA(double vitaminA) { this.vitaminA = vitaminA; }

    public double getVitaminC() { return vitaminC; }
    public void setVitaminC(double vitaminC) { this.vitaminC = vitaminC; }

    public double getVitaminD() { return vitaminD; }
    public void setVitaminD(double vitaminD) { this.vitaminD = vitaminD; }

    public double getFolicAcid() { return folicAcid; }
    public void setFolicAcid(double folicAcid) { this.folicAcid = folicAcid; }

    public double getWaterIntake() { return waterIntake; }
    public void setWaterIntake(double waterIntake) { this.waterIntake = waterIntake; }

    public boolean isDoctorModified() { return isDoctorModified; }
    public void setDoctorModified(boolean doctorModified) { isDoctorModified = doctorModified; }

    @Override
    public String toString() {
        return "NutritionTarget{trimester=" + trimester + ", calories=" + calories +
               ", protein=" + protein + ", iron=" + iron + ", calcium=" + calcium + "}";
    }
}
