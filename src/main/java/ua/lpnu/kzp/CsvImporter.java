package ua.lpnu.kzp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Читає CSV-файл, перевіряє заголовок і відновлює об'єкти предметним розбором рядків. */
public final class CsvImporter {

    /**
     * Перетворює поля одного запису CSV на об'єкт.
     *
     * @param <T> тип об'єкта
     */
    @FunctionalInterface
    public interface RowMapper<T> {
        /**
         * Створює об'єкт із полів запису.
         *
         * @param fields значення полів у порядку стовпців
         * @return відновлений об'єкт
         * @throws DataStorageException якщо поля хибні
         */
        T map(List<String> fields) throws DataStorageException;
    }

    /* Забороняє створення екземплярів службового класу. */
    private CsvImporter() {
    }

    /**
     * Читає файл у UTF-8, перевіряє заголовок і відновлює об'єкти.
     *
     * @param <T> тип об'єктів
     * @param path шлях до файла
     * @param type клас записів (з нього береться очікуваний заголовок)
     * @param mapper предметний розбір одного запису
     * @return відновлені об'єкти в порядку файла; для файла з лише заголовком — порожній список
     * @throws DataStorageException якщо файл недоступний, заголовок не збігається чи запис хибний
     */
    public static <T> List<T> read(Path path, Class<T> type, RowMapper<T> mapper)
            throws DataStorageException {
        String text;
        try {
            text = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new DataStorageException("Не вдалося прочитати файл " + path, exception);
        }
        List<List<String>> rows = CsvParser.parseDocument(text);
        if (rows.isEmpty()) {
            throw new DataStorageException("Файл " + path + " порожній: немає заголовка");
        }
        String expected = CsvExporter.headerOf(type);
        String actual = String.join(",", rows.get(0).stream().map(CsvExporter::escape).toList());
        if (!expected.equals(actual)) {
            throw new DataStorageException(
                    "Заголовок «%s» не збігається з очікуваним «%s»".formatted(actual, expected));
        }
        List<T> result = new ArrayList<>();
        for (int index = 1; index < rows.size(); index++) {
            try {
                result.add(mapper.map(rows.get(index)));
            } catch (DataStorageException exception) {
                throw new DataStorageException(
                        "Запис %d: %s".formatted(index, exception.getMessage()), exception);
            }
        }
        return result;
    }
}
