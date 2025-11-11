package com.example.a50zo.exceptions;
/**
 * Checked exception thrown when attempting to draw from an empty deck.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
public class EmptyDeckException extends Exception {

    /**
     * Constructor with error message.
     *
     * @param message Description of the error
     */
    public EmptyDeckException(String message) {
        super(message);
    }

    /**
     * Constructor with error message and cause.
     *
     * @param message Description of the error
     * @param cause The underlying cause
     */
    public EmptyDeckException(String message, Throwable cause) {
        super(message, cause);
    }
}