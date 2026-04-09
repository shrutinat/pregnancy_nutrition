package com.pregnancy.tracker.controller;

import com.pregnancy.tracker.dao.DailyLogDao;
import com.pregnancy.tracker.dao.DoctorUpdateDao;
import com.pregnancy.tracker.dao.NutritionTargetDao;
import com.pregnancy.tracker.model.DoctorUpdate;
import com.pregnancy.tracker.model.NutritionTarget;
import com.pregnancy.tracker.model.User;
import com.pregnancy.tracker.service.*;
import com.pregnancy.tracker.util.DateUtils;
import javafx.geometry.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DashboardController builds the main dashboard view.
 * Shows summary cards, nutrient progress, baby growth, and quick stats.
 */
public class DashboardController {

    private final User user;
    private final NutritionTargetDao nutritionTargetDao;
    private final ProgressTracker progressTracker;
    private final FoodSuggestionEngine suggestionEngine;
    private final DoctorUpdateDao doctorUpdateDao;

    public DashboardController(User user) {
        this.user = user;
        this.nutritionTargetDao = new NutritionTargetDao();
        this.progressTracker = new ProgressTracker();
        this.suggestionEngine = new FoodSuggestionEngine();
        this.doctorUpdateDao = new DoctorUpdateDao();
    }

