package fr.esgi.constants;

import java.util.Map;

public class Constants {
    private Constants() {
    }

    public static final int DEFAULT_BPM = 125;
    public static final int PATTERN_LENGTH = 64;

    public static final Map<String, Integer> NOTES = Map.ofEntries(
            Map.entry("C", 0),
            Map.entry("C#", 1),
            Map.entry("D", 2),
            Map.entry("D#", 3),
            Map.entry("E", 4),
            Map.entry("F", 5),
            Map.entry("F#", 6),
            Map.entry("G", 7),
            Map.entry("G#", 8),
            Map.entry("A", 9),
            Map.entry("A#", 10),
            Map.entry("B", 11)
    );

}
