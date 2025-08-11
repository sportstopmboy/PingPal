package com.pingpal.exceptions.imports;

/**
 * Thrown to indicate that the number of pings provided is outside of the
 * acceptable range.
 * <p>
 * This exception is typically raised when a value for the number of pings is
 * less than the minimum allowed or greater than the maximum allowed value.
 * </p>
 */
public class InvalidNumOfPingsException extends Exception {

    /**
     * Constructs a new InvalidNumOfPingsException with the specified detail
     * message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidNumOfPingsException(String message) {
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
    public InvalidNumOfPingsException(String message, Throwable cause) {
        super(message, cause);
    }

}
