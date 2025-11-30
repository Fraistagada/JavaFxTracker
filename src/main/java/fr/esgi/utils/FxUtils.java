package fr.esgi.utils;

import javafx.application.Platform;
import javafx.scene.control.Alert;

public final class FxUtils {

    private FxUtils() {
    }

    public static void showError(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
}
