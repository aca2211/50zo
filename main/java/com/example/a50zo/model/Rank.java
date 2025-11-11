package com.example.a50zo.model;
/**
 * Enum representing the ranks of playing cards.
 * Each rank has a symbol and up to two possible values for game calculations.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
public enum Rank {
    TWO("2", 2, 2),
    THREE("3", 3, 3),
    FOUR("4", 4, 4),
    FIVE("5", 5, 5),
    SIX("6", 6, 6),
    SEVEN("7", 7, 7),
    EIGHT("8", 8, 8),
    NINE("9", 0, 0),
    TEN("10", 10, 10),
    JACK("J", -10, -10),
    QUEEN("Q", -10, -10),
    KING("K", -10, -10),
    ACE("A", 1, 10);

    private final String symbol;
    private final int primaryValue;
    private final int secondaryValue;

    /**
     * Constructor for Rank enum.
     *
     * @param symbol Symbol representing the rank
     * @param primaryValue Primary value for calculations
     * @param secondaryValue Secondary value (used for Aces)
     */
    Rank(String symbol, int primaryValue, int secondaryValue) {
        this.symbol = symbol;
        this.primaryValue = primaryValue;
        this.secondaryValue = secondaryValue;
    }

    /**
     * Gets the symbol of the rank.
     *
     * @return The rank symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Gets the primary value of the rank.
     *
     * @return The primary value
     */
    public int getPrimaryValue() {
        return primaryValue;
    }

    /**
     * Gets the secondary value of the rank.
     *
     * @return The secondary value
     */
    public int getSecondaryValue() {
        return secondaryValue;
    }

    /**
     * Checks if this rank has multiple possible values.
     *
     * @return true if the rank has different primary and secondary values
     */
    public boolean hasMultipleValues() {
        return primaryValue != secondaryValue;
    }
}
