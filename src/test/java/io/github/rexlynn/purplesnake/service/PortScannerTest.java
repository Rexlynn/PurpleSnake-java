package io.github.rexlynn.purplesnake.service;

import io.github.rexlynn.purplesnake.exception.InvalidPortRangeException;
import io.github.rexlynn.purplesnake.model.PortRange;
import io.github.rexlynn.purplesnake.model.PortState;
import io.github.rexlynn.purplesnake.model.ScanReport;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** These tests scan the local computer only: a real server socket is opened for the test. */
class PortScannerTest {

    private final PortScanner scanner = new PortScanner(500);

    @Test
    void listeningPortIsOpen() throws IOException {
        try (ServerSocket server = new ServerSocket(0)) { // 0 = the system picks a free port
            InetAddress localhost = InetAddress.getByName("127.0.0.1");
            assertEquals(PortState.OPEN, scanner.probe(localhost, server.getLocalPort()));
        }
    }

    @Test
    void portWithoutServerIsClosed() throws IOException {
        int freePort;
        try (ServerSocket server = new ServerSocket(0)) {
            freePort = server.getLocalPort();
        } // the server is closed here, so nobody listens on that port any more
        InetAddress localhost = InetAddress.getByName("127.0.0.1");
        assertEquals(PortState.CLOSED, scanner.probe(localhost, freePort));
    }

    @Test
    void scanReturnsOneResultPerPortInOrder() throws IOException, InvalidPortRangeException {
        try (ServerSocket server = new ServerSocket(0)) {
            int port = server.getLocalPort();
            if (port == 65535) {
                return; // cannot build a range of 3 ports above it, extremely unlikely
            }
            ScanReport report = scanner.scan("127.0.0.1", new PortRange(port, port + 1));
            assertEquals(2, report.getResults().size());
            assertEquals(port, report.getResults().get(0).getPort());
            assertEquals(PortState.OPEN, report.getResults().get(0).getState());
            assertEquals("127.0.0.1", report.getTarget());
        }
    }

    @Test
    void blankTargetIsRejected() throws InvalidPortRangeException {
        PortRange range = new PortRange(80, 80);
        assertThrows(IllegalArgumentException.class, () -> scanner.scan("", range));
        assertThrows(IllegalArgumentException.class, () -> scanner.scan("   ", range));
    }

    @Test
    void unknownHostThrowsException() throws InvalidPortRangeException {
        PortRange range = new PortRange(80, 80);
        // ".invalid" is a reserved name that can never exist
        assertThrows(UnknownHostException.class, () -> scanner.scan("nohost.invalid", range));
    }

    @Test
    void timeoutMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> new PortScanner(0));
        assertThrows(IllegalArgumentException.class, () -> new PortScanner(-5));
    }
}
