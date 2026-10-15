package ua.lpnu.kzp;

import java.util.ArrayList;
import java.util.List;

/** Документний CSV-парсер: розрізняє коми й переноси рядків усередині та поза лапками. */
public final class CsvParser {
    /* Забороняє створення екземплярів службового класу. */
    private CsvParser() {
    }

    /**
     * Розбирає весь текст CSV на список записів (а не фізичних рядків).
     *
     * @param text вміст файла
     * @return записи, кожен — список полів; для порожнього тексту — порожній список
     * @throws DataStorageException якщо лапка не закрита
     */
    public static List<List<String>> parseDocument(String text) throws DataStorageException {
        List<List<String>> records = new ArrayList<>();
        List<String> record = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        boolean pending = false;

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < text.length() && text.charAt(i + 1) == '"') {
                        field.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    field.append(ch);
                }
                continue;
            }
            if (ch == '"' && field.length() == 0) {
                inQuotes = true;
                pending = true;
            } else if (ch == ',') {
                record.add(field.toString());
                field.setLength(0);
                pending = true;
            } else if (ch == '\n' || ch == '\r') {
                if (ch == '\r' && i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
                record.add(field.toString());
                records.add(record);
                record = new ArrayList<>();
                field.setLength(0);
                pending = false;
            } else {
                field.append(ch);
                pending = true;
            }
        }
        if (inQuotes) {
            throw new DataStorageException("CSV містить незакриту лапку");
        }
        if (pending) {
            record.add(field.toString());
            records.add(record);
        }
        return records;
    }
}
