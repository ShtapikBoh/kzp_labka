package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainExportTest {
    private static final List<String> LINES = List.of(
            "Програмування;Олексів М. В.;Понеділок;101;90",
            "Кросплатформні засоби;Рудик Ю. І.;Вівторок;215;120",
            "Бази даних;Коваленко О. П.;Середа;98;80",
            "",
            "Мережі;Петренко В. Л.;П'ятниця;abc;90");

    @Test
    void exportRoundTripReportsMatchAndSkipsInvalidLines(@TempDir Path dir) throws DataStorageException {
        String message = Main.exportRoundTrip(LINES, dir.resolve("lessons.csv"));

        assertTrue(message.contains("записів: 3"));
        assertTrue(message.contains("round-trip: збігається"));
    }

    @Test
    void exportOfNoValidLinesWritesHeaderOnly(@TempDir Path dir) throws DataStorageException, IOException {
        Path file = dir.resolve("lessons.csv");

        String message = Main.exportRoundTrip(List.of(""), file);

        assertTrue(message.contains("записів: 0"));
        assertEquals(1, Files.readAllLines(file, StandardCharsets.UTF_8).size());
    }

    @Test
    void exportToMissingDirectoryFails(@TempDir Path dir) {
        Path file = dir.resolve("немає").resolve("lessons.csv");

        assertThrows(DataStorageException.class, () -> Main.exportRoundTrip(LINES, file));
    }

    @Test
    void parseLessonsCollectsErrorsWithLineNumbers() {
        List<String> errors = new ArrayList<>();

        List<Lesson> lessons = Main.parseLessons(LINES, errors);

        assertEquals(3, lessons.size());
        assertEquals(List.of("Рядок 4: порожній рядок",
                "Рядок 5: числове поле має помилковий формат"), errors);
    }

    @Test
    void mainWithExportCreatesReportAndCsv(@TempDir Path dir) throws IOException {
        Path input = dir.resolve("input.csv");
        Files.write(input, LINES, StandardCharsets.UTF_8);
        Path report = dir.resolve("out").resolve("report.txt");
        Path csv = dir.resolve("out").resolve("lessons.csv");

        Main.main(new String[] {"--input", input.toString(), "--output", report.toString(),
            "--export", csv.toString()});

        assertTrue(Files.readString(report, StandardCharsets.UTF_8).contains("Коректних записів: 3"));
        assertTrue(Files.readString(csv, StandardCharsets.UTF_8).startsWith("день,пара,аудиторія,предмет,викладач"));
    }

    @Test
    void mainWithoutExportDoesNotCreateCsv(@TempDir Path dir) throws IOException {
        Path input = dir.resolve("input.csv");
        Files.write(input, LINES, StandardCharsets.UTF_8);
        Path report = dir.resolve("report.txt");

        Main.main(new String[] {"--input", input.toString(), "--output", report.toString()});

        assertTrue(Files.exists(report));
        try (var files = Files.list(dir)) {
            assertEquals(2, files.count());
        }
    }
}
