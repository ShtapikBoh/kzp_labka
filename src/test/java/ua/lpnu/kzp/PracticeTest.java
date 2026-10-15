package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PracticeTest {

    @Test
    void acceptsValidFields() {
        Practice practice = new Practice("Бази даних", "Коваленко О. П.", "Середа", 98, 80);
        assertEquals("Бази даних", practice.getSubject());
        assertEquals(98, practice.getRoom());
        assertEquals(80, practice.getDurationMin());
    }

    @Test
    void kindIsPractice() {
        assertEquals(LessonKind.PRACTICE, new Practice("Бази даних", "Коваленко О. П.", "Середа", 98, 80).kind());
    }

    @Test
    void resourcesAreTwoPerAcademicHourPlusOne() {
        assertEquals(3, new Practice("А", "Б", "В", 1, 1).requiredResources());
        assertEquals(3, new Practice("А", "Б", "В", 1, 45).requiredResources());
        assertEquals(5, new Practice("А", "Б", "В", 1, 46).requiredResources());
        assertEquals(5, new Practice("А", "Б", "В", 1, 80).requiredResources());
        assertEquals(5, new Practice("А", "Б", "В", 1, 90).requiredResources());
    }

    @Test
    void rejectsBlankText() {
        assertThrows(IllegalArgumentException.class, () -> new Practice("", "Б", "В", 1, 60));
        assertThrows(IllegalArgumentException.class, () -> new Practice("А", "", "В", 1, 60));
        assertThrows(IllegalArgumentException.class, () -> new Practice("А", "Б", "  ", 1, 60));
    }

    @Test
    void rejectsNonPositiveNumbers() {
        assertThrows(IllegalArgumentException.class, () -> new Practice("А", "Б", "В", 0, 60));
        assertThrows(IllegalArgumentException.class, () -> new Practice("А", "Б", "В", -5, 60));
        assertThrows(IllegalArgumentException.class, () -> new Practice("А", "Б", "В", 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Practice("А", "Б", "В", 1, -1));
    }
}
