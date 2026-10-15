package ua.lpnu.kzp;

/** Лекція: потребує одного ресурсу (проєктор) на кожну академічну годину. */
public final class Lecture extends Lesson {

    /**
     * Створює лекцію та перевіряє спільні інваріанти заняття.
     *
     * @param subject назва предмета
     * @param teacher викладач
     * @param day день тижня
     * @param room номер аудиторії
     * @param durationMin тривалість у хвилинах
     */
    public Lecture(String subject, String teacher, String day, int room, int durationMin) {
        super(subject, teacher, day, room, durationMin);
    }

    @Override
    public LessonKind kind() {
        return LessonKind.LECTURE;
    }

    @Override
    public int requiredResources() {
        return academicHours();
    }
}
