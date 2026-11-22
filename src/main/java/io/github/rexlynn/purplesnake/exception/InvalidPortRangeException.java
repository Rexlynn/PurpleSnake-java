package io.github.rexlynn.purplesnake.exception;

/**
 * Thrown when a port range breaks a domain rule
 * (ports must be 1-65535 and the start must not be greater than the end).
 *
 * It is a CHECKED exception (extends Exception): the compiler forces every caller
 * to handle it, because invalid user input is an expected situation, not a bug.
 */
public class InvalidPortRangeException extends Exception {

    private static final long serialVersionUID = 1L;

    public InvalidPortRangeException(String message) {
        super(message);
    }
}
