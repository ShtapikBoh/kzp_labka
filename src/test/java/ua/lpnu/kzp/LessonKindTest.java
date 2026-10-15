package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class LessonKindTest {

    @Test
    void hasExactlyTwoValuesInOrder() {
        assertEquals(2, LessonKind.values().length);
        assertSame(LessonKind.LECTURE, LessonKind.values()[0]);
        assertSame(LessonKind.PRACTICE, LessonKind.values()[1]);
    }

    @Test
    void labelsAreUkrainian() {
        assertEquals("Лекція", LessonKind.LECTURE.getLabel());
        assertEquals("Практика", LessonKind.PRACTICE.getLabel());
    }

    @Test
    void byDurationSwitchesAtThreshold() {
        assertEquals(LessonKind.PRACTICE, LessonKind.byDuration(1));
        assertEquals(LessonKind.PRACTICE, LessonKind.byDuration(LessonKind.LECTURE_MIN_DURATION - 1));
        assertEquals(LessonKind.LECTURE, LessonKind.byDuration(LessonKind.LECTURE_MIN_DURATION));
        assertEquals(LessonKind.LECTURE, LessonKind.byDuration(240));
    }

    @Test
    void valueOfFindsConstantByName() {
        assertSame(LessonKind.LECTURE, LessonKind.valueOf("LECTURE"));
        assertSame(LessonKind.PRACTICE, LessonKind.valueOf("PRACTICE"));
    }
}
