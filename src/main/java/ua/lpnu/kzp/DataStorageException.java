package ua.lpnu.kzp;

/** Помилка збереження або читання даних: формат, значення, файл чи рефлексія. */
public class DataStorageException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Створює виняток із повідомленням.
     *
     * @param message опис помилки
     */
    public DataStorageException(String message) {
        super(message);
    }

    /**
     * Створює виняток із повідомленням і причиною.
     *
     * @param message опис помилки
     * @param cause першопричина
     */
    public DataStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
