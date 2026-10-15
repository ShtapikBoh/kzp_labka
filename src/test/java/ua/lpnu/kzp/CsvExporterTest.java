package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvExporterTest {
    private static final String NL = System.lineSeparator();

    /** Клас без жодного знання про розклад: доводить універсальність експортера. */
    static class Animal {
        @CsvColumn("вид")
        private final String species;
        @CsvColumn("вік")
        private final int age;
        private final String ignored = "не експортується";

        Animal(String species, int age) {
            this.species = species;
            this.age = age;
        }
    }

    static class Pet extends Animal {
        @CsvColumn("кличка")
        private final String name;

        Pet(String species, int age, String name) {
            super(species, age);
            this.name = name;
        }
    }

    static class NoColumns {
        private final String value = "x";

        String value() {
            return value;
        }
    }

    @Test
    void headerFollowsSortedJavaNamesWithNamesFromAnnotation() throws DataStorageException {
        assertEquals("день,пара,аудиторія,предмет,викладач", CsvExporter.headerOf(LessonRecord.class));
    }

    @Test
    void exportsLessonRecordRow() throws DataStorageException {
        String csv = CsvExporter.toCsv(LessonRecord.class,
                List.of(new LessonRecord("Програмування", "Олексів М. В.", "Понеділок", 1, 101)));
        assertEquals("день,пара,аудиторія,предмет,викладач" + NL
                + "Понеділок,1,101,Програмування,Олексів М. В." + NL, csv);
    }

    @Test
    void worksWithAnyClassAndIgnoresUnannotatedFields() throws DataStorageException {
        String csv = CsvExporter.toCsv(Animal.class, List.of(new Animal("кіт", 3)));
        assertEquals("вік,вид" + NL + "3,кіт" + NL, csv);
    }

    @Test
    void includesAnnotatedFieldsOfSuperclass() throws DataStorageException {
        String csv = CsvExporter.toCsv(Pet.class, List.of(new Pet("пес", 5, "Рекс")));
        assertEquals("вік,кличка,вид" + NL + "5,Рекс,пес" + NL, csv);
    }

    @Test
    void emptyListStillGivesHeader() throws DataStorageException {
        assertEquals("вік,вид" + NL, CsvExporter.toCsv(Animal.class, List.of()));
    }

    @Test
    void nullValueBecomesEmptyField() throws DataStorageException {
        assertEquals("вік,вид" + NL + "1," + NL, CsvExporter.toCsv(Animal.class, List.of(new Animal(null, 1))));
    }

    @Test
    void classWithoutAnnotatedFieldsIsError() {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> CsvExporter.toCsv(NoColumns.class, List.of(new NoColumns())));
        assertTrue(exception.getMessage().contains("@CsvColumn"));
    }

    @Test
    void escapesCommaQuoteAndNewline() {
        assertEquals("звичайний", CsvExporter.escape("звичайний"));
        assertEquals("\"а,б\"", CsvExporter.escape("а,б"));
        assertEquals("\"він \"\"так\"\"\"", CsvExporter.escape("він \"так\""));
        assertEquals("\"рядок 1\nрядок 2\"", CsvExporter.escape("рядок 1\nрядок 2"));
        assertEquals("\"а\rб\"", CsvExporter.escape("а\rб"));
        assertEquals("", CsvExporter.escape(""));
    }

    @Test
    void doesNotChangeRecordsList() throws DataStorageException {
        List<Animal> animals = List.of(new Animal("кіт", 3));
        CsvExporter.toCsv(Animal.class, animals);
        assertEquals(1, animals.size());
    }

    @Test
    void writesUtf8File(@TempDir Path dir) throws DataStorageException, IOException {
        Path file = dir.resolve("out.csv");
        CsvExporter.write(file, LessonRecord.class,
                List.of(new LessonRecord("Фізика", "Бойко А. А.", "Вівторок", 2, 120)));

        byte[] bytes = Files.readAllBytes(file);
        String text = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(text.contains("Фізика"));
        assertTrue(text.startsWith("день,пара"));
    }

    @Test
    void replacesOldContentOfExistingFile(@TempDir Path dir) throws DataStorageException, IOException {
        Path file = dir.resolve("out.csv");
        CsvExporter.write(file, Animal.class, List.of(new Animal("кіт", 1), new Animal("пес", 2)));
        CsvExporter.write(file, Animal.class, List.of(new Animal("миша", 3)));

        assertEquals("вік,вид" + NL + "3,миша" + NL, Files.readString(file, StandardCharsets.UTF_8));
    }

    @Test
    void missingParentDirectoryGivesExceptionWithIoCause(@TempDir Path dir) {
        Path file = dir.resolve("немає").resolve("out.csv");
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> CsvExporter.write(file, Animal.class, List.of()));
        assertInstanceOf(IOException.class, exception.getCause());
        assertTrue(exception.getMessage().contains("out.csv"));
    }
}
