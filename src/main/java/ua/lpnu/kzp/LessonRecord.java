package ua.lpnu.kzp;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Плоский запис розкладу для CSV: предмет, викладач, день, номер пари й аудиторія. */
public final class LessonRecord {
    /** Кількість полів запису; порядок у CSV — за алфавітом Java-імен: day, period, room, subject, teacher. */
    private static final int FIELD_COUNT = 5;

    @CsvColumn("предмет")
    private final String subject;
    @CsvColumn("викладач")
    private final String teacher;
    @CsvColumn("день")
    private final String day;
    @CsvColumn("пара")
    private final int period;
    @CsvColumn("аудиторія")
    private final int room;

    /**
     * Створює запис і перевіряє інваріанти.
     *
     * @param subject предмет (не порожній)
     * @param teacher викладач (не порожній)
     * @param day день тижня (не порожній)
     * @param period номер пари (більше 0)
     * @param room номер аудиторії (більше 0)
     * @throws IllegalArgumentException якщо значення хибне
     */
    public LessonRecord(String subject, String teacher, String day, int period, int room) {
        if (isBlank(subject) || isBlank(teacher) || isBlank(day)) {
            throw new IllegalArgumentException("предмет, викладач і день не можуть бути порожніми");
        }
        if (period <= 0 || room <= 0) {
            throw new IllegalArgumentException("пара й аудиторія мають бути більшими за 0");
        }
        this.subject = subject.trim();
        this.teacher = teacher.trim();
        this.day = day.trim();
        this.period = period;
        this.room = room;
    }

    /**
     * Відновлює запис із полів CSV у порядку стовпців (day, period, room, subject, teacher).
     *
     * @param fields значення полів одного запису
     * @return запис
     * @throws DataStorageException якщо кількість полів хибна, число некоректне або значення порушує інваріанти
     */
    public static LessonRecord fromFields(List<String> fields) throws DataStorageException {
        if (fields.size() != FIELD_COUNT) {
            throw new DataStorageException(
                    "очікується %d полів, отримано %d".formatted(FIELD_COUNT, fields.size()));
        }
        try {
            return new LessonRecord(fields.get(3), fields.get(4), fields.get(0),
                    Integer.parseInt(fields.get(1).trim()), Integer.parseInt(fields.get(2).trim()));
        } catch (NumberFormatException exception) {
            throw new DataStorageException("пара або аудиторія не є цілим числом", exception);
        } catch (IllegalArgumentException exception) {
            throw new DataStorageException(exception.getMessage(), exception);
        }
    }

    /**
     * Перетворює заняття на записи: номер пари — це порядковий номер заняття
     * в цей день за порядком у списку (перше заняття дня має пару 1).
     *
     * @param lessons заняття
     * @return записи в тому самому порядку
     */
    public static List<LessonRecord> fromLessons(List<? extends Lesson> lessons) {
        Map<String, Integer> perDay = new HashMap<>();
        return lessons.stream()
                .map(lesson -> new LessonRecord(lesson.getSubject(), lesson.getTeacher(), lesson.getDay(),
                        perDay.merge(lesson.getDay(), 1, Integer::sum), lesson.getRoom()))
                .toList();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Повертає предмет.
     *
     * @return предмет
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Повертає день тижня.
     *
     * @return день
     */
    public String getDay() {
        return day;
    }

    /**
     * Повертає номер пари.
     *
     * @return пара
     */
    public int getPeriod() {
        return period;
    }

    /**
     * Повертає номер аудиторії.
     *
     * @return аудиторія
     */
    public int getRoom() {
        return room;
    }

    /**
     * Повертає викладача.
     *
     * @return викладач
     */
    public String getTeacher() {
        return teacher;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        LessonRecord that = (LessonRecord) other;
        return period == that.period && room == that.room && subject.equals(that.subject)
                && teacher.equals(that.teacher) && day.equals(that.day);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, teacher, day, period, room);
    }

    @Override
    public String toString() {
        return "LessonRecord[subject=%s, teacher=%s, day=%s, period=%d, room=%d]"
                .formatted(subject, teacher, day, period, room);
    }
}
