package com.pregnancy.tracker.controller;

import com.pregnancy.tracker.dao.DoctorUpdateDao;
import com.pregnancy.tracker.dao.NutritionTargetDao;
import com.pregnancy.tracker.model.DoctorUpdate;
import com.pregnancy.tracker.model.NutritionTarget;
import com.pregnancy.tracker.model.User;
import com.pregnancy.tracker.util.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.List;

/**
 * DoctorController handles doctor update views.
 * Doctors can add medical notes, update nutrition targets, and flag risk conditions.
 */
public class DoctorController {

    private final User user;
    private final DoctorUpdateDao doctorUpdateDao;
    private final NutritionTargetDao nutritionTargetDao;
    private TableView<DoctorUpdate> updatesTable;

    public DoctorController(User user) {
        this.user = user;
        this.doctorUpdateDao = new DoctorUpdateDao();
        this.nutritionTargetDao = new NutritionTargetDao();
    }

    /**
     * Build the doctor updates view.
     * @return ScrollPane containing doctor update forms and history
     */
    public ScrollPane buildView() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(0, 0, 30, 0));

        // Header
        Label title = new Label("🏥 Doctor Updates");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Medical notes, risk conditions, and nutrition target adjustments");
        subtitle.getStyleClass().add("page-subtitle");
        content.getChildren().add(new VBox(4, title, subtitle));

        // Two-column layout
        HBox columns = new HBox(20);

        // Left: Add new update form
        VBox formCard = new VBox(16);
        formCard.getStyleClass().add("card-elevated");
        formCard.setPrefWidth(450);
        HBox.setHgrow(formCard, Priority.ALWAYS);
        formCard.getChildren().add(buildDoctorForm());

        // Right: Update nutrition targets
        VBox targetCard = new VBox(16);
        targetCard.getStyleClass().add("card-elevated");
        targetCard.setPrefWidth(350);
        targetCard.getChildren().add(buildNutritionTargetForm());

        columns.getChildren().addAll(formCard, targetCard);

        // History table
        VBox historyCard = new VBox(16);
        historyCard.getStyleClass().add("card-elevated");
        historyCard.getChildren().add(buildUpdatesHistory());

        content.getChildren().addAll(columns, historyCard);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("content-area");
        return scrollPane;
    }

    /** Build the doctor update form */
    private VBox buildDoctorForm() {
        VBox form = new VBox(14);

        Label title = new Label("📝 Add Medical Update");
        title.getStyleClass().add("section-title");

        TextField doctorNameField = new TextField();
        doctorNameField.setPromptText("Doctor's name");
        doctorNameField.setId("doctor-name");

        TextArea notesField = new TextArea();
        notesField.setPromptText("Medical notes, observations, recommendations...");
        notesField.setPrefRowCount(4);
        notesField.setWrapText(true);
        notesField.setId("doctor-notes");

        TextField riskField = new TextField();
        riskField.setPromptText("Risk conditions (e.g., gestational diabetes, anemia)");
        riskField.setId("risk-conditions");

        Button saveBtn = new Button("💾 Save Update");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setId("save-doctor-btn");

        saveBtn.setOnAction(e -> {
            String doctorName = doctorNameField.getText();
            String notes = notesField.getText();
            String risks = riskField.getText();

            if (doctorName == null || doctorName.trim().isEmpty()) {
                AlertHelper.showWarning("Required", "Please enter the doctor's name.");
                return;
            }
            if (notes == null || notes.trim().isEmpty()) {
                AlertHelper.showWarning("Required", "Please enter medical notes.");
                return;
            }

            DoctorUpdate update = new DoctorUpdate(user.getId(), doctorName.trim(),
                    notes.trim(), risks != null ? risks.trim() : "");

            int id = doctorUpdateDao.insert(update);
            if (id > 0) {
                AlertHelper.showSuccess("Doctor update saved successfully!");
                doctorNameField.clear();
                notesField.clear();
                riskField.clear();
                refreshHistory();
            }
        });

        form.getChildren().addAll(
                title,
                createFormLabel("Doctor Name *"), doctorNameField,
                createFormLabel("Medical Notes *"), notesField,
                createFormLabel("Risk Conditions"), riskField,
                new Separator(),
                saveBtn
        );

        return form;
    }

    /** Build nutrition target modification form */
    private VBox buildNutritionTargetForm() {
        VBox form = new VBox(14);

        Label title = new Label("🎯 Adjust Nutrition Targets");
        title.getStyleClass().add("section-title");

        Label info = new Label("Current trimester: " + user.getCurrentTrimester() +
                " (Week " + user.getCurrentWeek() + ")");
        info.getStyleClass().add("info-text");

        NutritionTarget current = nutritionTargetDao.getTargetsForUser(
                user.getId(), user.getCurrentTrimester());

        TextField caloriesField = createNumField(String.valueOf((int) current.getCalories()));
        TextField proteinField = createNumField(String.format("%.1f", current.getProtein()));
        TextField ironField = createNumField(String.format("%.1f", current.getIron()));
        TextField calciumField = createNumField(String.format("%.0f", current.getCalcium()));
        TextField waterField = createNumField(String.format("%.1f", current.getWaterIntake()));

        Button updateBtn = new Button("📋 Update Targets");
        updateBtn.getStyleClass().add("btn-secondary");
        updateBtn.setMaxWidth(Double.MAX_VALUE);
        updateBtn.setId("update-targets-btn");

        updateBtn.setOnAction(e -> {
            try {
                current.setCalories(Double.parseDouble(caloriesField.getText()));
                current.setProtein(Double.parseDouble(proteinField.getText()));
                current.setIron(Double.parseDouble(ironField.getText()));
                current.setCalcium(Double.parseDouble(calciumField.getText()));
                current.setWaterIntake(Double.parseDouble(waterField.getText()));
                current.setDoctorModified(true);

                if (nutritionTargetDao.saveOrUpdate(current)) {
                    AlertHelper.showSuccess("Nutrition targets updated by doctor.");
                }
            } catch (NumberFormatException ex) {
                AlertHelper.showError("Invalid Input", "Please enter valid numbers.");
            }
        });

        form.getChildren().addAll(
                title, info,
                createFormLabel("Calories (kcal)"), caloriesField,
                createFormLabel("Protein (g)"), proteinField,
                createFormLabel("Iron (mg)"), ironField,
                createFormLabel("Calcium (mg)"), calciumField,
                createFormLabel("Water (liters)"), waterField,
                new Separator(),
                updateBtn
        );

        return form;
    }

    /** Build doctor updates history table */
    @SuppressWarnings("unchecked")
    private VBox buildUpdatesHistory() {
        VBox section = new VBox(12);

        Label title = new Label("📋 Update History");
        title.getStyleClass().add("section-title");

        updatesTable = new TableView<>();
        updatesTable.setPrefHeight(250);
        updatesTable.setPlaceholder(new Label("No doctor updates yet."));
        updatesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<DoctorUpdate, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUpdateDate().toString()));

        TableColumn<DoctorUpdate, String> doctorCol = new TableColumn<>("Doctor");
        doctorCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDoctorName()));

        TableColumn<DoctorUpdate, String> notesCol = new TableColumn<>("Notes");
        notesCol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNotes()));
        notesCol.setPrefWidth(250);

        TableColumn<DoctorUpdate, String> riskCol = new TableColumn<>("Risk Conditions");
        riskCol.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().hasRiskConditions() ? d.getValue().getRiskConditions() : "None"));

        TableColumn<DoctorUpdate, Void> actionCol = new TableColumn<>("Action");
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("🗑 Delete");
            {
                deleteBtn.getStyleClass().add("btn-danger");
                deleteBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    DoctorUpdate upd = getTableView().getItems().get(getIndex());
                    deleteBtn.setOnAction(e -> {
                        if (AlertHelper.showConfirmation("Delete", "Remove this doctor update?")) {
                            doctorUpdateDao.delete(upd.getId());
                            refreshHistory();
                        }
                    });
                    setGraphic(deleteBtn);
                }
            }
        });

        updatesTable.getColumns().addAll(dateCol, doctorCol, notesCol, riskCol, actionCol);
        refreshHistory();

        section.getChildren().addAll(title, updatesTable);
        return section;
    }

    /** Refresh updates table */
    private void refreshHistory() {
        List<DoctorUpdate> updates = doctorUpdateDao.findByUserId(user.getId());
        updatesTable.setItems(FXCollections.observableArrayList(updates));
    }

    /** Helper methods */
    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }

    private TextField createNumField(String value) {
        TextField field = new TextField(value);
        field.setPrefWidth(150);
        return field;
    }
}
