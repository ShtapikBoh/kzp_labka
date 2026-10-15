package ua.lpnu.kzp;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Універсальний рефлексивний CSV-експортер: працює з будь-яким класом, поля якого
 * позначено {@link CsvColumn}. Назв предметних класів не знає.
 */
public final class CsvExporter {
    /* Забороняє створення екземплярів службового класу. */
    private CsvExporter() {
    }

    /**
     * Формує текст CSV: заголовок і рядок для кожного запису.
     *
     * @param <T> тип записів
     * @param type клас записів (потрібен для заголовка навіть без записів)
     * @param records записи для експорту
     * @return текст CSV
     * @throws DataStorageException якщо немає анотованих полів або доступ до поля заборонено
     */
    public static <T> String toCsv(Class<T> type, List<? extends T> records) throws DataStorageException {
        List<Field> fields = annotatedFields(type);
        StringBuilder csv = new StringBuilder(header(fields)).append(System.lineSeparator());
        for (T record : records) {
            List<String> values = new ArrayList<>();
            for (Field field : fields) {
                values.add(escape(valueOf(field, record)));
            }
            csv.append(String.join(",", values)).append(System.lineSeparator());
        }
        return csv.toString();
    }

    /**
     * Записує анотовані поля записів у CSV-файл (UTF-8, старий вміст замінюється).
     *
     * @param <T> тип записів
     * @param path шлях до файла
     * @param type клас записів
     * @param records записи для експорту
     * @throws DataStorageException якщо поля неможливо прочитати або файл записати
     */
    public static <T> void write(Path path, Class<T> type, List<? extends T> records)
            throws DataStorageException {
        String csv = toCsv(type, records);
        try {
            Files.writeString(path, csv, StandardCharsets.UTF_8, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException exception) {
            throw new DataStorageException("Не вдалося записати файл " + path, exception);
        }
    }

    /**
     * Повертає заголовок CSV для класу.
     *
     * @param type клас записів
     * @return рядок заголовка без переведення рядка
     * @throws DataStorageException якщо в класі немає анотованих полів
     */
    public static String headerOf(Class<?> type) throws DataStorageException {
        return header(annotatedFields(type));
    }

    /**
     * Екранує значення за правилами CSV: поле з комою, лапкою чи переведенням рядка
     * береться в лапки, а лапки всередині подвоюються.
     *
     * @param value значення
     * @return екрановане значення
     */
    public static String escape(String value) {
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    /* Повертає анотовані поля класу й його предків у детермінованому порядку (за іменем). */
    private static List<Field> annotatedFields(Class<?> type) throws DataStorageException {
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = type; current != null && current != Object.class;
                current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (field.isAnnotationPresent(CsvColumn.class)) {
                    fields.add(field);
                }
            }
        }
        if (fields.isEmpty()) {
            throw new DataStorageException(
                    "Клас " + type.getName() + " не має полів з анотацією @CsvColumn");
        }
        fields.sort(Comparator.comparing(Field::getName));
        return fields;
    }

    private static String header(List<Field> fields) {
        return fields.stream()
                .map(field -> escape(field.getAnnotation(CsvColumn.class).value()))
                .collect(Collectors.joining(","));
    }

    private static String valueOf(Field field, Object record) throws DataStorageException {
        if (!field.trySetAccessible()) {
            throw new DataStorageException("Немає доступу до поля " + field.getName());
        }
        try {
            Object value = field.get(record);
            return value == null ? "" : String.valueOf(value);
        } catch (IllegalAccessException exception) {
            throw new DataStorageException("Не вдалося прочитати поле " + field.getName(), exception);
        }
    }
}
