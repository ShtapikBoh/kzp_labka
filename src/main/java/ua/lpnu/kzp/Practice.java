package ua.lpnu.kzp;

/** Практичне заняття: потребує двох ресурсів на академічну годину та одного для підготовки. */
public final class Practice extends Lesson {

    /**
     * Створює практичне заняття та перевіряє спільні інваріанти заняття.
     *
     * @param subject назва предмета
     * @param teacher викладач
     * @param day день тижня
     * @param room номер аудиторії
     * @param durationMin тривалість у хвилинах
     */
    public Practice(String subject, String teacher, String day, int room, int durationMin) {
        super(subject, teacher, day, room, durationMin);
    }

    @Override
    public LessonKind kind() {
        return LessonKind.PRACTICE;
    }

    @Override
    public int requiredResources() {
        return academicHours() * 2 + 1;
    }
}
