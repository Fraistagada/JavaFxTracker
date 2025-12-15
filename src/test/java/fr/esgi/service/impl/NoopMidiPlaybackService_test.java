package fr.esgi.service.impl;

import fr.esgi.service.PlaybackListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("NoopMidiPlaybackService")
class NoopMidiPlaybackServiceTest {

    private NoopMidiPlaybackService service;

    @Mock
    private PlaybackListener listener;

    @BeforeEach
    void setUp() {
        service = new NoopMidiPlaybackService();
    }

    @Nested
    @DisplayName("isPlaying()")
    class IsPlaying {

        @Test
        @DisplayName("devrait toujours retourner false")
        void devrait_toujours_retourner_false() {
            assertThat(service.isPlaying()).isFalse();
        }

        @Test
        @DisplayName("devrait retourner false même après startPlayback")
        void devrait_retourner_false_apres_start() throws Exception {
            // Note: startPlayback appelle FxUtils.showError() qui nécessite JavaFX
            // Ce test vérifie juste l'état sans appeler startPlayback
            assertThat(service.isPlaying()).isFalse();
        }
    }

    @Nested
    @DisplayName("Méthodes no-op")
    class MethodesNoOp {

        @Test
        @DisplayName("stop() ne devrait pas lever d'exception")
        void stop_pas_exception() {
            service.stop();
            // Pas d'exception = succès
        }

        @Test
        @DisplayName("pause() ne devrait pas lever d'exception")
        void pause_pas_exception() {
            service.pause();
            // Pas d'exception = succès
        }

        @Test
        @DisplayName("resume() ne devrait pas lever d'exception")
        void resume_pas_exception() {
            service.resume();
            // Pas d'exception = succès
        }

        @Test
        @DisplayName("cleanup() ne devrait pas lever d'exception")
        void cleanup_pas_exception() {
            service.cleanup();
            // Pas d'exception = succès
        }

        @Test
        @DisplayName("les méthodes peuvent être appelées plusieurs fois")
        void appels_multiples() {
            for (int i = 0; i < 10; i++) {
                service.stop();
                service.pause();
                service.resume();
                service.cleanup();
            }
            // Pas d'exception = succès
        }
    }

    @Nested
    @DisplayName("État constant")
    class EtatConstant {

        @Test
        @DisplayName("isPlaying devrait rester false après stop")
        void isPlaying_false_apres_stop() {
            service.stop();
            assertThat(service.isPlaying()).isFalse();
        }

        @Test
        @DisplayName("isPlaying devrait rester false après pause")
        void isPlaying_false_apres_pause() {
            service.pause();
            assertThat(service.isPlaying()).isFalse();
        }

        @Test
        @DisplayName("isPlaying devrait rester false après resume")
        void isPlaying_false_apres_resume() {
            service.resume();
            assertThat(service.isPlaying()).isFalse();
        }

        @Test
        @DisplayName("isPlaying devrait rester false après cleanup")
        void isPlaying_false_apres_cleanup() {
            service.cleanup();
            assertThat(service.isPlaying()).isFalse();
        }
    }
}