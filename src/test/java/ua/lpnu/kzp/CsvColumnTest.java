package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.Test;

class CsvColumnTest {

    @Test
    void isAvailableAtRuntime() {
        Retention retention = CsvColumn.class.getAnnotation(Retention.class);
        assertEquals(RetentionPolicy.RUNTIME, retention.value());
    }

    @Test
    void canBeAppliedOnlyToFields() {
        Target target = CsvColumn.class.getAnnotation(Target.class);
        assertArrayEquals(new ElementType[] {ElementType.FIELD}, target.value());
    }

    @Test
    void valueIsReadFromAnnotatedField() throws NoSuchFieldException {
        CsvColumn column = LessonRecord.class.getDeclaredField("subject").getAnnotation(CsvColumn.class);
        assertEquals("предмет", column.value());
    }

    @Test
    void everyExportedFieldOfLessonRecordIsAnnotated() {
        long annotated = java.util.Arrays.stream(LessonRecord.class.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(CsvColumn.class))
                .count();
        assertEquals(5, annotated);
    }
}
