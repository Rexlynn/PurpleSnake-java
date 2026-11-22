package io.github.rexlynn.purplesnake.exception;

/**
 * Thrown when a scan report cannot be saved or loaded
 * (disk problem or a corrupt report file).
 * It wraps the low-level cause so the rest of the program does not depend on file details.
 */
public class ReportStorageException extends Exception {

    private static final long serialVersionUID = 1L;

    public ReportStorageException(String message) {
        super(message);
    }

    public ReportStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
