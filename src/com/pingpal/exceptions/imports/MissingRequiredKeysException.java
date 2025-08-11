package com.pingpal.exceptions.imports;

/**
 * Thrown when expected top‐level keys are missing from a JSON object during
 * import validation.
 * <p>
 * Indicates that one or more required field names (keys) were not present in
 * the JSON being validated.
 * </p>
 */
public class MissingRequiredKeysException extends Exception {

    /**
     * Constructs a new MissingRequiredKeysException with the specified detail
     * message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public MissingRequiredKeysException(String message) {
        super(message);
    }

    /**
     * Constructs a new MissingRequiredKeysException with the specified detail
     * message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause (which is saved for later retrieval by the
     * {@link #getCause()} method)
     */
    public MissingRequiredKeysException(String message, Throwable cause) {
        super(message, cause);
    }
}
