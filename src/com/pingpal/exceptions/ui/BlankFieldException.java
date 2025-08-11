package com.pingpal.exceptions.ui;

/**
 * Thrown to indicate that a required UI input field is blank or null.
 * <p>
 * Use this exception during UI validation when a user fails to provide any
 * content for a mandatory text field.
 * </p>
 */
public class BlankFieldException extends Exception {

    /**
     * Constructs a new BlankFieldException with the specified
     * detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public BlankFieldException(String message) {
        super(message);
    }

    /**
     * Constructs a new BlankFieldException with the specified
     * detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause (which is saved for later retrieval by the
     * {@link #getCause()} method)
     */
    public BlankFieldException(String message, Throwable cause) {
        super(message, cause);
    }
}
