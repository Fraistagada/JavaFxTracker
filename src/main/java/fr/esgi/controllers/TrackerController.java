package fr.esgi.controllers;

import fr.esgi.constants.Constants;
import fr.esgi.constants.Effects;
import fr.esgi.constants.Instruments;
import fr.esgi.models.PatternRow;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;

import javax.sound.midi.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static fr.esgi.utils.FxUtils.showError;

public class TrackerController {

    // Table components
    @FXML
    private TableView<PatternRow> patternTable;
    @FXML
    private TableColumn<PatternRow, String> rowColumn;

    // Canal 1
    @FXML
    private TableColumn<PatternRow, String> note1Column;
    @FXML
    private TableColumn<PatternRow, String> octave1Column;
    @FXML
    private TableColumn<PatternRow, String> instrument1Column;
    @FXML
    private TableColumn<PatternRow, String> volume1Column;
    @FXML
    private TableColumn<PatternRow, String> effect1Column;

    // Separation
    @FXML
    private TableColumn<PatternRow, String> separatorColumn;

    // Canal 2
    @FXML
    private TableColumn<PatternRow, String> note2Column;
    @FXML
    private TableColumn<PatternRow, String> octave2Column;
    @FXML
    private TableColumn<PatternRow, String> instrument2Column;
    @FXML
    private TableColumn<PatternRow, String> volume2Column;
    @FXML
    private TableColumn<PatternRow, String> effect2Column;

    // Header labels
    @FXML
    private Label patternLabel;
    @FXML
    private Label bpmLabel;
    @FXML
    private Label tempoLabel;
    @FXML
    private Slider tempoSlider;

    // Control buttons
    @FXML
    private Button playBtn;
    @FXML
    private Button stopBtn;
    @FXML
    private Button pauseBtn;
    @FXML
    private Button downloadMidi;

    // Edit panel components
    @FXML
    private VBox editPanel;
    @FXML
    private Label selectedRowLabel;
    @FXML
    private ToggleButton channel1Btn;
    @FXML
    private ToggleButton channel2Btn;
    @FXML
    private ComboBox<String> noteCombo;
    @FXML
    private ComboBox<String> octaveCombo;
    @FXML
    private ComboBox<String> instrumentCombo;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Label volumeValueLabel;
    @FXML
    private ComboBox<String> effectTypeCombo;
    @FXML
    private TextField effectParamField;
    @FXML
    private Label effectDescLabel;

    private int bpm = Constants.DEFAULT_BPM;
    private final Synthesizer synth = MidiSystem.getSynthesizer();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private Thread playThread;
    private volatile boolean isPlaying = false;
    private volatile boolean isPaused = false;
    private int currentRowIndex = 0;

    // Currently selected row for editing
    private PatternRow selectedRow = null;
    private int selectedChannel = 1;

    // Flag to prevent listener loops
    private boolean isUpdatingFields = false;

    // ToggleGroup pour les boutons de canal
    private ToggleGroup channelToggleGroup;

    public TrackerController() throws MidiUnavailableException {
    }

