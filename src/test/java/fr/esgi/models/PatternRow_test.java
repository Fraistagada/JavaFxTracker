package fr.esgi.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PatternRow")
class PatternRowTest {

    private PatternRow row;

    @BeforeEach
    void setUp() {
        row = new PatternRow("00");
    }

    @Nested
    @DisplayName("Constructeur simplifié")
    class ConstructeurSimplifie {

        @Test
        @DisplayName("devrait initialiser le numéro de ligne")
        void devrait_initialiser_numero_ligne() {
            assertThat(row.getRow()).isEqualTo("00");
        }

        @Test
        @DisplayName("devrait initialiser le canal 1 avec des valeurs vides")
        void devrait_initialiser_canal1_vide() {
            assertThat(row.getNote1()).isEqualTo("---");
            assertThat(row.getOctave1()).isEqualTo("-");
            assertThat(row.getInstrument1()).isEqualTo("--");
            assertThat(row.getVolume1()).isEqualTo("--");
            assertThat(row.getEffect1()).isEqualTo("---");
        }

        @Test
        @DisplayName("devrait initialiser le canal 2 avec des valeurs vides")
        void devrait_initialiser_canal2_vide() {
            assertThat(row.getNote2()).isEqualTo("---");
            assertThat(row.getOctave2()).isEqualTo("-");
            assertThat(row.getInstrument2()).isEqualTo("--");
            assertThat(row.getVolume2()).isEqualTo("--");
            assertThat(row.getEffect2()).isEqualTo("---");
        }
    }

    @Nested
    @DisplayName("Constructeur complet")
    class ConstructeurComplet {

        @Test
        @DisplayName("devrait initialiser tous les champs correctement")
        void devrait_initialiser_tous_les_champs() {
            PatternRow fullRow = new PatternRow("05",
                    "C", "4", "00", "100", "A50",
                    "G", "2", "32", "80", "---");

            assertThat(fullRow.getRow()).isEqualTo("05");

            // Canal 1
            assertThat(fullRow.getNote1()).isEqualTo("C");
            assertThat(fullRow.getOctave1()).isEqualTo("4");
            assertThat(fullRow.getInstrument1()).isEqualTo("00");
            assertThat(fullRow.getVolume1()).isEqualTo("100");
            assertThat(fullRow.getEffect1()).isEqualTo("A50");

            // Canal 2
            assertThat(fullRow.getNote2()).isEqualTo("G");
            assertThat(fullRow.getOctave2()).isEqualTo("2");
            assertThat(fullRow.getInstrument2()).isEqualTo("32");
            assertThat(fullRow.getVolume2()).isEqualTo("80");
            assertThat(fullRow.getEffect2()).isEqualTo("---");
        }
    }

    @Nested
    @DisplayName("Getters génériques par canal")
    class GettersGeneriques {

        @BeforeEach
        void setUpChannels() {
            row.setSound(1, "C", "4");
            row.setInstrument(1, "00");
            row.setVolume(1, "100");
            row.setEffect(1, "A50");

            row.setSound(2, "G", "2");
            row.setInstrument(2, "32");
            row.setVolume(2, "80");
            row.setEffect(2, "F08");
        }

        @Test
        @DisplayName("getNote() devrait retourner la note du canal spécifié")
        void getNote_par_canal() {
            assertThat(row.getNote(1)).isEqualTo("C");
            assertThat(row.getNote(2)).isEqualTo("G");
        }

        @Test
        @DisplayName("getOctave() devrait retourner l'octave du canal spécifié")
        void getOctave_par_canal() {
            assertThat(row.getOctave(1)).isEqualTo("4");
            assertThat(row.getOctave(2)).isEqualTo("2");
        }

        @Test
        @DisplayName("getInstrument() devrait retourner l'instrument du canal spécifié")
        void getInstrument_par_canal() {
            assertThat(row.getInstrument(1)).isEqualTo("00");
            assertThat(row.getInstrument(2)).isEqualTo("32");
        }

        @Test
        @DisplayName("getVolume() devrait retourner le volume du canal spécifié")
        void getVolume_par_canal() {
            assertThat(row.getVolume(1)).isEqualTo("100");
            assertThat(row.getVolume(2)).isEqualTo("80");
        }

        @Test
        @DisplayName("getEffect() devrait retourner l'effet du canal spécifié")
        void getEffect_par_canal() {
            assertThat(row.getEffect(1)).isEqualTo("A50");
            assertThat(row.getEffect(2)).isEqualTo("F08");
        }

