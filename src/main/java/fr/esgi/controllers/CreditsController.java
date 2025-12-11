package fr.esgi.controllers;

import fr.esgi.service.MidiPlaybackService;
import fr.esgi.service.PatternService;
import fr.esgi.service.PersistenceService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.IOException;

import static fr.esgi.utils.FxUtils.showError;

public class CreditsController {

    @FXML
    private TextArea creditsTextArea;

    private PatternService patternService;
    private PersistenceService persistenceService;
    private MidiPlaybackService midiService;

    public void setPatternService(PatternService patternService) {
        this.patternService = patternService;
    }

    public void setPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    public void setMidiPlaybackService(MidiPlaybackService midiService) {
        this.midiService = midiService;
    }

    @FXML
    public void initialize() {
        String text = """
                Développeurs :
                
                - Baptiste BLASQUEZ, le codeur fou
                
                - Arno FRESNEDA, la fraise tagada
                
                - Florian BRIERE, le prompt engineer
                """;
        creditsTextArea.setText(text);
    }

    @FXML
    private void showTracker() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/esgi/views/TrackerView.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof fr.esgi.controllers.TrackerController) {
                fr.esgi.controllers.TrackerController tc = (fr.esgi.controllers.TrackerController) controller;
                tc.setPatternService(patternService);
                tc.setPersistenceService(persistenceService);
                tc.setMidiPlaybackService(midiService);
            }

            Stage stage = (Stage) creditsTextArea.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/fr/esgi/styles/tracker-style.css").toExternalForm());
            stage.setScene(scene);

        } catch (IOException e) {
            showError("Erreur de chargement", "Impossible de charger la vue Tracker: " + e.getMessage());
        }
    }

    @FXML
    private void showPiano() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/esgi/views/PianoView.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof fr.esgi.controllers.PianoController) {
                fr.esgi.controllers.PianoController pc = (fr.esgi.controllers.PianoController) controller;
                pc.setPatternService(patternService);
                pc.setPersistenceService(persistenceService);
                pc.setMidiPlaybackService(midiService);
            }

            Stage stage = (Stage) creditsTextArea.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/fr/esgi/styles/tracker-style.css").toExternalForm());
            stage.setScene(scene);

        } catch (IOException e) {
            showError("Erreur de chargement", "Impossible de charger la vue Piano: " + e.getMessage());
        }
    }
}
