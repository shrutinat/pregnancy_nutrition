package com.pregnancy.tracker.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.StageStyle;

import java.util.Optional;

/**
 * Utility class for JavaFX alert dialogs.
 * Provides convenient methods for showing information, warnings, errors, and confirmations.
 */
public class AlertHelper {

    /**
     * Show an information dialog.
     * @param title dialog title
     * @param message dialog message
     */
    public static void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show a warning dialog.
     * @param title dialog title
     * @param message dialog message
     */
    public static void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show an error dialog.
     * @param title dialog title
     * @param message dialog message
     */
    public static void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Show a confirmation dialog.
     * @param title dialog title
     * @param message dialog message
     * @return true if user clicked OK
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Show a text input dialog.
     * @param title dialog title
     * @param prompt prompt message
     * @param defaultValue default input value
     * @return user input or null if cancelled
     */
    public static String showTextInput(String title, String prompt, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(prompt);

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * Show a success notification-style dialog.
     * @param message success message
     */
    public static void showSuccess(String message) {
        showInfo("Success", message);
    }
}
