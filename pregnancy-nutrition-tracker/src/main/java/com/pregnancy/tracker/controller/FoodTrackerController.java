package com.pregnancy.tracker.controller;

import com.pregnancy.tracker.dao.DailyLogDao;
import com.pregnancy.tracker.dao.FoodItemDao;
import com.pregnancy.tracker.dao.NutritionTargetDao;
import com.pregnancy.tracker.model.*;
import com.pregnancy.tracker.service.FoodSuggestionEngine;
import com.pregnancy.tracker.service.NutritionCalculator;
import com.pregnancy.tracker.service.ProgressTracker;
import com.pregnancy.tracker.util.AlertHelper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.List;

/**
 * FoodTrackerController handles daily food logging.
 * Users can search foods, add them to daily log, set quantities,
 * mark as consumed, and see AI-based suggestions.
 */
public class FoodTrackerController {

    private final User user;
    private final FoodItemDao foodItemDao;
    private final DailyLogDao dailyLogDao;
    private final NutritionTargetDao nutritionTargetDao;
    private final FoodSuggestionEngine suggestionEngine;
    private final ProgressTracker progressTracker;

    private TableView<DailyLog> logTable;
    private ObservableList<DailyLog> logData;
    private VBox suggestionsBox;

    public FoodTrackerController(User user) {
        this.user = user;
        this.foodItemDao = new FoodItemDao();
        this.dailyLogDao = new DailyLogDao();
        this.nutritionTargetDao = new NutritionTargetDao();
        this.suggestionEngine = new FoodSuggestionEngine();
        this.progressTracker = new ProgressTracker();
    }

