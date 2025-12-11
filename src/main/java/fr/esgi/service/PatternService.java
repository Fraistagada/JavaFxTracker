package fr.esgi.service;

import fr.esgi.models.PatternRow;

import java.util.List;

public interface PatternService {
    List<PatternRow> createEmptyPattern(int length);
    List<PatternRow> getPattern();
    void setPattern(List<PatternRow> pattern);
    PatternRow getRow(int index);
    void setRow(int index, PatternRow row);
    int length();
    int noteToMidi(String note, int octave);
}

