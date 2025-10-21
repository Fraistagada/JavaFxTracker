package fr.esgi.controllers;

import fr.esgi.models.PatternRow;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class TrackerController {

    @FXML private TableView<PatternRow> patternTable;
    @FXML private TableColumn<PatternRow, String> rowColumn;
    @FXML private TableColumn<PatternRow, String> noteColumn;
    @FXML private TableColumn<PatternRow, String> octaveColumn;
    @FXML private TableColumn<PatternRow, String> instrumentColumn;
    @FXML private TableColumn<PatternRow, String> volumeColumn;
    @FXML private TableColumn<PatternRow, String> effectColumn;

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
        // Configuration des colonnes du tableau
        rowColumn.setCellValueFactory(data -> data.getValue().rowProperty());
        noteColumn.setCellValueFactory(data -> data.getValue().noteProperty());
        octaveColumn.setCellValueFactory(data -> data.getValue().octaveProperty());
        instrumentColumn.setCellValueFactory(data -> data.getValue().instrumentProperty());
        volumeColumn.setCellValueFactory(data -> data.getValue().volumeProperty());
        effectColumn.setCellValueFactory(data -> data.getValue().effectProperty());

        // Remplir le pattern avec 64 lignes vides
        for (int i = 0; i < 64; i++) {
            patternTable.getItems().add(new PatternRow(
                    String.format("%02d", i), "---", "-", "--", "--", "---"
            ));
        }

        // Ajouter quelques notes d'exemple
        patternTable.getItems().get(0).setNote("C-4");
        patternTable.getItems().get(0).setInstrument("01");
        patternTable.getItems().get(0).setVolume("40");

        patternTable.getItems().get(4).setNote("E-4");
        patternTable.getItems().get(4).setInstrument("01");
        patternTable.getItems().get(4).setVolume("40");

        patternTable.getItems().get(8).setNote("G-4");
        patternTable.getItems().get(8).setInstrument("01");
        patternTable.getItems().get(8).setVolume("40");

        patternTable.getItems().get(12).setNote("C-5");
        patternTable.getItems().get(12).setInstrument("01");
        patternTable.getItems().get(12).setVolume("40");

        // Configuration du slider tempo
        tempoSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            bpm = newVal.intValue();
            tempoLabel.setText("Tempo: " + bpm);
            bpmLabel.setText("BPM: " + bpm);
        });

        // Style personnalisé pour le tableau
        patternTable.setStyle(
                "-fx-font-family: 'Monospace';" +
                        "-fx-font-size: 12px;" +
                        "-fx-background-color: #000000;" +
                        "-fx-control-inner-background: #000000;"
        );
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
        System.out.println("Déjà sur la vue Tracker");
    }

    @FXML
    private void showPiano() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/esgi/views/PianoView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) patternTable.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/fr/esgi/styles/tracker-style.css").toExternalForm());
            stage.setScene(scene);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Erreur lors du chargement de la vue Piano");
        }
    }
}