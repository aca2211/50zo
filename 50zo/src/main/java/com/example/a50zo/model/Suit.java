package com.example.a50zo.model;

/**
 * Enum representing the four suits of a standard deck of cards.
 * Each suit has a symbol and a display name.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
public enum Suit {
    HEARTS("H", "Hearts"),
    DIAMONDS("D", "Diamonds"),
    CLUBS("C", "Clubs"),
    SPADES("S", "Spades");

    private final String symbol;
    private final String displayName;

    /**
     * Constructor for Suit enum.
     *
     * @param symbol Short symbol for the suit (H, D, C, S)
     * @param displayName Full name of the suit
     */
    Suit(String symbol, String displayName) {
        this.symbol = symbol;
        this.displayName = displayName;
    }

    /**
     * Gets the short symbol of the suit.
     *
     * @return The suit symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Gets the display name of the suit.
     *
     * @return The suit display name
     */
    public String getDisplayName() {
        return displayName;
    }
}
