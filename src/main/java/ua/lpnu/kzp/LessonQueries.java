package ua.lpnu.kzp;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/** Запити Stream API до колекції занять. Жоден метод не змінює вхідний список. */
public final class LessonQueries {
    /** Порядок для топ-N: тривалість спадаючи, потім предмет за алфавітом. */
    private static final Comparator<Lesson> TOP_ORDER =
            Comparator.comparingInt(Lesson::getDurationMin).reversed()
                    .thenComparing(Lesson::getSubject);

    /* Забороняє створення екземплярів службового класу. */
    private LessonQueries() {
    }

    /**
     * Відбирає заняття визначеного дня (filter).
     *
     * @param lessons заняття
     * @param day день тижня
     * @return заняття цього дня в початковому порядку
     */
    public static List<Lesson> filterByDay(List<Lesson> lessons, String day) {
        return lessons.stream()
                .filter(lesson -> lesson.getDay().equalsIgnoreCase(day))
                .toList();
    }

    /**
     * Перетворює заняття на назви предметів (map).
     *
     * @param lessons заняття
     * @return назви предметів у початковому порядку
     */
    public static List<String> subjectNames(List<Lesson> lessons) {
        return lessons.stream()
                .map(Lesson::getSubject)
                .toList();
    }

    /**
     * Групує заняття за викладачем і рахує їх кількість (groupingBy).
     *
     * @param lessons заняття
     * @return кількість занять за викладачем
     */
    public static Map<String, Long> countByTeacher(List<Lesson> lessons) {
        return lessons.stream()
                .collect(Collectors.groupingBy(Lesson::getTeacher, Collectors.counting()));
    }

    /**
     * Рахує кількість занять за видом; види без занять отримують 0 (groupingBy з EnumMap).
     *
     * @param lessons заняття
     * @return кількість занять за {@link LessonKind}
     */
    public static Map<LessonKind, Long> countByKind(List<Lesson> lessons) {
        Map<LessonKind, Long> counts = lessons.stream()
                .collect(Collectors.groupingBy(Lesson::kind,
                        () -> new EnumMap<>(LessonKind.class), Collectors.counting()));
        for (LessonKind kind : LessonKind.values()) {
            counts.putIfAbsent(kind, 0L);
        }
        return counts;
    }

    /**
     * Зводить статистику за тривалістю: кількість, сума, мінімум, середнє, максимум
     * (summarizingInt).
     *
     * @param lessons заняття
     * @return статистика тривалості; для порожнього списку кількість дорівнює 0
     */
    public static IntSummaryStatistics durationStatistics(List<Lesson> lessons) {
        return lessons.stream()
                .collect(Collectors.summarizingInt(Lesson::getDurationMin));
    }

    /**
     * Повертає перші {@code n} занять за найдовшою тривалістю, а за рівної
     * тривалості — за назвою предмета (sorted, limit).
     *
     * @param lessons заняття
     * @param n скільки занять повернути (не менше 0)
     * @return не більше {@code n} занять
     * @throws IllegalArgumentException якщо {@code n} від'ємне
     */
    public static List<Lesson> topLessons(List<Lesson> lessons, int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n не може бути від'ємним");
        }
        return lessons.stream()
                .sorted(TOP_ORDER)
                .limit(n)
                .toList();
    }

    /**
     * Шукає перше заняття в аудиторії.
     *
     * @param lessons заняття
     * @param room номер аудиторії
     * @return заняття або порожній {@link Optional}, якщо такої аудиторії немає
     */
    public static Optional<Lesson> findByRoom(List<Lesson> lessons, int room) {
        return lessons.stream()
                .filter(lesson -> lesson.getRoom() == room)
                .findFirst();
    }

    /**
     * Рахує сумарну потребу в ресурсах через поліморфний метод.
     *
     * @param lessons заняття
     * @return сума {@link Lesson#requiredResources()}
     */
    public static int totalResources(List<Lesson> lessons) {
        return lessons.stream()
                .mapToInt(Lesson::requiredResources)
                .sum();
    }

    /**
     * Знаходить найменший номер аудиторії.
     *
     * @param lessons заняття
     * @return найменша аудиторія або порожній {@link Optional} для порожнього списку
     */
    public static Optional<Integer> minRoom(List<Lesson> lessons) {
        return lessons.stream()
                .map(Lesson::getRoom)
                .min(Comparator.naturalOrder());
    }

    /**
     * Знаходить найдовше заняття; за рівної тривалості повертає перше за порядком.
     *
     * @param lessons заняття
     * @return найдовше заняття або порожній {@link Optional} для порожнього списку
     */
    public static Optional<Lesson> longestLesson(List<Lesson> lessons) {
        return lessons.stream()
                .max(Comparator.comparingInt(Lesson::getDurationMin));
    }
}
