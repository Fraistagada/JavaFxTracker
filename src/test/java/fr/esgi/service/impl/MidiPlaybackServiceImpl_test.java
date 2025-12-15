package fr.esgi.service.impl;

import fr.esgi.service.SchedulerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(MockitoExtension.class)
@DisplayName("MidiPlaybackServiceImpl")
class MidiPlaybackServiceImplTest {

    @Mock
    private SchedulerService schedulerService;

    private MidiPlaybackServiceImpl service;

    @BeforeEach
    void setUp() {
        try {
            service = new MidiPlaybackServiceImpl(schedulerService);
        } catch (Exception e) {
            // MIDI non disponible sur cette machine (CI/CD, etc.)
            assumeTrue(false, "MIDI non disponible, tests ignorés: " + e.getMessage());
        }
    }

    @Nested
    @DisplayName("État initial")
    class EtatInitial {

        @Test
        @DisplayName("isPlaying devrait être false au démarrage")
        void isPlaying_false_initial() {
            assertThat(service.isPlaying()).isFalse();
        }
    }

    @Nested
    @DisplayName("stop()")
    class Stop {

        @Test
        @DisplayName("devrait mettre isPlaying à false")
        void stop_met_isPlaying_false() {
            service.stop();
            assertThat(service.isPlaying()).isFalse();
        }

        @Test
        @DisplayName("devrait pouvoir être appelé plusieurs fois")
        void stop_multiple() {
            service.stop();
            service.stop();
            service.stop();
            // Pas d'exception = succès
        }
    }

    @Nested
    @DisplayName("pause()")
    class Pause {

        @Test
        @DisplayName("devrait mettre isPlaying à false (paused)")
        void pause_met_isPlaying_false() {
            service.pause();
            // isPlaying retourne isPlaying && !isPaused
            assertThat(service.isPlaying()).isFalse();
        }
    }

    @Nested
    @DisplayName("resume()")
    class Resume {

        @Test
        @DisplayName("ne devrait pas lever d'exception")
        void resume_pas_exception() {
            service.resume();
            // Pas d'exception = succès
        }
    }

    @Nested
    @DisplayName("cleanup()")
    class Cleanup {

        @Test
        @DisplayName("devrait appeler stop()")
        void cleanup_appelle_stop() {
            service.cleanup();
            assertThat(service.isPlaying()).isFalse();
        }

        @Test
        @DisplayName("devrait pouvoir être appelé plusieurs fois")
        void cleanup_multiple() {
            service.cleanup();
            service.cleanup();
            // Pas d'exception = succès
        }
    }

    @Nested
    @DisplayName("noteToMidi()")
    class NoteToMidi {

        private int invokeNoteToMidi(String note, int octave) throws Exception {
            Method method = MidiPlaybackServiceImpl.class.getDeclaredMethod("noteToMidi", String.class, int.class);
            method.setAccessible(true);
            return (int) method.invoke(service, note, octave);
        }

        @ParameterizedTest
        @CsvSource({
                "C, 4, 60",    // Middle C
                "A, 4, 69",    // A440
                "C, 0, 12",
                "C, -1, 0",    // Lowest MIDI note
                "G, 9, 127"    // Near highest
        })
        @DisplayName("devrait convertir les notes correctement")
        void devrait_convertir_notes(String note, int octave, int expectedMidi) throws Exception {
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
        @DisplayName("devrait convertir les dièses correctement")
        void devrait_convertir_dieses(String note, int octave, int expectedMidi) throws Exception {
            assertThat(invokeNoteToMidi(note, octave)).isEqualTo(expectedMidi);
        }

        @Test
        @DisplayName("devrait avoir 12 demi-tons par octave")
        void devrait_avoir_12_demitons() throws Exception {
            int c4 = invokeNoteToMidi("C", 4);
            int c5 = invokeNoteToMidi("C", 5);

            assertThat(c5 - c4).isEqualTo(12);
        }
    }

    @Nested
    @DisplayName("Cycle de vie")
    class CycleDeVie {

        @Test
        @DisplayName("stop après pause devrait fonctionner")
        void stop_apres_pause() {
            service.pause();
            service.stop();
            assertThat(service.isPlaying()).isFalse();
        }

        @Test
        @DisplayName("resume après stop ne devrait pas changer isPlaying")
        void resume_apres_stop() {
            service.stop();
            service.resume();
            assertThat(service.isPlaying()).isFalse();
        }

        @Test
        @DisplayName("pause puis resume puis stop")
        void pause_resume_stop() {
            service.pause();
            service.resume();
            service.stop();
            assertThat(service.isPlaying()).isFalse();
        }
    }
}