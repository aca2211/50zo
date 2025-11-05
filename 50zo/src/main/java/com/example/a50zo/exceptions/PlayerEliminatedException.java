package com.example.a50zo.exceptions;
/**
 * Unchecked exception thrown when a player is eliminated from the game.
 * This is a runtime exception as it represents a game state change.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
public class PlayerEliminatedException extends RuntimeException {

    /**
     * Constructor with error message.
     *
     * @param message Description of the elimination
     */
    public PlayerEliminatedException(String message) {
        super(message);
    }

    /**
     * Constructor with error message and cause.
     *
     * @param message Description of the elimination
     * @param cause The underlying cause
     */
    public PlayerEliminatedException(String message, Throwable cause) {
        super(message, cause);
    }
}