    /**
     * Build the dashboard view.
     * @return ScrollPane containing the dashboard
     */
    public ScrollPane buildView() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(0, 0, 30, 0));

        // Header
        content.getChildren().add(buildHeader());

        // Stat cards row
        content.getChildren().add(buildStatCards());

        // Main content grid
        HBox mainGrid = new HBox(20);
        VBox leftCol = new VBox(20);
        leftCol.setPrefWidth(500);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        VBox rightCol = new VBox(20);
        rightCol.setPrefWidth(300);

        // Nutrient progress card
        leftCol.getChildren().add(buildNutrientProgressCard());

        // Baby growth card
        leftCol.getChildren().add(buildBabyGrowthCard());

        // AI Suggestions
        rightCol.getChildren().add(buildSuggestionsCard());

        // Risk alerts
        rightCol.getChildren().add(buildRiskAlertCard());

        mainGrid.getChildren().addAll(leftCol, rightCol);
        content.getChildren().add(mainGrid);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("content-area");
        return scrollPane;
    }

    /** Build dashboard header */
    private VBox buildHeader() {
        VBox header = new VBox(4);
        Label greeting = new Label("Welcome back, " + user.getName() + "! 👋");
        greeting.getStyleClass().add("page-title");
        Label sub = new Label("Week " + user.getCurrentWeek() + " • " +
                DateUtils.getTrimesterDescription(user.getCurrentTrimester()) +
                " • Due: " + DateUtils.formatDate(user.getDueDate()));
        sub.getStyleClass().add("page-subtitle");
        header.getChildren().addAll(greeting, sub);
        return header;
    }

    /** Build the stat cards row */
    private HBox buildStatCards() {
        HBox row = new HBox(16);

        int week = user.getCurrentWeek();
        NutritionTarget target = nutritionTargetDao.getTargetsForUser(user.getId(), user.getCurrentTrimester());
        double completion = progressTracker.getOverallCompletion(user.getId(), target);

        row.getChildren().addAll(
                createStatCard("🗓", "Week " + week, "of 40 weeks", "stat-card-pink"),
                createStatCard("📊", String.format("%.0f%%", completion), "Today's Nutrition", "stat-card-purple"),
                createStatCard("⚖", String.format("%.1f", user.getBmi()), "BMI (" + user.getBMICategory() + ")", "stat-card-teal"),
                createStatCard("📅", String.valueOf(user.getDaysRemaining()), "Days Remaining", "stat-card-orange")
        );

        return row;
    }

    /** Create a single stat card */
    private VBox createStatCard(String icon, String value, String label, String styleClass) {
        VBox card = new VBox(6);
        card.getStyleClass().addAll("stat-card", styleClass);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPrefWidth(200);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("stat-icon");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");

        Label nameLabel = new Label(label);
        nameLabel.getStyleClass().add("stat-label");

        card.getChildren().addAll(iconLabel, valueLabel, nameLabel);
        return card;
    }

    /** Build nutrient progress card with bar chart */
    private VBox buildNutrientProgressCard() {
        VBox card = new VBox(16);
        card.getStyleClass().add("card-elevated");

        Label title = new Label("📊 Today's Nutrient Progress");
        title.getStyleClass().add("section-title");

        NutritionTarget target = nutritionTargetDao.getTargetsForUser(user.getId(), user.getCurrentTrimester());
        Map<String, Double> completion = progressTracker.getDailyCompletion(user.getId(), target);

        VBox bars = new VBox(12);
        String[] nutrients = {"Calories", "Protein", "Iron", "Calcium"};
        String[] icons = {"🔥", "💪", "🩸", "🦴"};
        double[] targets = {target.getCalories(), target.getProtein(), target.getIron(), target.getCalcium()};
        double[] consumed = progressTracker.getTodayConsumed(user.getId());

        for (int i = 0; i < nutrients.length; i++) {
            double pct = completion.getOrDefault(nutrients[i], 0.0);
            bars.getChildren().add(buildNutrientBar(
                    icons[i] + " " + nutrients[i],
                    pct,
                    String.format("%.1f / %.1f", consumed[i], targets[i])
            ));
        }

        card.getChildren().addAll(title, bars);
        return card;
    }

    /** Build a single nutrient progress bar */
    private VBox buildNutrientBar(String name, double percentage, String detail) {
        VBox bar = new VBox(4);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label pctLabel = new Label(String.format("%.0f%%", percentage));
        pctLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: " +
                (percentage >= 80 ? "#4caf50" : percentage >= 50 ? "#ff9800" : "#f44336") + ";");

        Label detailLabel = new Label(detail);
        detailLabel.getStyleClass().add("info-text");

        header.getChildren().addAll(nameLabel, spacer, pctLabel);

        ProgressBar progressBar = new ProgressBar(percentage / 100.0);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(10);

        // Color the bar based on completion
        String barColor;
        if (percentage >= 80) barColor = "#4caf50";
        else if (percentage >= 50) barColor = "#ff9800";
        else barColor = "#f44336";
        progressBar.setStyle("-fx-accent: " + barColor + ";");

        bar.getChildren().addAll(header, progressBar, detailLabel);
        return bar;
    }

    /** Build baby growth milestone card */
    private VBox buildBabyGrowthCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card-elevated");

        Label title = new Label("👶 Baby Growth - Week " + user.getCurrentWeek());
        title.getStyleClass().add("section-title");

        String[] milestone = progressTracker.getBabyGrowthMilestone(user.getCurrentWeek());
        if (milestone != null) {
            HBox stats = new HBox(20);
            stats.setAlignment(Pos.CENTER_LEFT);

            stats.getChildren().addAll(
                    createMiniStat("📏 Length", milestone[1] + " cm"),
                    createMiniStat("⚖ Weight", milestone[2] + " g"),
                    createMiniStat("🍼 Week", milestone[0])
            );

            Label developmentLabel = new Label("🌱 " + milestone[3]);
            developmentLabel.setWrapText(true);
            developmentLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #495057; -fx-padding: 10 0 0 0;");

            // Pregnancy progress bar
            double progress = DateUtils.getProgressPercentage(user.getCurrentWeek());
            ProgressBar pregnancyProgress = new ProgressBar(progress / 100.0);
            pregnancyProgress.setMaxWidth(Double.MAX_VALUE);
            pregnancyProgress.setPrefHeight(14);

            HBox progressHeader = new HBox();
            Label progressLabel = new Label("Pregnancy Progress");
            progressLabel.setStyle("-fx-font-weight: bold;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label progressPct = new Label(String.format("%.0f%%", progress));
            progressPct.setStyle("-fx-font-weight: bold; -fx-text-fill: #e91e8c;");
            progressHeader.getChildren().addAll(progressLabel, spacer, progressPct);

            card.getChildren().addAll(title, stats, developmentLabel, new Separator(), progressHeader, pregnancyProgress);
        } else {
            Label noData = new Label("No growth data available for this week.");
            noData.getStyleClass().add("info-text");
            card.getChildren().addAll(title, noData);
        }

        return card;
    }

    /** Create mini stat display */
    private VBox createMiniStat(String label, String value) {
        VBox stat = new VBox(2);
        stat.setAlignment(Pos.CENTER);
        stat.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 12; -fx-background-radius: 8;");
        stat.setPrefWidth(120);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");

        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d;");

        stat.getChildren().addAll(valueLabel, nameLabel);
        return stat;
    }

    /** Build AI food suggestions card */
    private VBox buildSuggestionsCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card-elevated");

        Label title = new Label("🧠 Smart Suggestions");
        title.getStyleClass().add("section-title");

        NutritionTarget target = nutritionTargetDao.getTargetsForUser(user.getId(), user.getCurrentTrimester());
        List<FoodSuggestionEngine.Suggestion> suggestions = suggestionEngine.generateSuggestions(user.getId(), target);

        if (suggestions.isEmpty()) {
            Label goodJob = new Label("🎉 Great job! Your nutrition looks balanced today.");
            goodJob.setWrapText(true);
            goodJob.setStyle("-fx-font-size: 14px; -fx-text-fill: #4caf50;");
            card.getChildren().addAll(title, goodJob);
        } else {
            VBox list = new VBox(8);
            for (int i = 0; i < Math.min(suggestions.size(), 5); i++) {
                FoodSuggestionEngine.Suggestion s = suggestions.get(i);
                VBox item = new VBox(2);
                item.setStyle("-fx-background-color: #fce4ec; -fx-padding: 10; -fx-background-radius: 8;");

                Label foodName = new Label("🍽 " + s.getFood().getName());
                foodName.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

                Label reason = new Label(s.getReason());
                reason.setWrapText(true);
                reason.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

                item.getChildren().addAll(foodName, reason);
                list.getChildren().add(item);
            }
            card.getChildren().addAll(title, list);
        }

        return card;
    }

    /** Build risk alert card */
    private VBox buildRiskAlertCard() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card-elevated");

        Label title = new Label("⚠ Health Alerts");
        title.getStyleClass().add("section-title");

        VBox alerts = new VBox(8);

        // Check nutrient status
        NutritionTarget target = nutritionTargetDao.getTargetsForUser(user.getId(), user.getCurrentTrimester());
        Map<String, String> status = suggestionEngine.getNutrientStatus(user.getId(), target);

        boolean hasAlerts = false;
        for (Map.Entry<String, String> entry : status.entrySet()) {
            if (!"Good".equals(entry.getValue())) {
                hasAlerts = true;
                Label alert = new Label("⚠ " + entry.getKey() + " level is " + entry.getValue());
                alert.setWrapText(true);
                String bgClass = "Critical".equals(entry.getValue()) ? "#ffebee" : "#fff3e0";
                String textColor = "Critical".equals(entry.getValue()) ? "#c62828" : "#e65100";
                alert.setStyle("-fx-background-color: " + bgClass +
                        "; -fx-padding: 8 12; -fx-background-radius: 6; -fx-text-fill: " + textColor + ";");
                alert.setMaxWidth(Double.MAX_VALUE);
                alerts.getChildren().add(alert);
            }
        }

        // Check doctor risk conditions
        String risks = doctorUpdateDao.getActiveRiskConditions(user.getId());
        if (risks != null) {
            hasAlerts = true;
            Label riskLabel = new Label("🏥 Doctor flagged: " + risks);
            riskLabel.setWrapText(true);
            riskLabel.setStyle("-fx-background-color: #ffebee; -fx-padding: 8 12; " +
                    "-fx-background-radius: 6; -fx-text-fill: #c62828;");
            riskLabel.setMaxWidth(Double.MAX_VALUE);
            alerts.getChildren().add(riskLabel);
        }

        if (!hasAlerts) {
            Label allGood = new Label("✅ Everything looks good! Keep it up.");
            allGood.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 14px;");
            alerts.getChildren().add(allGood);
        }

        card.getChildren().addAll(title, alerts);
        return card;
    }
}
