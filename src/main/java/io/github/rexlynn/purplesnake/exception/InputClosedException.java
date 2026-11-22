package io.github.rexlynn.purplesnake.exception;

/**
 * Thrown when the input stream ends while the program still waits for the user
 * (Ctrl+D on Linux/WSL, Ctrl+Z then Enter on Windows).
 *
 * It is UNCHECKED (extends RuntimeException) because it can happen at any prompt,
 * and forcing every method to declare it would only add noise. Main catches it once.
 */
public class InputClosedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InputClosedException() {
        super("Input stream closed.");
    }
}
