package com.pingpal.exceptions.imports;

/**
 * Thrown to indicate that the round trip time provided is outside of the
 * acceptable range.
 * <p>
 * This exception is typically raised when a value for the round trip time is
 * less than 0 or greater than the provided ping interval.
 * </p>
 */
public class InvalidRoundTripTimeException extends Exception {

    /**
     * Constructs a new InvalidRoundTripTimeException with the specified detail
     * message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidRoundTripTimeException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidRoundTripTimeException with the specified detail
     * message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause (which is saved for later retrieval by the
     * {@link #getCause()} method)
     */
    public InvalidRoundTripTimeException(String message, Throwable cause) {
        super(message, cause);
    }

}
