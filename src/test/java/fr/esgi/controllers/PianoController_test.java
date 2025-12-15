package fr.esgi.controllers;

import fr.esgi.service.MidiPlaybackService;
import fr.esgi.service.PatternService;
import fr.esgi.service.PersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(MockitoExtension.class)
@DisplayName("PianoController")
class PianoControllerTest {

    @Mock
    private PatternService patternService;

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private MidiPlaybackService midiService;

    private PianoController controller;

    @BeforeEach
    void setUp() {
        try {
            controller = new PianoController();
        } catch (Exception e) {
            // MIDI non disponible sur cette machine (CI/CD, etc.)
            assumeTrue(false, "MIDI non disponible, tests ignorés: " + e.getMessage());
        }
    }

    @Nested
    @DisplayName("Injection des services")
    class ServiceInjection {

        @Test
        @DisplayName("devrait accepter l'injection du PatternService")
        void devrait_injecter_pattern_service() throws Exception {
            controller.setPatternService(patternService);

            Field field = PianoController.class.getDeclaredField("patternService");
            field.setAccessible(true);
            assertThat(field.get(controller)).isSameAs(patternService);
        }

        @Test
        @DisplayName("devrait accepter l'injection du PersistenceService")
        void devrait_injecter_persistence_service() throws Exception {
            controller.setPersistenceService(persistenceService);

            Field field = PianoController.class.getDeclaredField("persistenceService");
            field.setAccessible(true);
            assertThat(field.get(controller)).isSameAs(persistenceService);
        }

        @Test
        @DisplayName("devrait accepter l'injection du MidiPlaybackService")
        void devrait_injecter_midi_service() throws Exception {
            controller.setMidiPlaybackService(midiService);

            Field field = PianoController.class.getDeclaredField("midiService");
            field.setAccessible(true);
            assertThat(field.get(controller)).isSameAs(midiService);
        }

        @Test
        @DisplayName("devrait accepter null pour les services")
        void devrait_accepter_null() throws Exception {
            controller.setPatternService(null);
            controller.setPersistenceService(null);
            controller.setMidiPlaybackService(null);

            Field patternField = PianoController.class.getDeclaredField("patternService");
            patternField.setAccessible(true);
            assertThat(patternField.get(controller)).isNull();

            Field persistenceField = PianoController.class.getDeclaredField("persistenceService");
            persistenceField.setAccessible(true);
            assertThat(persistenceField.get(controller)).isNull();

            Field midiField = PianoController.class.getDeclaredField("midiService");
            midiField.setAccessible(true);
            assertThat(midiField.get(controller)).isNull();
        }
    }

    @Nested
    @DisplayName("noteToMidi()")
    class NoteToMidi {

        private int invokeNoteToMidi(String note, int octave) throws Exception {
            Method method = PianoController.class.getDeclaredMethod("noteToMidi", String.class, int.class);
            method.setAccessible(true);
            return (int) method.invoke(null, note, octave);
        }

        @ParameterizedTest
        @CsvSource({
                "C, 4, 60",   // C4 = Middle C = 60
                "A, 4, 69",   // A4 = 440Hz = 69
                "C, 0, 12",   // C0 = 12
                "C, -1, 0",   // C-1 = 0 (note MIDI la plus basse)
                "G, 9, 127"   // G9 = 127 (proche de la note MIDI la plus haute)
        })
        @DisplayName("devrait convertir les notes standard correctement")
        void devrait_convertir_notes_standard(String note, int octave, int expectedMidi) throws Exception {
            assertThat(invokeNoteToMidi(note, octave)).isEqualTo(expectedMidi);
        }

        @ParameterizedTest
        @CsvSource({
                "C#, 4, 61",
                "D#, 4, 63",
                "F#, 4, 66",
                "G#, 4, 68",
                "A#, 4, 70"
        })
        @DisplayName("devrait convertir les notes avec dièse correctement")
        void devrait_convertir_notes_dieses(String note, int octave, int expectedMidi) throws Exception {
            assertThat(invokeNoteToMidi(note, octave)).isEqualTo(expectedMidi);
        }

        @ParameterizedTest
        @CsvSource({
                "C, 2, 36",
                "C, 3, 48",
                "C, 4, 60",
                "C, 5, 72"
        })
        @DisplayName("devrait respecter les octaves du piano (2-5)")
        void devrait_respecter_octaves_piano(String note, int octave, int expectedMidi) throws Exception {
            assertThat(invokeNoteToMidi(note, octave)).isEqualTo(expectedMidi);
        }

        @Test
        @DisplayName("devrait avoir 12 demi-tons entre deux octaves")
        void devrait_avoir_12_demitons_par_octave() throws Exception {
            int c4 = invokeNoteToMidi("C", 4);
            int c5 = invokeNoteToMidi("C", 5);
            assertThat(c5 - c4).isEqualTo(12);
        }

