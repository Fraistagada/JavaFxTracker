package fr.esgi.constants;

import java.util.LinkedHashMap;
import java.util.Map;

public class Instruments {

    private Instruments() {
    }

    // Map: ID (00-127) -> Nom de l'instrument
    public static final Map<String, String> GENERAL_MIDI = new LinkedHashMap<>();

    static {
        // Piano (0-7)
        GENERAL_MIDI.put("00", "Acoustic Grand Piano");
        GENERAL_MIDI.put("01", "Bright Acoustic Piano");
        GENERAL_MIDI.put("02", "Electric Grand Piano");
        GENERAL_MIDI.put("03", "Honky-tonk Piano");
        GENERAL_MIDI.put("04", "Electric Piano 1");
        GENERAL_MIDI.put("05", "Electric Piano 2");
        GENERAL_MIDI.put("06", "Harpsichord");
        GENERAL_MIDI.put("07", "Clavinet");

        // Chromatic Percussion (8-15)
        GENERAL_MIDI.put("08", "Celesta");
        GENERAL_MIDI.put("09", "Glockenspiel");
        GENERAL_MIDI.put("10", "Music Box");
        GENERAL_MIDI.put("11", "Vibraphone");
        GENERAL_MIDI.put("12", "Marimba");
        GENERAL_MIDI.put("13", "Xylophone");
        GENERAL_MIDI.put("14", "Tubular Bells");
        GENERAL_MIDI.put("15", "Dulcimer");

        // Organ (16-23)
        GENERAL_MIDI.put("16", "Drawbar Organ");
        GENERAL_MIDI.put("17", "Percussive Organ");
        GENERAL_MIDI.put("18", "Rock Organ");
        GENERAL_MIDI.put("19", "Church Organ");
        GENERAL_MIDI.put("20", "Reed Organ");
        GENERAL_MIDI.put("21", "Accordion");
        GENERAL_MIDI.put("22", "Harmonica");
        GENERAL_MIDI.put("23", "Tango Accordion");

        // Guitar (24-31)
        GENERAL_MIDI.put("24", "Acoustic Guitar (nylon)");
        GENERAL_MIDI.put("25", "Acoustic Guitar (steel)");
        GENERAL_MIDI.put("26", "Electric Guitar (jazz)");
        GENERAL_MIDI.put("27", "Electric Guitar (clean)");
        GENERAL_MIDI.put("28", "Electric Guitar (muted)");
        GENERAL_MIDI.put("29", "Overdriven Guitar");
        GENERAL_MIDI.put("30", "Distortion Guitar");
        GENERAL_MIDI.put("31", "Guitar Harmonics");

        // Bass (32-39)
        GENERAL_MIDI.put("32", "Acoustic Bass");
        GENERAL_MIDI.put("33", "Electric Bass (finger)");
        GENERAL_MIDI.put("34", "Electric Bass (pick)");
        GENERAL_MIDI.put("35", "Fretless Bass");
        GENERAL_MIDI.put("36", "Slap Bass 1");
        GENERAL_MIDI.put("37", "Slap Bass 2");
        GENERAL_MIDI.put("38", "Synth Bass 1");
        GENERAL_MIDI.put("39", "Synth Bass 2");

        // Strings (40-47)
        GENERAL_MIDI.put("40", "Violin");
        GENERAL_MIDI.put("41", "Viola");
        GENERAL_MIDI.put("42", "Cello");
        GENERAL_MIDI.put("43", "Contrabass");
        GENERAL_MIDI.put("44", "Tremolo Strings");
        GENERAL_MIDI.put("45", "Pizzicato Strings");
        GENERAL_MIDI.put("46", "Orchestral Harp");
        GENERAL_MIDI.put("47", "Timpani");

        // Ensemble (48-55)
        GENERAL_MIDI.put("48", "String Ensemble 1");
        GENERAL_MIDI.put("49", "String Ensemble 2");
        GENERAL_MIDI.put("50", "Synth Strings 1");
        GENERAL_MIDI.put("51", "Synth Strings 2");
        GENERAL_MIDI.put("52", "Choir Aahs");
        GENERAL_MIDI.put("53", "Voice Oohs");
        GENERAL_MIDI.put("54", "Synth Voice");
        GENERAL_MIDI.put("55", "Orchestra Hit");

        // Brass (56-63)
        GENERAL_MIDI.put("56", "Trumpet");
        GENERAL_MIDI.put("57", "Trombone");
        GENERAL_MIDI.put("58", "Tuba");
        GENERAL_MIDI.put("59", "Muted Trumpet");
        GENERAL_MIDI.put("60", "French Horn");
        GENERAL_MIDI.put("61", "Brass Section");
        GENERAL_MIDI.put("62", "Synth Brass 1");
        GENERAL_MIDI.put("63", "Synth Brass 2");

        // Reed (64-71)
        GENERAL_MIDI.put("64", "Soprano Sax");
        GENERAL_MIDI.put("65", "Alto Sax");
        GENERAL_MIDI.put("66", "Tenor Sax");
        GENERAL_MIDI.put("67", "Baritone Sax");
        GENERAL_MIDI.put("68", "Oboe");
        GENERAL_MIDI.put("69", "English Horn");
        GENERAL_MIDI.put("70", "Bassoon");
        GENERAL_MIDI.put("71", "Clarinet");

        // Pipe (72-79)
        GENERAL_MIDI.put("72", "Piccolo");
        GENERAL_MIDI.put("73", "Flute");
        GENERAL_MIDI.put("74", "Recorder");
        GENERAL_MIDI.put("75", "Pan Flute");
        GENERAL_MIDI.put("76", "Blown Bottle");
        GENERAL_MIDI.put("77", "Shakuhachi");
        GENERAL_MIDI.put("78", "Whistle");
        GENERAL_MIDI.put("79", "Ocarina");

        // Synth Lead (80-87)
        GENERAL_MIDI.put("80", "Lead 1 (square)");
        GENERAL_MIDI.put("81", "Lead 2 (sawtooth)");
        GENERAL_MIDI.put("82", "Lead 3 (calliope)");
        GENERAL_MIDI.put("83", "Lead 4 (chiff)");
        GENERAL_MIDI.put("84", "Lead 5 (charang)");
        GENERAL_MIDI.put("85", "Lead 6 (voice)");
        GENERAL_MIDI.put("86", "Lead 7 (fifths)");
        GENERAL_MIDI.put("87", "Lead 8 (bass + lead)");

        // Synth Pad (88-95)
        GENERAL_MIDI.put("88", "Pad 1 (new age)");
        GENERAL_MIDI.put("89", "Pad 2 (warm)");
        GENERAL_MIDI.put("90", "Pad 3 (polysynth)");
        GENERAL_MIDI.put("91", "Pad 4 (choir)");
        GENERAL_MIDI.put("92", "Pad 5 (bowed)");
        GENERAL_MIDI.put("93", "Pad 6 (metallic)");
        GENERAL_MIDI.put("94", "Pad 7 (halo)");
        GENERAL_MIDI.put("95", "Pad 8 (sweep)");

        // Synth Effects (96-103)
        GENERAL_MIDI.put("96", "FX 1 (rain)");
        GENERAL_MIDI.put("97", "FX 2 (soundtrack)");
        GENERAL_MIDI.put("98", "FX 3 (crystal)");
        GENERAL_MIDI.put("99", "FX 4 (atmosphere)");
        GENERAL_MIDI.put("100", "FX 5 (brightness)");
        GENERAL_MIDI.put("101", "FX 6 (goblins)");
        GENERAL_MIDI.put("102", "FX 7 (echoes)");
        GENERAL_MIDI.put("103", "FX 8 (sci-fi)");

        // Ethnic (104-111)
        GENERAL_MIDI.put("104", "Sitar");
        GENERAL_MIDI.put("105", "Banjo");
        GENERAL_MIDI.put("106", "Shamisen");
        GENERAL_MIDI.put("107", "Koto");
        GENERAL_MIDI.put("108", "Kalimba");
        GENERAL_MIDI.put("109", "Bagpipe");
        GENERAL_MIDI.put("110", "Fiddle");
        GENERAL_MIDI.put("111", "Shanai");

        // Percussive (112-119)
        GENERAL_MIDI.put("112", "Tinkle Bell");
        GENERAL_MIDI.put("113", "Agogo");
        GENERAL_MIDI.put("114", "Steel Drums");
        GENERAL_MIDI.put("115", "Woodblock");
        GENERAL_MIDI.put("116", "Taiko Drum");
        GENERAL_MIDI.put("117", "Melodic Tom");
        GENERAL_MIDI.put("118", "Synth Drum");
        GENERAL_MIDI.put("119", "Reverse Cymbal");

        // Sound Effects (120-127)
        GENERAL_MIDI.put("120", "Guitar Fret Noise");
        GENERAL_MIDI.put("121", "Breath Noise");
        GENERAL_MIDI.put("122", "Seashore");
        GENERAL_MIDI.put("123", "Bird Tweet");
        GENERAL_MIDI.put("124", "Telephone Ring");
        GENERAL_MIDI.put("125", "Helicopter");
        GENERAL_MIDI.put("126", "Applause");
        GENERAL_MIDI.put("127", "Gunshot");
    }

    /**
     * Retourne le nom de l'instrument pour un ID donné
     */
    public static String getName(String id) {
        return GENERAL_MIDI.getOrDefault(id, "Unknown");
    }

    /**
     * Retourne l'ID MIDI (int) pour un ID string
     */
    public static int getMidiId(String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}