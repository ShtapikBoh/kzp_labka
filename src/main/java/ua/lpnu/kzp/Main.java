package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Консольна програма «Розклад занять»: читає записи, перевіряє їх і формує звіт. */
public final class Main {
    private static final String VERSION = "1.0.1";

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

        for (int i = 0; i < args.length; i++) {
            if ("--help".equals(args[i])) {
                System.out.printf(Locale.ROOT,
                        "Використання: java -jar lab01.jar [--help] [--version] [--input <файл>] [--output <файл>]%n");
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
        } catch (IOException exception) {
            System.out.printf(Locale.ROOT, "Помилка читання/запису файлу: %s%n", exception.getMessage());
        }
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
        int validCount = 0;
        long totalMinutes = 0;
        int minRoom = Integer.MAX_VALUE;
        int longestMinutes = -1;
        String longestSubject = "";

        for (int index = 0; index < lines.size(); index++) {
            try {
                Lesson lesson = Lesson.fromCsv(lines.get(index));
                RoomDuration roomDuration = lesson.roomDuration();
                validCount++;
                totalMinutes += roomDuration.durationMin();
                minRoom = Math.min(minRoom, roomDuration.room());
                if (roomDuration.durationMin() > longestMinutes) {
                    longestMinutes = roomDuration.durationMin();
                    longestSubject = lesson.getSubject();
                }
            } catch (IllegalArgumentException exception) {
                errors.add("Рядок %d: %s".formatted(index + 1, exception.getMessage()));
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.ROOT, "Коректних записів: %d%n", validCount));
        sb.append(String.format(Locale.ROOT, "Сумарна тривалість: %d хв (%.2f год)%n",
                totalMinutes, totalMinutes / 60.0));
        if (validCount == 0) {
            sb.append(String.format(Locale.ROOT, "Найменший номер аудиторії: немає даних%n"));
            sb.append(String.format(Locale.ROOT, "Найдовше заняття: немає даних%n"));
        } else {
            sb.append(String.format(Locale.ROOT, "Найменший номер аудиторії: %d%n", minRoom));
            sb.append(String.format(Locale.ROOT, "Найдовше заняття: %s (%d хв)%n",
                    longestSubject, longestMinutes));
        }
        sb.append(String.format(Locale.ROOT, "Помилок: %d%n", errors.size()));
        for (String error : errors) {
            sb.append(error).append(System.lineSeparator());
        }
        return sb.toString();
    }
}
