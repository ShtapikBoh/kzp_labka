package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Консольна програма «Розклад занять»: читає записи, перевіряє їх і формує звіт. */
public final class Main {
    private static final String VERSION = "1.3.0";

    /* Забороняє створення екземплярів службового класу. */
    private Main() {
    }

    /**
     * Точка входу до програми.
     *
     * @param args аргументи командного рядка
     */
    public static void main(String[] args) {
        String inputPath = "data/input.csv";
        String outputPath = "out/report.txt";
        String exportPath = null;

        for (int i = 0; i < args.length; i++) {
            if ("--help".equals(args[i])) {
                System.out.printf(Locale.ROOT,
                        "Використання: java -jar lab01.jar [--help] [--version] [--input <файл>] [--output <файл>] [--export <файл.csv>]%n");
                return;
            }
            if ("--version".equals(args[i])) {
                System.out.printf(Locale.ROOT, "lab01 версія %s%n", VERSION);
                return;
            }
            if ("--input".equals(args[i]) && i + 1 < args.length) {
                inputPath = args[++i];
            } else if ("--output".equals(args[i]) && i + 1 < args.length) {
                outputPath = args[++i];
            } else if ("--export".equals(args[i]) && i + 1 < args.length) {
                exportPath = args[++i];
            }
        }

        try {
            List<String> lines = Files.readAllLines(Path.of(inputPath), StandardCharsets.UTF_8);
            String report = buildReport(lines);
            System.out.print(report);

            Path output = Path.of(outputPath);
            Path parent = output.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(output, report, StandardCharsets.UTF_8);

            if (exportPath != null) {
                Path csv = Path.of(exportPath);
                Path csvParent = csv.getParent();
                if (csvParent != null) {
                    Files.createDirectories(csvParent);
                }
                System.out.println(exportRoundTrip(lines, csv));
            }
        } catch (IOException exception) {
            System.out.printf(Locale.ROOT, "Помилка читання/запису файлу: %s%n", exception.getMessage());
        } catch (DataStorageException exception) {
            System.out.printf(Locale.ROOT, "Помилка експорту CSV: %s%n", exception.getMessage());
            if (exception.getCause() != null) {
                System.out.printf(Locale.ROOT, "Причина: %s%n", exception.getCause());
            }
        }
    }

    /**
     * Експортує заняття у CSV через {@link Repository} і {@link CsvExporter}, читає файл назад
     * та порівнює початкові й відновлені записи.
     *
     * @param lines рядки вхідного файлу розкладу
     * @param csvPath шлях до CSV
     * @return повідомлення про результат round-trip
     * @throws DataStorageException якщо запис, читання або розбір не вдалися, або записи не збігаються
     */
    static String exportRoundTrip(List<String> lines, Path csvPath) throws DataStorageException {
        Repository<LessonRecord> repository = new Repository<>();
        LessonRecord.fromLessons(parseLessons(lines, new ArrayList<>())).forEach(repository::add);

        CsvExporter.write(csvPath, LessonRecord.class, repository.all());
        List<LessonRecord> restored = CsvImporter.read(csvPath, LessonRecord.class, LessonRecord::fromFields);
        if (!repository.all().equals(restored)) {
            throw new DataStorageException("Round-trip не збігається: записано %d, відновлено %d"
                    .formatted(repository.all().size(), restored.size()));
        }
        return "CSV: %s, записів: %d, round-trip: збігається".formatted(csvPath, restored.size());
    }

    /**
     * Розбирає рядки розкладу; хибні рядки додає до списку помилок з номером і причиною.
     *
     * @param lines рядки вхідного файлу
     * @param errors список, до якого додаються повідомлення про помилки
     * @return коректні заняття в порядку файла
     */
    static List<Lesson> parseLessons(List<String> lines, List<String> errors) {
        List<Lesson> lessons = new ArrayList<>();
        for (int index = 0; index < lines.size(); index++) {
            try {
                lessons.add(Lesson.fromCsv(lines.get(index)));
            } catch (IllegalArgumentException exception) {
                errors.add("Рядок %d: %s".formatted(index + 1, exception.getMessage()));
            }
        }
        return lessons;
    }

    /**
     * Обробляє рядки розкладу та формує текст звіту.
     *
     * <p>Формат рядка: {@code subject;teacher;day;room;durationMin}. Розбір і перевірка
     * виконуються в {@link Lesson#fromCsv(String)}. Хибні рядки пропускаються
     * з повідомленням про номер рядка та причину.
     *
     * @param lines рядки вхідного файлу
     * @return готовий текст звіту
     */
    static String buildReport(List<String> lines) {
        List<String> errors = new ArrayList<>();
        List<Lesson> lessons = parseLessons(lines, errors);

        long totalMinutes = LessonQueries.durationStatistics(lessons).getSum();
        Map<LessonKind, Long> countByKind = LessonQueries.countByKind(lessons);

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "Коректних записів: %d%n", lessons.size()));
        sb.append(String.format(Locale.ROOT, "Сумарна тривалість: %d хв (%.2f год)%n",
                totalMinutes, totalMinutes / 60.0));
        sb.append(LessonQueries.minRoom(lessons)
                .map(room -> String.format(Locale.ROOT, "Найменший номер аудиторії: %d%n", room))
                .orElse(String.format(Locale.ROOT, "Найменший номер аудиторії: немає даних%n")));
        sb.append(LessonQueries.longestLesson(lessons)
                .map(lesson -> String.format(Locale.ROOT, "Найдовше заняття: %s (%d хв)%n",
                        lesson.getSubject(), lesson.getDurationMin()))
                .orElse(String.format(Locale.ROOT, "Найдовше заняття: немає даних%n")));
        sb.append(String.format(Locale.ROOT, "Сумарна потреба в ресурсах: %d%n",
                LessonQueries.totalResources(lessons)));
        for (LessonKind kind : LessonKind.values()) {
            sb.append(String.format(Locale.ROOT, "%s: %d%n", kind.getLabel(), countByKind.get(kind)));
        }
        sb.append(String.format(Locale.ROOT, "Помилок: %d%n", errors.size()));
        errors.forEach(error -> sb.append(error).append(System.lineSeparator()));
        return sb.toString();
    }
}
