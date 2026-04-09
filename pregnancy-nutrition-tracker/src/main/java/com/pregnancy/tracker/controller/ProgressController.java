package com.pregnancy.tracker.controller;

import com.pregnancy.tracker.dao.NutritionTargetDao;
import com.pregnancy.tracker.model.NutritionTarget;
import com.pregnancy.tracker.model.User;
import com.pregnancy.tracker.service.ProgressTracker;
import com.pregnancy.tracker.util.AlertHelper;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * ProgressController displays charts and progress tracking.
 * Includes bar charts for daily nutrients, line chart for weight gain,
 * pie chart for nutrient completion, and baby growth timeline.
 */
public class ProgressController {

    private final User user;
    private final ProgressTracker progressTracker;
    private final NutritionTargetDao nutritionTargetDao;

    public ProgressController(User user) {
        this.user = user;
        this.progressTracker = new ProgressTracker();
        this.nutritionTargetDao = new NutritionTargetDao();
    }

    /**
     * Build the progress tracking view.
     * @return ScrollPane containing charts and progress data
     */
    public ScrollPane buildView() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(0, 0, 30, 0));

        // Header
        Label title = new Label("📈 Progress & Analytics");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Track your nutrition journey and baby's growth");
        subtitle.getStyleClass().add("page-subtitle");
        content.getChildren().add(new VBox(4, title, subtitle));

        // Weight logging section
        content.getChildren().add(buildWeightLogSection());

        // Charts grid
        HBox chartsRow1 = new HBox(20);
        chartsRow1.getChildren().addAll(buildNutrientBarChart(), buildCompletionPieChart());

        HBox chartsRow2 = new HBox(20);
        chartsRow2.getChildren().addAll(buildWeightLineChart(), buildWeeklyProgressChart());

        content.getChildren().addAll(chartsRow1, chartsRow2);

        // Baby growth timeline
        content.getChildren().add(buildBabyGrowthTimeline());

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("content-area");
        return scrollPane;
    }

    /** Build weight logging section */
    private HBox buildWeightLogSection() {
        HBox section = new HBox(16);
        section.getStyleClass().add("card-elevated");
        section.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label("⚖");
        icon.setStyle("-fx-font-size: 32px;");

        VBox info = new VBox(4);
        Label lbl = new Label("Log Today's Weight");
        lbl.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        Label sub = new Label("Track your weight weekly for best results");
        sub.getStyleClass().add("info-text");
        info.getChildren().addAll(lbl, sub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField weightField = new TextField();
        weightField.setPromptText("Weight (kg)");
        weightField.setPrefWidth(120);

        Button logBtn = new Button("Log Weight");
        logBtn.getStyleClass().add("btn-primary");
        logBtn.setOnAction(e -> {
            try {
                double weight = Double.parseDouble(weightField.getText().trim());
                if (weight < 30 || weight > 200) {
                    AlertHelper.showWarning("Invalid", "Please enter a valid weight (30-200 kg).");
                    return;
                }
                if (progressTracker.logWeight(user.getId(), weight)) {
                    AlertHelper.showSuccess("Weight logged: " + weight + " kg");
                    weightField.clear();
                }
            } catch (NumberFormatException ex) {
                AlertHelper.showWarning("Invalid", "Please enter a valid number.");
            }
        });

        section.getChildren().addAll(icon, info, spacer, weightField, logBtn);
        return section;
    }

    /** Build today's nutrient bar chart */
    private VBox buildNutrientBarChart() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card-elevated");
        card.setPrefWidth(450);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label title = new Label("📊 Today's Nutrient Intake");
        title.getStyleClass().add("section-title");

        NutritionTarget target = nutritionTargetDao.getTargetsForUser(user.getId(), user.getCurrentTrimester());
        double[] consumed = progressTracker.getTodayConsumed(user.getId());

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Nutrient");
        yAxis.setLabel("Percentage (%)");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Nutrient Completion (%)");
        barChart.setPrefHeight(300);
        barChart.setLegendVisible(false);
        barChart.setAnimated(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Completion %");

        String[] nutrients = {"Calories", "Protein", "Iron", "Calcium"};
        double[] targets = {target.getCalories(), target.getProtein(), target.getIron(), target.getCalcium()};

        for (int i = 0; i < nutrients.length; i++) {
            double pct = targets[i] > 0 ? Math.min((consumed[i] / targets[i]) * 100, 100) : 0;
            series.getData().add(new XYChart.Data<>(nutrients[i], pct));
        }

        barChart.getData().add(series);

        card.getChildren().addAll(title, barChart);
        return card;
    }

    /** Build completion pie chart */
    private VBox buildCompletionPieChart() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card-elevated");
        card.setPrefWidth(350);

        Label title = new Label("🥧 Overall Balance");
        title.getStyleClass().add("section-title");

        NutritionTarget target = nutritionTargetDao.getTargetsForUser(user.getId(), user.getCurrentTrimester());
        Map<String, Double> completion = progressTracker.getDailyCompletion(user.getId(), target);

        PieChart pieChart = new PieChart();
        pieChart.setPrefHeight(300);
        pieChart.setAnimated(true);
        pieChart.setLabelsVisible(true);

        for (Map.Entry<String, Double> entry : completion.entrySet()) {
            pieChart.getData().add(new PieChart.Data(
                    entry.getKey() + " (" + String.format("%.0f%%", entry.getValue()) + ")",
                    entry.getValue()));
        }

        card.getChildren().addAll(title, pieChart);
        return card;
    }

    /** Build weight gain line chart */
    private VBox buildWeightLineChart() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card-elevated");
        card.setPrefWidth(450);
        HBox.setHgrow(card, Priority.ALWAYS);

        Label title = new Label("📉 Weight Tracking");
        title.getStyleClass().add("section-title");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("Date");
        yAxis.setLabel("Weight (kg)");

        LineChart<String, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Weight Over Time");
        lineChart.setPrefHeight(300);
        lineChart.setAnimated(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Weight (kg)");

        List<String[]> history = progressTracker.getWeightHistory(user.getId());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM dd");

        if (history.isEmpty()) {
            // Add current weight as starting point
            series.getData().add(new XYChart.Data<>("Start", user.getWeight()));
        } else {
            for (String[] entry : history) {
                LocalDate date = LocalDate.parse(entry[0]);
                series.getData().add(new XYChart.Data<>(date.format(fmt), Double.parseDouble(entry[1])));
            }
        }

        lineChart.getData().add(series);

        card.getChildren().addAll(title, lineChart);
        return card;
    }

    /** Build weekly progress chart */
    private VBox buildWeeklyProgressChart() {
        VBox card = new VBox(12);
        card.getStyleClass().add("card-elevated");
        card.setPrefWidth(350);

        Label title = new Label("📅 7-Day Progress");
        title.getStyleClass().add("section-title");

        NutritionTarget target = nutritionTargetDao.getTargetsForUser(user.getId(), user.getCurrentTrimester());
        Map<LocalDate, Map<String, Double>> weeklyData = progressTracker.getWeeklyProgress(user.getId(), target);

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis(0, 100, 25);
        xAxis.setLabel("Day");
        yAxis.setLabel("Completion (%)");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setPrefHeight(300);
        barChart.setAnimated(true);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Avg Completion");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("EEE");
        for (Map.Entry<LocalDate, Map<String, Double>> entry : weeklyData.entrySet()) {
            double avg = entry.getValue().values().stream()
                    .mapToDouble(Double::doubleValue).average().orElse(0);
            series.getData().add(new XYChart.Data<>(entry.getKey().format(fmt), avg));
        }

        barChart.getData().add(series);
        barChart.setLegendVisible(false);

        card.getChildren().addAll(title, barChart);
        return card;
    }

    /** Build baby growth timeline */
    private VBox buildBabyGrowthTimeline() {
        VBox card = new VBox(16);
        card.getStyleClass().add("card-elevated");

        Label title = new Label("👶 Baby Growth Timeline");
        title.getStyleClass().add("section-title");

        int currentWeek = user.getCurrentWeek();
        List<String[]> growthData = progressTracker.getAllBabyGrowthData();

        // Show milestones around current week
        HBox timeline = new HBox(12);
        timeline.setAlignment(Pos.CENTER_LEFT);

        int startWeek = Math.max(currentWeek - 2, 1);
        int endWeek = Math.min(currentWeek + 3, 40);

        for (String[] data : growthData) {
            int week = Integer.parseInt(data[0]);
            if (week >= startWeek && week <= endWeek) {
                VBox milestone = new VBox(4);
                milestone.setAlignment(Pos.CENTER);
                milestone.setPrefWidth(150);

                boolean isCurrent = week == currentWeek;
                String bgColor = isCurrent ? "#e91e8c" : "#f8f9fa";
                String textColor = isCurrent ? "white" : "#1a1a2e";

                milestone.setStyle("-fx-background-color: " + bgColor +
                        "; -fx-padding: 12; -fx-background-radius: 10;");

                Label weekLabel = new Label("Week " + data[0]);
                weekLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: " + textColor + ";");

                Label sizeLabel = new Label(data[1] + " cm • " + data[2] + " g");
                sizeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " +
                        (isCurrent ? "rgba(255,255,255,0.9)" : "#6c757d") + ";");

                Label devLabel = new Label(data[3]);
                devLabel.setWrapText(true);
                devLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " +
                        (isCurrent ? "rgba(255,255,255,0.8)" : "#7f8c8d") + ";");
                devLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

                milestone.getChildren().addAll(weekLabel, sizeLabel, devLabel);
                timeline.getChildren().add(milestone);
            }
        }

        ScrollPane timelineScroll = new ScrollPane(timeline);
        timelineScroll.setFitToHeight(true);
        timelineScroll.setStyle("-fx-background-color: transparent;");
        timelineScroll.setPrefHeight(160);

        card.getChildren().addAll(title, timelineScroll);
        return card;
    }
}
