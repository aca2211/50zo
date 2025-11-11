package com.example.a50zo.view;

import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;

/**
 * Custom alert dialog with styled appearance.
 * Extends JavaFX Alert for consistent UI design.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
public class Alert extends javafx.scene.control.Alert {

    /**
     * Constructor for styled alert.
     *
     * @param alertType Type of alert (INFO, WARNING, ERROR, etc.)
     * @param title Title of the alert dialog
     * @param header Header text
     * @param content Content message
     */
    public Alert(AlertType alertType, String title, String header, String content) {
        super(alertType);
        setTitle(title);
        setHeaderText(header);
        setContentText(content);

        styleDialog();
    }

    /**
     * Applies custom styling to the dialog.
     */
    private void styleDialog() {
        DialogPane dialogPane = getDialogPane();
        dialogPane.getStylesheets().add(
                getClass().getResource("/css/game-style.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("custom-alert");
    }

    /**
     * Shows an information alert.
     *
     * @param title Dialog title
     * @param header Header text
     * @param content Content message
     */
    public static void showInfo(String title, String header, String content) {
        Alert alert = new Alert(AlertType.INFORMATION, title, header, content);
        alert.showAndWait();
    }

    /**
     * Shows a warning alert.
     *
     * @param title Dialog title
     * @param header Header text
     * @param content Content message
     */
    public static void showWarning(String title, String header, String content) {
        Alert alert = new Alert(AlertType.WARNING, title, header, content);
        alert.showAndWait();
    }

    /**
     * Shows an error alert.
     *
     * @param title Dialog title
     * @param header Header text
     * @param content Content message
     */
    public static void showError(String title, String header, String content) {
        Alert alert = new Alert(AlertType.ERROR, title, header, content);
        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog.
     *
     * @param title Dialog title
     * @param header Header text
     * @param content Content message
     * @return true if user clicked OK
     */
    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION, title, header, content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
}