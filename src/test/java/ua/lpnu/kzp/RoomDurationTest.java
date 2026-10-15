package ua.lpnu.kzp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class RoomDurationTest {

    @Test
    void recordExposesComponents() {
        RoomDuration value = new RoomDuration(215, 120);
        assertEquals(215, value.room());
        assertEquals(120, value.durationMin());
    }

    @Test
    void recordHasValueEquality() {
        assertEquals(new RoomDuration(1, 2), new RoomDuration(1, 2));
        assertEquals(new RoomDuration(1, 2).hashCode(), new RoomDuration(1, 2).hashCode());
        assertNotEquals(new RoomDuration(1, 2), new RoomDuration(2, 1));
    }

    @Test
    void recordToStringListsComponents() {
        assertEquals("RoomDuration[room=1, durationMin=2]", new RoomDuration(1, 2).toString());
    }
}
