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
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrackerController")
class TrackerControllerTest {

    @Mock
    private PatternService patternService;

    @Mock
    private PersistenceService persistenceService;

    @Mock
    private MidiPlaybackService midiService;

    private TrackerController controller;

    @BeforeEach
    void setUp() {
        controller = new TrackerController();
    }

    @Nested
    @DisplayName("Injection des services")
    class ServiceInjection {

        @Test
        @DisplayName("devrait accepter l'injection du PatternService")
        void devrait_injecter_pattern_service() throws Exception {
            controller.setPatternService(patternService);

            Field field = TrackerController.class.getDeclaredField("patternService");
            field.setAccessible(true);
            assertThat(field.get(controller)).isSameAs(patternService);
        }

        @Test
        @DisplayName("devrait accepter l'injection du PersistenceService")
        void devrait_injecter_persistence_service() throws Exception {
            controller.setPersistenceService(persistenceService);

            Field field = TrackerController.class.getDeclaredField("persistenceService");
            field.setAccessible(true);
            assertThat(field.get(controller)).isSameAs(persistenceService);
        }

        @Test
        @DisplayName("devrait accepter l'injection du MidiPlaybackService")
        void devrait_injecter_midi_service() throws Exception {
            controller.setMidiPlaybackService(midiService);

            Field field = TrackerController.class.getDeclaredField("midiService");
            field.setAccessible(true);
            assertThat(field.get(controller)).isSameAs(midiService);
        }

        @Test
        @DisplayName("devrait accepter null pour tous les services")
        void devrait_accepter_null() throws Exception {
            controller.setPatternService(null);
            controller.setPersistenceService(null);
            controller.setMidiPlaybackService(null);

            Field patternField = TrackerController.class.getDeclaredField("patternService");
            patternField.setAccessible(true);
            assertThat(patternField.get(controller)).isNull();

            Field persistenceField = TrackerController.class.getDeclaredField("persistenceService");
            persistenceField.setAccessible(true);
            assertThat(persistenceField.get(controller)).isNull();

            Field midiField = TrackerController.class.getDeclaredField("midiService");
            midiField.setAccessible(true);
            assertThat(midiField.get(controller)).isNull();
        }
    }

    @Nested
    @DisplayName("extractInstrumentId()")
    class ExtractInstrumentId {

        private String invokeExtractInstrumentId(String input) throws Exception {
            Method method = TrackerController.class.getDeclaredMethod("extractInstrumentId", String.class);
            method.setAccessible(true);
            return (String) method.invoke(controller, input);
        }

        @ParameterizedTest
        @CsvSource({
                "'00 - Acoustic Grand Piano', 00",
                "'01 - Bright Acoustic Piano', 01",
                "'32 - Acoustic Bass', 32",
                "'127 - Gunshot', 127"
        })
        @DisplayName("devrait extraire l'ID d'un affichage complet")
        void devrait_extraire_id(String input, String expectedId) throws Exception {
            assertThat(invokeExtractInstrumentId(input)).isEqualTo(expectedId);
        }

        @Test
        @DisplayName("devrait retourner '--' pour null")
        void devrait_retourner_tirets_pour_null() throws Exception {
            assertThat(invokeExtractInstrumentId(null)).isEqualTo("--");
        }

        @Test
        @DisplayName("devrait retourner '--' pour '--'")
        void devrait_retourner_tirets_pour_tirets() throws Exception {
            assertThat(invokeExtractInstrumentId("--")).isEqualTo("--");
        }

        @Test
        @DisplayName("devrait retourner l'entrée si pas de tiret")
        void devrait_retourner_entree_sans_tiret() throws Exception {
            assertThat(invokeExtractInstrumentId("00")).isEqualTo("00");
        }
    }

    @Nested
    @DisplayName("getInstrumentDisplay()")
    class GetInstrumentDisplay {

        private String invokeGetInstrumentDisplay(String input) throws Exception {
            Method method = TrackerController.class.getDeclaredMethod("getInstrumentDisplay", String.class);
            method.setAccessible(true);
            return (String) method.invoke(controller, input);
        }

        @ParameterizedTest
        @CsvSource({
                "00, 00 - Acoustic Grand Piano",
                "01, 01 - Bright Acoustic Piano",
                "32, 32 - Acoustic Bass",
                "73, 73 - Flute"
        })
        @DisplayName("devrait formater l'affichage avec le nom")
        void devrait_formater_affichage(String id, String expectedDisplay) throws Exception {
            assertThat(invokeGetInstrumentDisplay(id)).isEqualTo(expectedDisplay);
        }

        @Test
        @DisplayName("devrait retourner '--' pour null")
        void devrait_retourner_tirets_pour_null() throws Exception {
            assertThat(invokeGetInstrumentDisplay(null)).isEqualTo("--");
        }

        @Test
        @DisplayName("devrait retourner '--' pour '--'")
        void devrait_retourner_tirets_pour_tirets() throws Exception {
            assertThat(invokeGetInstrumentDisplay("--")).isEqualTo("--");
        }

        @Test
        @DisplayName("devrait gérer un ID inconnu")
        void devrait_gerer_id_inconnu() throws Exception {
            String result = invokeGetInstrumentDisplay("999");
            assertThat(result).contains("999");
            assertThat(result).contains("Unknown");
        }
    }

