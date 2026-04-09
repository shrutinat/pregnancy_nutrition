package com.pregnancy.tracker.service;

import com.pregnancy.tracker.dao.DailyLogDao;
import com.pregnancy.tracker.dao.ReminderDao;
import com.pregnancy.tracker.model.NutritionTarget;
import com.pregnancy.tracker.model.Reminder;
import javafx.application.Platform;
import javafx.scene.control.Alert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ReminderService handles scheduled alerts and reminders.
 * Uses ScheduledExecutorService for periodic checking of due reminders
 * and nutrient-level risk alerts.
 */
public class ReminderService {

    private final ReminderDao reminderDao;
    private final DailyLogDao dailyLogDao;
    private ScheduledExecutorService scheduler;
    private int currentUserId;

    public ReminderService() {
        this.reminderDao = new ReminderDao();
        this.dailyLogDao = new DailyLogDao();
    }

    /**
     * Start the reminder checking service.
     * Checks for due reminders every 60 seconds.
     * @param userId the logged-in user's ID
     */
    public void startService(int userId) {
        this.currentUserId = userId;

        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
        }

        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ReminderService");
            t.setDaemon(true);
            return t;
        });

        // Check reminders every 60 seconds
        scheduler.scheduleAtFixedRate(this::checkReminders, 10, 60, TimeUnit.SECONDS);

        System.out.println("[ReminderService] Started for user " + userId);
    }

    /**
     * Stop the reminder service.
     */
    public void stopService() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            System.out.println("[ReminderService] Stopped.");
        }
    }

    /**
     * Check for due reminders and show alerts.
     */
    private void checkReminders() {
        try {
            List<Reminder> activeReminders = reminderDao.findActiveByUserId(currentUserId);

            for (Reminder reminder : activeReminders) {
                if (reminder.isDue()) {
                    showReminderAlert(reminder);

                    // If not recurring, deactivate after triggering
                    if (!reminder.isRecurring()) {
                        reminder.setActive(false);
                        reminderDao.update(reminder);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[ReminderService] Error: " + e.getMessage());
        }
    }

    /**
     * Check nutrient levels and generate risk alerts.
     * Call this periodically to warn about critically low intake.
     * @param target daily nutrition targets
     */
    public void checkNutrientAlerts(NutritionTarget target) {
        try {
            double[] consumed = dailyLogDao.getConsumedNutrients(currentUserId, LocalDate.now());

            // Check iron levels
            if (NutritionCalculator.isCriticallyLow(consumed[2], target.getIron())) {
                showNutrientAlert("Iron", consumed[2], target.getIron());
            }

            // Check calorie intake
            if (NutritionCalculator.isCriticallyLow(consumed[0], target.getCalories())) {
                showNutrientAlert("Calories", consumed[0], target.getCalories());
            }

            // Check calcium
            if (NutritionCalculator.isCriticallyLow(consumed[3], target.getCalcium())) {
                showNutrientAlert("Calcium", consumed[3], target.getCalcium());
            }
        } catch (Exception e) {
            System.err.println("[ReminderService] Nutrient alert error: " + e.getMessage());
        }
    }

    /**
     * Show a reminder alert on the JavaFX application thread.
     */
    private void showReminderAlert(Reminder reminder) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("⏰ " + reminder.getType().getDisplayName());
            alert.setHeaderText(reminder.getTitle());
            alert.setContentText(reminder.getDescription());
            alert.show();
        });
    }

    /**
     * Show a nutrient risk alert on the JavaFX application thread.
     */
    private void showNutrientAlert(String nutrient, double consumed, double target) {
        Platform.runLater(() -> {
            double pct = (consumed / target) * 100;
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("⚠ Low " + nutrient + " Alert");
            alert.setHeaderText("Your " + nutrient + " intake is critically low!");
            alert.setContentText(String.format(
                    "You've consumed only %.0f%% of your daily %s target.\n" +
                    "Consumed: %.1f / Target: %.1f\n\n" +
                    "Please consider eating foods rich in %s.",
                    pct, nutrient, consumed, target, nutrient.toLowerCase()
            ));
            alert.show();
        });
    }

    /**
     * Create default meal reminders for a user.
     */
    public void createDefaultReminders(int userId) {
        LocalDateTime today = LocalDate.now().atStartOfDay();

        // Breakfast reminder
        Reminder breakfast = new Reminder(userId, Reminder.ReminderType.MEAL,
                "Breakfast Time", "Don't forget to eat a healthy breakfast!",
                today.withHour(8).withMinute(0));
        breakfast.setRecurring(true);
        breakfast.setRecurrencePattern("DAILY");
        reminderDao.insert(breakfast);

        // Lunch reminder
        Reminder lunch = new Reminder(userId, Reminder.ReminderType.MEAL,
                "Lunch Time", "Time for a nutritious lunch!",
                today.withHour(13).withMinute(0));
        lunch.setRecurring(true);
        lunch.setRecurrencePattern("DAILY");
        reminderDao.insert(lunch);

        // Dinner reminder
        Reminder dinner = new Reminder(userId, Reminder.ReminderType.MEAL,
                "Dinner Time", "Have a balanced dinner with all food groups!",
                today.withHour(19).withMinute(30));
        dinner.setRecurring(true);
        dinner.setRecurrencePattern("DAILY");
        reminderDao.insert(dinner);

        // Prenatal vitamin reminder
        Reminder vitamin = new Reminder(userId, Reminder.ReminderType.MEDICINE,
                "Prenatal Vitamins", "Take your prenatal vitamin supplement!",
                today.withHour(9).withMinute(0));
        vitamin.setRecurring(true);
        vitamin.setRecurrencePattern("DAILY");
        reminderDao.insert(vitamin);

        System.out.println("[ReminderService] Created default reminders for user " + userId);
    }
}
