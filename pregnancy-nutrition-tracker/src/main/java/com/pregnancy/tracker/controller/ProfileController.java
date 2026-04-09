package com.pregnancy.tracker.controller;

import com.pregnancy.tracker.dao.UserDao;
import com.pregnancy.tracker.model.User;
import com.pregnancy.tracker.service.BMICalculator;
import com.pregnancy.tracker.util.AlertHelper;
import com.pregnancy.tracker.util.DateUtils;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 * ProfileController handles user profile display and editing.
 */
public class ProfileController {
    private final User user;
    private final UserDao userDao;

    public ProfileController(User user) {
        this.user = user;
        this.userDao = new UserDao();
    }

    public ScrollPane buildView() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(0, 0, 30, 0));
        Label title = new Label("Profile"); title.getStyleClass().add("page-title");
        Label sub = new Label("Manage your health profile");
        sub.getStyleClass().add("page-subtitle");
        content.getChildren().add(new VBox(4, title, sub));
        HBox cols = new HBox(20);
        VBox infoCard = new VBox(16); infoCard.getStyleClass().add("card-elevated");
        infoCard.setPrefWidth(400); HBox.setHgrow(infoCard, Priority.ALWAYS);
        infoCard.getChildren().add(buildProfileInfo());
        VBox editCard = new VBox(16); editCard.getStyleClass().add("card-elevated");
        editCard.setPrefWidth(400);
        editCard.getChildren().add(buildEditForm());
        cols.getChildren().addAll(infoCard, editCard);
        content.getChildren().add(cols);
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true); sp.getStyleClass().add("content-area");
        return sp;
    }

    private VBox buildProfileInfo() {
        VBox info = new VBox(12);
        Label title = new Label("Your Profile"); title.getStyleClass().add("section-title");
        Label icon = new Label("\uD83E\uDD30"); icon.setStyle("-fx-font-size:64px;");
        info.getChildren().addAll(title, icon,
            infoRow("Name", user.getName()), infoRow("Email", user.getEmail()),
            infoRow("Age", user.getAge() + " years"),
            infoRow("Height", user.getHeight() + " cm"),
            infoRow("Weight", user.getWeight() + " kg"),
            infoRow("BMI", String.format("%.1f (%s)", user.getBmi(), user.getBMICategory())),
            infoRow("Pregnancy Week", "Week " + user.getCurrentWeek()),
            infoRow("Trimester", DateUtils.getTrimesterDescription(user.getCurrentTrimester())),
            infoRow("Due Date", DateUtils.formatDate(user.getDueDate())),
            infoRow("Days Remaining", user.getDaysRemaining() + " days"),
            infoRow("Recommended Gain", BMICalculator.getRecommendedWeightGain(user.getBmi())));
        return info;
    }

    private HBox infoRow(String label, String value) {
        HBox row = new HBox(10); row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color:#f8f9fa;-fx-padding:10 14;-fx-background-radius:8;");
        Label l = new Label(label); l.setStyle("-fx-font-weight:bold;-fx-min-width:140;");
        Label v = new Label(value); v.setStyle("-fx-text-fill:#495057;");
        row.getChildren().addAll(l, v); return row;
    }

    private VBox buildEditForm() {
        VBox form = new VBox(14);
        Label title = new Label("Edit Profile"); title.getStyleClass().add("section-title");
        TextField nameF = new TextField(user.getName());
        TextField ageF = new TextField(String.valueOf(user.getAge()));
        TextField heightF = new TextField(String.valueOf(user.getHeight()));
        TextField weightF = new TextField(String.valueOf(user.getWeight()));
        DatePicker datePicker = new DatePicker(user.getPregnancyStartDate());
        datePicker.setMaxWidth(Double.MAX_VALUE);
        Button saveBtn = new Button("Save Changes"); saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setMaxWidth(Double.MAX_VALUE);
        saveBtn.setOnAction(e -> {
            try {
                user.setName(nameF.getText().trim());
                user.setAge(Integer.parseInt(ageF.getText().trim()));
                user.setHeight(Double.parseDouble(heightF.getText().trim()));
                user.setWeight(Double.parseDouble(weightF.getText().trim()));
                if (datePicker.getValue() != null) user.setPregnancyStartDate(datePicker.getValue());
                user.calculateBMI();
                if (userDao.update(user)) AlertHelper.showSuccess("Profile updated!");
            } catch (NumberFormatException ex) {
                AlertHelper.showError("Invalid Input", "Please enter valid numbers.");
            }
        });
        form.getChildren().addAll(title, lbl("Name"), nameF, lbl("Age"), ageF,
            lbl("Height (cm)"), heightF, lbl("Weight (kg)"), weightF,
            lbl("Pregnancy Start Date"), datePicker, new Separator(), saveBtn);
        return form;
    }

    private Label lbl(String t) { Label l = new Label(t); l.getStyleClass().add("form-label"); return l; }
}
