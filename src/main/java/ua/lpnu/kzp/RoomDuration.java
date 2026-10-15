package ua.lpnu.kzp;

/**
 * Допоміжне незмінне значення: аудиторія та тривалість заняття.
 *
 * @param room номер аудиторії
 * @param durationMin тривалість у хвилинах
 */
public record RoomDuration(int room, int durationMin) {
}
