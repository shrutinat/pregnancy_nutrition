package com.pregnancy.tracker.controller;

import com.pregnancy.tracker.dao.ReminderDao;
import com.pregnancy.tracker.model.Reminder;
import com.pregnancy.tracker.model.Reminder.ReminderType;
import com.pregnancy.tracker.model.User;
import com.pregnancy.tracker.util.AlertHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.*;
import java.util.List;

public class ReminderController {
    private final User user;
    private final ReminderDao reminderDao;
    private TableView<Reminder> reminderTable;

    public ReminderController(User user) {
        this.user = user;
        this.reminderDao = new ReminderDao();
    }

    public ScrollPane buildView() {
        VBox content = new VBox(24);
        content.setPadding(new Insets(0, 0, 30, 0));
        Label title = new Label("\u23F0 Reminders & Alerts");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Never miss a meal, medicine, or appointment");
        subtitle.getStyleClass().add("page-subtitle");
        content.getChildren().add(new VBox(4, title, subtitle));
        HBox columns = new HBox(20);
        VBox formCard = new VBox(16);
        formCard.getStyleClass().add("card-elevated");
        formCard.setPrefWidth(400);
        formCard.getChildren().add(buildReminderForm());
        VBox summaryCard = new VBox(16);
        summaryCard.getStyleClass().add("card-elevated");
        summaryCard.setPrefWidth(300);
        HBox.setHgrow(summaryCard, Priority.ALWAYS);
        summaryCard.getChildren().add(buildSummary());
        columns.getChildren().addAll(formCard, summaryCard);
        VBox tableCard = new VBox(16);
        tableCard.getStyleClass().add("card-elevated");
        tableCard.getChildren().add(buildReminderTable());
        content.getChildren().addAll(columns, tableCard);
        ScrollPane sp = new ScrollPane(content);
        sp.setFitToWidth(true);
        sp.getStyleClass().add("content-area");
        return sp;
    }

    private VBox buildReminderForm() {
        VBox form = new VBox(14);
        Label title = new Label("+ Create New Reminder");
        title.getStyleClass().add("section-title");
        ComboBox<String> typeCombo = new ComboBox<>(FXCollections.observableArrayList(
            "Meal Reminder","Medicine Reminder","Doctor Appointment","Custom Reminder"));
        typeCombo.setPromptText("Select type");
        typeCombo.setMaxWidth(Double.MAX_VALUE);
        TextField titleField = new TextField();
        titleField.setPromptText("e.g., Take iron supplements");
        TextArea descField = new TextArea();
        descField.setPromptText("Additional details (optional)");
        descField.setPrefRowCount(2);
        descField.setWrapText(true);
        DatePicker datePicker = new DatePicker(LocalDate.now());
        datePicker.setMaxWidth(Double.MAX_VALUE);
        HBox timeRow = new HBox(10);
        timeRow.setAlignment(Pos.CENTER_LEFT);
        Spinner<Integer> hourSp = new Spinner<>(0,23,9);
        hourSp.setPrefWidth(80);
        hourSp.setEditable(true);
        Spinner<Integer> minSp = new Spinner<>(0,59,0,5);
        minSp.setPrefWidth(80);
        minSp.setEditable(true);
        timeRow.getChildren().addAll(hourSp, new Label(":"), minSp);
        CheckBox recurCheck = new CheckBox("Repeat daily");
        Button createBtn = new Button("Create Reminder");
        createBtn.getStyleClass().add("btn-primary");
        createBtn.setMaxWidth(Double.MAX_VALUE);
        createBtn.setOnAction(e -> {
            if (typeCombo.getValue() == null) { AlertHelper.showWarning("Required","Select type."); return; }
            if (titleField.getText()==null||titleField.getText().trim().isEmpty()) { AlertHelper.showWarning("Required","Enter title."); return; }
            ReminderType type = switch(typeCombo.getSelectionModel().getSelectedIndex()){
                case 0->ReminderType.MEAL; case 1->ReminderType.MEDICINE;
                case 2->ReminderType.APPOINTMENT; default->ReminderType.CUSTOM;};
            LocalDateTime sched = LocalDateTime.of(datePicker.getValue(), LocalTime.of(hourSp.getValue(),minSp.getValue()));
            Reminder r = new Reminder(user.getId(),type,titleField.getText().trim(),
                descField.getText()!=null?descField.getText().trim():"",sched);
            r.setRecurring(recurCheck.isSelected());
            if(recurCheck.isSelected()) r.setRecurrencePattern("DAILY");
            if (reminderDao.insert(r) > 0) {
                AlertHelper.showSuccess("Reminder created!");
                titleField.clear(); descField.clear(); refreshTable();
            }
        });
        form.getChildren().addAll(title,lbl("Type *"),typeCombo,lbl("Title *"),titleField,
            lbl("Description"),descField,lbl("Date"),datePicker,lbl("Time"),timeRow,
            recurCheck,new Separator(),createBtn);
        return form;
    }

