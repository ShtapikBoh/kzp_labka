package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class LessonPolymorphismTest {

    @Test
    void subtypesCalculateResourcesDifferently() {
        Lesson lecture = new Lecture("Програмування", "Олексів М. В.", "Понеділок", 101, 90);
        Lesson practice = new Practice("Програмування", "Олексів М. В.", "Понеділок", 101, 90);

        assertEquals(2, lecture.requiredResources());
        assertEquals(5, practice.requiredResources());
        assertNotEquals(lecture.requiredResources(), practice.requiredResources());
        assertNotEquals(lecture.kind(), practice.kind());
    }

    @Test
    void factoryPicksSubtypeByDuration() {
        assertInstanceOf(Practice.class, Lesson.of("А", "Б", "В", 1, 89));
        assertInstanceOf(Lecture.class, Lesson.of("А", "Б", "В", 1, 90));
        assertInstanceOf(Lecture.class, Lesson.of("А", "Б", "В", 1, 91));
    }

    @Test
    void fromCsvReturnsSubtypeThroughBaseType() {
        Lesson lecture = Lesson.fromCsv("Кросплатформні засоби;Рудик Ю. І.;Вівторок;215;120");
        Lesson practice = Lesson.fromCsv("Бази даних;Коваленко О. П.;Середа;98;80");

        assertInstanceOf(Lecture.class, lecture);
        assertInstanceOf(Practice.class, practice);
        assertEquals(3, lecture.requiredResources());
        assertEquals(5, practice.requiredResources());
    }

    @Test
    void mixedListIsProcessedThroughBaseType() {
        List<Lesson> lessons = new ArrayList<>();
        lessons.add(new Lecture("А", "Б", "В", 1, 90));
        lessons.add(new Practice("Г", "Д", "Е", 2, 80));
        lessons.add(new Lecture("Є", "Ж", "З", 3, 120));

        int total = 0;
        for (Lesson lesson : lessons) {
            total += lesson.requiredResources();
        }
        assertEquals(2 + 5 + 3, total);
    }

    @Test
    void emptyListGivesZeroResources() {
        List<Lesson> lessons = List.of();
        assertEquals(0, lessons.stream().mapToInt(Lesson::requiredResources).sum());
    }

    @Test
    void resourcesGrowWithDuration() {
        Lesson shortLecture = new Lecture("А", "Б", "В", 1, 90);
        Lesson longLecture = new Lecture("А", "Б", "В", 1, 180);
        assertTrue(longLecture.requiredResources() > shortLecture.requiredResources());
    }

    @Test
    void reportShowsResourcesAndCountsByKind() {
        String report = Main.buildReport(List.of(
                "Програмування;Олексів М. В.;Понеділок;101;90",
                "Кросплатформні засоби;Рудик Ю. І.;Вівторок;215;120",
                "Бази даних;Коваленко О. П.;Середа;98;80"));

        assertTrue(report.contains("Сумарна потреба в ресурсах: 10"));
        assertTrue(report.contains("Лекція: 2"));
        assertTrue(report.contains("Практика: 1"));
    }

    @Test
    void reportWithoutValidLinesShowsZeros() {
        String report = Main.buildReport(List.of(""));
        assertTrue(report.contains("Сумарна потреба в ресурсах: 0"));
        assertTrue(report.contains("Лекція: 0"));
        assertTrue(report.contains("Практика: 0"));
    }
}
