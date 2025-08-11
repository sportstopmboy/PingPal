package com.pingpal.exceptions.imports;

/**
 * Thrown to indicate that the protocol provided does not match the
 * corresponding protocol to the port number provided.
 * <p>
 * This exception is typically raised when the protocol does not match the port
 * number provided in the
 * {@code .\src\com\pingpal\resources\databases\port_list.csv} file.
 * </p>
 */
public class InvalidPortProtocolRelationshipException extends Exception {

    /**
     * Constructs a new InvalidPortProtocolRelationshipException with the
     * specified detail message.
     *
     * @param message the detail message explaining the reason for the exception
     */
    public InvalidPortProtocolRelationshipException(String message) {
        super(message);
    }

    /**
     * Constructs a new InvalidPortProtocolRelationshipException with the
     * specified detail message and cause.
     *
     * @param message the detail message explaining the reason for the exception
     * @param cause the cause (which is saved for later retrieval by the
     * {@link #getCause()} method)
     */
    public InvalidPortProtocolRelationshipException(String message, Throwable cause) {
        super(message, cause);
    }

}
