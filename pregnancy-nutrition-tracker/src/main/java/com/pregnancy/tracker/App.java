package com.pregnancy.tracker;

import com.pregnancy.tracker.controller.*;
import com.pregnancy.tracker.dao.DatabaseManager;
import com.pregnancy.tracker.dao.FoodItemDao;
import com.pregnancy.tracker.model.FoodItem;
import com.pregnancy.tracker.model.User;
import com.pregnancy.tracker.service.ReminderService;
import com.pregnancy.tracker.util.CSVLoader;
import javafx.application.Application;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

/**
 * Main Application class for Pregnancy Nutrition Tracker.
 * Entry point that initializes the database, loads food data,
 * and builds the JavaFX UI with sidebar navigation.
 */
public class App extends Application {

    private Stage primaryStage;
    private BorderPane mainLayout;
    private User currentUser;
    private VBox sidebar;
    private StackPane contentArea;
    private String activeNav = "";
    private ReminderService reminderService;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("Pregnancy Nutrition Tracker");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);

        // Initialize database
        DatabaseManager.getInstance().initializeDatabase();

        // Load food database from CSV
        loadFoodDatabase();

        // Show login screen
        showLoginScreen();
    }

    /** Load food items from CSV into database */
    private void loadFoodDatabase() {
        FoodItemDao foodDao = new FoodItemDao();
        if (foodDao.getCount() == 0) {
            List<FoodItem> foods = CSVLoader.loadFoodItems("/data/food_database.csv");
            foodDao.bulkInsert(foods);
        }
    }

    /** Show the login/register screen */
    private void showLoginScreen() {
        LoginController loginController = new LoginController();
        loginController.setOnLoginSuccess(() -> {
            currentUser = loginController.getLoggedInUser();
            showMainScreen();
        });

        StackPane loginView = loginController.buildLoginView();
        Scene scene = new Scene(loginView, 1100, 700);

        // Load CSS
        String css = getClass().getResource("styles/app.css") != null
                ? getClass().getResource("styles/app.css").toExternalForm()
                : null;
        if (css != null) scene.getStylesheets().add(css);

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /** Show the main application screen with sidebar */
    private void showMainScreen() {
        mainLayout = new BorderPane();

        // Build sidebar
        sidebar = buildSidebar();
        mainLayout.setLeft(sidebar);

        // Content area
        contentArea = new StackPane();
        contentArea.getStyleClass().add("content-area");
        mainLayout.setCenter(contentArea);

        // Default to dashboard
        navigateTo("dashboard");

        Scene scene = new Scene(mainLayout, 1200, 750);
        String css = getClass().getResource("styles/app.css") != null
                ? getClass().getResource("styles/app.css").toExternalForm()
                : null;
        if (css != null) scene.getStylesheets().add(css);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Pregnancy Nutrition Tracker - " + currentUser.getName());
        primaryStage.centerOnScreen();

        // Start reminder service
        reminderService = new ReminderService();
        reminderService.startService(currentUser.getId());
    }

    /** Build the sidebar navigation */
    private VBox buildSidebar() {
        VBox sb = new VBox(0);
        sb.getStyleClass().add("sidebar");
        sb.setPrefWidth(220);

        // Header
        VBox header = new VBox(4);
        header.getStyleClass().add("sidebar-header");

        Label appName = new Label("\uD83E\uDD30 NutriMom");
        appName.getStyleClass().add("sidebar-title");

        Label appSub = new Label("Pregnancy Tracker");
        appSub.getStyleClass().add("sidebar-subtitle");

        header.getChildren().addAll(appName, appSub);

        // Navigation buttons
        VBox nav = new VBox(2);
        nav.setPadding(new Insets(10, 0, 0, 0));

        nav.getChildren().addAll(
            createNavButton("\uD83C\uDFE0  Dashboard", "dashboard"),
            createNavButton("\uD83C\uDF7D  Food Tracker", "food"),
            createNavButton("\uD83D\uDCC8  Progress", "progress"),
            createNavButton("\uD83C\uDFE5  Doctor Updates", "doctor"),
            createNavButton("\u23F0  Reminders", "reminders"),
            createNavButton("\uD83D\uDC64  Profile", "profile")
        );

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // Logout button
        Button logoutBtn = new Button("\uD83D\uDEAA  Logout");
        logoutBtn.getStyleClass().add("nav-button");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> {
            if (reminderService != null) reminderService.stopService();
            currentUser = null;
            showLoginScreen();
        });

        // User info at bottom
        VBox userInfo = new VBox(2);
        userInfo.setPadding(new Insets(10, 15, 15, 15));
        userInfo.setStyle("-fx-background-color: rgba(255,255,255,0.05);");

        Label userName = new Label(currentUser.getName());
        userName.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");

        Label userWeek = new Label("Week " + currentUser.getCurrentWeek() +
                " \u2022 T" + currentUser.getCurrentTrimester());
        userWeek.setStyle("-fx-text-fill: rgba(255,255,255,0.6); -fx-font-size: 11px;");

        userInfo.getChildren().addAll(userName, userWeek);

        sb.getChildren().addAll(header, nav, spacer, logoutBtn, userInfo);
        return sb;
    }

    /** Create a sidebar navigation button */
    private Button createNavButton(String text, String target) {
        Button btn = new Button(text);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setId("nav-" + target);

        btn.setOnAction(e -> navigateTo(target));
        return btn;
    }

    /** Navigate to a specific view */
    private void navigateTo(String target) {
        activeNav = target;
        contentArea.getChildren().clear();

        // Update sidebar button states
        for (var node : sidebar.lookupAll(".nav-button")) {
            node.getStyleClass().remove("nav-button-active");
            if (node.getId() != null && node.getId().equals("nav-" + target)) {
                node.getStyleClass().add("nav-button-active");
            }
        }

        // Load the target view
        switch (target) {
            case "dashboard":
                DashboardController dc = new DashboardController(currentUser);
                contentArea.getChildren().add(dc.buildView());
                break;
            case "food":
                FoodTrackerController ftc = new FoodTrackerController(currentUser);
                contentArea.getChildren().add(ftc.buildView());
                break;
            case "progress":
                ProgressController pc = new ProgressController(currentUser);
                contentArea.getChildren().add(pc.buildView());
                break;
            case "doctor":
                DoctorController doc = new DoctorController(currentUser);
                contentArea.getChildren().add(doc.buildView());
                break;
            case "reminders":
                ReminderController rc = new ReminderController(currentUser);
                contentArea.getChildren().add(rc.buildView());
                break;
            case "profile":
                ProfileController prc = new ProfileController(currentUser);
                contentArea.getChildren().add(prc.buildView());
                break;
        }
    }

    @Override
    public void stop() {
        if (reminderService != null) reminderService.stopService();
        DatabaseManager.getInstance().closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
