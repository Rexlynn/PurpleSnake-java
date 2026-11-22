package io.github.rexlynn.purplesnake.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScanResultTest {

    @Test
    void storesPortAndState() {
        ScanResult result = new ScanResult(80, PortState.OPEN);
        assertEquals(80, result.getPort());
        assertEquals(PortState.OPEN, result.getState());
        assertTrue(result.isOpen());
    }

    @Test
    void closedPortIsNotOpen() {
        assertFalse(new ScanResult(81, PortState.CLOSED).isOpen());
    }

    @Test
    void invalidPortIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ScanResult(0, PortState.OPEN));
        assertThrows(IllegalArgumentException.class, () -> new ScanResult(65536, PortState.OPEN));
    }

    @Test
    void nullStateIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new ScanResult(80, null));
    }

    @Test
    void knownPortHasServiceName() {
        assertEquals("HTTP", new ScanResult(80, PortState.OPEN).getServiceName());
    }

    @Test
    void naturalOrderIsByPortNumber() {
        ScanResult low = new ScanResult(22, PortState.CLOSED);
        ScanResult high = new ScanResult(80, PortState.OPEN);
        assertTrue(low.compareTo(high) < 0);
        assertTrue(high.compareTo(low) > 0);
        assertEquals(0, low.compareTo(new ScanResult(22, PortState.OPEN)));
    }

    @Test
    void toStringShowsServiceOnlyForOpenPorts() {
        assertEquals("Port    80 OPEN (HTTP)", new ScanResult(80, PortState.OPEN).toString());
        assertEquals("Port    81 CLOSED", new ScanResult(81, PortState.CLOSED).toString());
    }
}
