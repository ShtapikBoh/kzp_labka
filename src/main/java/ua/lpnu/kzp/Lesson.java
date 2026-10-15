package ua.lpnu.kzp;

import java.util.Locale;

/** Заняття розкладу: предмет, викладач, день, аудиторія й тривалість. */
public final class Lesson {
    private static final int FIELD_COUNT = 5;

    private final String subject;
    private final String teacher;
    private final String day;
    private final int room;
    private final int durationMin;

    /**
     * Створює заняття та перевіряє всі інваріанти.
     *
     * @param subject назва предмета (не порожня)
     * @param teacher викладач (не порожній)
     * @param day день тижня (не порожній)
     * @param room номер аудиторії (більше 0)
     * @param durationMin тривалість у хвилинах (більше 0)
     * @throws IllegalArgumentException якщо будь-яке значення хибне
     */
    public Lesson(String subject, String teacher, String day, int room, int durationMin) {
        if (isBlank(subject) || isBlank(teacher) || isBlank(day)) {
            throw new IllegalArgumentException("порожній предмет, викладач або день");
        }
        if (room < 0 || durationMin < 0) {
            throw new IllegalArgumentException("від'ємне числове значення");
        }
        if (room == 0 || durationMin == 0) {
            throw new IllegalArgumentException("нульове числове значення");
        }
        this.subject = subject.trim();
        this.teacher = teacher.trim();
        this.day = day.trim();
        this.room = room;
        this.durationMin = durationMin;
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
        return new Lesson(fields[0], fields[1], fields[2], room, duration);
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

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s (%s, %s, ауд. %d, %d хв, %.2f год)",
                subject, teacher, day, room, durationMin, durationMin / 60.0);
    }
}
