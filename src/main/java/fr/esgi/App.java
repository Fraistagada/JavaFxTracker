package fr.esgi;

import fr.esgi.service.impl.*;
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

            PatternServiceImpl patternService = new PatternServiceImpl();
            SchedulerServiceImpl schedulerService = new SchedulerServiceImpl();
            FilePersistenceServiceImpl persistenceService = new FilePersistenceServiceImpl();
            MidiPlaybackServiceImpl midiServiceImpl;
            fr.esgi.service.MidiPlaybackService midiService;
            try {
                midiServiceImpl = new MidiPlaybackServiceImpl(schedulerService);
                midiService = midiServiceImpl;
            } catch (javax.sound.midi.MidiUnavailableException e) {
                midiService = new NoopMidiPlaybackService();
            }

            Object controller = loader.getController();
            if (controller instanceof fr.esgi.controllers.TrackerController) {
                fr.esgi.controllers.TrackerController tc = (fr.esgi.controllers.TrackerController) controller;
                tc.setPatternService(patternService);
                tc.setPersistenceService(persistenceService);
                tc.setMidiPlaybackService(midiService);
            }

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