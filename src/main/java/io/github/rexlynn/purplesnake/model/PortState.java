package io.github.rexlynn.purplesnake.model;

/** The possible states of a scanned port. An enum, because there are only three fixed values. */
public enum PortState {
    /** A TCP connection was established. */
    OPEN,
    /** The target actively refused the connection. */
    CLOSED,
    /** No answer (probably a firewall) or another network error. */
    FILTERED
}