    @FXML
    public void initialize() {
        // Configuration des colonnes du tableau
        rowColumn.setCellValueFactory(data -> data.getValue().rowProperty());

        // Canal 1
        note1Column.setCellValueFactory(data -> data.getValue().note1Property());
        octave1Column.setCellValueFactory(data -> data.getValue().octave1Property());
        instrument1Column.setCellValueFactory(data -> data.getValue().instrument1Property());
        volume1Column.setCellValueFactory(data -> data.getValue().volume1Property());
        effect1Column.setCellValueFactory(data -> data.getValue().effect1Property());

        // Colonne séparatrice (vide)
        separatorColumn.setCellValueFactory(data -> new SimpleStringProperty(""));
        separatorColumn.setSortable(false);
        separatorColumn.setReorderable(false);

        // Canal 2
        note2Column.setCellValueFactory(data -> data.getValue().note2Property());
        octave2Column.setCellValueFactory(data -> data.getValue().octave2Property());
        instrument2Column.setCellValueFactory(data -> data.getValue().instrument2Property());
        volume2Column.setCellValueFactory(data -> data.getValue().volume2Property());
        effect2Column.setCellValueFactory(data -> data.getValue().effect2Property());

        // Remplir le pattern avec 64 lignes vides
        for (int i = 0; i < Constants.PATTERN_LENGTH; i++) {
            patternTable.getItems().add(new PatternRow(String.format("%02d", i)));
        }

        // Ajouter quelques notes d'exemple sur le canal 1
        patternTable.getItems().get(0).setSound(1, "C", "4");
        patternTable.getItems().get(0).setInstrument(1, "00");
        patternTable.getItems().get(0).setVolume(1, "100");

        patternTable.getItems().get(4).setSound(1, "E", "4");
        patternTable.getItems().get(4).setInstrument(1, "00");
        patternTable.getItems().get(4).setVolume(1, "80");

        patternTable.getItems().get(8).setSound(1, "G", "4");
        patternTable.getItems().get(8).setInstrument(1, "00");
        patternTable.getItems().get(8).setVolume(1, "80");

        patternTable.getItems().get(12).setSound(1, "C", "5");
        patternTable.getItems().get(12).setInstrument(1, "00");
        patternTable.getItems().get(12).setVolume(1, "80");

        // Ajouter quelques notes d'exemple sur le canal 2 (basse)
        patternTable.getItems().get(0).setSound(2, "C", "2");
        patternTable.getItems().get(0).setInstrument(2, "32");
        patternTable.getItems().get(0).setVolume(2, "100");

        patternTable.getItems().get(8).setSound(2, "G", "2");
        patternTable.getItems().get(8).setInstrument(2, "32");
        patternTable.getItems().get(8).setVolume(2, "100");

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

        // Initialiser le panneau d'édition
        initEditPanel();

        // Listener pour la sélection de ligne
        patternTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectRowForEdit(newVal);
            }
        });
    }

    private void initEditPanel() {
        // Configuration des boutons de canal
        channelToggleGroup = new ToggleGroup();
        channel1Btn.setToggleGroup(channelToggleGroup);
        channel2Btn.setToggleGroup(channelToggleGroup);
        channel1Btn.setSelected(true);

        // Listener pour le changement de canal
        channelToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == channel1Btn) {
                selectedChannel = 1;
                channel1Btn.setStyle("-fx-background-color: #00aa00; -fx-text-fill: white; -fx-font-family: monospace;");
                channel2Btn.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-family: monospace;");
            } else {
                selectedChannel = 2;
                channel1Btn.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-family: monospace;");
                channel2Btn.setStyle("-fx-background-color: #0088aa; -fx-text-fill: white; -fx-font-family: monospace;");
            }

            // Recharger les valeurs pour le canal sélectionné
            if (selectedRow != null && !isUpdatingFields) {
                loadChannelValues();
            }
        });

        // Notes disponibles
        List<String> notes = new ArrayList<>();
        notes.add("---");
        notes.addAll(Constants.NOTES.keySet().stream().sorted((a, b) -> {
            return Constants.NOTES.get(a) - Constants.NOTES.get(b);
        }).toList());
        noteCombo.setItems(FXCollections.observableArrayList(notes));
        noteCombo.setValue("---");

        // Octaves (1-8)
        List<String> octaves = new ArrayList<>();
        octaves.add("-");
        for (int i = 1; i <= 8; i++) {
            octaves.add(String.valueOf(i));
        }
        octaveCombo.setItems(FXCollections.observableArrayList(octaves));
        octaveCombo.setValue("-");

        // Instruments General MIDI
        List<String> instruments = new ArrayList<>();
        instruments.add("--");
        for (String id : Instruments.GENERAL_MIDI.keySet()) {
            instruments.add(id + " - " + Instruments.getName(id));
        }
        instrumentCombo.setItems(FXCollections.observableArrayList(instruments));
        instrumentCombo.setValue("--");

        // Effets
        List<String> effects = new ArrayList<>();
        effects.add("---");
        for (String code : Effects.EFFECTS.keySet()) {
            effects.add(code + " - " + Effects.EFFECTS.get(code).split(" - ")[0]);
        }
        effectTypeCombo.setItems(FXCollections.observableArrayList(effects));
        effectTypeCombo.setValue("---");
        effectParamField.setText("00");

        // === LISTENERS TEMPS RÉEL ===

        noteCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingFields && selectedRow != null && newVal != null) {
                applyNoteChange(newVal);
            }
        });

        octaveCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingFields && selectedRow != null && newVal != null) {
                selectedRow.setSound(selectedChannel, selectedRow.getNote(selectedChannel), newVal);
                patternTable.refresh();
            }
        });

        instrumentCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingFields && selectedRow != null && newVal != null) {
                String instrumentId = extractInstrumentId(newVal);
                selectedRow.setInstrument(selectedChannel, instrumentId);
                patternTable.refresh();
            }
        });

        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int vol = newVal.intValue();
            volumeValueLabel.setText(String.valueOf(vol));

            if (!isUpdatingFields && selectedRow != null) {
                if (selectedRow.getNote(selectedChannel).equals("---")) {
                    selectedRow.setVolume(selectedChannel, "--");
                } else {
                    selectedRow.setVolume(selectedChannel, String.valueOf(vol));
                }
                patternTable.refresh();
            }
        });

        effectTypeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingFields && selectedRow != null && newVal != null) {
                updateEffect();
                updateEffectDescription();
            }
        });

        effectParamField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!isUpdatingFields && selectedRow != null) {
                if (newVal != null && newVal.length() > 2) {
                    effectParamField.setText(newVal.substring(0, 2));
                    return;
                }
                updateEffect();
            }
        });

        setEditPanelEnabled(false);
    }

    private void loadChannelValues() {
        isUpdatingFields = true;

        noteCombo.setValue(selectedRow.getNote(selectedChannel));
        octaveCombo.setValue(selectedRow.getOctave(selectedChannel));
        instrumentCombo.setValue(getInstrumentDisplay(selectedRow.getInstrument(selectedChannel)));
        effectTypeCombo.setValue(getEffectDisplay(selectedRow.getEffect(selectedChannel)));
        effectParamField.setText(getEffectParams(selectedRow.getEffect(selectedChannel)));
        updateEffectDescription();

        try {
            int vol = Integer.parseInt(selectedRow.getVolume(selectedChannel));
            volumeSlider.setValue(vol);
        } catch (NumberFormatException e) {
            volumeSlider.setValue(100);
        }

        isUpdatingFields = false;
    }

    private void applyNoteChange(String newNote) {
        if (newNote.equals("---")) {
            selectedRow.clearChannel(selectedChannel);

            isUpdatingFields = true;
            octaveCombo.setValue("-");
            instrumentCombo.setValue("--");
            volumeSlider.setValue(100);
            effectTypeCombo.setValue("---");
            effectParamField.setText("00");
            effectDescLabel.setText("");
            isUpdatingFields = false;
        } else {
            if (selectedRow.getNote(selectedChannel).equals("---")) {
                isUpdatingFields = true;
                octaveCombo.setValue("4");
                instrumentCombo.setValue("00 - Acoustic Grand Piano");
                volumeSlider.setValue(100);
                effectTypeCombo.setValue("---");
                effectParamField.setText("00");
                isUpdatingFields = false;

                selectedRow.setSound(selectedChannel, newNote, "4");
                selectedRow.setInstrument(selectedChannel, "00");
                selectedRow.setVolume(selectedChannel, "100");
                selectedRow.setEffect(selectedChannel, "---");
            } else {
                selectedRow.setSound(selectedChannel, newNote, selectedRow.getOctave(selectedChannel));
            }
        }
        patternTable.refresh();
    }

    private void updateEffect() {
        if (selectedRow == null) return;

        String effectType = effectTypeCombo.getValue();
        String param = effectParamField.getText();

        if (effectType == null || effectType.equals("---")) {
            selectedRow.setEffect(selectedChannel, "---");
        } else {
            String code = effectType.split(" - ")[0];
            String formattedParam = (param == null || param.isEmpty()) ? "00" : param.toUpperCase();
            if (formattedParam.length() == 1) {
                formattedParam = "0" + formattedParam;
            }
            selectedRow.setEffect(selectedChannel, code + formattedParam);
        }
        patternTable.refresh();
    }

    private void updateEffectDescription() {
        String effectType = effectTypeCombo.getValue();
        if (effectType == null || effectType.equals("---")) {
            effectDescLabel.setText("");
        } else {
            String code = effectType.split(" - ")[0];
            effectDescLabel.setText(Effects.getDescription(code));
        }
    }

    private void setEditPanelEnabled(boolean enabled) {
        channel1Btn.setDisable(!enabled);
        channel2Btn.setDisable(!enabled);
        noteCombo.setDisable(!enabled);
        octaveCombo.setDisable(!enabled);
        instrumentCombo.setDisable(!enabled);
        volumeSlider.setDisable(!enabled);
        effectTypeCombo.setDisable(!enabled);
        effectParamField.setDisable(!enabled);
    }

    private void selectRowForEdit(PatternRow row) {
        selectedRow = row;
        selectedRowLabel.setText("Ligne: " + row.getRow());
        loadChannelValues();
        setEditPanelEnabled(true);
    }

    private String extractInstrumentId(String instrumentDisplay) {
        if (instrumentDisplay == null || instrumentDisplay.equals("--")) {
            return "--";
        }
        int dashIndex = instrumentDisplay.indexOf(" - ");
        if (dashIndex > 0) {
            return instrumentDisplay.substring(0, dashIndex);
        }
        return instrumentDisplay;
    }

    private String getInstrumentDisplay(String instrumentId) {
        if (instrumentId == null || instrumentId.equals("--")) {
            return "--";
        }
        String name = Instruments.getName(instrumentId);
        return instrumentId + " - " + name;
    }

    private String getEffectDisplay(String effectCode) {
        if (effectCode == null || effectCode.equals("---") || effectCode.isEmpty()) {
            return "---";
        }
        String code = effectCode.substring(0, 1).toUpperCase();
        for (String key : Effects.EFFECTS.keySet()) {
            if (key.equals(code)) {
                return code + " - " + Effects.EFFECTS.get(key).split(" - ")[0];
            }
        }
        return "---";
    }

    private String getEffectParams(String effectCode) {
        if (effectCode == null || effectCode.equals("---") || effectCode.length() < 2) {
            return "00";
        }
        return effectCode.substring(1);
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

                    // Jouer les deux canaux simultanément
                    if (row.hasNote(1)) {
                        playSample(1, row.getNote(1), row.getOctave(1), row.getInstrument(1),
                                Integer.parseInt(row.getVolume(1)), row.getEffect(1));
                    }

                    if (row.hasNote(2)) {
                        playSample(2, row.getNote(2), row.getOctave(2), row.getInstrument(2),
                                Integer.parseInt(row.getVolume(2)), row.getEffect(2));
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
            } finally {
                if (!isPaused) {
                    endPlaybackCleanup();
                    currentRowIndex = 0;
                }
            }
        });

        playThread.start();
    }

    private void playSample(int channel, String note, String octave, String instrument, int volume, String effect) {
        // Utiliser un canal MIDI différent pour chaque piste (0 et 1)
        MidiChannel midiChannel = synth.getChannels()[channel - 1];

        // Changer l'instrument
        int instrumentId = Instruments.getMidiId(instrument);
        midiChannel.programChange(instrumentId);

        int noteMidi = noteToMidi(note, Integer.parseInt(octave));
        int duration = (int) ((60.0 / bpm) * 1000);
        int vel = Math.max(0, Math.min(127, volume));

        // Appliquer les effets
        Effects.EffectData fx = Effects.parse(effect);

        if (!fx.isEmpty()) {
            switch (fx.type) {
                case "8" -> {
                    int pan = Math.min(127, fx.fullParam);
                    midiChannel.controlChange(10, pan);
                }
                case "C" -> vel = Math.min(127, fx.fullParam);
                case "F" -> {
                    if (fx.fullParam >= 0x20) {
                        final int newBpm = fx.fullParam;
                        javafx.application.Platform.runLater(() -> tempoSlider.setValue(newBpm));
                    }
                }
            }
        }

        midiChannel.noteOn(noteMidi, vel);

        if (!fx.isEmpty()) {
            switch (fx.type) {
                case "4" -> {
                    int speed = fx.param1;
                    int depth = fx.param2;
                    if (speed > 0 && depth > 0) {
                        applyVibrato(midiChannel, noteMidi, speed, depth, duration);
                    }
                }
                case "0" -> {
                    if (fx.param1 > 0 || fx.param2 > 0) {
                        applyArpeggio(midiChannel, noteMidi, fx.param1, fx.param2, vel, duration);
                        return;
                    }
                }
            }
        }

        scheduler.schedule(() -> midiChannel.noteOff(noteMidi), duration, TimeUnit.MILLISECONDS);
    }

    private void applyVibrato(MidiChannel channel, int note, int speed, int depth, int duration) {
        int steps = duration / 50;
        int currentDepth = depth * 100;

        for (int i = 0; i < steps; i++) {
            final int step = i;
            scheduler.schedule(() -> {
                double angle = (step * speed * Math.PI) / 8;
                int bend = (int) (8192 + Math.sin(angle) * currentDepth);
                bend = Math.max(0, Math.min(16383, bend));
                channel.setPitchBend(bend);
            }, i * 50L, TimeUnit.MILLISECONDS);
        }

        scheduler.schedule(() -> channel.setPitchBend(8192), duration, TimeUnit.MILLISECONDS);
    }

    private void applyArpeggio(MidiChannel channel, int baseNote, int semi1, int semi2, int velocity, int duration) {
        int[] notes = {baseNote, baseNote + semi1, baseNote + semi2};
        int stepDuration = duration / 6;

        for (int i = 0; i < 6; i++) {
            final int noteIndex = i % 3;
            final int currentNote = notes[noteIndex];

            scheduler.schedule(() -> {
                channel.allNotesOff();
                channel.noteOn(currentNote, velocity);
            }, i * stepDuration, TimeUnit.MILLISECONDS);
        }

        scheduler.schedule(() -> channel.noteOff(notes[0]), duration, TimeUnit.MILLISECONDS);
    }

    private static int noteToMidi(String note, int octave) {
        return 12 * (octave + 1) + Constants.NOTES.get(note);
    }

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
        isPaused = true;

        if (synth.isOpen()) {
            for (MidiChannel channel : synth.getChannels()) {
                channel.allNotesOff();
            }
        }
    }

    private void endPlaybackCleanup() {
        javafx.application.Platform.runLater(this::clearPlayingStyle);

        if (synth.isOpen()) {
            for (MidiChannel channel : synth.getChannels()) {
                channel.allNotesOff();
                channel.setPitchBend(8192);
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

    public void cleanup() {
        isPlaying = false;
        isPaused = false;

        if (playThread != null && playThread.isAlive()) {
            playThread.interrupt();
        }

        if (synth.isOpen()) {
            for (MidiChannel channel : synth.getChannels()) {
                channel.allNotesOff();
            }
            synth.close();
        }

        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(500, TimeUnit.MILLISECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @FXML
    private void showTracker() {
        System.out.println("Déjà sur la vue Tracker");
    }

    @FXML
    private void showPiano() {
        cleanup();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fr/esgi/views/PianoView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) patternTable.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/fr/esgi/styles/tracker-style.css").toExternalForm());
            stage.setScene(scene);

        } catch (IOException e) {
            showError("Erreur de chargement", "Impossible de charger la vue Piano: " + e.getMessage());
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
            Track track1 = sequence.createTrack();
            Track track2 = sequence.createTrack();

            int tick = 0;
            int tickPerRow = 120;

            for (PatternRow row : patternTable.getItems()) {
                // Canal 1
                if (row.hasNote(1)) {
                    int note = noteToMidi(row.getNote(1), Integer.parseInt(row.getOctave(1)));
                    int vel = Math.min(127, Integer.parseInt(row.getVolume(1)));

                    track1.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, note, vel), tick));
                    track1.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, note, 0), tick + tickPerRow));
                }

                // Canal 2
                if (row.hasNote(2)) {
                    int note = noteToMidi(row.getNote(2), Integer.parseInt(row.getOctave(2)));
                    int vel = Math.min(127, Integer.parseInt(row.getVolume(2)));

                    track2.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 1, note, vel), tick));
                    track2.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 1, note, 0), tick + tickPerRow));
                }

                tick += tickPerRow;
            }

            MidiSystem.write(sequence, 1, file);

        } catch (Exception e) {
            showError("Erreur d'export", "Impossible d'exporter le fichier MIDI: " + e.getMessage());
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
            out.println("TEMPO=" + (int) tempoSlider.getValue());
            out.println("CHANNELS=2");
            out.println("ROW;N1;O1;I1;V1;FX1;N2;O2;I2;V2;FX2");

            for (PatternRow row : patternTable.getItems()) {
                out.printf("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s%n",
                        row.getRow(),
                        row.getNote(1), row.getOctave(1), row.getInstrument(1), row.getVolume(1), row.getEffect(1),
                        row.getNote(2), row.getOctave(2), row.getInstrument(2), row.getVolume(2), row.getEffect(2)
                );
            }

        } catch (IOException e) {
            showError("Erreur d'écriture", "Impossible d'enregistrer la piste: " + e.getMessage());
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
                int tempo = Integer.parseInt(line.substring(6));
                tempoSlider.setValue(tempo);
                tempoLabel.setText("Tempo: " + tempo);
            }

            // Skip CHANNELS line
            line = reader.readLine();

            // Skip header line
            line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] p = line.split(";");
                if (p.length == 11) {
                    PatternRow row = new PatternRow(p[0],
                            p[1], p[2], p[3], p[4], p[5],
                            p[6], p[7], p[8], p[9], p[10]);
                    patternTable.getItems().add(row);
                }
            }

            selectedRow = null;
            setEditPanelEnabled(false);
            selectedRowLabel.setText("Ligne: --");

        } catch (IOException e) {
            showError("Erreur de lecture", "Impossible de charger la piste: " + e.getMessage());
        } catch (NumberFormatException e) {
            showError("Erreur de format", "Le fichier contient des données invalides: " + e.getMessage());
        }
    }
}