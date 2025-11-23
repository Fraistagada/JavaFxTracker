package fr.esgi.controllers;

import fr.esgi.constants.Constants;
import fr.esgi.models.PatternRow;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TrackerController {

    @FXML
    private TableView<PatternRow> patternTable;
    @FXML
    private TableColumn<PatternRow, String> rowColumn;
    @FXML
    private TableColumn<PatternRow, String> noteColumn;
    @FXML
    private TableColumn<PatternRow, String> octaveColumn;
    @FXML
    private TableColumn<PatternRow, String> instrumentColumn;
    @FXML
    private TableColumn<PatternRow, String> volumeColumn;
    @FXML
    private TableColumn<PatternRow, String> effectColumn;

    @FXML
    private Label patternLabel;
    @FXML
    private Label bpmLabel;
    @FXML
    private Label tempoLabel;
    @FXML
    private Slider tempoSlider;

    @FXML
    private Button playBtn;
    @FXML
    private Button stopBtn;
    @FXML
    private Button pauseBtn;
    @FXML
    private Button recordBtn;

    private int bpm = Constants.DEFAULT_BPM;
    private final Synthesizer synth = MidiSystem.getSynthesizer();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private Thread playThread;
    private volatile boolean isPlaying = false;
    private volatile boolean isPaused = false;
    private int currentRowIndex = 0;

    public TrackerController() throws MidiUnavailableException {
    }

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
        for (int i = 0; i < Constants.PATTERN_LENGTH; i++) {
            patternTable.getItems().add(new PatternRow(
                    String.format("%02d", i), "---", "-", "--", "--", "---"
            ));
        }

        // Ajouter quelques notes d'exemple
        patternTable.getItems().getFirst().setSound("C", "4");
        patternTable.getItems().getFirst().setInstrument("01");
        patternTable.getItems().getFirst().setVolume("40");

        patternTable.getItems().get(4).setSound("E", "4");
        patternTable.getItems().get(4).setInstrument("01");
        patternTable.getItems().get(4).setVolume("40");

        patternTable.getItems().get(8).setSound("G", "4");
        patternTable.getItems().get(8).setInstrument("01");
        patternTable.getItems().get(8).setVolume("40");

        patternTable.getItems().get(12).setSound("C", "5");
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
    private void handlePlay() throws MidiUnavailableException {
        if (isPlaying && !isPaused) {
            return;
        }

        if (isPaused) {
            isPaused = false;
            return;
        }

        isPlaying = true;
        isPaused = false;
        synth.open();

        playThread = new Thread(() -> {
            try {
                for (int i = currentRowIndex; i < patternTable.getItems().size() && isPlaying; i++) {

                    while (isPaused) {
                        Thread.sleep(50);
                    }

                    currentRowIndex = i;
                    PatternRow row = patternTable.getItems().get(i);

                    if (!row.getNote().equals("---")) {
                        playSample(row.getNote(), row.getOctave(), row.getInstrument());
                    }

                    javafx.application.Platform.runLater(() -> {
                        clearPlayingStyle();
                        TableRow<PatternRow> currentRow = getTableRow(currentRowIndex);
                        if (currentRow != null) currentRow.getStyleClass().add("playing");
                    });

                    int delay = (int) ((60.0 / bpm) * 1000 / 4);
                    Thread.sleep(delay);
                }

            } catch (InterruptedException ignored) {
                // Thread interrompu = arrêt de lecture
            } finally {
                if (!isPaused) {
                    endPlaybackCleanup();
                    currentRowIndex = 0;
                }
            }
        });

        playThread.start();
    }

    // Méthode utilitaire pour récupérer une TableRow
    private TableRow<PatternRow> getTableRow(int index) {
        for (javafx.scene.Node node : patternTable.lookupAll(".table-row-cell")) {
            if (node instanceof TableRow) {
                @SuppressWarnings("unchecked")
                TableRow<PatternRow> row = (TableRow<PatternRow>) node;
                if (row.getIndex() == index) {
                    return row;
                }
            }
        }
        return null;
    }

    @FXML
    private void handleStop() {
        System.out.println("STOP - Arrêt de la lecture");

        isPlaying = false;
        isPaused = false;
        currentRowIndex = 0;

        if (playThread != null && playThread.isAlive()) {
            playThread.interrupt();
        }

        endPlaybackCleanup();
    }


    @FXML
    private void handlePause() {
        System.out.println("PAUSE - Mise en pause");
        isPaused = true;

        if (synth.isOpen()) {
            for (MidiChannel channel : synth.getChannels()) {
                channel.allNotesOff();
            }
        }
    }

    @FXML
    private void handleRecord() {
        System.out.println("REC - Mode enregistrement activé");

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer la piste");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Tracker File (*.trk)", "*.trk")
        );
        File file = fileChooser.showSaveDialog(patternTable.getScene().getWindow());
        if (file != null) {
            savePatternToFile(file);
        }
    }

    private void playSample(String note, String octave, String instrument) {
        // TODO : choisir l'instrument
        MidiChannel channel = synth.getChannels()[0];
        int noteMidi = noteToMidi(note, Integer.parseInt(octave));

        // TODO : Mettre la durée en paramètre
        int duration = 5000;
        channel.noteOn(noteMidi, Constants.VELOCITY);

        // Planifier le noteOff sans bloquer le thread principal
        scheduler.schedule(() -> channel.noteOff(noteMidi), duration, TimeUnit.MILLISECONDS);
    }

    private static int noteToMidi(String note, int octave) {
        return 12 * (octave + 1) + Constants.NOTES.get(note);
    }


    private void pausePlayback() {
        System.out.println("Lecture mise en pause (simulation)");
    }

    private void savePatternToFile(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            for (PatternRow row : patternTable.getItems()) {
                writer.printf("%s;%s;%s;%s;%s;%s%n",
                        row.getRow(),
                        row.getNote(),
                        row.getOctave(),
                        row.getInstrument(),
                        row.getVolume(),
                        row.getEffect()
                );
            }
            System.out.println("Piste enregistrée : " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleOpenFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Ouvrir une piste");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Tracker File (*.trk)", "*.trk")
        );
        File file = fileChooser.showOpenDialog(patternTable.getScene().getWindow());
        if (file != null) {
            loadPatternFromFile(file);
        }
    }

    private void loadPatternFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            patternTable.getItems().clear();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 6) {
                    patternTable.getItems().add(new PatternRow(
                            parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]
                    ));
                }
            }
            System.out.println("Piste chargée : " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void endPlaybackCleanup() {
        javafx.application.Platform.runLater(this::clearPlayingStyle);

        if (synth.isOpen()) {
            for (MidiChannel channel : synth.getChannels()) {
                channel.allNotesOff();
            }
            synth.close();
        }

        isPlaying = false;
    }

    private void clearPlayingStyle() {
        for (int j = 0; j < patternTable.getItems().size(); j++) {
            TableRow<PatternRow> row = getTableRow(j);
            if (row != null) row.getStyleClass().remove("playing");
        }
    }

}