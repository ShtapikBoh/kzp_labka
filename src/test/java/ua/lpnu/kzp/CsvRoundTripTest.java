package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvRoundTripTest {

    private static List<LessonRecord> sample() {
        return List.of(
                new LessonRecord("Програмування", "Олексів М. В.", "Понеділок", 1, 101),
                new LessonRecord("Кросплатформні засоби", "Рудик Ю. І.", "Вівторок", 1, 215),
                new LessonRecord("Бази даних", "Коваленко О. П.", "Середа", 2, 98),
                new LessonRecord("Мережі, протоколи", "Петренко В. Л.", "Четвер", 1, 305),
                new LessonRecord("Математика \"вища\"", "Сидоренко Н. Г.", "Понеділок", 2, 110),
                new LessonRecord("Фізика\n(лабораторна)", "Бойко А. А.", "П'ятниця", 3, 120));
    }

    @Test
    void restoredObjectsEqualOriginals(@TempDir Path dir) throws DataStorageException {
        Path file = dir.resolve("lessons.csv");
        List<LessonRecord> original = sample();

        CsvExporter.write(file, LessonRecord.class, original);
        List<LessonRecord> restored = CsvImporter.read(file, LessonRecord.class, LessonRecord::fromFields);

        assertEquals(original, restored);
    }

    @Test
    void multilineFieldMakesMorePhysicalLinesThanRecords(@TempDir Path dir)
            throws DataStorageException, IOException {
        Path file = dir.resolve("lessons.csv");
        CsvExporter.write(file, LessonRecord.class, sample());

        long physicalLines = Files.readAllLines(file, StandardCharsets.UTF_8).size();
        long records = CsvParser.parseDocument(Files.readString(file, StandardCharsets.UTF_8)).size();

        assertEquals(7, records);
        assertTrue(physicalLines > records);
    }

    @Test
    void keysAreUniqueAfterRestore(@TempDir Path dir) throws DataStorageException {
        Path file = dir.resolve("lessons.csv");
        CsvExporter.write(file, LessonRecord.class, sample());
        List<LessonRecord> restored = CsvImporter.read(file, LessonRecord.class, LessonRecord::fromFields);

        Set<String> keys = new HashSet<>();
        restored.forEach(record -> keys.add(record.getSubject() + "|" + record.getDay() + "|" + record.getPeriod()));
        assertEquals(sample().size(), keys.size());
    }

    @Test
    void worksThroughRepository(@TempDir Path dir) throws DataStorageException {
        Repository<LessonRecord> repository = new Repository<>();
        sample().forEach(repository::add);
        Path file = dir.resolve("lessons.csv");

        CsvExporter.write(file, LessonRecord.class, repository.all());

        assertEquals(repository.all(), CsvImporter.read(file, LessonRecord.class, LessonRecord::fromFields));
    }

    @Test
    void emptyCsvWithHeaderOnlyGivesNoObjects(@TempDir Path dir) throws DataStorageException {
        Path file = dir.resolve("empty.csv");
        CsvExporter.write(file, LessonRecord.class, List.of());

        assertTrue(CsvImporter.read(file, LessonRecord.class, LessonRecord::fromFields).isEmpty());
    }

    @Test
    void completelyEmptyFileIsError(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("blank.csv");
        Files.writeString(file, "", StandardCharsets.UTF_8);

        assertThrows(DataStorageException.class,
                () -> CsvImporter.read(file, LessonRecord.class, LessonRecord::fromFields));
    }

    @Test
    void wrongHeaderIsError(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("wrong.csv");
        Files.writeString(file, "a,b,c,d,e\n", StandardCharsets.UTF_8);

        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> CsvImporter.read(file, LessonRecord.class, LessonRecord::fromFields));
        assertTrue(exception.getMessage().contains("Заголовок"));
    }

    @Test
    void wrongFieldCountNamesRecordAndKeepsCause(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("broken.csv");
        Files.writeString(file, "день,пара,аудиторія,предмет,викладач\n"
                + "Понеділок,1,101,Програмування,Олексів М. В.\n"
                + "Вівторок,1,215\n", StandardCharsets.UTF_8);

        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> CsvImporter.read(file, LessonRecord.class, LessonRecord::fromFields));
        assertTrue(exception.getMessage().startsWith("Запис 2:"));
        assertInstanceOf(DataStorageException.class, exception.getCause());
    }

    @Test
    void badNumberHasNumberFormatRootCause(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("number.csv");
        Files.writeString(file, "день,пара,аудиторія,предмет,викладач\n"
                + "Понеділок,x,101,Програмування,Олексів М. В.\n", StandardCharsets.UTF_8);

        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> CsvImporter.read(file, LessonRecord.class, LessonRecord::fromFields));
        assertInstanceOf(NumberFormatException.class, exception.getCause().getCause());
    }

    @Test
    void missingFileGivesExceptionWithIoCause(@TempDir Path dir) {
        Path file = dir.resolve("немає.csv");

        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> CsvImporter.read(file, LessonRecord.class, LessonRecord::fromFields));
        assertInstanceOf(IOException.class, exception.getCause());
    }

    @Test
    void unclosedQuoteInFileIsError(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("quote.csv");
        Files.writeString(file, "день,пара,аудиторія,предмет,викладач\n\"Понеділок,1,101,А,Б\n",
                StandardCharsets.UTF_8);

        assertThrows(DataStorageException.class,
                () -> CsvImporter.read(file, LessonRecord.class, LessonRecord::fromFields));
    }
}
