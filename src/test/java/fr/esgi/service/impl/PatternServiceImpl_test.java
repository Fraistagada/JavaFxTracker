package fr.esgi.service.impl;

import fr.esgi.models.PatternRow;
import fr.esgi.service.PatternServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("PatternServiceImpl")
class PatternServiceImplTest {

    private PatternServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PatternServiceImpl();
    }

    @Nested
    @DisplayName("createEmptyPattern()")
    class CreateEmptyPattern {

        @Test
        @DisplayName("devrait créer un pattern de la taille demandée")
        void devrait_creer_pattern_taille_demandee() {
            List<PatternRow> pattern = service.createEmptyPattern(64);

            assertThat(pattern).hasSize(64);
        }

        @Test
        @DisplayName("devrait créer des lignes avec des numéros formatés")
        void devrait_creer_lignes_numerotees() {
            List<PatternRow> pattern = service.createEmptyPattern(16);

            assertThat(pattern.get(0).getRow()).isEqualTo("00");
            assertThat(pattern.get(9).getRow()).isEqualTo("09");
            assertThat(pattern.get(10).getRow()).isEqualTo("10");
            assertThat(pattern.get(15).getRow()).isEqualTo("15");
        }

        @Test
        @DisplayName("devrait créer des lignes vides")
        void devrait_creer_lignes_vides() {
            List<PatternRow> pattern = service.createEmptyPattern(1);

            PatternRow row = pattern.get(0);
            assertThat(row.getNote(1)).isEqualTo("---");
            assertThat(row.getNote(2)).isEqualTo("---");
            assertThat(row.hasNote(1)).isFalse();
            assertThat(row.hasNote(2)).isFalse();
        }

        @Test
        @DisplayName("devrait retourner une liste vide pour length=0")
        void devrait_retourner_liste_vide() {
            List<PatternRow> pattern = service.createEmptyPattern(0);

            assertThat(pattern).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 32, 64, 128})
        @DisplayName("devrait fonctionner avec différentes tailles")
        void devrait_fonctionner_differentes_tailles(int length) {
            List<PatternRow> pattern = service.createEmptyPattern(length);

            assertThat(pattern).hasSize(length);
        }

        @Test
        @DisplayName("devrait remplacer le pattern existant")
        void devrait_remplacer_pattern_existant() {
            service.createEmptyPattern(10);
            assertThat(service.length()).isEqualTo(10);

            service.createEmptyPattern(20);
            assertThat(service.length()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("getPattern() / setPattern()")
    class GetSetPattern {

        @Test
        @DisplayName("devrait retourner le pattern créé")
        void devrait_retourner_pattern_cree() {
            List<PatternRow> created = service.createEmptyPattern(8);
            List<PatternRow> retrieved = service.getPattern();

            assertThat(retrieved).isSameAs(created);
        }

        @Test
        @DisplayName("devrait permettre de définir un pattern externe")
        void devrait_definir_pattern_externe() {
            List<PatternRow> externalPattern = new ArrayList<>();
            externalPattern.add(new PatternRow("00"));
            externalPattern.add(new PatternRow("01"));

            service.setPattern(externalPattern);

            assertThat(service.getPattern()).isSameAs(externalPattern);
            assertThat(service.length()).isEqualTo(2);
        }

        @Test
        @DisplayName("devrait retourner une liste vide par défaut")
        void devrait_retourner_liste_vide_par_defaut() {
            assertThat(service.getPattern()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getRow() / setRow()")
    class GetSetRow {

        @BeforeEach
        void setUpPattern() {
            service.createEmptyPattern(8);
        }

        @Test
        @DisplayName("devrait retourner la ligne à l'index spécifié")
        void devrait_retourner_ligne_index() {
            PatternRow row = service.getRow(3);

            assertThat(row.getRow()).isEqualTo("03");
        }

        @Test
        @DisplayName("devrait permettre de modifier une ligne")
        void devrait_modifier_ligne() {
            PatternRow newRow = new PatternRow("03", "C", "4", "00", "100", "---",
                    "G", "2", "32", "80", "---");

            service.setRow(3, newRow);

            assertThat(service.getRow(3)).isSameAs(newRow);
            assertThat(service.getRow(3).getNote(1)).isEqualTo("C");
        }

        @Test
        @DisplayName("devrait lever une exception pour un index invalide")
        void devrait_lever_exception_index_invalide() {
            assertThatThrownBy(() -> service.getRow(100))
                    .isInstanceOf(IndexOutOfBoundsException.class);

            assertThatThrownBy(() -> service.getRow(-1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }
    }

    @Nested
    @DisplayName("length()")
    class Length {

        @Test
        @DisplayName("devrait retourner 0 pour un service nouvellement créé")
        void devrait_retourner_zero_initial() {
            assertThat(service.length()).isZero();
        }

        @Test
        @DisplayName("devrait retourner la taille du pattern")
        void devrait_retourner_taille_pattern() {
            service.createEmptyPattern(64);

            assertThat(service.length()).isEqualTo(64);
        }

        @Test
        @DisplayName("devrait refléter les changements de pattern")
        void devrait_refleter_changements() {
            service.createEmptyPattern(32);
            assertThat(service.length()).isEqualTo(32);

            service.createEmptyPattern(16);
            assertThat(service.length()).isEqualTo(16);
        }
    }

    @Nested
    @DisplayName("noteToMidi()")
    class NoteToMidi {

        @ParameterizedTest
        @CsvSource({
                "C, 4, 60",    // Middle C
                "A, 4, 69",    // A440
                "C, 0, 12",
                "C, -1, 0",    // Lowest MIDI note
                "G, 9, 127"    // Near highest MIDI note
        })
        @DisplayName("devrait convertir les notes correctement")
        void devrait_convertir_notes(String note, int octave, int expectedMidi) {
            assertThat(service.noteToMidi(note, octave)).isEqualTo(expectedMidi);
        }

        @ParameterizedTest
        @CsvSource({
                "C#, 4, 61",
                "D#, 4, 63",
                "F#, 4, 66",
                "G#, 4, 68",
                "A#, 4, 70"
        })
        @DisplayName("devrait convertir les notes avec dièse")
        void devrait_convertir_dieses(String note, int octave, int expectedMidi) {
            assertThat(service.noteToMidi(note, octave)).isEqualTo(expectedMidi);
        }

        @Test
        @DisplayName("devrait avoir 12 demi-tons par octave")
        void devrait_avoir_12_demitons_par_octave() {
            int c4 = service.noteToMidi("C", 4);
            int c5 = service.noteToMidi("C", 5);

            assertThat(c5 - c4).isEqualTo(12);
        }

        @Test
        @DisplayName("devrait avoir des demi-tons consécutifs")
        void devrait_avoir_demitons_consecutifs() {
            int c = service.noteToMidi("C", 4);
            int cSharp = service.noteToMidi("C#", 4);
            int d = service.noteToMidi("D", 4);
            int dSharp = service.noteToMidi("D#", 4);
            int e = service.noteToMidi("E", 4);

            assertThat(cSharp - c).isEqualTo(1);
            assertThat(d - cSharp).isEqualTo(1);
            assertThat(dSharp - d).isEqualTo(1);
            assertThat(e - dSharp).isEqualTo(1);
        }
    }
}