package io.github.rexlynn.purplesnake.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServiceCatalogTest {

    @Test
    void knownPortsReturnTheirName() {
        assertEquals("SSH", ServiceCatalog.nameFor(22));
        assertEquals("HTTP", ServiceCatalog.nameFor(80));
        assertEquals("HTTPS", ServiceCatalog.nameFor(443));
    }

    @Test
    void bothDhcpPortsShareOneName() {
        assertEquals("DHCP", ServiceCatalog.nameFor(67));
        assertEquals("DHCP", ServiceCatalog.nameFor(68));
    }

    @Test
    void unknownPortReturnsDefaultText() {
        assertEquals(ServiceCatalog.UNKNOWN, ServiceCatalog.nameFor(8080));
    }
}
