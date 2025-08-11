package com.pingpal.exceptions.imports;

/**
 * Thrown to indicate that the ping interval provided is outside of the
 * acceptable range.
 * <p>
 * This exception is typically raised when a value for the ping interval
 * is less than 100 or greater than 10000.
 * </p>
 */
public class InvalidPingIntervalRangeException extends Exception {

    /**
     * Constructs a new InvalidPingIntervalRangeException with the specified
     * detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidPingIntervalRangeException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidPingIntervalRangeException with the specified
     * detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause (which is saved for later retrieval by the
     * {@link #getCause()} method)
     */
    public InvalidPingIntervalRangeException(String message, Throwable cause) {
        super(message, cause);
    }

}
