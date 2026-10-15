package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LessonTest {

    @Test
    void constructorStoresValidValues() {
        Lesson lesson = new Lesson(" Бази даних ", "Коваленко О. П.", "Середа", 98, 80);
        assertEquals("Бази даних", lesson.getSubject());
        assertEquals("Коваленко О. П.", lesson.getTeacher());
        assertEquals("Середа", lesson.getDay());
        assertEquals(98, lesson.getRoom());
        assertEquals(80, lesson.getDurationMin());
    }

    @Test
    void constructorAcceptsBoundaryValueOne() {
        Lesson lesson = new Lesson("Фізика", "Бойко А. А.", "Вівторок", 1, 1);
        assertEquals(1, lesson.getRoom());
        assertEquals(1, lesson.getDurationMin());
    }

    @Test
    void constructorRejectsBlankAndNullText() {
        assertThrows(IllegalArgumentException.class,
                () -> new Lesson("", "Бойко А. А.", "Вівторок", 10, 60));
        assertThrows(IllegalArgumentException.class,
                () -> new Lesson("Фізика", "  ", "Вівторок", 10, 60));
        assertThrows(IllegalArgumentException.class,
                () -> new Lesson("Фізика", "Бойко А. А.", null, 10, 60));
    }

    @Test
    void constructorRejectsNonPositiveNumbers() {
        assertThrows(IllegalArgumentException.class,
                () -> new Lesson("Фізика", "Бойко А. А.", "Вівторок", -1, 60));
        assertThrows(IllegalArgumentException.class,
                () -> new Lesson("Фізика", "Бойко А. А.", "Вівторок", 10, -5));
        assertThrows(IllegalArgumentException.class,
                () -> new Lesson("Фізика", "Бойко А. А.", "Вівторок", 0, 60));
        assertThrows(IllegalArgumentException.class,
                () -> new Lesson("Фізика", "Бойко А. А.", "Вівторок", 10, 0));
    }

    @Test
    void fromCsvParsesValidLine() {
        Lesson lesson = Lesson.fromCsv("Програмування;Олексів М. В.;Понеділок;101;90");
        assertEquals("Програмування", lesson.getSubject());
        assertEquals(101, lesson.getRoom());
        assertEquals(90, lesson.getDurationMin());
    }

    @Test
    void fromCsvReportsReasonForInvalidLines() {
        assertEquals("порожній рядок", messageOf(""));
        assertEquals("очікується 5 полів, отримано 4", messageOf("Фізика;Бойко А. А.;Вівторок;120"));
        assertEquals("числове поле має помилковий формат", messageOf("Мережі;Петренко В. Л.;П'ятниця;abc;90"));
        assertEquals("від'ємне числове значення", messageOf("Математика;Сидоренко Н. Г.;Понеділок;110;-45"));
        assertEquals("порожній предмет, викладач або день", messageOf(";Іваненко Т. С.;Четвер;305;90"));
    }

    @Test
    void roomDurationExposesRoomAndDuration() {
        RoomDuration value = new Lesson("Фізика", "Бойко А. А.", "Вівторок", 120, 60).roomDuration();
        assertEquals(new RoomDuration(120, 60), value);
    }

    @Test
    void toStringUsesRootLocaleForDecimals() {
        Lesson lesson = new Lesson("Фізика", "Бойко А. А.", "Вівторок", 120, 90);
        assertEquals("Фізика (Бойко А. А., Вівторок, ауд. 120, 90 хв, 1.50 год)", lesson.toString());
    }

    private static String messageOf(String line) {
        return assertThrows(IllegalArgumentException.class, () -> Lesson.fromCsv(line)).getMessage();
    }
}
