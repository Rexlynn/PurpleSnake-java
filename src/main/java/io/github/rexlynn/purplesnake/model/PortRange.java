package io.github.rexlynn.purplesnake.model;

import io.github.rexlynn.purplesnake.exception.InvalidPortRangeException;

/**
 * A validated range of ports, for example 20-80.
 * The constructor only creates objects that follow the rules, so every PortRange
 * that exists in the program is valid (that is what "establishing valid objects" means).
 * The class is immutable: fields are private and final, and there are no setters.
 */
public final class PortRange {

    public static final int MIN_PORT = 1;
    public static final int MAX_PORT = 65535;

    private final int start;
    private final int end;

    public PortRange(int start, int end) throws InvalidPortRangeException {
        if (start < MIN_PORT || start > MAX_PORT || end < MIN_PORT || end > MAX_PORT) {
            throw new InvalidPortRangeException(
                    "Ports must be between " + MIN_PORT + " and " + MAX_PORT + ".");
        }
        if (start > end) {
            throw new InvalidPortRangeException("Start port must not be greater than end port.");
        }
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    /** How many ports the range contains (both ends included). */
    public int size() {
        return end - start + 1;
    }

    @Override
    public String toString() {
        return start + "-" + end;
    }
}
