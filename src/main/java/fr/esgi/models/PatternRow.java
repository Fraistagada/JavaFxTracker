package fr.esgi.models;

import javafx.beans.property.SimpleStringProperty;

public class PatternRow {
    private final SimpleStringProperty row;
    private final SimpleStringProperty note;
    private final SimpleStringProperty octave;
    private final SimpleStringProperty instrument;
    private final SimpleStringProperty volume;
    private final SimpleStringProperty effect;

    public PatternRow(String row, String note, String octave,
                      String instrument, String volume, String effect) {
        this.row = new SimpleStringProperty(row);
        this.note = new SimpleStringProperty(note);
        this.octave = new SimpleStringProperty(octave);
        this.instrument = new SimpleStringProperty(instrument);
        this.volume = new SimpleStringProperty(volume);
        this.effect = new SimpleStringProperty(effect);
    }

    // Getters pour les properties (nécessaires pour JavaFX TableView)
    public SimpleStringProperty rowProperty() {
        return row;
    }

    public SimpleStringProperty noteProperty() {
        return note;
    }

    public SimpleStringProperty octaveProperty() {
        return octave;
    }

    public SimpleStringProperty instrumentProperty() {
        return instrument;
    }

    public SimpleStringProperty volumeProperty() {
        return volume;
    }

    public SimpleStringProperty effectProperty() {
        return effect;
    }

    // Getters standards
    public String getRow() {
        return row.get();
    }

    public String getNote() {
        return note.get();
    }

    public String getOctave() {
        return octave.get();
    }

    public String getInstrument() {
        return instrument.get();
    }

    public String getVolume() {
        return volume.get();
    }

    public String getEffect() {
        return effect.get();
    }

    // Setters
    public void setRow(String row) {
        this.row.set(row);
    }

    public void setSound(String note, String octave) {
        setNote(note);
        setOctave(octave);
    }

    private void setNote(String note) {
        this.note.set(note);
    }

    private void setOctave(String octave) {
        this.octave.set(octave);
    }

    public void setInstrument(String instrument) {
        this.instrument.set(instrument);
    }

    public void setVolume(String volume) {
        this.volume.set(volume);
    }

    public void setEffect(String effect) {
        this.effect.set(effect);
    }
}