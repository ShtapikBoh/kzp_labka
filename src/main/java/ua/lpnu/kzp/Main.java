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
    private static final String VERSION = "1.0.0";
    private static final int FIELD_COUNT = 5;

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
     * <p>Формат рядка: {@code subject;teacher;day;room;durationMin}. Хибні рядки
     * пропускаються з повідомленням про номер рядка та причину.
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
            int lineNumber = index + 1;
            String line = lines.get(index);

            if (line.isBlank()) {
                errors.add("Рядок %d: порожній рядок".formatted(lineNumber));
                continue;
            }

            String[] fields = line.split(";", -1);
            if (fields.length != FIELD_COUNT) {
                errors.add("Рядок %d: очікується %d полів, отримано %d"
                        .formatted(lineNumber, FIELD_COUNT, fields.length));
                continue;
            }

            String subject = fields[0].trim();
            String teacher = fields[1].trim();
            String day = fields[2].trim();
            if (subject.isEmpty() || teacher.isEmpty() || day.isEmpty()) {
                errors.add("Рядок %d: порожній предмет, викладач або день".formatted(lineNumber));
                continue;
            }

            int room;
            int duration;
            try {
                room = Integer.parseInt(fields[3].trim());
                duration = Integer.parseInt(fields[4].trim());
            } catch (NumberFormatException exception) {
                errors.add("Рядок %d: числове поле має помилковий формат".formatted(lineNumber));
                continue;
            }
            if (room < 0 || duration < 0) {
                errors.add("Рядок %d: від'ємне числове значення".formatted(lineNumber));
                continue;
            }

            validCount++;
            totalMinutes += duration;
            minRoom = Math.min(minRoom, room);
            if (duration > longestMinutes) {
                longestMinutes = duration;
                longestSubject = subject;
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
