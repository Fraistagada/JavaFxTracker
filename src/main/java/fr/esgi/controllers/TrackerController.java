package fr.esgi.controllers;

import fr.esgi.constants.Constants;
import fr.esgi.constants.Effects;
import fr.esgi.constants.Instruments;
import fr.esgi.models.PatternRow;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.sound.midi.MidiUnavailableException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    private fr.esgi.service.MidiPlaybackService midiService;
    private fr.esgi.service.PersistenceService persistenceService;
    private fr.esgi.service.PatternService patternService;

    private int currentRowIndex = 0;

    // Currently selected row for editing
    private PatternRow selectedRow = null;
    private int selectedChannel = 1;

    // Flag to prevent listener loops
    private boolean isUpdatingFields = false;

    // ToggleGroup pour les boutons de canal
    private ToggleGroup channelToggleGroup;

    public TrackerController() {
    }

    public void setMidiPlaybackService(fr.esgi.service.MidiPlaybackService midiService) {
        this.midiService = midiService;
    }

    public void setPersistenceService(fr.esgi.service.PersistenceService persistenceService) {
        this.persistenceService = persistenceService;
    }

    public void setPatternService(fr.esgi.service.PatternService patternService) {
        this.patternService = patternService;
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
                channel1Btn.setStyle("-fx-background-color: #00aa00; -fx-text-fill: white;");
                channel2Btn.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");
            } else {
                selectedChannel = 2;
                channel1Btn.setStyle("-fx-background-color: #555555; -fx-text-fill: white;");
                channel2Btn.setStyle("-fx-background-color: #0088aa; -fx-text-fill: white;");
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
    private void handlePlay() {
        if (midiService == null) {
            showError("Erreur MIDI", "Service MIDI non initialisé");
            return;
        }

        try {
            midiService.startPlayback(new ArrayList<>(patternTable.getItems()), currentRowIndex, bpm, new fr.esgi.service.PlaybackListener() {
                @Override
                public void onRowPlayed(int rowIndex) {
                    currentRowIndex = rowIndex;
                    javafx.application.Platform.runLater(() -> {
                        clearPlayingStyle();
                        TableRow<PatternRow> currentRow = getTableRow(currentRowIndex);
                        if (currentRow != null) currentRow.getStyleClass().add("playing");
                    });
                }

                @Override
                public void onPlaybackEnded() {
                    javafx.application.Platform.runLater(() -> {
                        endPlaybackCleanup();
                        currentRowIndex = 0;
                    });
                }
            });
        } catch (MidiUnavailableException e) {
            showError("Erreur MIDI", "Impossible de démarrer la lecture: " + e.getMessage());
        }
    }

    @FXML
    private void handleStop() {
        if (midiService != null) midiService.stop();
        currentRowIndex = 0;
        endPlaybackCleanup();
    }

    @FXML
    private void handlePause() {
        if (midiService != null) midiService.pause();
    }

    private void endPlaybackCleanup() {
        javafx.application.Platform.runLater(this::clearPlayingStyle);
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

    private void clearPlayingStyle() {
        for (int j = 0; j < patternTable.getItems().size(); j++) {
            TableRow<PatternRow> row = getTableRow(j);
            if (row != null) row.getStyleClass().remove("playing");
        }
    }

    public void cleanup() {
        if (midiService != null) midiService.cleanup();
        javafx.application.Platform.runLater(this::clearPlayingStyle);
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

            Object controller = loader.getController();
            if (controller instanceof fr.esgi.controllers.PianoController) {
                fr.esgi.controllers.PianoController pc = (fr.esgi.controllers.PianoController) controller;
                pc.setPatternService(patternService);
                pc.setPersistenceService(persistenceService);
                pc.setMidiPlaybackService(midiService);
            }

            Stage stage = (Stage) patternTable.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/fr/esgi/styles/tracker-style.css").toExternalForm());
            stage.setScene(scene);

        } catch (IOException e) {
            showError("Erreur de chargement", "Impossible de charger la vue Piano: " + e.getMessage());
        }
    }

    // Nouvelle méthode : afficher la vue Crédits
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

            Stage stage = (Stage) patternTable.getScene().getWindow();
            Scene scene = new Scene(root, 1000, 600);
            scene.getStylesheets().add(getClass().getResource("/fr/esgi/styles/tracker-style.css").toExternalForm());
            stage.setScene(scene);

        } catch (IOException e) {
            showError("Erreur de chargement", "Impossible de charger la vue Crédits: " + e.getMessage());
        }
    }

    @FXML
    private void downloadMidi() {
        if (persistenceService == null) {
            showError("Erreur d'export", "Service de persistance non initialisé");
            return;
        }

        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Exporter en MIDI");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichier MIDI (*.mid)", "*.mid")
            );

            File file = chooser.showSaveDialog(patternTable.getScene().getWindow());
            if (file == null) return;

            persistenceService.exportMidi(file, new ArrayList<>(patternTable.getItems()), bpm);

        } catch (Exception e) {
            showError("Erreur d'export", "Impossible d'exporter le fichier MIDI: " + e.getMessage());
        }
    }

    @FXML
    private void handleSave() {
        if (persistenceService == null) {
            showError("Erreur d'écriture", "Service de persistance non initialisé");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer la piste");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier Tracker (*.trk)", "*.trk")
        );

        File file = chooser.showSaveDialog(patternTable.getScene().getWindow());
        if (file == null) return;

        try {
            persistenceService.savePattern(file, new ArrayList<>(patternTable.getItems()), bpm, (int) tempoSlider.getValue());
        } catch (Exception e) {
            showError("Erreur d'écriture", "Impossible d'enregistrer la piste: " + e.getMessage());
        }
    }

    @FXML
    private void handleLoad() {
        if (persistenceService == null) {
            showError("Erreur de lecture", "Service de persistance non initialisé");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Charger une piste");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichier Tracker (*.trk)", "*.trk")
        );

        File file = chooser.showOpenDialog(patternTable.getScene().getWindow());
        if (file == null) return;

        try {
            fr.esgi.service.PatternLoadResult result = persistenceService.loadPattern(file);
            patternTable.getItems().clear();
            patternTable.getItems().addAll(result.pattern);

            bpm = result.bpm;
            bpmLabel.setText("BPM: " + bpm);

            tempoSlider.setValue(result.tempo);
            tempoLabel.setText("Tempo: " + result.tempo);

            selectedRow = null;
            setEditPanelEnabled(false);
            selectedRowLabel.setText("Ligne: --");

        } catch (Exception e) {
            showError("Erreur de lecture", "Impossible de charger la piste: " + e.getMessage());
        }
    }
}

