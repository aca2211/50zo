package com.example.a50zo.model;
import com.example.a50zo.exceptions.InvalidCardPlayException;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class representing a player in the Cincuentazo game.
 * Contains common functionality for both human and machine players.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
public abstract class Player {
    protected final String name;
    protected final List<Card> hand;
    protected boolean isEliminated;

    /**
     * Constructor for Player.
     *
     * @param name The player's name
     */
    public Player(String name) {
        this.name = name;
        this.hand = new ArrayList<>();
        this.isEliminated = false;
    }

    /**
     * Gets the player's name.
     *
     * @return The player name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the player's hand of cards.
     *
     * @return List of cards in hand
     */
    public List<Card> getHand() {
        return new ArrayList<>(hand);
    }

    /**
     * Adds a card to the player's hand.
     *
     * @param card The card to add
     */
    public void addCardToHand(Card card) {
        hand.add(card);
    }

    /**
     * Removes a card from the player's hand.
     *
     * @param card The card to remove
     * @return true if the card was removed successfully
     */
    public boolean removeCardFromHand(Card card) {
        return hand.remove(card);
    }

    /**
     * Gets the number of cards in the player's hand.
     *
     * @return Number of cards in hand
     */
    public int getHandSize() {
        return hand.size();
    }

    /**
     * Checks if the player is eliminated.
     *
     * @return true if the player is eliminated
     */
    public boolean isEliminated() {
        return isEliminated;
    }

    /**
     * Eliminates the player from the game.
     */
    public void eliminate() {
        this.isEliminated = true;
    }

    /**
     * Checks if the player has any valid cards to play.
     *
     * @param currentSum The current sum on the table
     * @return true if the player can play at least one card
     */
    public boolean hasValidMove(int currentSum) {
        return hand.stream().anyMatch(card -> card.canBePlayed(currentSum));
    }

    /**
     * Gets all cards from the player's hand and clears it.
     * Used when a player is eliminated.
     *
     * @return List of all cards that were in hand
     */
    public List<Card> removeAllCards() {
        List<Card> cards = new ArrayList<>(hand);
        hand.clear();
        return cards;
    }

    /**
     * Abstract method to select a card to play.
     * Implementation differs for human and machine players.
     *
     * @param currentSum The current sum on the table
     * @return The selected card to play
     * @throws InvalidCardPlayException if no valid card can be played
     */
    public abstract Card selectCard(int currentSum) throws InvalidCardPlayException;

    /**
     * Returns a string representation of the player.
     *
     * @return The player's name
     */
    @Override
    public String toString() {
        return name;
    }
}