    @Nested
    @DisplayName("getEffectDisplay()")
    class GetEffectDisplay {

        private String invokeGetEffectDisplay(String input) throws Exception {
            Method method = TrackerController.class.getDeclaredMethod("getEffectDisplay", String.class);
            method.setAccessible(true);
            return (String) method.invoke(controller, input);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"---"})
        @DisplayName("devrait retourner '---' pour valeurs vides ou null")
        void devrait_retourner_tirets_pour_vide(String input) throws Exception {
            assertThat(invokeGetEffectDisplay(input)).isEqualTo("---");
        }

        @Test
        @DisplayName("devrait extraire le type d'effet A")
        void devrait_extraire_effet_a() throws Exception {
            String result = invokeGetEffectDisplay("A50");
            assertThat(result).startsWith("A - ");
        }

        @Test
        @DisplayName("devrait extraire le type d'effet C")
        void devrait_extraire_effet_c() throws Exception {
            String result = invokeGetEffectDisplay("C40");
            assertThat(result).startsWith("C - ");
        }

        @Test
        @DisplayName("devrait être insensible à la casse")
        void devrait_etre_insensible_casse() throws Exception {
            String resultLower = invokeGetEffectDisplay("a50");
            String resultUpper = invokeGetEffectDisplay("A50");
            assertThat(resultLower).isEqualTo(resultUpper);
        }
    }

    @Nested
    @DisplayName("getEffectParams()")
    class GetEffectParams {

        private String invokeGetEffectParams(String input) throws Exception {
            Method method = TrackerController.class.getDeclaredMethod("getEffectParams", String.class);
            method.setAccessible(true);
            return (String) method.invoke(controller, input);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"---", "A"})
        @DisplayName("devrait retourner '00' pour valeurs vides, null ou trop courtes")
        void devrait_retourner_00_pour_invalide(String input) throws Exception {
            assertThat(invokeGetEffectParams(input)).isEqualTo("00");
        }

        @ParameterizedTest
        @CsvSource({
                "A50, 50",
                "C40, 40",
                "F08, 08",
                "E9A, 9A"
        })
        @DisplayName("devrait extraire les paramètres correctement")
        void devrait_extraire_params(String input, String expectedParams) throws Exception {
            assertThat(invokeGetEffectParams(input)).isEqualTo(expectedParams);
        }

        @Test
        @DisplayName("devrait extraire tous les caractères après le premier")
        void devrait_extraire_tous_chars() throws Exception {
            assertThat(invokeGetEffectParams("ABCD")).isEqualTo("BCD");
        }
    }

    @Nested
    @DisplayName("Valeurs par défaut")
    class DefaultValues {

        @Test
        @DisplayName("bpm devrait être DEFAULT_BPM (125)")
        void bpm_default() throws Exception {
            Field field = TrackerController.class.getDeclaredField("bpm");
            field.setAccessible(true);
            assertThat(field.get(controller)).isEqualTo(125);
        }

        @Test
        @DisplayName("currentRowIndex devrait être 0")
        void current_row_index_default() throws Exception {
            Field field = TrackerController.class.getDeclaredField("currentRowIndex");
            field.setAccessible(true);
            assertThat(field.get(controller)).isEqualTo(0);
        }

        @Test
        @DisplayName("selectedChannel devrait être 1")
        void selected_channel_default() throws Exception {
            Field field = TrackerController.class.getDeclaredField("selectedChannel");
            field.setAccessible(true);
            assertThat(field.get(controller)).isEqualTo(1);
        }

        @Test
        @DisplayName("selectedRow devrait être null")
        void selected_row_default() throws Exception {
            Field field = TrackerController.class.getDeclaredField("selectedRow");
            field.setAccessible(true);
            assertThat(field.get(controller)).isNull();
        }

        @Test
        @DisplayName("isUpdatingFields devrait être false")
        void is_updating_fields_default() throws Exception {
            Field field = TrackerController.class.getDeclaredField("isUpdatingFields");
            field.setAccessible(true);
            assertThat(field.get(controller)).isEqualTo(false);
        }
    }

    @Nested
    @DisplayName("Symétrie extractInstrumentId / getInstrumentDisplay")
    class InstrumentSymmetry {

        private String invokeExtractInstrumentId(String input) throws Exception {
            Method method = TrackerController.class.getDeclaredMethod("extractInstrumentId", String.class);
            method.setAccessible(true);
            return (String) method.invoke(controller, input);
        }

        private String invokeGetInstrumentDisplay(String input) throws Exception {
            Method method = TrackerController.class.getDeclaredMethod("getInstrumentDisplay", String.class);
            method.setAccessible(true);
            return (String) method.invoke(controller, input);
        }

        @ParameterizedTest
        @ValueSource(strings = {"00", "01", "32", "73", "127"})
        @DisplayName("extract(display(id)) devrait retourner l'id original")
        void devrait_etre_symetrique(String id) throws Exception {
            String display = invokeGetInstrumentDisplay(id);
            String extracted = invokeExtractInstrumentId(display);
            assertThat(extracted).isEqualTo(id);
        }
    }

    // Note: Les tests de cleanup() sont omis car cette méthode appelle
    // Platform.runLater() qui nécessite l'initialisation du toolkit JavaFX.
    // Pour tester cleanup(), utiliser TestFX.
}