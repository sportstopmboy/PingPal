package com.pingpal.exceptions.imports;

/**
 * Thrown to signal that the logical relationship between the round-trip time
 * and the successful-ping flag is invalid.
 * <p>
 * For example, if a ping reports a round-trip time less than the ping interval
 * but marks successfulPing as false, this exception should be thrown to
 * indicate inconsistent ping data.
 * </p>
 */
public class InvalidSuccessfulPingException extends Exception {

    /**
     * Constructs a new InvalidScanTypeException with the specified detail
     * message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidSuccessfulPingException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidScanTypeException with the specified detail
     * message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause (which is saved for later retrieval by the
     * {@link #getCause()} method)
     */
    public InvalidSuccessfulPingException(String message, Throwable cause) {
        super(message, cause);
    }

}
