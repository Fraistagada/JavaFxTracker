package fr.esgi.constants;

import java.util.LinkedHashMap;
import java.util.Map;

public class Effects {

    private Effects() {
    }

    // Map: Code effet -> Description
    public static final Map<String, String> EFFECTS = new LinkedHashMap<>();

    static {
        // Effets de hauteur (Pitch)
        EFFECTS.put("0", "Arpeggio (0xy) - Alterne entre note, note+x, note+y");
        EFFECTS.put("1", "Portamento Up (1xx) - Glisse vers le haut");
        EFFECTS.put("2", "Portamento Down (2xx) - Glisse vers le bas");
        EFFECTS.put("3", "Tone Portamento (3xx) - Glisse vers la note cible");
        EFFECTS.put("4", "Vibrato (4xy) - x=vitesse, y=profondeur");
        EFFECTS.put("5", "Tone Porta + Vol Slide (5xy)");
        EFFECTS.put("6", "Vibrato + Vol Slide (6xy)");
        EFFECTS.put("7", "Tremolo (7xy) - x=vitesse, y=profondeur");

        // Effets de volume
        EFFECTS.put("A", "Volume Slide (Axy) - x=up, y=down");
        EFFECTS.put("C", "Set Volume (Cxx) - Volume 00-7F");

        // Effets de pattern/timing
        EFFECTS.put("B", "Jump to Pattern (Bxx)");
        EFFECTS.put("D", "Pattern Break (Dxx) - Saute à la ligne xx");
        EFFECTS.put("F", "Set Speed/Tempo (Fxx) - 01-1F=speed, 20+=tempo");

        // Effets spéciaux
        EFFECTS.put("8", "Set Panning (8xx) - 00=gauche, 80=centre, FF=droite");
        EFFECTS.put("9", "Sample Offset (9xx) - Démarre à l'offset xx");
        EFFECTS.put("E", "Extended Effects (Exy)");

        // Sous-effets Extended (E)
        EFFECTS.put("E1", "Fine Porta Up (E1x)");
        EFFECTS.put("E2", "Fine Porta Down (E2x)");
        EFFECTS.put("E9", "Retrigger Note (E9x) - Rejoue toutes les x ticks");
        EFFECTS.put("EA", "Fine Volume Up (EAx)");
        EFFECTS.put("EB", "Fine Volume Down (EBx)");
        EFFECTS.put("EC", "Note Cut (ECx) - Coupe après x ticks");
        EFFECTS.put("ED", "Note Delay (EDx) - Délai de x ticks");
    }

    /**
     * Retourne la description d'un effet
     */
    public static String getDescription(String code) {
        if (code == null || code.isEmpty() || code.equals("---")) {
            return "Aucun effet";
        }

        // Chercher d'abord le code complet (ex: "E9")
        if (code.length() >= 2) {
            String extended = code.substring(0, 2).toUpperCase();
            if (EFFECTS.containsKey(extended)) {
                return EFFECTS.get(extended);
            }
        }

        // Sinon chercher juste la première lettre
        String firstChar = code.substring(0, 1).toUpperCase();
        return EFFECTS.getOrDefault(firstChar, "Effet inconnu");
    }

    /**
     * Parse un effet et retourne ses composants
     * Format: Xyy où X = type d'effet, yy = paramètres
     */
    public static EffectData parse(String effectString) {
        if (effectString == null || effectString.equals("---") || effectString.length() < 1) {
            return new EffectData("", 0, 0);
        }

        String effect = effectString.toUpperCase();
        String type = effect.substring(0, 1);
        int param1 = 0;
        int param2 = 0;

        if (effect.length() >= 2) {
            try {
                param1 = Integer.parseInt(effect.substring(1, 2), 16);
            } catch (NumberFormatException ignored) {}
        }

        if (effect.length() >= 3) {
            try {
                param2 = Integer.parseInt(effect.substring(2, 3), 16);
            } catch (NumberFormatException ignored) {}
        }

        return new EffectData(type, param1, param2);
    }

    /**
     * Classe pour stocker les données d'un effet parsé
     */
    public static class EffectData {
        public final String type;
        public final int param1;
        public final int param2;
        public final int fullParam;

        public EffectData(String type, int param1, int param2) {
            this.type = type;
            this.param1 = param1;
            this.param2 = param2;
            this.fullParam = (param1 << 4) | param2;
        }

        public boolean isEmpty() {
            return type.isEmpty();
        }
    }
}