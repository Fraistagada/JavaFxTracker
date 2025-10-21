package fr.esgi.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class PianoController {

    @FXML private HBox pianoContainer;
    @FXML private Label patternLabel;
    @FXML private Label bpmLabel;
    @FXML private Label tempoLabel;
    @FXML private Slider tempoSlider;

    @FXML private Button playBtn;
    @FXML private Button stopBtn;
    @FXML private Button pauseBtn;
    @FXML private Button recordBtn;

    private int currentPattern = 0;
    private int bpm = 125;

    @FXML
    public void initialize() {
        // Créer le piano
        createPiano();

        // Configuration du slider tempo
        tempoSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            bpm = newVal.intValue();
            tempoLabel.setText("Tempo: " + bpm);
            bpmLabel.setText("BPM: " + bpm);
        });
    }

    private void createPiano() {
        String[] whiteNotes = {"C", "D", "E", "F", "G", "A", "B"};
        String[] blackNotes = {"C#", "D#", "", "F#", "G#", "A#", ""};

        for (int octave = 3; octave <= 5; octave++) {
            for (int i = 0; i < whiteNotes.length; i++) {
                StackPane notePane = new StackPane();

                // Touche blanche
                Button whiteKey = createPianoKey(whiteNotes[i] + octave, true);
                notePane.getChildren().add(whiteKey);

                // Touche noire (si elle existe)
                if (!blackNotes[i].isEmpty()) {
                    Button blackKey = createPianoKey(blackNotes[i] + octave, false);
                    StackPane.setAlignment(blackKey, Pos.TOP_RIGHT);
                    StackPane.setMargin(blackKey, new Insets(0, -20, 0, 0));
                    notePane.getChildren().add(blackKey);
                }

                pianoContainer.getChildren().add(notePane);
            }
        }
    }

    private Button createPianoKey(String note, boolean isWhite) {
        Button key = new Button(note);

        if (isWhite) {
            key.setPrefSize(50, 150);
            key.setStyle("-fx-background-color: white; -fx-border-color: black; " +
                    "-fx-border-width: 1; -fx-text-fill: black; " +
                    "-fx-font-family: monospace; -fx-font-size: 10;");

            key.setOnMousePressed(e -> {
                key.setStyle("-fx-background-color: #cccccc; -fx-border-color: black; " +
                        "-fx-border-width: 1; -fx-text-fill: black; " +
                        "-fx-font-family: monospace; -fx-font-size: 10;");
                playNote(note);
            });

            key.setOnMouseReleased(e ->
                    key.setStyle("-fx-background-color: white; -fx-border-color: black; " +
                            "-fx-border-width: 1; -fx-text-fill: black; " +
                            "-fx-font-family: monospace; -fx-font-size: 10;"));
        } else {
            key.setPrefSize(30, 100);
            key.setStyle("-fx-background-color: black; -fx-border-color: #333333; " +
                    "-fx-border-width: 1; -fx-text-fill: white; " +
                    "-fx-font-family: monospace; -fx-font-size: 9;");

            key.setOnMousePressed(e -> {
                key.setStyle("-fx-background-color: #333333; -fx-border-color: #333333; " +
                        "-fx-border-width: 1; -fx-text-fill: white; " +
                        "-fx-font-family: monospace; -fx-font-size: 9;");
                playNote(note);
            });

            key.setOnMouseReleased(e ->
                    key.setStyle("-fx-background-color: black; -fx-border-color: #333333; " +
                            "-fx-border-width: 1; -fx-text-fill: white; " +
                            "-fx-font-family: monospace; -fx-font-size: 9;"));
        }

        return key;
    }

    private void playNote(String note) {
        System.out.println("Note jouée: " + note);
        // TODO: Implémenter la génération du son
    }

    @FXML
    private void handlePlay() {
        System.out.println("PLAY - Lecture du pattern " + currentPattern);
        // TODO: Implémenter la logique de lecture
    }

    @FXML
    private void handleStop() {
        System.out.println("STOP - Arrêt de la lecture");
        // TODO: Implémenter la logique d'arrêt
    }

    @FXML
    private void handlePause() {
        System.out.println("PAUSE - Mise en pause");
        // TODO: Implémenter la logique de pause
    }

    @FXML
    private void handleRecord() {
        System.out.println("REC - Mode enregistrement activé");
        // TODO: Implémenter la logique d'enregistrement
    }

    @FXML
    private void showTracker() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/esgi/views/TrackerView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) pianoContainer.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/fr/esgi/styles/tracker-style.css").toExternalForm());
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la vue Tracker");
        }
    }

    @FXML
    private void showPiano() {
        System.out.println("Déjà sur la vue Piano");
    }
}