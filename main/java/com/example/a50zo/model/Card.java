package com.example.a50zo.model;

/**
 * Represents a playing card with a rank and suit.
 * Provides methods to get card values and image file names.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
public class Card {
    private final Rank rank;
    private final Suit suit;

    /**
     * Constructor for Card.
     *
     * @param rank The rank of the card
     * @param suit The suit of the card
     */
    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    /**
     * Gets the rank of the card.
     *
     * @return The card rank
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * Gets the suit of the card.
     *
     * @return The card suit
     */
    public Suit getSuit() {
        return suit;
    }

    /**
     * Gets the best value to play this card given the current table sum.
     * For Aces, chooses between 1 and 10 based on which keeps the sum <= 50.
     *
     * @param currentSum The current sum on the table
     * @return The best value to use for this card
     */
    public int getBestValue(int currentSum) {
        if (rank.hasMultipleValues()) {
            // For Ace: choose value that keeps sum <= 50
            if (currentSum + rank.getSecondaryValue() <= 50) {
                return rank.getSecondaryValue();
            } else {
                return rank.getPrimaryValue();
            }
        }
        return rank.getPrimaryValue();
    }

    /**
     * Checks if this card can be played given the current table sum.
     *
     * @param currentSum The current sum on the table
     * @return true if playing this card keeps the sum <= 50
     */
    public boolean canBePlayed(int currentSum) {
        int newSum = currentSum + getBestValue(currentSum);
        return newSum <= 50;
    }

    /**
     * Gets the image file name for this card.
     * Format: [Rank][Suit].png (e.g., "2H.png", "AS.png")
     *
     * @return The image file name
     */
    public String getImageFileName() {
        return rank.getSymbol() + suit.getSymbol() + ".png";
    }

    /**
     * Returns a string representation of the card.
     *
     * @return String in format "Rank of Suit"
     */
    @Override
    public String toString() {
        return rank.getSymbol() + suit.getSymbol();
    }
}
