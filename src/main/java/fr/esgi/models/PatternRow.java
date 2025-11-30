package fr.esgi.models;

import javafx.beans.property.SimpleStringProperty;

public class PatternRow {
    private final SimpleStringProperty row;

    // Canal 1
    private final SimpleStringProperty note1;
    private final SimpleStringProperty octave1;
    private final SimpleStringProperty instrument1;
    private final SimpleStringProperty volume1;
    private final SimpleStringProperty effect1;

    // Canal 2
    private final SimpleStringProperty note2;
    private final SimpleStringProperty octave2;
    private final SimpleStringProperty instrument2;
    private final SimpleStringProperty volume2;
    private final SimpleStringProperty effect2;

    public PatternRow(String row,
                      String note1, String octave1, String instrument1, String volume1, String effect1,
                      String note2, String octave2, String instrument2, String volume2, String effect2) {
        this.row = new SimpleStringProperty(row);

        this.note1 = new SimpleStringProperty(note1);
        this.octave1 = new SimpleStringProperty(octave1);
        this.instrument1 = new SimpleStringProperty(instrument1);
        this.volume1 = new SimpleStringProperty(volume1);
        this.effect1 = new SimpleStringProperty(effect1);

        this.note2 = new SimpleStringProperty(note2);
        this.octave2 = new SimpleStringProperty(octave2);
        this.instrument2 = new SimpleStringProperty(instrument2);
        this.volume2 = new SimpleStringProperty(volume2);
        this.effect2 = new SimpleStringProperty(effect2);
    }

    // Constructeur simplifié (canaux vides)
    public PatternRow(String row) {
        this(row, "---", "-", "--", "--", "---", "---", "-", "--", "--", "---");
    }

    // === Properties pour JavaFX TableView ===

    public SimpleStringProperty rowProperty() { return row; }

    // Canal 1
    public SimpleStringProperty note1Property() { return note1; }
    public SimpleStringProperty octave1Property() { return octave1; }
    public SimpleStringProperty instrument1Property() { return instrument1; }
    public SimpleStringProperty volume1Property() { return volume1; }
    public SimpleStringProperty effect1Property() { return effect1; }

    // Canal 2
    public SimpleStringProperty note2Property() { return note2; }
    public SimpleStringProperty octave2Property() { return octave2; }
    public SimpleStringProperty instrument2Property() { return instrument2; }
    public SimpleStringProperty volume2Property() { return volume2; }
    public SimpleStringProperty effect2Property() { return effect2; }

    // === Getters ===

    public String getRow() { return row.get(); }

    // Canal 1
    public String getNote1() { return note1.get(); }
    public String getOctave1() { return octave1.get(); }
    public String getInstrument1() { return instrument1.get(); }
    public String getVolume1() { return volume1.get(); }
    public String getEffect1() { return effect1.get(); }

    // Canal 2
    public String getNote2() { return note2.get(); }
    public String getOctave2() { return octave2.get(); }
    public String getInstrument2() { return instrument2.get(); }
    public String getVolume2() { return volume2.get(); }
    public String getEffect2() { return effect2.get(); }

    // === Getters génériques par canal ===

    public String getNote(int channel) {
        return channel == 1 ? getNote1() : getNote2();
    }

    public String getOctave(int channel) {
        return channel == 1 ? getOctave1() : getOctave2();
    }

    public String getInstrument(int channel) {
        return channel == 1 ? getInstrument1() : getInstrument2();
    }

    public String getVolume(int channel) {
        return channel == 1 ? getVolume1() : getVolume2();
    }

    public String getEffect(int channel) {
        return channel == 1 ? getEffect1() : getEffect2();
    }

    // === Setters ===

    public void setRow(String row) { this.row.set(row); }

    // Canal 1
    public void setNote1(String note) { this.note1.set(note); }
    public void setOctave1(String octave) { this.octave1.set(octave); }
    public void setInstrument1(String instrument) { this.instrument1.set(instrument); }
    public void setVolume1(String volume) { this.volume1.set(volume); }
    public void setEffect1(String effect) { this.effect1.set(effect); }

    // Canal 2
    public void setNote2(String note) { this.note2.set(note); }
    public void setOctave2(String octave) { this.octave2.set(octave); }
    public void setInstrument2(String instrument) { this.instrument2.set(instrument); }
    public void setVolume2(String volume) { this.volume2.set(volume); }
    public void setEffect2(String effect) { this.effect2.set(effect); }

    // === Setters génériques par canal ===

    public void setSound(int channel, String note, String octave) {
        if (channel == 1) {
            setNote1(note);
            setOctave1(octave);
        } else {
            setNote2(note);
            setOctave2(octave);
        }
    }

    public void setInstrument(int channel, String instrument) {
        if (channel == 1) {
            setInstrument1(instrument);
        } else {
            setInstrument2(instrument);
        }
    }

    public void setVolume(int channel, String volume) {
        if (channel == 1) {
            setVolume1(volume);
        } else {
            setVolume2(volume);
        }
    }

    public void setEffect(int channel, String effect) {
        if (channel == 1) {
            setEffect1(effect);
        } else {
            setEffect2(effect);
        }
    }

    // === Méthode pour effacer un canal ===

    public void clearChannel(int channel) {
        setSound(channel, "---", "-");
        setInstrument(channel, "--");
        setVolume(channel, "--");
        setEffect(channel, "---");
    }

    // === Vérifier si un canal a une note ===

    public boolean hasNote(int channel) {
        return !getNote(channel).equals("---");
    }
}