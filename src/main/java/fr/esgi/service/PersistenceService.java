package fr.esgi.service;

import fr.esgi.models.PatternRow;

import java.io.File;
import java.util.List;

public interface PersistenceService {
    void savePattern(File file, List<PatternRow> pattern, int bpm, int tempo) throws Exception;
    PatternLoadResult loadPattern(File file) throws Exception;
    void exportMidi(File file, List<PatternRow> pattern, int bpm) throws Exception;
}

