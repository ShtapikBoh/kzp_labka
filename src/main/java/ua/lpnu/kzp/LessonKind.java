package ua.lpnu.kzp;

/** Вид заняття, який визначається за тривалістю. */
public enum LessonKind {
    /** Лекція. */
    LECTURE("Лекція"),
    /** Практичне заняття. */
    PRACTICE("Практика");

    /** Мінімальна тривалість (хв), починаючи з якої заняття вважається лекцією. */
    public static final int LECTURE_MIN_DURATION = 90;

    private final String label;

    LessonKind(String label) {
        this.label = label;
    }

    /**
     * Повертає українську назву виду заняття.
     *
     * @return назва для звіту
     */
    public String getLabel() {
        return label;
    }

    /**
     * Визначає вид заняття за тривалістю.
     *
     * @param durationMin тривалість у хвилинах
     * @return {@link #LECTURE}, якщо тривалість не менша за {@link #LECTURE_MIN_DURATION}, інакше {@link #PRACTICE}
     */
    public static LessonKind byDuration(int durationMin) {
        return durationMin >= LECTURE_MIN_DURATION ? LECTURE : PRACTICE;
    }
}
