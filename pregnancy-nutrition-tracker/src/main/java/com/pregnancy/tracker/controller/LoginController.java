package com.pregnancy.tracker.controller;

import com.pregnancy.tracker.dao.UserDao;
import com.pregnancy.tracker.model.User;
import com.pregnancy.tracker.service.ReminderService;
import com.pregnancy.tracker.util.AlertHelper;
import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDate;

/**
 * LoginController handles user authentication and registration.
 * Provides a modern login/register form with input validation.
 */
public class LoginController {

    private final UserDao userDao;
    private final ReminderService reminderService;
    private Runnable onLoginSuccess;
    private User loggedInUser;

    // Form fields
    private TextField nameField, emailField, ageField, heightField, weightField;
    private PasswordField passwordField;
    private DatePicker pregnancyDatePicker;

    public LoginController() {
        this.userDao = new UserDao();
        this.reminderService = new ReminderService();
    }

    /**
     * Set callback for successful login.
     */
    public void setOnLoginSuccess(Runnable callback) {
        this.onLoginSuccess = callback;
    }

    /**
     * Get the logged-in user.
     */
    public User getLoggedInUser() {
        return loggedInUser;
    }

    /**
     * Build the login screen UI.
     * @return StackPane containing the login form
     */
    public StackPane buildLoginView() {
        StackPane root = new StackPane();
        root.getStyleClass().add("login-container");
        root.setAlignment(Pos.CENTER);

        VBox card = new VBox(20);
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.CENTER);
        card.setMaxWidth(420);

        // App icon/emoji
        Label icon = new Label("🤰");
        icon.setStyle("-fx-font-size: 48px;");

        // Title
        Label title = new Label("Pregnancy Nutrition Tracker");
        title.getStyleClass().add("login-title");
        title.setTextAlignment(TextAlignment.CENTER);
        title.setWrapText(true);

        Label subtitle = new Label("Track your nutrition journey through pregnancy");
        subtitle.getStyleClass().add("login-subtitle");
        subtitle.setTextAlignment(TextAlignment.CENTER);
        subtitle.setWrapText(true);

        // Tab pane for Login / Register
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: transparent;");

        Tab loginTab = new Tab("Login", buildLoginForm());
        Tab registerTab = new Tab("Register", buildRegisterForm());

        tabPane.getTabs().addAll(loginTab, registerTab);

        card.getChildren().addAll(icon, title, subtitle, tabPane);