    /**
     * Build the food tracker view.
     * @return ScrollPane containing the food tracker
     */
    public ScrollPane buildView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(0, 0, 30, 0));

        // Header
        Label title = new Label("🍽 Daily Food Tracker");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Track everything you eat today — " + LocalDate.now().toString());
        subtitle.getStyleClass().add("page-subtitle");

        // Top section: Search + Add
        HBox topSection = new HBox(20);

        VBox addSection = new VBox(16);
        addSection.getStyleClass().add("card-elevated");
        addSection.setPrefWidth(500);
        HBox.setHgrow(addSection, Priority.ALWAYS);
        addSection.getChildren().add(buildAddFoodSection());

        VBox summarySection = new VBox(16);
        summarySection.getStyleClass().add("card-elevated");
        summarySection.setPrefWidth(300);
        summarySection.getChildren().add(buildNutrientSummary());

        topSection.getChildren().addAll(addSection, summarySection);

        // Food log table
        VBox tableCard = new VBox(12);
        tableCard.getStyleClass().add("card-elevated");
        Label tableTitle = new Label("📋 Today's Food Log");
        tableTitle.getStyleClass().add("section-title");
        tableCard.getChildren().addAll(tableTitle, buildFoodLogTable());

        // AI Suggestions section
        VBox suggestionsCard = new VBox(12);
        suggestionsCard.getStyleClass().add("card-elevated");
        Label sugTitle = new Label("🧠 AI Food Suggestions");
        sugTitle.getStyleClass().add("section-title");
        suggestionsBox = new VBox(8);
        refreshSuggestions();
        suggestionsCard.getChildren().addAll(sugTitle, suggestionsBox);

        content.getChildren().addAll(
                new VBox(4, title, subtitle),
                topSection,
                tableCard,
                suggestionsCard
        );

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("content-area");
        return scrollPane;
    }

    /** Build the add food section with search */
    private VBox buildAddFoodSection() {
        VBox section = new VBox(12);

        Label title = new Label("➕ Add Food to Today's Log");
        title.getStyleClass().add("section-title");

        // Search
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search food... (e.g., spinach, egg, milk)");
        searchField.setId("food-search");

        // Search results
        ListView<FoodItem> searchResults = new ListView<>();
        searchResults.setPrefHeight(150);
        searchResults.setPlaceholder(new Label("Type to search foods..."));
        searchResults.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(FoodItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%s  |  %s  |  %.0f kcal  |  %.1fg protein  |  %.1fmg iron  |  %.0fmg calcium",
                            item.getName(), item.getCategory(),
                            item.getCalories(), item.getProtein(),
                            item.getIron(), item.getCalcium()));
                }
            }
        });

        // Live search
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() >= 2) {
                List<FoodItem> results = foodItemDao.searchByName(newVal);
                searchResults.setItems(FXCollections.observableArrayList(results));
            } else {
                searchResults.getItems().clear();
            }
        });

        // Quantity input and add button
        HBox addRow = new HBox(10);
        addRow.setAlignment(Pos.CENTER_LEFT);

        TextField quantityField = new TextField("100");
        quantityField.setPrefWidth(80);
        quantityField.setPromptText("Qty (g)");

        Label unitLabel = new Label("grams");
        unitLabel.getStyleClass().add("info-text");

        Button addBtn = new Button("Add to Log");
        addBtn.getStyleClass().add("btn-primary");
        addBtn.setId("add-food-btn");

        addBtn.setOnAction(e -> {
            FoodItem selected = searchResults.getSelectionModel().getSelectedItem();
            if (selected == null) {
                AlertHelper.showWarning("Selection Required", "Please select a food from the search results.");
                return;
            }
            try {
                double qty = Double.parseDouble(quantityField.getText().trim());
                if (qty <= 0) throw new NumberFormatException();

                DailyLog log = new DailyLog(user.getId(), LocalDate.now(), selected.getId(), qty);
                int id = dailyLogDao.insert(log);
                if (id > 0) {
                    refreshLogTable();
                    refreshSuggestions();
                    AlertHelper.showSuccess(selected.getName() + " added to your log!");
                    searchField.clear();
                    searchResults.getItems().clear();
                }
            } catch (NumberFormatException ex) {
                AlertHelper.showWarning("Invalid Quantity", "Please enter a valid quantity in grams.");
            }
        });

        addRow.getChildren().addAll(new Label("Quantity:"), quantityField, unitLabel, addBtn);

        section.getChildren().addAll(title, searchField, searchResults, addRow);
        return section;
    }

    /** Build today's nutrient summary */
    private VBox buildNutrientSummary() {
        VBox section = new VBox(12);

        Label title = new Label("📊 Today's Summary");
        title.getStyleClass().add("section-title");

        NutritionTarget target = nutritionTargetDao.getTargetsForUser(user.getId(), user.getCurrentTrimester());
        double[] consumed = progressTracker.getTodayConsumed(user.getId());

        String[] names = {"Calories", "Protein", "Iron", "Calcium"};
        String[] units = {"kcal", "g", "mg", "mg"};
        double[] targetVals = {target.getCalories(), target.getProtein(), target.getIron(), target.getCalcium()};

        for (int i = 0; i < names.length; i++) {
            double pct = NutritionCalculator.calculateCompletion(consumed[i], targetVals[i]);
            VBox bar = new VBox(4);

            HBox header = new HBox();
            Label name = new Label(names[i]);
            name.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label value = new Label(String.format("%.1f / %.1f %s", consumed[i], targetVals[i], units[i]));
            value.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");
            header.getChildren().addAll(name, spacer, value);

            ProgressBar pb = new ProgressBar(pct / 100.0);
            pb.setMaxWidth(Double.MAX_VALUE);
            pb.setPrefHeight(8);
            String color = pct >= 80 ? "#4caf50" : pct >= 50 ? "#ff9800" : "#f44336";
            pb.setStyle("-fx-accent: " + color + ";");

            bar.getChildren().addAll(header, pb);
            section.getChildren().add(bar);
        }

        section.getChildren().add(0, title);
        return section;
    }

    /** Build the food log table */
    @SuppressWarnings("unchecked")
    private VBox buildFoodLogTable() {
        VBox tableContainer = new VBox(10);

        logTable = new TableView<>();
        logTable.setEditable(true);
        logTable.setPrefHeight(300);
        logTable.setPlaceholder(new Label("No foods logged today. Start adding!"));
        logTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Columns
        TableColumn<DailyLog, String> foodCol = new TableColumn<>("Food");
        foodCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFoodName()));
        foodCol.setPrefWidth(180);

        TableColumn<DailyLog, String> qtyCol = new TableColumn<>("Quantity (g)");
        qtyCol.setCellValueFactory(data -> new SimpleStringProperty(
                String.format("%.0f", data.getValue().getQuantity())));

        TableColumn<DailyLog, String> calCol = new TableColumn<>("Calories");
        calCol.setCellValueFactory(data -> new SimpleStringProperty(
                String.format("%.1f", data.getValue().getCalories())));

        TableColumn<DailyLog, String> protCol = new TableColumn<>("Protein (g)");
        protCol.setCellValueFactory(data -> new SimpleStringProperty(
                String.format("%.1f", data.getValue().getProtein())));

        TableColumn<DailyLog, String> ironCol = new TableColumn<>("Iron (mg)");
        ironCol.setCellValueFactory(data -> new SimpleStringProperty(
                String.format("%.1f", data.getValue().getIron())));

        TableColumn<DailyLog, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().isConsumed() ? "✅ Eaten" : "⏳ Planned"));
        statusCol.setPrefWidth(100);

        // Action column
        TableColumn<DailyLog, Void> actionCol = new TableColumn<>("Actions");
        actionCol.setPrefWidth(200);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button toggleBtn = new Button("Toggle");
            private final Button deleteBtn = new Button("Delete");
            {
                toggleBtn.getStyleClass().add("btn-outline");
                toggleBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8;");
                deleteBtn.getStyleClass().add("btn-danger");
                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    DailyLog log = getTableView().getItems().get(getIndex());
                    toggleBtn.setText(log.isConsumed() ? "Unmark" : "✓ Mark Eaten");
                    toggleBtn.setOnAction(e -> {
                        dailyLogDao.updateConsumedStatus(log.getId(), !log.isConsumed());
                        refreshLogTable();
                        refreshSuggestions();
                    });
                    deleteBtn.setOnAction(e -> {
                        if (AlertHelper.showConfirmation("Delete", "Remove this entry?")) {
                            dailyLogDao.delete(log.getId());
                            refreshLogTable();
                            refreshSuggestions();
                        }
                    });
                    HBox btns = new HBox(5, toggleBtn, deleteBtn);
                    setGraphic(btns);
                }
            }
        });

        logTable.getColumns().addAll(foodCol, qtyCol, calCol, protCol, ironCol, statusCol, actionCol);

        refreshLogTable();

        tableContainer.getChildren().add(logTable);
        return tableContainer;
    }

    /** Refresh the food log table data */
    private void refreshLogTable() {
        List<DailyLog> logs = dailyLogDao.getLogsForDate(user.getId(), LocalDate.now());
        logData = FXCollections.observableArrayList(logs);
        logTable.setItems(logData);
    }

    /** Refresh AI suggestions */
    private void refreshSuggestions() {
        if (suggestionsBox == null) return;
        suggestionsBox.getChildren().clear();

        NutritionTarget target = nutritionTargetDao.getTargetsForUser(user.getId(), user.getCurrentTrimester());
        List<FoodSuggestionEngine.Suggestion> suggestions = suggestionEngine.generateSuggestions(user.getId(), target);

        if (suggestions.isEmpty()) {
            Label good = new Label("🎉 Your nutrition is well-balanced today! Great work!");
            good.setStyle("-fx-text-fill: #4caf50; -fx-font-size: 14px;");
            suggestionsBox.getChildren().add(good);
        } else {
            HBox grid = new HBox(10);
            grid.setAlignment(Pos.CENTER_LEFT);

            for (int i = 0; i < Math.min(suggestions.size(), 4); i++) {
                FoodSuggestionEngine.Suggestion s = suggestions.get(i);
                VBox item = new VBox(4);
                item.setStyle("-fx-background-color: #f3e5f5; -fx-padding: 12; -fx-background-radius: 8;");
                item.setPrefWidth(200);

                Label name = new Label("🍽 " + s.getFood().getName());
                name.setStyle("-fx-font-weight: bold;");
                Label reason = new Label(s.getReason());
                reason.setWrapText(true);
                reason.setStyle("-fx-font-size: 11px; -fx-text-fill: #6c757d;");

                item.getChildren().addAll(name, reason);
                grid.getChildren().add(item);
            }
            suggestionsBox.getChildren().add(grid);
        }
    }
}
