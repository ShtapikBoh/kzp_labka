package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class RepositoryTest {

    @Test
    void emptyRepositoryHasNoItems() {
        Repository<String> repository = new Repository<>();
        assertTrue(repository.all().isEmpty());
        assertTrue(repository.find(item -> true).isEmpty());
    }

    @Test
    void keepsItemsInInsertionOrder() {
        Repository<String> repository = new Repository<>();
        repository.add("б");
        repository.add("а");
        assertEquals(List.of("б", "а"), repository.all());
    }

    @Test
    void snapshotIsImmutable() {
        Repository<String> repository = new Repository<>();
        repository.add("а");
        List<String> snapshot = repository.all();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.add("б"));
    }

    @Test
    void snapshotDoesNotSeeLaterChanges() {
        Repository<String> repository = new Repository<>();
        repository.add("а");
        List<String> snapshot = repository.all();
        repository.add("б");
        assertEquals(1, snapshot.size());
        assertEquals(2, repository.all().size());
    }

    @Test
    void findUsesPredicate() {
        Repository<LessonRecord> repository = new Repository<>();
        repository.add(new LessonRecord("А", "Т1", "Понеділок", 1, 10));
        repository.add(new LessonRecord("Б", "Т2", "Вівторок", 1, 20));
        repository.add(new LessonRecord("В", "Т1", "Середа", 2, 30));

        assertEquals(2, repository.find(record -> record.getTeacher().equals("Т1")).size());
        assertTrue(repository.find(record -> record.getRoom() > 100).isEmpty());
    }

    @Test
    void findAcceptsPredicateOfSupertype() {
        Repository<String> repository = new Repository<>();
        repository.add("abc");
        java.util.function.Predicate<Object> notNull = java.util.Objects::nonNull;
        assertEquals(1, repository.find(notNull).size());
    }

    @Test
    void rejectsNull() {
        Repository<String> repository = new Repository<>();
        assertThrows(NullPointerException.class, () -> repository.add(null));
    }
}
