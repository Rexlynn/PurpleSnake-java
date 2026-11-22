package io.github.rexlynn.purplesnake.model;

/**
 * The result of scanning ONE port: which port, and what state it was in.
 * Immutable. Implements Comparable so a list of results can be sorted by port number.
 */
public class ScanResult implements Comparable<ScanResult> {

    private final int port;
    private final PortState state;

    public ScanResult(int port, PortState state) {
        if (port < PortRange.MIN_PORT || port > PortRange.MAX_PORT) {
            throw new IllegalArgumentException("Port out of range: " + port);
        }
        if (state == null) {
            throw new IllegalArgumentException("State must not be null.");
        }
        this.port = port;
        this.state = state;
    }

    public int getPort() {
        return port;
    }

    public PortState getState() {
        return state;
    }

    public boolean isOpen() {
        return state == PortState.OPEN;
    }

    public String getServiceName() {
        return ServiceCatalog.nameFor(port);
    }

    /** Natural order = ascending port number. */
    @Override
    public int compareTo(ScanResult other) {
        return Integer.compare(this.port, other.port);
    }

    @Override
    public String toString() {
        if (isOpen()) {
            return String.format("Port %5d OPEN (%s)", port, getServiceName());
        }
        return String.format("Port %5d %s", port, state);
    }
}
