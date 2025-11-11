package com.example.a50zo.model;

import com.example.a50zo.exceptions.InvalidCardPlayException;

/**
 * Represents a human player in the game.
 * Card selection is handled through the GUI.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
public class HumanPlayer extends Player {

    /**
     * Constructor for HumanPlayer.
     *
     * @param name The player's name
     */
    public HumanPlayer(String name) {
        super(name);
    }

    /**
     * Selects a card to play. For human players, this is handled by the GUI.
     * This method validates that the card can be played.
     *
     * @param currentSum The current sum on the table
     * @return The selected card (null for human players, actual selection done via GUI)
     * @throws InvalidCardPlayException if the player has no valid moves
     */
    @Override
    public Card selectCard(int currentSum) throws InvalidCardPlayException {
        if (!hasValidMove(currentSum)) {
            throw new InvalidCardPlayException("No valid cards to play. You are eliminated!");
        }
        // Actual card selection is handled by GUI interaction
        return null;
    }

    /**
     * Validates and plays a specific card from the hand.
     *
     * @param card The card to play
     * @param currentSum The current sum on the table
     * @throws InvalidCardPlayException if the card cannot be played
     */
    public void playCard(Card card, int currentSum) throws InvalidCardPlayException {
        if (!hand.contains(card)) {
            throw new InvalidCardPlayException("Card not in hand");
        }
        if (!card.canBePlayed(currentSum)) {
            throw new InvalidCardPlayException("Playing this card would exceed 50");
        }
        removeCardFromHand(card);
    }
}
