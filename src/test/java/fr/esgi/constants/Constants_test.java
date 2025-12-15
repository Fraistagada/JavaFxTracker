package fr.esgi.constants;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Constants")
class ConstantsTest {

    @Nested
    @DisplayName("Constantes de configuration")
    class ConfigConstants {

        @Test
        @DisplayName("DEFAULT_BPM devrait être 125")
        void default_bpm_devrait_etre_125() {
            assertThat(Constants.DEFAULT_BPM).isEqualTo(125);
        }

        @Test
        @DisplayName("PATTERN_LENGTH devrait être 64")
        void pattern_length_devrait_etre_64() {
            assertThat(Constants.PATTERN_LENGTH).isEqualTo(64);
        }
    }

    @Nested
    @DisplayName("NOTES map")
    class NotesMap {

        @Test
        @DisplayName("devrait contenir les 12 notes de la gamme chromatique")
        void devrait_contenir_12_notes() {
            assertThat(Constants.NOTES).hasSize(12);
        }

        @Test
        @DisplayName("devrait mapper C à 0")
        void c_devrait_etre_0() {
            assertThat(Constants.NOTES.get("C")).isZero();
        }

        @Test
        @DisplayName("devrait mapper B à 11")
        void b_devrait_etre_11() {
            assertThat(Constants.NOTES.get("B")).isEqualTo(11);
        }

        @Test
        @DisplayName("devrait contenir toutes les notes naturelles")
        void devrait_contenir_notes_naturelles() {
            assertThat(Constants.NOTES)
                    .containsKeys("C", "D", "E", "F", "G", "A", "B");
        }

        @Test
        @DisplayName("devrait contenir toutes les notes avec dièse")
        void devrait_contenir_notes_dieses() {
            assertThat(Constants.NOTES)
                    .containsKeys("C#", "D#", "F#", "G#", "A#");
        }

        @Test
        @DisplayName("devrait avoir les valeurs chromatiques correctes")
        void devrait_avoir_valeurs_chromatiques() {
            assertThat(Constants.NOTES.get("C")).isEqualTo(0);
            assertThat(Constants.NOTES.get("C#")).isEqualTo(1);
            assertThat(Constants.NOTES.get("D")).isEqualTo(2);
            assertThat(Constants.NOTES.get("D#")).isEqualTo(3);
            assertThat(Constants.NOTES.get("E")).isEqualTo(4);
            assertThat(Constants.NOTES.get("F")).isEqualTo(5);
            assertThat(Constants.NOTES.get("F#")).isEqualTo(6);
            assertThat(Constants.NOTES.get("G")).isEqualTo(7);
            assertThat(Constants.NOTES.get("G#")).isEqualTo(8);
            assertThat(Constants.NOTES.get("A")).isEqualTo(9);
            assertThat(Constants.NOTES.get("A#")).isEqualTo(10);
            assertThat(Constants.NOTES.get("B")).isEqualTo(11);
        }

        @Test
        @DisplayName("ne devrait pas contenir E# ou B# (enharmoniques)")
        void ne_devrait_pas_contenir_enharmoniques() {
            assertThat(Constants.NOTES).doesNotContainKeys("E#", "B#");
        }

        @Test
        @DisplayName("devrait retourner null pour une note inexistante")
        void devrait_retourner_null_pour_note_inexistante() {
            assertThat(Constants.NOTES.get("X")).isNull();
            assertThat(Constants.NOTES.get("Cb")).isNull();
            assertThat(Constants.NOTES.get("Db")).isNull(); // bémols non inclus
        }

        @Test
        @DisplayName("devrait être immuable")
        void devrait_etre_immuable() {
            assertThat(Constants.NOTES)
                    .isUnmodifiable();
        }
    }
}