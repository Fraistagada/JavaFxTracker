package fr.esgi.service;

import fr.esgi.constants.Constants;
import fr.esgi.models.PatternRow;
import fr.esgi.service.PatternService;

import java.util.ArrayList;
import java.util.List;

public class PatternServiceImpl implements PatternService {

    private List<PatternRow> pattern = new ArrayList<>();

    @Override
    public List<PatternRow> createEmptyPattern(int length) {
        pattern = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            pattern.add(new PatternRow(String.format("%02d", i)));
        }
        return pattern;
    }

    @Override
    public List<PatternRow> getPattern() {
        return pattern;
    }

    @Override
    public void setPattern(List<PatternRow> pattern) {
        this.pattern = pattern;
    }

    @Override
    public PatternRow getRow(int index) {
        return pattern.get(index);
    }

    @Override
    public void setRow(int index, PatternRow row) {
        pattern.set(index, row);
    }

    @Override
    public int length() {
        return pattern.size();
    }

    @Override
    public int noteToMidi(String note, int octave) {
        return 12 * (octave + 1) + Constants.NOTES.get(note);
    }
}

