package fr.esgi.service;

import fr.esgi.models.PatternRow;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PatternLoadResult")
class PatternLoadResultTest {

    @Nested
    @DisplayName("Constructeur")
    class Constructeur {

        @Test
        @DisplayName("devrait stocker le pattern")
        void devrait_stocker_pattern() {
            List<PatternRow> pattern = new ArrayList<>();
            pattern.add(new PatternRow("00"));
            pattern.add(new PatternRow("01"));

            PatternLoadResult result = new PatternLoadResult(pattern, 120, 125);

            assertThat(result.pattern).isSameAs(pattern);
            assertThat(result.pattern).hasSize(2);
        }

        @Test
        @DisplayName("devrait stocker le BPM")
        void devrait_stocker_bpm() {
            PatternLoadResult result = new PatternLoadResult(new ArrayList<>(), 140, 120);

            assertThat(result.bpm).isEqualTo(140);
        }

        @Test
        @DisplayName("devrait stocker le tempo")
        void devrait_stocker_tempo() {
            PatternLoadResult result = new PatternLoadResult(new ArrayList<>(), 120, 145);

            assertThat(result.tempo).isEqualTo(145);
        }

        @Test
        @DisplayName("devrait accepter un pattern vide")
        void devrait_accepter_pattern_vide() {
            PatternLoadResult result = new PatternLoadResult(new ArrayList<>(), 120, 120);

            assertThat(result.pattern).isEmpty();
        }

        @Test
        @DisplayName("devrait accepter un pattern null")
        void devrait_accepter_pattern_null() {
            PatternLoadResult result = new PatternLoadResult(null, 120, 120);

            assertThat(result.pattern).isNull();
        }

        @Test
        @DisplayName("devrait accepter des valeurs BPM et tempo extrêmes")
        void devrait_accepter_valeurs_extremes() {
            PatternLoadResult slow = new PatternLoadResult(new ArrayList<>(), 1, 1);
            PatternLoadResult fast = new PatternLoadResult(new ArrayList<>(), 999, 999);

            assertThat(slow.bpm).isEqualTo(1);
            assertThat(slow.tempo).isEqualTo(1);
            assertThat(fast.bpm).isEqualTo(999);
            assertThat(fast.tempo).isEqualTo(999);
        }
    }

    @Nested
    @DisplayName("Champs publics finals")
    class ChampsPublics {

        @Test
        @DisplayName("les champs devraient être accessibles directement")
        void champs_accessibles() {
            List<PatternRow> pattern = List.of(new PatternRow("00"));
            PatternLoadResult result = new PatternLoadResult(pattern, 125, 130);

            // Accès direct sans getters
            assertThat(result.pattern).isNotNull();
            assertThat(result.bpm).isEqualTo(125);
            assertThat(result.tempo).isEqualTo(130);
        }

        @Test
        @DisplayName("la référence au pattern devrait être la même instance")
        void reference_pattern_meme_instance() {
            List<PatternRow> original = new ArrayList<>();
            original.add(new PatternRow("00"));

            PatternLoadResult result = new PatternLoadResult(original, 120, 120);

            // Modifier l'original devrait affecter le résultat (même référence)
            original.add(new PatternRow("01"));
            assertThat(result.pattern).hasSize(2);
        }
    }
}