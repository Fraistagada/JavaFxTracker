package fr.esgi.service.impl;

import fr.esgi.models.PatternRow;
import fr.esgi.service.PersistenceService;

import javax.sound.midi.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FilePersistenceServiceImpl implements PersistenceService {

    @Override
    public void savePattern(File file, List<PatternRow> pattern, int bpm, int tempo) throws Exception {
        try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
            out.println("BPM=" + bpm);
            out.println("TEMPO=" + tempo);
            out.println("CHANNELS=2");
            out.println("ROW;N1;O1;I1;V1;FX1;N2;O2;I2;V2;FX2");

            for (PatternRow row : pattern) {
                out.printf("%s;%s;%s;%s;%s;%s;%s;%s;%s;%s;%s%n",
                        row.getRow(),
                        row.getNote(1), row.getOctave(1), row.getInstrument(1), row.getVolume(1), row.getEffect(1),
                        row.getNote(2), row.getOctave(2), row.getInstrument(2), row.getVolume(2), row.getEffect(2)
                );
            }
        }
    }

    @Override
    public PatternLoadResult loadPattern(File file) throws Exception {
        List<PatternRow> pattern = new ArrayList<>();
        int bpm = 120;
        int tempo = 120;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            line = reader.readLine();
            if (line != null && line.startsWith("BPM=")) {
                bpm = Integer.parseInt(line.substring(4));
            }

            line = reader.readLine();
            if (line != null && line.startsWith("TEMPO=")) {
                tempo = Integer.parseInt(line.substring(6));
            }

            // Skip CHANNELS line
            reader.readLine();

            // Skip header line
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] p = line.split(";");
                if (p.length == 11) {
                    PatternRow row = new PatternRow(p[0],
                            p[1], p[2], p[3], p[4], p[5],
                            p[6], p[7], p[8], p[9], p[10]);
                    pattern.add(row);
                }
            }
        }

        return new PatternLoadResult(pattern, bpm, tempo);
    }

    @Override
    public void exportMidi(File file, List<PatternRow> pattern, int bpm) throws Exception {
        Sequence sequence = new Sequence(Sequence.PPQ, 480);
        Track track1 = sequence.createTrack();
        Track track2 = sequence.createTrack();

        int tick = 0;
        int tickPerRow = 120;

        for (PatternRow row : pattern) {
            // Canal 1
            if (row.hasNote(1)) {
                int note = 12 * (Integer.parseInt(row.getOctave(1)) + 1) + fr.esgi.constants.Constants.NOTES.get(row.getNote(1));
                int vel = Math.min(127, Integer.parseInt(row.getVolume(1)));

                track1.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, note, vel), tick));
                track1.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, note, 0), tick + tickPerRow));
            }

            // Canal 2
            if (row.hasNote(2)) {
                int note = 12 * (Integer.parseInt(row.getOctave(2)) + 1) + fr.esgi.constants.Constants.NOTES.get(row.getNote(2));
                int vel = Math.min(127, Integer.parseInt(row.getVolume(2)));

                track2.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 1, note, vel), tick));
                track2.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 1, note, 0), tick + tickPerRow));
            }

            tick += tickPerRow;
        }

        MidiSystem.write(sequence, 1, file);
    }
}

