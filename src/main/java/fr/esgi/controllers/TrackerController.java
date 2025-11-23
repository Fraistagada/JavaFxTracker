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

import javax.sound.midi.*;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static fr.esgi.utils.FxUtils.showError;

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
    private Button downloadMidi;

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
            showError("Erreur de chargement", "Impossible de charger la piste.");
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
            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/fr/esgi/styles/tracker-style.css").toExternalForm());
            stage.setScene(scene);

        } catch (IOException e) {
            showError(
                    "Erreur de chargement",
                    "Impossible de charger la vue Piano."
            );
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

    @FXML
    private void downloadMidi() {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Exporter en MIDI");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichier MIDI (*.mid)", "*.mid")
            );

            File file = chooser.showSaveDialog(patternTable.getScene().getWindow());
            if (file == null) return;

            Sequence sequence = new Sequence(Sequence.PPQ, 480);
            Track track = sequence.createTrack();

            int tick = 0;
            int tickPerRow = 120;

            for (PatternRow row : patternTable.getItems()) {
                if (!row.getNote().equals("---")) {
                    int note = noteToMidi(row.getNote(), Integer.parseInt(row.getOctave()));

                    track.add(new MidiEvent(
                            new ShortMessage(ShortMessage.NOTE_ON, 0, note, 100),
                            tick
                    ));

                    track.add(new MidiEvent(
                            new ShortMessage(ShortMessage.NOTE_OFF, 0, note, 100),
                            tick + tickPerRow
                    ));
                }

                tick += tickPerRow;
            }

            MidiSystem.write(sequence, 1, file);

            System.out.println("MIDI exporté : " + file.getAbsolutePath());

        } catch (Exception e) {
            showError("Erreur d'export", "Impossible d'exporter le fichier MIDI.");
        }
    }

    @FXML
    private void handleSave() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer la piste");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier Tracker (*.trk)", "*.trk")
        );

        File file = chooser.showSaveDialog(patternTable.getScene().getWindow());
        if (file == null) return;

        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {

            out.println("BPM=" + bpm);
            out.println("ROW;NOTE;OCT;INST;VOL;FX");

            for (PatternRow row : patternTable.getItems()) {
                out.printf("%s;%s;%s;%s;%s;%s%n",
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

            showError("Erreur d'écriture", "Impossible d'enregistrer la piste.");
        }
    }


    @FXML
    private void handleLoad() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Charger une piste");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier Tracker (*.trk)", "*.trk")
        );

        File file = chooser.showOpenDialog(patternTable.getScene().getWindow());
        if (file == null) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            patternTable.getItems().clear();
            String line;

            line = reader.readLine();
            if (line != null && line.startsWith("BPM=")) {
                bpm = Integer.parseInt(line.substring(4));
                bpmLabel.setText("BPM: " + bpm);
            }

            line = reader.readLine();
            if (line != null && line.startsWith("TEMPO=")) {
                int t = Integer.parseInt(line.substring(6));
                tempoSlider.setValue(t);
                tempoLabel.setText("Tempo: " + t);
            }

            line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] p = line.split(";");
                if (p.length == 6) {
                    patternTable.getItems().add(
                            new PatternRow(p[0], p[1], p[2], p[3], p[4], p[5])
                    );
                }
            }

            System.out.println("Piste chargée : " + file.getAbsolutePath());

        } catch (IOException e) {
            showError("Erreur de lecture", "Impossible de charger la piste.");
        }
    }

}