package ua.lpnu.kzp;

import java.util.Locale;
import java.util.Objects;

/**
 * Заняття розкладу: спільні поля та правила для {@link Lecture} і {@link Practice}.
 *
 * <p>Підтипи відрізняються лише реалізацією {@link #kind()} і {@link #requiredResources()}.
 */
public abstract sealed class Lesson permits Lecture, Practice {
    private static final int ACADEMIC_HOUR_MIN = 45;
    private static final int FIELD_COUNT = 5;

    private final String subject;
    private final String teacher;
    private final String day;
    private final int room;
    private final int durationMin;

    /**
     * Перевіряє всі інваріанти заняття (викликається з конструкторів підтипів).
     *
     * @param subject назва предмета (не порожня)
     * @param teacher викладач (не порожній)
     * @param day день тижня (не порожній)
     * @param room номер аудиторії (більше 0)
     * @param durationMin тривалість у хвилинах (більше 0)
     * @throws IllegalArgumentException якщо будь-яке значення хибне
     */
    protected Lesson(String subject, String teacher, String day, int room, int durationMin) {
        this(subject, teacher, day, room, durationMin, checkValues(subject, teacher, day, room, durationMin));
    }

    /* Приймає вже перевірені значення; перевірку виконано в аргументі, до створення об'єкта. */
    private Lesson(String subject, String teacher, String day, int room, int durationMin, boolean checked) {
        this.subject = subject.trim();
        this.teacher = teacher.trim();
        this.day = day.trim();
        this.room = room;
        this.durationMin = durationMin;
    }

    /* Кидає IllegalArgumentException, якщо будь-яке значення порушує інваріанти. */
    private static boolean checkValues(String subject, String teacher, String day, int room, int durationMin) {
        if (isBlank(subject) || isBlank(teacher) || isBlank(day)) {
            throw new IllegalArgumentException("порожній предмет, викладач або день");
        }
        if (room < 0 || durationMin < 0) {
            throw new IllegalArgumentException("від'ємне числове значення");
        }
        if (room == 0 || durationMin == 0) {
            throw new IllegalArgumentException("нульове числове значення");
        }
        return true;
    }

    /**
     * Створює заняття потрібного підтипу за тривалістю (див. {@link LessonKind#byDuration(int)}).
     *
     * @param subject назва предмета
     * @param teacher викладач
     * @param day день тижня
     * @param room номер аудиторії
     * @param durationMin тривалість у хвилинах
     * @return {@link Lecture} або {@link Practice}
     * @throws IllegalArgumentException якщо будь-яке значення хибне
     */
    public static Lesson of(String subject, String teacher, String day, int room, int durationMin) {
        return switch (LessonKind.byDuration(durationMin)) {
            case LECTURE -> new Lecture(subject, teacher, day, room, durationMin);
            case PRACTICE -> new Practice(subject, teacher, day, room, durationMin);
        };
    }

    /**
     * Створює заняття з одного рядка CSV формату
     * {@code subject;teacher;day;room;durationMin}.
     *
     * @param line рядок вхідного файлу
     * @return коректне заняття
     * @throws IllegalArgumentException якщо рядок порожній або має хибний формат чи значення
     */
    public static Lesson fromCsv(String line) {
        if (line == null || line.isBlank()) {
            throw new IllegalArgumentException("порожній рядок");
        }
        String[] fields = line.split(";", -1);
        if (fields.length != FIELD_COUNT) {
            throw new IllegalArgumentException(
                    "очікується %d полів, отримано %d".formatted(FIELD_COUNT, fields.length));
        }
        int room;
        int duration;
        try {
            room = Integer.parseInt(fields[3].trim());
            duration = Integer.parseInt(fields[4].trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("числове поле має помилковий формат", exception);
        }
        return of(fields[0], fields[1], fields[2], room, duration);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Повертає назву предмета.
     *
     * @return предмет
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Повертає викладача.
     *
     * @return викладач
     */
    public String getTeacher() {
        return teacher;
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
     * Повертає номер аудиторії.
     *
     * @return аудиторія
     */
    public int getRoom() {
        return room;
    }

    /**
     * Повертає тривалість заняття.
     *
     * @return хвилини
     */
    public int getDurationMin() {
        return durationMin;
    }

    /**
     * Повертає аудиторію й тривалість як незмінне значення.
     *
     * @return {@link RoomDuration} цього заняття
     */
    public RoomDuration roomDuration() {
        return new RoomDuration(room, durationMin);
    }

    /**
     * Повертає вид заняття.
     *
     * @return значення {@link LessonKind}
     */
    public abstract LessonKind kind();

    /**
     * Повертає кількість ресурсів, потрібних для проведення заняття.
     *
     * @return кількість одиниць ресурсів
     */
    public abstract int requiredResources();

    /**
     * Повертає кількість академічних годин (по 45 хв), округлену вгору.
     *
     * @return академічні години
     */
    protected final int academicHours() {
        return (durationMin + ACADEMIC_HOUR_MIN - 1) / ACADEMIC_HOUR_MIN;
    }

    @Override
    public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        Lesson that = (Lesson) other;
        return room == that.room && durationMin == that.durationMin
                && subject.equals(that.subject) && teacher.equals(that.teacher) && day.equals(that.day);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getClass(), subject, teacher, day, room, durationMin);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s (%s, %s, ауд. %d, %d хв, %.2f год)",
                subject, teacher, day, room, durationMin, durationMin / 60.0);
    }
}
