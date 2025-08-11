package com.pingpal.exceptions.ui;

/**
 * Thrown to indicate that a provided network range (CIDR notation) does not
 * conform to the expected format (e.g., "192.168.0.0/24").
 * <p>
 * Use this exception during UI validation when the user-entered network range
 * fails the regex or format checks.
 * </p>
 */
public class InvalidNetworkRangeException extends Exception {

    /**
     * Constructs a new InvalidNetworkRangeException with the specified
     * detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidNetworkRangeException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidNetworkRangeException with the specified
     * detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause (which is saved for later retrieval by the
     * {@link #getCause()} method)
     */
    public InvalidNetworkRangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
