package com.pingpal.exceptions.imports;

/**
 * Thrown to indicate that the data from the imported file does not contain the
 * results of a scan provided by the functionality of PingPal.
 * <p>
 * This exception is typically raised when the data in the file does not match
 * the format of the results from either a {@code Subnet Scan},
 * {@code Device Ping}, or {@code Port Scan}.
 * </p>
 */
public class InvalidScanTypeException extends Exception {

    /**
     * Constructs a new InvalidScanTypeException with the specified detail
     * message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidScanTypeException(String message) {
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
    public InvalidScanTypeException(String message, Throwable cause) {
        super(message, cause);
    }
}
