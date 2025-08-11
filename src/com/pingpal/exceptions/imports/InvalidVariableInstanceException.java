package com.pingpal.exceptions.imports;

/**
 * Thrown when a variable’s runtime type does not match the expected type during
 * JSON or data validation.
 * <p>
 * Use this exception in validation utilities to signal that a field was
 * expected to be one type (e.g., Integer) but was another.
 * </p>
 */
public class InvalidVariableInstanceException extends Exception {

    /**
     * Constructs a new InvalidVariableInstanceException with the specified
     * detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidVariableInstanceException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidVariableInstanceException with the specified
     * detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause (which is saved for later retrieval by the
     * {@link #getCause()} method)
     */
    public InvalidVariableInstanceException(String message, Throwable cause) {
        super(message, cause);
    }

}
