package fr.esgi.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Instruments")
class InstrumentsTest {

    @Nested
    @DisplayName("getName()")
    class GetName {

        @ParameterizedTest
        @CsvSource({
                "00, Acoustic Grand Piano",
                "01, Bright Acoustic Piano",
                "07, Clavinet",
                "24, Acoustic Guitar (nylon)",
                "40, Violin",
                "56, Trumpet",
                "73, Flute",
                "127, Gunshot"
        })
        @DisplayName("devrait retourner le nom correct pour les IDs valides")
        void devrait_retourner_nom_correct(String id, String expectedName) {
            assertThat(Instruments.getName(id)).isEqualTo(expectedName);
        }

        @ParameterizedTest
        @ValueSource(strings = {"128", "999", "abc", "", "invalid"})
        @DisplayName("devrait retourner 'Unknown' pour les IDs invalides")
        void devrait_retourner_unknown_pour_id_invalide(String id) {
            assertThat(Instruments.getName(id)).isEqualTo("Unknown");
        }

        @Test
        @DisplayName("devrait retourner 'Unknown' pour null")
        void devrait_retourner_unknown_pour_null() {
            assertThat(Instruments.getName(null)).isEqualTo("Unknown");
        }

        @Nested
        @DisplayName("par catégorie d'instrument")
        class ParCategorie {

            @ParameterizedTest
            @CsvSource({
                    "00, Acoustic Grand Piano",
                    "01, Bright Acoustic Piano",
                    "02, Electric Grand Piano",
                    "03, Honky-tonk Piano",
                    "04, Electric Piano 1",
                    "05, Electric Piano 2",
                    "06, Harpsichord",
                    "07, Clavinet"
            })
            @DisplayName("Piano (00-07)")
            void devrait_contenir_pianos(String id, String name) {
                assertThat(Instruments.getName(id)).isEqualTo(name);
            }

            @ParameterizedTest
            @CsvSource({
                    "32, Acoustic Bass",
                    "33, Electric Bass (finger)",
                    "34, Electric Bass (pick)",
                    "35, Fretless Bass",
                    "36, Slap Bass 1",
                    "37, Slap Bass 2",
                    "38, Synth Bass 1",
                    "39, Synth Bass 2"
            })
            @DisplayName("Bass (32-39)")
            void devrait_contenir_basses(String id, String name) {
                assertThat(Instruments.getName(id)).isEqualTo(name);
            }

            @ParameterizedTest
            @CsvSource({
                    "64, Soprano Sax",
                    "65, Alto Sax",
                    "66, Tenor Sax",
                    "67, Baritone Sax",
                    "68, Oboe",
                    "69, English Horn",
                    "70, Bassoon",
                    "71, Clarinet"
            })
            @DisplayName("Reed (64-71)")
            void devrait_contenir_anches(String id, String name) {
                assertThat(Instruments.getName(id)).isEqualTo(name);
            }

            @ParameterizedTest
            @CsvSource({
                    "120, Guitar Fret Noise",
                    "121, Breath Noise",
                    "122, Seashore",
                    "123, Bird Tweet",
                    "124, Telephone Ring",
                    "125, Helicopter",
                    "126, Applause",
                    "127, Gunshot"
            })
            @DisplayName("Sound Effects (120-127)")
            void devrait_contenir_effets_sonores(String id, String name) {
                assertThat(Instruments.getName(id)).isEqualTo(name);
            }
        }
    }

    @Nested
    @DisplayName("getMidiId()")
    class GetMidiId {

        @ParameterizedTest
        @CsvSource({
                "0, 0",
                "00, 0",
                "1, 1",
                "01, 1",
                "64, 64",
                "127, 127"
        })
        @DisplayName("devrait convertir les IDs string en int")
        void devrait_convertir_id_en_int(String input, int expected) {
            assertThat(Instruments.getMidiId(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @ValueSource(strings = {"abc", "invalid", "", "12.5", "0xFF"})
        @DisplayName("devrait retourner 0 pour les valeurs non numériques")
        void devrait_retourner_zero_pour_invalide(String input) {
            assertThat(Instruments.getMidiId(input)).isZero();
        }

        @Test
        @DisplayName("devrait retourner 0 pour null")
        void devrait_retourner_zero_pour_null() {
            assertThat(Instruments.getMidiId(null)).isZero();
        }

        @Test
        @DisplayName("devrait accepter les nombres négatifs (même si non MIDI valide)")
        void devrait_accepter_nombres_negatifs() {
            assertThat(Instruments.getMidiId("-1")).isEqualTo(-1);
        }

        @Test
        @DisplayName("devrait accepter les nombres au-delà de 127")
        void devrait_accepter_nombres_hors_plage() {
            assertThat(Instruments.getMidiId("200")).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("GENERAL_MIDI map")
    class GeneralMidiMap {

        @Test
        @DisplayName("devrait contenir exactement 128 instruments")
        void devrait_contenir_128_instruments() {
            assertThat(Instruments.GENERAL_MIDI).hasSize(128);
        }

        @Test
        @DisplayName("devrait être une LinkedHashMap (ordre préservé)")
        void devrait_preserver_ordre() {
            assertThat(Instruments.GENERAL_MIDI)
                    .isInstanceOf(java.util.LinkedHashMap.class);
        }

        @Test
        @DisplayName("devrait avoir des clés de 00 à 127 en format string")
        void devrait_avoir_cles_formatees() {
            // Vérifier quelques clés avec padding zéro
            assertThat(Instruments.GENERAL_MIDI).containsKey("00");
            assertThat(Instruments.GENERAL_MIDI).containsKey("01");
            assertThat(Instruments.GENERAL_MIDI).containsKey("09");

            // Après 09, pas de padding
            assertThat(Instruments.GENERAL_MIDI).containsKey("10");
            assertThat(Instruments.GENERAL_MIDI).containsKey("99");
            assertThat(Instruments.GENERAL_MIDI).containsKey("100");
            assertThat(Instruments.GENERAL_MIDI).containsKey("127");
        }

        @Test
        @DisplayName("ne devrait pas contenir de valeurs null")
        void ne_devrait_pas_contenir_null() {
            assertThat(Instruments.GENERAL_MIDI.values())
                    .doesNotContainNull();
        }

        @Test
        @DisplayName("ne devrait pas contenir de valeurs vides")
        void ne_devrait_pas_contenir_vide() {
            assertThat(Instruments.GENERAL_MIDI.values())
                    .allMatch(name -> !name.isEmpty());
        }
    }
}