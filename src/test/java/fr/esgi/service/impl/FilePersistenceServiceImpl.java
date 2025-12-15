package fr.esgi.service.impl;

import fr.esgi.models.PatternRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("FilePersistenceServiceImpl")
class FilePersistenceServiceImplTest {

    private FilePersistenceServiceImpl service;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        service = new FilePersistenceServiceImpl();
    }

    @Nested
    @DisplayName("savePattern()")
    class SavePattern {

        @Test
        @DisplayName("devrait créer un fichier avec le contenu correct")
        void devrait_creer_fichier() throws Exception {
            File file = tempDir.resolve("test.trk").toFile();
            List<PatternRow> pattern = createTestPattern();

            service.savePattern(file, pattern, 125, 130);

            assertThat(file).exists();
            String content = Files.readString(file.toPath());
            assertThat(content).contains("BPM=125");
            assertThat(content).contains("TEMPO=130");
            assertThat(content).contains("CHANNELS=2");
        }

        @Test
        @DisplayName("devrait écrire l'en-tête correct")
        void devrait_ecrire_entete() throws Exception {
            File file = tempDir.resolve("test.trk").toFile();
            List<PatternRow> pattern = createTestPattern();

            service.savePattern(file, pattern, 120, 120);

            List<String> lines = Files.readAllLines(file.toPath());
            assertThat(lines.get(0)).isEqualTo("BPM=120");
            assertThat(lines.get(1)).isEqualTo("TEMPO=120");
            assertThat(lines.get(2)).isEqualTo("CHANNELS=2");
            assertThat(lines.get(3)).isEqualTo("ROW;N1;O1;I1;V1;FX1;N2;O2;I2;V2;FX2");
        }

        @Test
        @DisplayName("devrait sauvegarder toutes les lignes du pattern")
        void devrait_sauvegarder_lignes() throws Exception {
            File file = tempDir.resolve("test.trk").toFile();
            List<PatternRow> pattern = new ArrayList<>();
            pattern.add(new PatternRow("00", "C", "4", "00", "100", "A50",
                    "G", "2", "32", "80", "---"));
            pattern.add(new PatternRow("01"));

            service.savePattern(file, pattern, 120, 120);

            List<String> lines = Files.readAllLines(file.toPath());
            // 4 lignes d'en-tête + 2 lignes de données
            assertThat(lines).hasSize(6);
            assertThat(lines.get(4)).isEqualTo("00;C;4;00;100;A50;G;2;32;80;---");
            assertThat(lines.get(5)).isEqualTo("01;---;-;--;--;---;---;-;--;--;---");
        }

        @Test
        @DisplayName("devrait gérer un pattern vide")
        void devrait_gerer_pattern_vide() throws Exception {
            File file = tempDir.resolve("test.trk").toFile();
            List<PatternRow> pattern = new ArrayList<>();

            service.savePattern(file, pattern, 120, 120);

            List<String> lines = Files.readAllLines(file.toPath());
            assertThat(lines).hasSize(4); // Juste l'en-tête
        }
    }

    @Nested
    @DisplayName("loadPattern()")
    class LoadPattern {

        @Test
        @DisplayName("devrait charger BPM et tempo")
        void devrait_charger_bpm_tempo() throws Exception {
            File file = createTestFile(125, 130);

            PatternLoadResult result = service.loadPattern(file);

            assertThat(result.bpm).isEqualTo(125);
            assertThat(result.tempo).isEqualTo(130);
        }

        @Test
        @DisplayName("devrait charger le pattern complet")
        void devrait_charger_pattern() throws Exception {
            File file = createTestFile(120, 120);

            PatternLoadResult result = service.loadPattern(file);

            assertThat(result.pattern).hasSize(2);

            PatternRow row0 = result.pattern.get(0);
            assertThat(row0.getRow()).isEqualTo("00");
            assertThat(row0.getNote(1)).isEqualTo("C");
            assertThat(row0.getOctave(1)).isEqualTo("4");
            assertThat(row0.getInstrument(1)).isEqualTo("00");
            assertThat(row0.getVolume(1)).isEqualTo("100");
            assertThat(row0.getEffect(1)).isEqualTo("A50");

            assertThat(row0.getNote(2)).isEqualTo("G");
            assertThat(row0.getOctave(2)).isEqualTo("2");
        }

        @Test
        @DisplayName("devrait utiliser des valeurs par défaut si en-tête manquant")
        void devrait_utiliser_defauts() throws Exception {
            File file = tempDir.resolve("invalid.trk").toFile();
            Files.writeString(file.toPath(), "INVALID\nINVALID\nINVALID\nINVALID\n");

            PatternLoadResult result = service.loadPattern(file);

            assertThat(result.bpm).isEqualTo(120); // Valeur par défaut
            assertThat(result.tempo).isEqualTo(120);
        }

        @Test
        @DisplayName("devrait ignorer les lignes avec un nombre incorrect de colonnes")
        void devrait_ignorer_lignes_invalides() throws Exception {
            File file = tempDir.resolve("partial.trk").toFile();
            String content = """
                    BPM=120
                    TEMPO=120
                    CHANNELS=2
                    ROW;N1;O1;I1;V1;FX1;N2;O2;I2;V2;FX2
                    00;C;4;00;100;A50;G;2;32;80;---
                    INVALID_LINE
                    02;E;4
                    """;
            Files.writeString(file.toPath(), content);

            PatternLoadResult result = service.loadPattern(file);

            // Seule la première ligne valide devrait être chargée
            assertThat(result.pattern).hasSize(1);
        }

        @Test
        @DisplayName("devrait lever une exception si le fichier n'existe pas")
        void devrait_lever_exception_fichier_inexistant() {
            File file = new File(tempDir.toFile(), "inexistant.trk");

            assertThatThrownBy(() -> service.loadPattern(file))
                    .isInstanceOf(IOException.class);
        }
    }

    @Nested
    @DisplayName("Cycle save/load (round-trip)")
    class RoundTrip {

        @Test
        @DisplayName("devrait préserver les données après save puis load")
        void devrait_preserver_donnees() throws Exception {
            File file = tempDir.resolve("roundtrip.trk").toFile();
            List<PatternRow> original = new ArrayList<>();
            original.add(new PatternRow("00", "C", "4", "00", "100", "A50",
                    "G", "2", "32", "80", "F08"));
            original.add(new PatternRow("01", "D#", "5", "24", "90", "---",
                    "---", "-", "--", "--", "---"));

            service.savePattern(file, original, 140, 145);
            PatternLoadResult result = service.loadPattern(file);

            assertThat(result.bpm).isEqualTo(140);
            assertThat(result.tempo).isEqualTo(145);
            assertThat(result.pattern).hasSize(2);

            PatternRow loaded0 = result.pattern.get(0);
            assertThat(loaded0.getNote(1)).isEqualTo("C");
            assertThat(loaded0.getOctave(1)).isEqualTo("4");
            assertThat(loaded0.getInstrument(1)).isEqualTo("00");
            assertThat(loaded0.getEffect(1)).isEqualTo("A50");
            assertThat(loaded0.getNote(2)).isEqualTo("G");
            assertThat(loaded0.getEffect(2)).isEqualTo("F08");

            PatternRow loaded1 = result.pattern.get(1);
            assertThat(loaded1.getNote(1)).isEqualTo("D#");
            assertThat(loaded1.hasNote(2)).isFalse();
        }
    }

    @Nested
    @DisplayName("exportMidi()")
    class ExportMidi {

        @Test
        @DisplayName("devrait créer un fichier MIDI")
        void devrait_creer_fichier_midi() throws Exception {
            File file = tempDir.resolve("test.mid").toFile();
            List<PatternRow> pattern = createTestPattern();

            service.exportMidi(file, pattern, 120);

            assertThat(file).exists();
            assertThat(file.length()).isGreaterThan(0);
        }

        @Test
        @DisplayName("devrait créer un fichier MIDI valide (magic number)")
        void devrait_creer_midi_valide() throws Exception {
            File file = tempDir.resolve("test.mid").toFile();
            List<PatternRow> pattern = createTestPattern();

            service.exportMidi(file, pattern, 120);

            byte[] bytes = Files.readAllBytes(file.toPath());
            // Les fichiers MIDI commencent par "MThd"
            assertThat(bytes[0]).isEqualTo((byte) 'M');
            assertThat(bytes[1]).isEqualTo((byte) 'T');
            assertThat(bytes[2]).isEqualTo((byte) 'h');
            assertThat(bytes[3]).isEqualTo((byte) 'd');
        }

        @Test
        @DisplayName("devrait gérer un pattern vide")
        void devrait_gerer_pattern_vide() throws Exception {
            File file = tempDir.resolve("empty.mid").toFile();
            List<PatternRow> pattern = new ArrayList<>();

            service.exportMidi(file, pattern, 120);

            assertThat(file).exists();
        }

        @Test
        @DisplayName("devrait gérer un pattern avec uniquement canal 1")
        void devrait_gerer_canal1_seul() throws Exception {
            File file = tempDir.resolve("mono.mid").toFile();
            List<PatternRow> pattern = new ArrayList<>();
            PatternRow row = new PatternRow("00");
            row.setSound(1, "C", "4");
            row.setInstrument(1, "00");
            row.setVolume(1, "100");
            pattern.add(row);

            service.exportMidi(file, pattern, 120);

            assertThat(file).exists();
            assertThat(file.length()).isGreaterThan(0);
        }
    }

    // Helper methods

    private List<PatternRow> createTestPattern() {
        List<PatternRow> pattern = new ArrayList<>();
        PatternRow row = new PatternRow("00");
        row.setSound(1, "C", "4");
        row.setInstrument(1, "00");
        row.setVolume(1, "100");
        row.setSound(2, "G", "2");
        row.setInstrument(2, "32");
        row.setVolume(2, "80");
        pattern.add(row);
        return pattern;
    }

    private File createTestFile(int bpm, int tempo) throws IOException {
        File file = tempDir.resolve("test.trk").toFile();
        String content = """
                BPM=%d
                TEMPO=%d
                CHANNELS=2
                ROW;N1;O1;I1;V1;FX1;N2;O2;I2;V2;FX2
                00;C;4;00;100;A50;G;2;32;80;---
                01;---;-;--;--;---;---;-;--;--;---
                """.formatted(bpm, tempo);
        Files.writeString(file.toPath(), content);
        return file;
    }
}