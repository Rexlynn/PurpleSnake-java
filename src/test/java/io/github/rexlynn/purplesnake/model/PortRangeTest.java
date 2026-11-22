package io.github.rexlynn.purplesnake.model;

import io.github.rexlynn.purplesnake.exception.InvalidPortRangeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PortRangeTest {

    @Test
    void validRangeKeepsItsValues() throws InvalidPortRangeException {
        PortRange range = new PortRange(20, 80);
        assertEquals(20, range.getStart());
        assertEquals(80, range.getEnd());
        assertEquals(61, range.size());
        assertEquals("20-80", range.toString());
    }

    @Test
    void singlePortRangeIsAllowed() throws InvalidPortRangeException {
        assertEquals(1, new PortRange(443, 443).size());
    }

    @Test
    void startGreaterThanEndIsRejected() {
        assertThrows(InvalidPortRangeException.class, () -> new PortRange(100, 50));
    }

    @Test
    void portZeroIsRejected() {
        assertThrows(InvalidPortRangeException.class, () -> new PortRange(0, 10));
    }

    @Test
    void portAboveMaximumIsRejected() {
        assertThrows(InvalidPortRangeException.class, () -> new PortRange(1, 65536));
        assertThrows(InvalidPortRangeException.class, () -> new PortRange(70000, 80000));
    }

    @Test
    void fullRangeIsAllowed() throws InvalidPortRangeException {
        assertEquals(65535, new PortRange(1, 65535).size());
    }
}
