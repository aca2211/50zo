package com.example.a50zo.exceptions;

/**
 * Checked exception thrown when an invalid card play is attempted.
 * This includes playing cards that would exceed the sum of 50.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
public class InvalidCardPlayException extends Exception {

    /**
     * Constructor with error message.
     *
     * @param message Description of the invalid play
     */
    public InvalidCardPlayException(String message) {
        super(message);
    }

    /**
     * Constructor with error message and cause.
     *
     * @param message Description of the invalid play
     * @param cause The underlying cause
     */
    public InvalidCardPlayException(String message, Throwable cause) {
        super(message, cause);
    }
}
