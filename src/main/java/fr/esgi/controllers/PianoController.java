package fr.esgi.controllers;

import fr.esgi.constants.Constants;
import fr.esgi.service.MidiPlaybackService;
import fr.esgi.service.PatternService;
import fr.esgi.service.PersistenceService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import static fr.esgi.utils.FxUtils.showError;

public class PianoController {

    @FXML
    private HBox pianoContainer;
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

    private int currentPattern = 0;
    private int bpm = Constants.DEFAULT_BPM;

    // MIDI - même pattern que TrackerController
    private final Synthesizer synth = MidiSystem.getSynthesizer();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    // Notes blanches et noires basées sur Constants
    private static final String[] WHITE_NOTES = {"C", "D", "E", "F", "G", "A", "B"};
    private static final boolean[] HAS_BLACK_KEY = {true, true, false, true, true, true, false};

    private MidiPlaybackService midiService;
    private PersistenceService persistenceService;
    private PatternService patternService;

    public PianoController() throws MidiUnavailableException {
    }

    public void setMidiPlaybackService(MidiPlaybackService midiService) {
        this.midiService = midiService;
    }

    public void setPersistenceService(PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    public void setPatternService(PatternService patternService) {
        this.patternService = patternService;
    }

    @FXML
    public void initialize() {
        try {
            synth.open();
        } catch (MidiUnavailableException e) {
            showError("Erreur MIDI", "Impossible d'initialiser le synthétiseur: " + e.getMessage());
        }

        createPiano();

        tempoSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            bpm = newVal.intValue();
            tempoLabel.setText("Tempo: " + bpm);
            bpmLabel.setText("BPM: " + bpm);
        });
    }

    private void createPiano() {
        // Octaves de 2 à 5 (4 octaves)
        for (int octave = 2; octave <= 5; octave++) {
            for (int i = 0; i < WHITE_NOTES.length; i++) {
                String whiteNote = WHITE_NOTES[i];
                String fullWhiteNote = whiteNote + octave;

                Pane keyPane = new Pane();
                keyPane.setPrefSize(45, 160);

                // Touche blanche
                Button whiteKey = createWhiteKey(fullWhiteNote, octave);
                whiteKey.setLayoutX(0);
                whiteKey.setLayoutY(0);
                keyPane.getChildren().add(whiteKey);

                // Touche noire (si elle existe)
                if (HAS_BLACK_KEY[i]) {
                    String blackNote = whiteNote + "#";
                    Button blackKey = createBlackKey(blackNote, octave);
                    blackKey.setLayoutX(28);
                    blackKey.setLayoutY(0);
                    keyPane.getChildren().add(blackKey);
                }

                pianoContainer.getChildren().add(keyPane);
            }
        }
    }

    private Button createWhiteKey(String noteWithOctave, int octave) {
        String note = noteWithOctave.substring(0, noteWithOctave.length() - 1);

        Button key = new Button();
        key.setPrefSize(44, 160);
        key.setStyle(getWhiteKeyStyle(false));
        key.setAlignment(Pos.BOTTOM_CENTER);
        key.setText(noteWithOctave);

        key.setOnMousePressed(e -> {
            key.setStyle(getWhiteKeyStyle(true));
            playNote(note, octave);
        });

        key.setOnMouseReleased(e -> {
            key.setStyle(getWhiteKeyStyle(false));
            stopNote(note, octave);
        });

        key.setOnMouseExited(e -> {
            if (e.isPrimaryButtonDown()) {
                key.setStyle(getWhiteKeyStyle(false));
                stopNote(note, octave);
            }
        });

        return key;
    }

    private Button createBlackKey(String note, int octave) {
        Button key = new Button();
        key.setPrefSize(28, 100);
        key.setStyle(getBlackKeyStyle(false));

        key.setOnMousePressed(e -> {
            key.setStyle(getBlackKeyStyle(true));
            playNote(note, octave);
            e.consume();
        });

        key.setOnMouseReleased(e -> {
            key.setStyle(getBlackKeyStyle(false));
            stopNote(note, octave);
        });

        key.setOnMouseExited(e -> {
            if (e.isPrimaryButtonDown()) {
                key.setStyle(getBlackKeyStyle(false));
                stopNote(note, octave);
            }
        });

        return key;
    }

    private String getWhiteKeyStyle(boolean pressed) {
        String bgColor = pressed ? "#cccccc" : "white";
        return "-fx-background-color: " + bgColor + "; " +
                "-fx-border-color: #333333; " +
                "-fx-border-width: 1; " +
                "-fx-text-fill: #333333; " +
                "-fx-font-family: monospace; " +
                "-fx-font-size: 9; " +
                "-fx-background-radius: 0 0 5 5; " +
                "-fx-border-radius: 0 0 5 5;";
    }

    private String getBlackKeyStyle(boolean pressed) {
        String bgColor = pressed ? "#444444" : "#1a1a1a";
        return "-fx-background-color: " + bgColor + "; " +
                "-fx-border-color: #000000; " +
                "-fx-border-width: 1; " +
                "-fx-text-fill: white; " +
                "-fx-font-family: monospace; " +
                "-fx-font-size: 8; " +
                "-fx-background-radius: 0 0 3 3; " +
                "-fx-border-radius: 0 0 3 3;";
    }

    /**
     * Convertit une note en numéro MIDI - même méthode que TrackerController
     */
    private static int noteToMidi(String note, int octave) {
        return 12 * (octave + 1) + Constants.NOTES.get(note);
    }

    private void playNote(String note, int octave) {
        if (!synth.isOpen()) return;

        MidiChannel channel = synth.getChannels()[0];
        int noteMidi = noteToMidi(note, octave);
        int velocity = 100;

        channel.noteOn(noteMidi, velocity);
        System.out.println("Note jouée: " + note + octave + " (MIDI: " + noteMidi + ")");
    }

    private void stopNote(String note, int octave) {
        if (!synth.isOpen()) return;

        MidiChannel channel = synth.getChannels()[0];
        int noteMidi = noteToMidi(note, octave);
        channel.noteOff(noteMidi);
    }

    @FXML
    private void handlePlay() {
        System.out.println("PLAY - Lecture du pattern " + currentPattern);
    }

    @FXML
    private void handleStop() {
        System.out.println("STOP - Arrêt de la lecture");
        if (synth.isOpen()) {
            for (MidiChannel channel : synth.getChannels()) {
                channel.allNotesOff();
            }
        }
    }

    @FXML
    private void handlePause() {
        System.out.println("PAUSE - Mise en pause");
    }

    @FXML
    private void handleRecord() {
        System.out.println("REC - Mode enregistrement activé");
    }

    @FXML
    private void showTracker() {
        cleanup();
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

            Stage stage = (Stage) pianoContainer.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/fr/esgi/styles/tracker-style.css").toExternalForm());
            stage.setScene(scene);

        } catch (IOException e) {
            showError("Erreur de chargement", "Impossible de charger la vue Tracker: " + e.getMessage());
        }
    }

    @FXML
    private void showPiano() {
        System.out.println("Déjà sur la vue Piano");
    }

    /**
     * Nouvelle méthode : afficher la vue Crédits
     */
    @FXML
    private void showCredits() {
        cleanup();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/esgi/views/CreditsView.fxml"));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof fr.esgi.controllers.CreditsController) {
                fr.esgi.controllers.CreditsController cc = (fr.esgi.controllers.CreditsController) controller;
                cc.setPatternService(patternService);
                cc.setPersistenceService(persistenceService);
                cc.setMidiPlaybackService(midiService);
            }

            Stage stage = (Stage) pianoContainer.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/fr/esgi/styles/tracker-style.css").toExternalForm());
            stage.setScene(scene);

        } catch (IOException e) {
            showError("Erreur de chargement", "Impossible de charger la vue Crédits: " + e.getMessage());
        }
    }

    /**
     * Nettoyage des ressources - à appeler lors de la fermeture
     */
    public void cleanup() {
        if (synth.isOpen()) {
            for (MidiChannel channel : synth.getChannels()) {
                channel.allNotesOff();
            }
            synth.close();
        }
        scheduler.shutdown();
    }
}