        @ParameterizedTest
        @ValueSource(ints = {0, 3, -1, 100})
        @DisplayName("les getters génériques avec canal != 1 devraient retourner canal 2")
        void getters_canal_invalide_retourne_canal2(int invalidChannel) {
            // Comportement actuel : tout canal != 1 retourne canal 2
            assertThat(row.getNote(invalidChannel)).isEqualTo("G");
            assertThat(row.getOctave(invalidChannel)).isEqualTo("2");
            assertThat(row.getInstrument(invalidChannel)).isEqualTo("32");
            assertThat(row.getVolume(invalidChannel)).isEqualTo("80");
            assertThat(row.getEffect(invalidChannel)).isEqualTo("F08");
        }
    }

    @Nested
    @DisplayName("Setters génériques par canal")
    class SettersGeneriques {

        @Test
        @DisplayName("setSound() devrait modifier note et octave du canal 1")
        void setSound_canal1() {
            row.setSound(1, "D", "5");

            assertThat(row.getNote1()).isEqualTo("D");
            assertThat(row.getOctave1()).isEqualTo("5");
        }

        @Test
        @DisplayName("setSound() devrait modifier note et octave du canal 2")
        void setSound_canal2() {
            row.setSound(2, "E", "3");

            assertThat(row.getNote2()).isEqualTo("E");
            assertThat(row.getOctave2()).isEqualTo("3");
        }

        @Test
        @DisplayName("setInstrument() devrait modifier l'instrument du canal spécifié")
        void setInstrument_par_canal() {
            row.setInstrument(1, "24");
            row.setInstrument(2, "56");

            assertThat(row.getInstrument1()).isEqualTo("24");
            assertThat(row.getInstrument2()).isEqualTo("56");
        }

        @Test
        @DisplayName("setVolume() devrait modifier le volume du canal spécifié")
        void setVolume_par_canal() {
            row.setVolume(1, "127");
            row.setVolume(2, "64");

            assertThat(row.getVolume1()).isEqualTo("127");
            assertThat(row.getVolume2()).isEqualTo("64");
        }

        @Test
        @DisplayName("setEffect() devrait modifier l'effet du canal spécifié")
        void setEffect_par_canal() {
            row.setEffect(1, "C40");
            row.setEffect(2, "E9A");

            assertThat(row.getEffect1()).isEqualTo("C40");
            assertThat(row.getEffect2()).isEqualTo("E9A");
        }
    }

    @Nested
    @DisplayName("clearChannel()")
    class ClearChannel {

        @BeforeEach
        void setUpWithData() {
            row.setSound(1, "C", "4");
            row.setInstrument(1, "00");
            row.setVolume(1, "100");
            row.setEffect(1, "A50");

            row.setSound(2, "G", "2");
            row.setInstrument(2, "32");
            row.setVolume(2, "80");
            row.setEffect(2, "F08");
        }

        @Test
        @DisplayName("devrait effacer le canal 1 sans affecter le canal 2")
        void clear_canal1() {
            row.clearChannel(1);

            // Canal 1 effacé
            assertThat(row.getNote1()).isEqualTo("---");
            assertThat(row.getOctave1()).isEqualTo("-");
            assertThat(row.getInstrument1()).isEqualTo("--");
            assertThat(row.getVolume1()).isEqualTo("--");
            assertThat(row.getEffect1()).isEqualTo("---");

            // Canal 2 intact
            assertThat(row.getNote2()).isEqualTo("G");
            assertThat(row.getOctave2()).isEqualTo("2");
            assertThat(row.getInstrument2()).isEqualTo("32");
            assertThat(row.getVolume2()).isEqualTo("80");
            assertThat(row.getEffect2()).isEqualTo("F08");
        }

        @Test
        @DisplayName("devrait effacer le canal 2 sans affecter le canal 1")
        void clear_canal2() {
            row.clearChannel(2);

            // Canal 1 intact
            assertThat(row.getNote1()).isEqualTo("C");
            assertThat(row.getOctave1()).isEqualTo("4");
            assertThat(row.getInstrument1()).isEqualTo("00");
            assertThat(row.getVolume1()).isEqualTo("100");
            assertThat(row.getEffect1()).isEqualTo("A50");

            // Canal 2 effacé
            assertThat(row.getNote2()).isEqualTo("---");
            assertThat(row.getOctave2()).isEqualTo("-");
            assertThat(row.getInstrument2()).isEqualTo("--");
            assertThat(row.getVolume2()).isEqualTo("--");
            assertThat(row.getEffect2()).isEqualTo("---");
        }

