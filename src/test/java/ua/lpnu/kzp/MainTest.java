package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class MainTest {

    @Test
    void computesAllMetrics() {
        String report = Main.buildReport(List.of(
                "Програмування;Олексів М. В.;Понеділок;101;90",
                "Бази даних;Коваленко О. П.;Середа;98;120"));
        assertTrue(report.contains("Коректних записів: 2"));
        assertTrue(report.contains("Сумарна тривалість: 210 хв (3.50 год)"));
        assertTrue(report.contains("Найменший номер аудиторії: 98"));
        assertTrue(report.contains("Найдовше заняття: Бази даних (120 хв)"));
    }

    @Test
    void skipsInvalidLinesWithReason() {
        String report = Main.buildReport(List.of(
                "Фізика;Бойко А. А.;Вівторок;120;60",
                "",
                "Мережі;Петренко В. Л.;П'ятниця;abc;90",
                "Математика;Сидоренко Н. Г.;Понеділок;110;-45",
                "Фізика;Бойко А. А.;Вівторок;120"));
        assertTrue(report.contains("Коректних записів: 1"));
        assertTrue(report.contains("Помилок: 4"));
        assertTrue(report.contains("Рядок 2: порожній рядок"));
        assertTrue(report.contains("Рядок 3: числове поле має помилковий формат"));
        assertTrue(report.contains("Рядок 4: від'ємне числове значення"));
        assertTrue(report.contains("Рядок 5: очікується 5 полів, отримано 4"));
    }

    @Test
    void emptyInputGivesNoData() {
        String report = Main.buildReport(List.of());
        assertTrue(report.contains("Коректних записів: 0"));
        assertTrue(report.contains("Найдовше заняття: немає даних"));
    }
}
