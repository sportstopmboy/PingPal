package com.pingpal.exceptions.imports;

/**
 * Thrown to indicate that the port number provided is outside of the
 * acceptable range.
 * <p>
 * This exception is typically raised when a value for the packet loss
 * percentage is less than 1 or greater than 65535.
 * </p>
 */
public class InvalidPortNumberRangeException extends Exception {

    /**
     * Constructs a new InvalidPortNumberRangeException with the specified
     * detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidPortNumberRangeException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidPortNumberRangeException with the specified
     * detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause (which is saved for later retrieval by the
     * {@link #getCause()} method)
     */
    public InvalidPortNumberRangeException(String message, Throwable cause) {
        super(message, cause);
    }

}