    private VBox buildSummary() {
        VBox s = new VBox(12);
        Label title = new Label("Summary"); title.getStyleClass().add("section-title");
        List<Reminder> all = reminderDao.findByUserId(user.getId());
        List<Reminder> active = reminderDao.findActiveByUserId(user.getId());
        long meals = active.stream().filter(r->r.getType()==ReminderType.MEAL).count();
        long meds = active.stream().filter(r->r.getType()==ReminderType.MEDICINE).count();
        long appts = active.stream().filter(r->r.getType()==ReminderType.APPOINTMENT).count();
        s.getChildren().addAll(title,
            sumRow("Total",String.valueOf(all.size())),sumRow("Active",String.valueOf(active.size())),
            sumRow("Meals",String.valueOf(meals)),sumRow("Medicine",String.valueOf(meds)),
            sumRow("Appointments",String.valueOf(appts)));
        return s;
    }

    private HBox sumRow(String l,String v){
        HBox r=new HBox(); r.setAlignment(Pos.CENTER_LEFT);
        r.setStyle("-fx-background-color:#f8f9fa;-fx-padding:8 12;-fx-background-radius:6;");
        Label n=new Label(l); Region sp=new Region(); HBox.setHgrow(sp,Priority.ALWAYS);
        Label vl=new Label(v); vl.setStyle("-fx-font-weight:bold;-fx-text-fill:#e91e8c;");
        r.getChildren().addAll(n,sp,vl); return r;
    }

    @SuppressWarnings("unchecked")
    private VBox buildReminderTable() {
        VBox sec = new VBox(12);
        Label title = new Label("All Reminders"); title.getStyleClass().add("section-title");
        reminderTable = new TableView<>(); reminderTable.setPrefHeight(300);
        reminderTable.setPlaceholder(new Label("No reminders yet."));
        reminderTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Reminder,String> tC=new TableColumn<>("Type");
        tC.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getType().getDisplayName()));
        TableColumn<Reminder,String> tiC=new TableColumn<>("Title");
        tiC.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getTitle())); tiC.setPrefWidth(200);
        TableColumn<Reminder,String> tmC=new TableColumn<>("Scheduled");
        tmC.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getScheduledTime()!=null?
            d.getValue().getScheduledTime().toString().replace("T"," "):"N/A"));
        TableColumn<Reminder,String> stC=new TableColumn<>("Status");
        stC.setCellValueFactory(d->new SimpleStringProperty(d.getValue().isActive()?"Active":"Paused"));
        TableColumn<Reminder,Void> aC=new TableColumn<>("Actions"); aC.setPrefWidth(200);
        aC.setCellFactory(col->new TableCell<>(){
            final Button tog=new Button("Toggle"); final Button del=new Button("Delete");
            { tog.getStyleClass().add("btn-outline"); tog.setStyle("-fx-font-size:11px;-fx-padding:4 8;");
              del.getStyleClass().add("btn-danger"); del.setStyle("-fx-font-size:11px;-fx-padding:4 8;"); }
            @Override protected void updateItem(Void i,boolean empty){
                super.updateItem(i,empty); if(empty){setGraphic(null);}else{
                Reminder r=getTableView().getItems().get(getIndex());
                tog.setText(r.isActive()?"Pause":"Activate");
                tog.setOnAction(e->{reminderDao.toggleActive(r.getId());refreshTable();});
                del.setOnAction(e->{if(AlertHelper.showConfirmation("Delete","Remove?")){
                    reminderDao.delete(r.getId());refreshTable();}});
                setGraphic(new HBox(5,tog,del));}}});
        reminderTable.getColumns().addAll(tC,tiC,tmC,stC,aC); refreshTable();
        sec.getChildren().addAll(title,reminderTable); return sec;
    }

    private void refreshTable(){ reminderTable.setItems(FXCollections.observableArrayList(
        reminderDao.findByUserId(user.getId()))); }
    private Label lbl(String t){ Label l=new Label(t); l.getStyleClass().add("form-label"); return l; }
}
