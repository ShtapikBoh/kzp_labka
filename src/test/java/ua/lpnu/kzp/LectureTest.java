package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LectureTest {

    @Test
    void acceptsValidFields() {
        Lecture lecture = new Lecture("Програмування", "Олексів М. В.", "Понеділок", 101, 90);
        assertEquals("Програмування", lecture.getSubject());
        assertEquals(101, lecture.getRoom());
        assertEquals(90, lecture.getDurationMin());
    }

    @Test
    void kindIsLecture() {
        assertEquals(LessonKind.LECTURE, new Lecture("Програмування", "Олексів М. В.", "Понеділок", 101, 90).kind());
    }

    @Test
    void resourcesEqualAcademicHours() {
        assertEquals(1, new Lecture("А", "Б", "В", 1, 1).requiredResources());
        assertEquals(1, new Lecture("А", "Б", "В", 1, 45).requiredResources());
        assertEquals(2, new Lecture("А", "Б", "В", 1, 46).requiredResources());
        assertEquals(2, new Lecture("А", "Б", "В", 1, 90).requiredResources());
        assertEquals(3, new Lecture("А", "Б", "В", 1, 91).requiredResources());
    }

    @Test
    void rejectsBlankText() {
        assertThrows(IllegalArgumentException.class, () -> new Lecture("", "Б", "В", 1, 90));
        assertThrows(IllegalArgumentException.class, () -> new Lecture("А", " ", "В", 1, 90));
        assertThrows(IllegalArgumentException.class, () -> new Lecture("А", "Б", null, 1, 90));
    }

    @Test
    void rejectsNonPositiveNumbers() {
        assertThrows(IllegalArgumentException.class, () -> new Lecture("А", "Б", "В", 0, 90));
        assertThrows(IllegalArgumentException.class, () -> new Lecture("А", "Б", "В", -1, 90));
        assertThrows(IllegalArgumentException.class, () -> new Lecture("А", "Б", "В", 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Lecture("А", "Б", "В", 1, -90));
    }
}