        @Test
        @DisplayName("devrait avoir des demi-tons consécutifs")
        void devrait_avoir_demitons_consecutifs() throws Exception {
            int c4 = invokeNoteToMidi("C", 4);
            int csharp4 = invokeNoteToMidi("C#", 4);
            int d4 = invokeNoteToMidi("D", 4);

            assertThat(csharp4 - c4).isEqualTo(1);
            assertThat(d4 - csharp4).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Styles des touches")
    class KeyStyles {

        private String invokeGetWhiteKeyStyle(boolean pressed) throws Exception {
            Method method = PianoController.class.getDeclaredMethod("getWhiteKeyStyle", boolean.class);
            method.setAccessible(true);
            return (String) method.invoke(controller, pressed);
        }

        private String invokeGetBlackKeyStyle(boolean pressed) throws Exception {
            Method method = PianoController.class.getDeclaredMethod("getBlackKeyStyle", boolean.class);
            method.setAccessible(true);
            return (String) method.invoke(controller, pressed);
        }

        @Test
        @DisplayName("touche blanche non pressée devrait être blanche")
        void touche_blanche_non_pressee() throws Exception {
            String style = invokeGetWhiteKeyStyle(false);
            assertThat(style).contains("-fx-background-color: white");
        }

        @Test
        @DisplayName("touche blanche pressée devrait être grise")
        void touche_blanche_pressee() throws Exception {
            String style = invokeGetWhiteKeyStyle(true);
            assertThat(style).contains("-fx-background-color: #cccccc");
        }

        @Test
        @DisplayName("touche noire non pressée devrait être très sombre")
        void touche_noire_non_pressee() throws Exception {
            String style = invokeGetBlackKeyStyle(false);
            assertThat(style).contains("-fx-background-color: #1a1a1a");
        }

        @Test
        @DisplayName("touche noire pressée devrait être gris foncé")
        void touche_noire_pressee() throws Exception {
            String style = invokeGetBlackKeyStyle(true);
            assertThat(style).contains("-fx-background-color: #444444");
        }

        @Test
        @DisplayName("les styles devraient contenir les bordures")
        void styles_avec_bordures() throws Exception {
            String whiteStyle = invokeGetWhiteKeyStyle(false);
            String blackStyle = invokeGetBlackKeyStyle(false);

            assertThat(whiteStyle).contains("-fx-border-color:");
            assertThat(blackStyle).contains("-fx-border-color:");
        }

        @Test
        @DisplayName("les styles devraient avoir des coins arrondis en bas")
        void styles_avec_coins_arrondis() throws Exception {
            String whiteStyle = invokeGetWhiteKeyStyle(false);
            String blackStyle = invokeGetBlackKeyStyle(false);

            assertThat(whiteStyle).contains("-fx-background-radius: 0 0 5 5");
            assertThat(blackStyle).contains("-fx-background-radius: 0 0 3 3");
        }
    }

    @Nested
    @DisplayName("Constantes du piano")
    class PianoConstants {

        @Test
        @DisplayName("devrait avoir 7 notes blanches")
        void devrait_avoir_7_notes_blanches() throws Exception {
            Field field = PianoController.class.getDeclaredField("WHITE_NOTES");
            field.setAccessible(true);
            String[] whiteNotes = (String[]) field.get(null);

            assertThat(whiteNotes).hasSize(7);
            assertThat(whiteNotes).containsExactly("C", "D", "E", "F", "G", "A", "B");
        }

        @Test
        @DisplayName("devrait définir correctement les touches noires")
        void devrait_definir_touches_noires() throws Exception {
            Field field = PianoController.class.getDeclaredField("HAS_BLACK_KEY");
            field.setAccessible(true);
            boolean[] hasBlackKey = (boolean[]) field.get(null);

            assertThat(hasBlackKey).hasSize(7);
            // C#, D#, pas de E#, F#, G#, A#, pas de B#
            assertThat(hasBlackKey).containsExactly(true, true, false, true, true, true, false);
        }

        @Test
        @DisplayName("devrait avoir 5 touches noires par octave")
        void devrait_avoir_5_touches_noires_par_octave() throws Exception {
            Field field = PianoController.class.getDeclaredField("HAS_BLACK_KEY");
            field.setAccessible(true);
            boolean[] hasBlackKey = (boolean[]) field.get(null);

            long blackKeyCount = 0;
            for (boolean has : hasBlackKey) {
                if (has) blackKeyCount++;
            }
            assertThat(blackKeyCount).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Valeurs par défaut")
    class DefaultValues {

        @Test
        @DisplayName("currentPattern devrait être 0 par défaut")
        void current_pattern_default() throws Exception {
            Field field = PianoController.class.getDeclaredField("currentPattern");
            field.setAccessible(true);
            assertThat(field.get(controller)).isEqualTo(0);
        }

        @Test
        @DisplayName("bpm devrait être DEFAULT_BPM par défaut")
        void bpm_default() throws Exception {
            Field field = PianoController.class.getDeclaredField("bpm");
            field.setAccessible(true);
            assertThat(field.get(controller)).isEqualTo(125); // Constants.DEFAULT_BPM
        }
    }

    @Nested
    @DisplayName("cleanup()")
    class Cleanup {

        @Test
        @DisplayName("devrait pouvoir être appelé sans erreur")
        void cleanup_sans_erreur() {
            // cleanup() ferme le synth et le scheduler
            // Ne devrait pas lever d'exception
            controller.cleanup();
        }

        @Test
        @DisplayName("devrait pouvoir être appelé plusieurs fois")
        void cleanup_multiple_fois() {
            controller.cleanup();
            controller.cleanup(); // Ne devrait pas lever d'exception
        }
    }
}