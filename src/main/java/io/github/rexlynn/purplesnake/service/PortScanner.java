package io.github.rexlynn.purplesnake.service;

import io.github.rexlynn.purplesnake.model.PortRange;
import io.github.rexlynn.purplesnake.model.PortState;
import io.github.rexlynn.purplesnake.model.ScanReport;
import io.github.rexlynn.purplesnake.model.ScanResult;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TCP connect scanner: it tries a full TCP connection to every port.
 * If the connection succeeds, the port is open.
 * The scan is serial (one port after another), which keeps the code easy to follow.
 */
public class PortScanner {

    private static final int DEFAULT_TIMEOUT_MILLIS = 1000;

    private final int timeoutMillis;

    public PortScanner() {
        this(DEFAULT_TIMEOUT_MILLIS);
    }

    public PortScanner(int timeoutMillis) {
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("Timeout must be positive.");
        }
        this.timeoutMillis = timeoutMillis;
    }

    public int getTimeoutMillis() {
        return timeoutMillis;
    }

    /** Tries to connect to ONE port and returns what happened. */
    public PortState probe(InetAddress host, int port) {
        // try-with-resources: the socket is closed automatically when the block ends
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMillis);
            return PortState.OPEN;
        } catch (SocketTimeoutException e) {
            // No answer in time: usually a firewall silently dropping packets
            return PortState.FILTERED;
        } catch (ConnectException e) {
            // The target refused the connection: the port is closed
            return PortState.CLOSED;
        } catch (IOException e) {
            // Any other network problem (for example "network unreachable")
            return PortState.FILTERED;
        }
    }

    /**
     * Scans every port of the range and returns a finished report.
     *
     * @throws UnknownHostException if the target name cannot be resolved
     */
    public ScanReport scan(String target, PortRange range) throws UnknownHostException {
        if (target == null || target.isBlank()) {
            // An empty text would silently mean "this computer", so we reject it.
            throw new IllegalArgumentException("Target must not be empty.");
        }
        // Resolve the name ONCE, before the loop (not again for every port).
        InetAddress host = InetAddress.getByName(target.trim());

        List<ScanResult> results = new ArrayList<>();
        for (int port = range.getStart(); port <= range.getEnd(); port++) {
            results.add(new ScanResult(port, probe(host, port)));
        }
        return new ScanReport(target, LocalDateTime.now(), results);
    }
}
