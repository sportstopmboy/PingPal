package com.pingpal.exceptions.ui;

/**
 * Thrown to indicate that a provided IP address string does not match the
 * expected IPv4 format.
 * <p>
 * Use this exception during UI validation when the user enters an IP address
 * that fails the regex or format checks.
 * </p>
 */
public class InvalidIPAddressException extends Exception {

    /**
     * Constructs a new InvalidIPAddressException with the specified
     * detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidIPAddressException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidIPAddressException with the specified
     * detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause (which is saved for later retrieval by the
     * {@link #getCause()} method)
     */
    public InvalidIPAddressException(String message, Throwable cause) {
        super(message, cause);
    }
}
