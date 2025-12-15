package fr.esgi.controllers;

import fr.esgi.service.MidiPlaybackService;
import fr.esgi.service.PatternService;
import fr.esgi.service.PersistenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreditsController")
class CreditsControllerTest {

    @Mock
    private PatternService patternService;

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private MidiPlaybackService midiService;

    private CreditsController controller;

    @BeforeEach
    void setUp() {
        controller = new CreditsController();
    }

    @Nested
    @DisplayName("Injection des services")
    class ServiceInjection {

        @Test
        @DisplayName("devrait accepter l'injection du PatternService")
        void devrait_injecter_pattern_service() throws Exception {
            controller.setPatternService(patternService);

            Field field = CreditsController.class.getDeclaredField("patternService");
            field.setAccessible(true);
            assertThat(field.get(controller)).isSameAs(patternService);
        }

        @Test
        @DisplayName("devrait accepter l'injection du PersistenceService")
        void devrait_injecter_persistence_service() throws Exception {
            controller.setPersistenceService(persistenceService);

            Field field = CreditsController.class.getDeclaredField("persistenceService");
            field.setAccessible(true);
            assertThat(field.get(controller)).isSameAs(persistenceService);
        }

        @Test
        @DisplayName("devrait accepter l'injection du MidiPlaybackService")
        void devrait_injecter_midi_service() throws Exception {
            controller.setMidiPlaybackService(midiService);

            Field field = CreditsController.class.getDeclaredField("midiService");
            field.setAccessible(true);
            assertThat(field.get(controller)).isSameAs(midiService);
        }

        @Test
        @DisplayName("devrait accepter null pour PatternService")
        void devrait_accepter_null_pattern_service() throws Exception {
            controller.setPatternService(null);

            Field field = CreditsController.class.getDeclaredField("patternService");
            field.setAccessible(true);
            assertThat(field.get(controller)).isNull();
        }

        @Test
        @DisplayName("devrait accepter null pour PersistenceService")
        void devrait_accepter_null_persistence_service() throws Exception {
            controller.setPersistenceService(null);

            Field field = CreditsController.class.getDeclaredField("persistenceService");
            field.setAccessible(true);
            assertThat(field.get(controller)).isNull();
        }

        @Test
        @DisplayName("devrait accepter null pour MidiPlaybackService")
        void devrait_accepter_null_midi_service() throws Exception {
            controller.setMidiPlaybackService(null);

            Field field = CreditsController.class.getDeclaredField("midiService");
            field.setAccessible(true);
            assertThat(field.get(controller)).isNull();
        }
    }
}