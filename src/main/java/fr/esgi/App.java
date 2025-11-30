package fr.esgi;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static fr.esgi.utils.FxUtils.showError;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Charger la vue Tracker en premier
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/esgi/views/TrackerView.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root, 1000, 600);

            // Charger le fichier CSS
            scene.getStylesheets().add(getClass().getResource("/fr/esgi/styles/tracker-style.css").toExternalForm());

            primaryStage.setTitle("Audio Tracker - Style Protracker");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            showError("Erreur de démarrage", "Impossible de démarrer l'application: " + e.getMessage());
        }
    }
}