        root.getChildren().add(card);
        return root;
    }

    /**
     * Build the login form.
     */
    private VBox buildLoginForm() {
        VBox form = new VBox(15);
        form.setPadding(new Insets(20, 0, 0, 0));

        TextField loginEmail = new TextField();
        loginEmail.setPromptText("Email address");
        loginEmail.setId("login-email");

        PasswordField loginPassword = new PasswordField();
        loginPassword.setPromptText("Password");
        loginPassword.setId("login-password");

        Button loginBtn = new Button("Sign In");
        loginBtn.getStyleClass().add("btn-primary");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setId("login-button");

        loginBtn.setOnAction(e -> handleLogin(loginEmail.getText(), loginPassword.getText()));

        // Enter key support
        loginPassword.setOnAction(e -> handleLogin(loginEmail.getText(), loginPassword.getText()));

        form.getChildren().addAll(
                createFormLabel("Email"),
                loginEmail,
                createFormLabel("Password"),
                loginPassword,
                new Separator(),
                loginBtn
        );

        return form;
    }

    /**
     * Build the registration form.
     */
    private ScrollPane buildRegisterForm() {
        VBox form = new VBox(12);
        form.setPadding(new Insets(20, 5, 20, 5));

        nameField = new TextField();
        nameField.setPromptText("Full name");
        nameField.setId("register-name");

        emailField = new TextField();
        emailField.setPromptText("Email address");
        emailField.setId("register-email");

        passwordField = new PasswordField();
        passwordField.setPromptText("Create password (min 6 chars)");
        passwordField.setId("register-password");

        ageField = new TextField();
        ageField.setPromptText("e.g. 28");
        ageField.setId("register-age");

        heightField = new TextField();
        heightField.setPromptText("Height in cm (e.g. 165)");
        heightField.setId("register-height");

        weightField = new TextField();
        weightField.setPromptText("Weight in kg (e.g. 60)");
        weightField.setId("register-weight");

        pregnancyDatePicker = new DatePicker();
        pregnancyDatePicker.setPromptText("Select date");
        pregnancyDatePicker.setMaxWidth(Double.MAX_VALUE);
        pregnancyDatePicker.setId("register-pregnancy-date");

        Button registerBtn = new Button("Create Account");
        registerBtn.getStyleClass().add("btn-primary");
        registerBtn.setMaxWidth(Double.MAX_VALUE);
        registerBtn.setId("register-button");

        registerBtn.setOnAction(e -> handleRegister());

        form.getChildren().addAll(
                createFormLabel("Full Name *"),
                nameField,
                createFormLabel("Email *"),
                emailField,
                createFormLabel("Password *"),
                passwordField,
                createFormLabel("Age"),
                ageField,
                createFormLabel("Height (cm)"),
                heightField,
                createFormLabel("Weight (kg)"),
                weightField,
                createFormLabel("Pregnancy Start Date (LMP)"),
                pregnancyDatePicker,
                new Separator(),
                registerBtn
        );

        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        scrollPane.setPrefHeight(350);

        return scrollPane;
    }

    /**
     * Handle login attempt.
     */
    private void handleLogin(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            AlertHelper.showWarning("Validation Error", "Please enter your email address.");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            AlertHelper.showWarning("Validation Error", "Please enter your password.");
            return;
        }

        String hashedPassword = hashPassword(password);
        User user = userDao.findByEmailAndPassword(email.trim(), hashedPassword);

        if (user != null) {
            loggedInUser = user;
            reminderService.startService(user.getId());
            if (onLoginSuccess != null) {
                onLoginSuccess.run();
            }
        } else {
            AlertHelper.showError("Login Failed", "Invalid email or password. Please try again.");
        }
    }

    /**
     * Handle registration attempt with validation.
     */
    private void handleRegister() {
        // Validate required fields
        String name = nameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (name == null || name.trim().isEmpty()) {
            AlertHelper.showWarning("Validation Error", "Name is required.");
            return;
        }
        if (email == null || email.trim().isEmpty() || !email.contains("@")) {
            AlertHelper.showWarning("Validation Error", "Please enter a valid email address.");
            return;
        }
        if (password == null || password.length() < 6) {
            AlertHelper.showWarning("Validation Error", "Password must be at least 6 characters.");
            return;
        }

        // Check if email already exists
        if (userDao.findByEmail(email.trim()) != null) {
            AlertHelper.showError("Registration Error", "An account with this email already exists.");
            return;
        }

        // Create user
        User user = new User();
        user.setName(name.trim());
        user.setEmail(email.trim());
        user.setPassword(hashPassword(password));

        // Parse optional fields
        try {
            if (!ageField.getText().isEmpty()) {
                int age = Integer.parseInt(ageField.getText().trim());
                if (age < 15 || age > 55) {
                    AlertHelper.showWarning("Validation", "Please enter a valid age (15-55).");
                    return;
                }
                user.setAge(age);
            }
        } catch (NumberFormatException e) {
            AlertHelper.showWarning("Validation", "Please enter a valid age number.");
            return;
        }

        try {
            if (!heightField.getText().isEmpty()) {
                user.setHeight(Double.parseDouble(heightField.getText().trim()));
            }
        } catch (NumberFormatException e) {
            AlertHelper.showWarning("Validation", "Please enter a valid height in cm.");
            return;
        }

        try {
            if (!weightField.getText().isEmpty()) {
                user.setWeight(Double.parseDouble(weightField.getText().trim()));
            }
        } catch (NumberFormatException e) {
            AlertHelper.showWarning("Validation", "Please enter a valid weight in kg.");
            return;
        }

        if (pregnancyDatePicker.getValue() != null) {
            LocalDate pDate = pregnancyDatePicker.getValue();
            if (pDate.isAfter(LocalDate.now())) {
                AlertHelper.showWarning("Validation", "Pregnancy start date cannot be in the future.");
                return;
            }
            user.setPregnancyStartDate(pDate);
        }

        user.calculateBMI();

        // Save to database
        int id = userDao.insert(user);
        if (id > 0) {
            user.setId(id);
            // Create default reminders
            reminderService.createDefaultReminders(id);
            AlertHelper.showSuccess("Account created successfully! You can now login.");
        } else {
            AlertHelper.showError("Error", "Failed to create account. Please try again.");
        }
    }

    /**
     * Hash password using SHA-256.
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return password; // Fallback to plain text
        }
    }

    /** Helper to create styled form labels */
    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("form-label");
        return label;
    }
}
