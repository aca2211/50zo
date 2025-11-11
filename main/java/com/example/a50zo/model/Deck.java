package com.example.a50zo.model;

import com.example.a50zo.exceptions.EmptyDeckException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a deck of playing cards.
 * Manages drawing cards, shuffling, and replenishing from the table.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
public class Deck {
    private final List<Card> cards;

    /**
     * Constructor that creates a standard 52-card deck and shuffles it.
     */
    public Deck() {
        cards = new ArrayList<>();
        initializeDeck();
        shuffle();
    }

    /**
     * Initializes the deck with all 52 cards.
     */
    private void initializeDeck() {
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.add(new Card(rank, suit));
            }
        }
    }

    /**
     * Shuffles the deck randomly.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Draws a card from the top of the deck.
     *
     * @return The drawn card
     * @throws EmptyDeckException if the deck is empty
     */
    public Card drawCard() throws EmptyDeckException {
        if (cards.isEmpty()) {
            throw new EmptyDeckException("Cannot draw from an empty deck");
        }
        return cards.remove(0);
    }

    /**
     * Adds a card to the bottom of the deck.
     *
     * @param card The card to add
     */
    public void addCard(Card card) {
        cards.add(card);
    }

    /**
     * Adds multiple cards to the bottom of the deck.
     *
     * @param cardsToAdd List of cards to add
     */
    public void addCards(List<Card> cardsToAdd) {
        cards.addAll(cardsToAdd);
    }

    /**
     * Gets the number of cards remaining in the deck.
     *
     * @return Number of cards in the deck
     */
    public int size() {
        return cards.size();
    }

    /**
     * Checks if the deck is empty.
     *
     * @return true if the deck has no cards
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }

    /**
     * Replenishes the deck with cards from the table pile.
     * Keeps the top card on the table and shuffles the rest into the deck.
     *
     * @param tablePile List of cards from the table
     */
    public void replenishFromTable(List<Card> tablePile) {
        if (tablePile.size() > 1) {
            // Add all cards except the last one (top card stays on table)
            List<Card> cardsToAdd = new ArrayList<>(tablePile.subList(0, tablePile.size() - 1));
            cards.addAll(cardsToAdd);
            shuffle();
        }
    }
}