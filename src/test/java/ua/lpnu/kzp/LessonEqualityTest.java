package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LessonEqualityTest {

    @Test
    void equalObjectsHaveSameHashCode() {
        Lesson first = new Lecture("Програмування", "Олексів М. В.", "Понеділок", 101, 90);
        Lesson second = new Lecture("Програмування", "Олексів М. В.", "Понеділок", 101, 90);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void equalsIsReflexiveSymmetricAndRejectsNullAndOtherTypes() {
        Lesson first = new Practice("Бази даних", "Коваленко О. П.", "Середа", 98, 80);
        Lesson second = new Practice("Бази даних", "Коваленко О. П.", "Середа", 98, 80);

        assertEquals(first, first);
        assertEquals(first, second);
        assertEquals(second, first);
        assertFalse(first.equals(null));
        assertFalse(first.equals("Бази даних"));
    }

    @Test
    void trimmedTextGivesEqualObjects() {
        assertEquals(new Lecture(" А ", "Б", "В", 1, 90), new Lecture("А", "Б", "В", 1, 90));
    }

    @Test
    void differentSubtypesWithSameDataAreNotEqual() {
        Lesson lecture = new Lecture("А", "Б", "В", 1, 90);
        Lesson practice = new Practice("А", "Б", "В", 1, 90);

        assertNotEquals(lecture, practice);
        assertNotEquals(practice, lecture);
    }

    @Test
    void changingAnySingleFieldBreaksEquality() {
        Lesson base = new Lecture("А", "Б", "В", 1, 90);

        assertNotEquals(base, new Lecture("Я", "Б", "В", 1, 90));
        assertNotEquals(base, new Lecture("А", "Я", "В", 1, 90));
        assertNotEquals(base, new Lecture("А", "Б", "Я", 1, 90));
        assertNotEquals(base, new Lecture("А", "Б", "В", 2, 90));
        assertNotEquals(base, new Lecture("А", "Б", "В", 1, 91));
    }

    @Test
    void hashSetDoesNotDuplicateEqualObjects() {
        Set<Lesson> lessons = new HashSet<>();
        lessons.add(new Lecture("Програмування", "Олексів М. В.", "Понеділок", 101, 90));
        lessons.add(new Lecture("Програмування", "Олексів М. В.", "Понеділок", 101, 90));

        assertEquals(1, lessons.size());
    }

    @Test
    void hashSetKeepsDifferentObjectsAndSubtypes() {
        Set<Lesson> lessons = new HashSet<>();
        lessons.add(new Lecture("А", "Б", "В", 1, 90));
        lessons.add(new Practice("А", "Б", "В", 1, 90));
        lessons.add(new Lecture("А", "Б", "В", 2, 90));

        assertEquals(3, lessons.size());
    }

    @Test
    void hashSetFindsObjectCreatedFromCsv() {
        Set<Lesson> lessons = new HashSet<>();
        lessons.add(Lesson.fromCsv("Фізика;Бойко А. А.;Вівторок;120;60"));

        assertTrue(lessons.contains(new Practice("Фізика", "Бойко А. А.", "Вівторок", 120, 60)));
        assertFalse(lessons.contains(new Practice("Фізика", "Бойко А. А.", "Вівторок", 121, 60)));
    }

    @Test
    void hashMapUsesLessonAsKey() {
        Map<Lesson, String> rooms = new HashMap<>();
        rooms.put(new Lecture("А", "Б", "В", 1, 90), "перший");
        rooms.put(new Lecture("А", "Б", "В", 1, 90), "другий");

        assertEquals(1, rooms.size());
        assertEquals("другий", rooms.get(new Lecture("А", "Б", "В", 1, 90)));
    }
}
