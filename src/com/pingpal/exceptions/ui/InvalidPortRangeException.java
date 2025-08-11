package com.pingpal.exceptions.ui;

/**
 * Thrown to indicate that a provided port range is invalid, for example when
 * the lower bound is greater than the upper bound.
 * <p>
 * Use this exception during UI validation when users enter a port range that
 * does not satisfy {@code minPort <= bottomRange <= topRange <= maxPort}.
 * </p>
 */
public class InvalidPortRangeException extends Exception {

    /**
     * Constructs a new InvalidPortRangeException with the specified
     * detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidPortRangeException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidPortRangeException with the specified
     * detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause (which is saved for later retrieval by the
     * {@link #getCause()} method)
     */
    public InvalidPortRangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
