package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class CsvParserTest {

    @Test
    void parsesSimpleRecords() throws DataStorageException {
        assertEquals(List.of(List.of("a", "b"), List.of("c", "d")), CsvParser.parseDocument("a,b\nc,d\n"));
    }

    @Test
    void emptyTextGivesNoRecords() throws DataStorageException {
        assertTrue(CsvParser.parseDocument("").isEmpty());
    }

    @Test
    void trailingNewlineDoesNotAddRecord() throws DataStorageException {
        assertEquals(1, CsvParser.parseDocument("a,b\n").size());
        assertEquals(1, CsvParser.parseDocument("a,b").size());
    }

    @Test
    void keepsEmptyFieldsIncludingLast() throws DataStorageException {
        assertEquals(List.of(List.of("a", "", "b")), CsvParser.parseDocument("a,,b"));
        assertEquals(List.of(List.of("a", "b", "")), CsvParser.parseDocument("a,b,"));
    }

    @Test
    void commaInsideQuotesDoesNotSplit() throws DataStorageException {
        assertEquals(List.of(List.of("A-03", "Карти, що говорять", "1988")),
                CsvParser.parseDocument("A-03,\"Карти, що говорять\",1988"));
    }

    @Test
    void doubledQuoteBecomesOneQuote() throws DataStorageException {
        assertEquals(List.of(List.of("він сказав \"так\"")),
                CsvParser.parseDocument("\"він сказав \"\"так\"\"\""));
    }

    @Test
    void newlineInsideQuotesStaysInsideRecord() throws DataStorageException {
        List<List<String>> records = CsvParser.parseDocument("a,\"рядок 1\nрядок 2\"\nb,c\n");
        assertEquals(2, records.size());
        assertEquals("рядок 1\nрядок 2", records.get(0).get(1));
    }

    @Test
    void acceptsWindowsAndUnixLineEndings() throws DataStorageException {
        assertEquals(2, CsvParser.parseDocument("a,b\r\nc,d\r\n").size());
        assertEquals(2, CsvParser.parseDocument("a,b\nc,d\n").size());
    }

    @Test
    void blankLineIsRecordWithOneEmptyField() throws DataStorageException {
        assertEquals(List.of(List.of("a"), List.of(""), List.of("b")), CsvParser.parseDocument("a\n\nb\n"));
    }

    @Test
    void unclosedQuoteIsError() {
        assertThrows(DataStorageException.class, () -> CsvParser.parseDocument("a,\"abc"));
    }
}
