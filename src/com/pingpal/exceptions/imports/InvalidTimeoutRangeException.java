package com.pingpal.exceptions.imports;

/**
 * Thrown to indicate that a timeout value falls outside the allowed range.
 * <p>
 * Use this exception when validating timeout parameters to enforce
 * {@code MIN_TIMEOUT <= timeout <= MAX_TIMEOUT}.
 * </p>
 */
public class InvalidTimeoutRangeException extends Exception {

    /**
     * Constructs a new InvalidNumOfPingsException with the specified detail
     * message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidTimeoutRangeException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidNumOfPingsException with the specified detail
     * message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause (which is saved for later retrieval by the
     * {@link #getCause()} method)
     */
    public InvalidTimeoutRangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
