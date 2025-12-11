package fr.esgi.service;

import fr.esgi.models.PatternRow;

import java.util.List;

public class PatternLoadResult {
    public final List<PatternRow> pattern;
    public final int bpm;
    public final int tempo;

    public PatternLoadResult(List<PatternRow> pattern, int bpm, int tempo) {
        this.pattern = pattern;
        this.bpm = bpm;
        this.tempo = tempo;
    }
}