        @Test
        @DisplayName("devrait pouvoir être appelé plusieurs fois")
        void clear_multiple_fois() {
            row.clearChannel(1);
            row.clearChannel(1);

            assertThat(row.getNote1()).isEqualTo("---");
        }
    }

    @Nested
    @DisplayName("hasNote()")
    class HasNote {

        @Test
        @DisplayName("devrait retourner false pour un canal vide")
        void hasNote_false_pour_vide() {
            assertThat(row.hasNote(1)).isFalse();
            assertThat(row.hasNote(2)).isFalse();
        }

        @Test
        @DisplayName("devrait retourner true si une note est présente")
        void hasNote_true_avec_note() {
            row.setSound(1, "C", "4");

            assertThat(row.hasNote(1)).isTrue();
            assertThat(row.hasNote(2)).isFalse();
        }

        @Test
        @DisplayName("devrait retourner false après clearChannel()")
        void hasNote_false_apres_clear() {
            row.setSound(1, "C", "4");
            row.clearChannel(1);

            assertThat(row.hasNote(1)).isFalse();
        }

        @Test
        @DisplayName("devrait fonctionner indépendamment pour chaque canal")
        void hasNote_independant_par_canal() {
            row.setSound(1, "C", "4");
            row.setSound(2, "G", "2");

            assertThat(row.hasNote(1)).isTrue();
            assertThat(row.hasNote(2)).isTrue();

            row.clearChannel(1);

            assertThat(row.hasNote(1)).isFalse();
            assertThat(row.hasNote(2)).isTrue();
        }
    }

    @Nested
    @DisplayName("Properties JavaFX")
    class PropertiesJavaFX {

        @Test
        @DisplayName("rowProperty devrait être synchronisé avec getRow/setRow")
        void rowProperty_synchronise() {
            assertThat(row.rowProperty().get()).isEqualTo("00");

            row.setRow("42");
            assertThat(row.rowProperty().get()).isEqualTo("42");
            assertThat(row.getRow()).isEqualTo("42");
        }

        @Test
        @DisplayName("les properties canal 1 devraient être synchronisées")
        void properties_canal1_synchronisees() {
            row.setNote1("D");
            row.setOctave1("5");
            row.setInstrument1("10");
            row.setVolume1("90");
            row.setEffect1("C40");

            assertThat(row.note1Property().get()).isEqualTo("D");
            assertThat(row.octave1Property().get()).isEqualTo("5");
            assertThat(row.instrument1Property().get()).isEqualTo("10");
            assertThat(row.volume1Property().get()).isEqualTo("90");
            assertThat(row.effect1Property().get()).isEqualTo("C40");
        }

        @Test
        @DisplayName("les properties canal 2 devraient être synchronisées")
        void properties_canal2_synchronisees() {
            row.setNote2("E");
            row.setOctave2("3");
            row.setInstrument2("32");
            row.setVolume2("70");
            row.setEffect2("F08");

            assertThat(row.note2Property().get()).isEqualTo("E");
            assertThat(row.octave2Property().get()).isEqualTo("3");
            assertThat(row.instrument2Property().get()).isEqualTo("32");
            assertThat(row.volume2Property().get()).isEqualTo("70");
            assertThat(row.effect2Property().get()).isEqualTo("F08");
        }
    }

    @Nested
    @DisplayName("Setters directs canal 1")
    class SettersCanal1 {

        @Test
        @DisplayName("setNote1() devrait modifier uniquement la note")
        void setNote1() {
            row.setNote1("F#");
            assertThat(row.getNote1()).isEqualTo("F#");
            assertThat(row.getOctave1()).isEqualTo("-"); // Inchangé
        }

        @Test
        @DisplayName("setOctave1() devrait modifier uniquement l'octave")
        void setOctave1() {
            row.setOctave1("7");
            assertThat(row.getOctave1()).isEqualTo("7");
            assertThat(row.getNote1()).isEqualTo("---"); // Inchangé
        }
    }

    @Nested
    @DisplayName("Setters directs canal 2")
    class SettersCanal2 {

        @Test
        @DisplayName("setNote2() devrait modifier uniquement la note")
        void setNote2() {
            row.setNote2("A#");
            assertThat(row.getNote2()).isEqualTo("A#");
            assertThat(row.getOctave2()).isEqualTo("-"); // Inchangé
        }

        @Test
        @DisplayName("setOctave2() devrait modifier uniquement l'octave")
        void setOctave2() {
            row.setOctave2("1");
            assertThat(row.getOctave2()).isEqualTo("1");
            assertThat(row.getNote2()).isEqualTo("---"); // Inchangé
        }
    }
}