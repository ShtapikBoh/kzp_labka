package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class LessonRecordTest {

    @Test
    void storesAndTrimsValues() {
        LessonRecord record = new LessonRecord(" Фізика ", " Бойко А. А. ", " Вівторок ", 2, 120);
        assertEquals("Фізика", record.getSubject());
        assertEquals("Бойко А. А.", record.getTeacher());
        assertEquals("Вівторок", record.getDay());
        assertEquals(2, record.getPeriod());
        assertEquals(120, record.getRoom());
    }

    @Test
    void acceptsBoundaryValueOne() {
        LessonRecord record = new LessonRecord("А", "Б", "В", 1, 1);
        assertEquals(1, record.getPeriod());
        assertEquals(1, record.getRoom());
    }

    @Test
    void rejectsBlankTextAndNonPositiveNumbers() {
        assertThrows(IllegalArgumentException.class, () -> new LessonRecord("", "Б", "В", 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new LessonRecord("А", " ", "В", 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new LessonRecord("А", "Б", null, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> new LessonRecord("А", "Б", "В", 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new LessonRecord("А", "Б", "В", 1, -1));
    }

    @Test
    void equalsAndHashCodeUseAllFields() {
        LessonRecord base = new LessonRecord("А", "Б", "В", 1, 2);
        assertEquals(base, new LessonRecord("А", "Б", "В", 1, 2));
        assertEquals(base.hashCode(), new LessonRecord("А", "Б", "В", 1, 2).hashCode());
        assertNotEquals(base, new LessonRecord("Я", "Б", "В", 1, 2));
        assertNotEquals(base, new LessonRecord("А", "Я", "В", 1, 2));
        assertNotEquals(base, new LessonRecord("А", "Б", "Я", 1, 2));
        assertNotEquals(base, new LessonRecord("А", "Б", "В", 2, 2));
        assertNotEquals(base, new LessonRecord("А", "Б", "В", 1, 3));
    }

    @Test
    void fromFieldsUsesColumnOrder() throws DataStorageException {
        LessonRecord record = LessonRecord.fromFields(List.of("Середа", "2", "98", "Бази даних", "Коваленко О. П."));
        assertEquals(new LessonRecord("Бази даних", "Коваленко О. П.", "Середа", 2, 98), record);
    }

    @Test
    void fromFieldsRejectsWrongFieldCount() {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> LessonRecord.fromFields(List.of("Середа", "2", "98")));
        assertEquals("очікується 5 полів, отримано 3", exception.getMessage());
    }

    @Test
    void fromFieldsKeepsNumberFormatCause() {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> LessonRecord.fromFields(List.of("Середа", "abc", "98", "Бази даних", "Т")));
        assertInstanceOf(NumberFormatException.class, exception.getCause());
    }

    @Test
    void fromFieldsKeepsValidationCause() {
        DataStorageException exception = assertThrows(DataStorageException.class,
                () -> LessonRecord.fromFields(List.of("Середа", "0", "98", "Бази даних", "Т")));
        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void fromLessonsNumbersPeriodsWithinEachDay() {
        List<Lesson> lessons = List.of(
                Lesson.of("А", "Т1", "Понеділок", 10, 90),
                Lesson.of("Б", "Т2", "Вівторок", 20, 80),
                Lesson.of("В", "Т3", "Понеділок", 30, 90));

        List<LessonRecord> records = LessonRecord.fromLessons(lessons);

        assertEquals(1, records.get(0).getPeriod());
        assertEquals(1, records.get(1).getPeriod());
        assertEquals(2, records.get(2).getPeriod());
        assertEquals(30, records.get(2).getRoom());
    }

    @Test
    void fromLessonsOfEmptyListIsEmpty() {
        assertEquals(List.of(), LessonRecord.fromLessons(List.of()));
    }
}
