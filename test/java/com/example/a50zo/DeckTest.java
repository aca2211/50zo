package com.example.a50zo;

import com.example.a50zo.exceptions.EmptyDeckException;
import com.example.a50zo.model.Card;
import com.example.a50zo.model.Deck;
import com.example.a50zo.model.Rank;
import com.example.a50zo.model.Suit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Deck class.
 * Tests deck initialization, drawing, and replenishment.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
class DeckTest {

    private Deck deck;

    @BeforeEach
    void setUp() {
        deck = new Deck();
    }

    @Test
    @DisplayName("Test deck initializes with 52 cards")
    void testDeckInitialization() {
        assertEquals(52, deck.size());
        assertFalse(deck.isEmpty());
    }

    @Test
    @DisplayName("Test drawing a card reduces deck size")
    void testDrawCard() throws EmptyDeckException {
        Card card = deck.drawCard();
        assertNotNull(card);
        assertEquals(51, deck.size());
    }

    @Test
    @DisplayName("Test drawing all cards empties the deck")
    void testDrawAllCards() throws EmptyDeckException {
        for (int i = 0; i < 52; i++) {
            deck.drawCard();
        }
        assertEquals(0, deck.size());
        assertTrue(deck.isEmpty());
    }

    @Test
    @DisplayName("Test drawing from empty deck throws exception")
    void testDrawFromEmptyDeck() throws EmptyDeckException {
        for (int i = 0; i < 52; i++) {
            deck.drawCard();
        }
        assertThrows(EmptyDeckException.class, () -> deck.drawCard());
    }

    @Test
    @DisplayName("Test adding a card to deck")
    void testAddCard() {
        Card card = new Card(Rank.ACE, Suit.HEARTS);
        deck.addCard(card);
        assertEquals(53, deck.size());
    }

    @Test
    @DisplayName("Test adding multiple cards to deck")
    void testAddCards() {
        List<Card> cards = new ArrayList<>();
        cards.add(new Card(Rank.ACE, Suit.HEARTS));
        cards.add(new Card(Rank.KING, Suit.SPADES));
        cards.add(new Card(Rank.QUEEN, Suit.DIAMONDS));

        deck.addCards(cards);
        assertEquals(55, deck.size());
    }

    @Test
    @DisplayName("Test deck contains all 52 unique cards")
    void testDeckContainsAllCards() throws EmptyDeckException {
        Set<String> drawnCards = new HashSet<>();

        for (int i = 0; i < 52; i++) {
            Card card = deck.drawCard();
            drawnCards.add(card.toString());
        }

        assertEquals(52, drawnCards.size());
    }

    @Test
    @DisplayName("Test replenish from table pile")
    void testReplenishFromTable() throws EmptyDeckException {
        // Draw all cards
        for (int i = 0; i < 52; i++) {
            deck.drawCard();
        }

        // Create table pile
        List<Card> tablePile = new ArrayList<>();
        tablePile.add(new Card(Rank.TWO, Suit.HEARTS));
        tablePile.add(new Card(Rank.THREE, Suit.DIAMONDS));
        tablePile.add(new Card(Rank.FOUR, Suit.CLUBS));
        tablePile.add(new Card(Rank.FIVE, Suit.SPADES));

        // Replenish (should keep last card on table)
        deck.replenishFromTable(tablePile);

        assertEquals(3, deck.size()); // 4 cards - 1 kept on table
        assertFalse(deck.isEmpty());
    }

    @Test
    @DisplayName("Test replenish with only one card on table does nothing")
    void testReplenishWithOneCard() {
        int initialSize = deck.size();

        List<Card> tablePile = new ArrayList<>();
        tablePile.add(new Card(Rank.ACE, Suit.HEARTS));

        deck.replenishFromTable(tablePile);

        assertEquals(initialSize, deck.size());
    }

    @Test
    @DisplayName("Test shuffle randomizes deck order")
    void testShuffle() throws EmptyDeckException {
        List<String> firstOrder = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            firstOrder.add(deck.drawCard().toString());
        }

        // Create new deck and draw same amount
        Deck newDeck = new Deck();
        List<String> secondOrder = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            secondOrder.add(newDeck.drawCard().toString());
        }

        // Orders should be different (extremely unlikely to be same if shuffled)
        assertNotEquals(firstOrder, secondOrder);
    }

    @Test
    @DisplayName("Test isEmpty returns correct value")
    void testIsEmpty() throws EmptyDeckException {
        assertFalse(deck.isEmpty());

        for (int i = 0; i < 52; i++) {
            deck.drawCard();
        }

        assertTrue(deck.isEmpty());
    }

    @Test
    @DisplayName("Test size returns correct count after operations")
    void testSize() throws EmptyDeckException {
        assertEquals(52, deck.size());

        deck.drawCard();
        assertEquals(51, deck.size());

        deck.addCard(new Card(Rank.ACE, Suit.HEARTS));
        assertEquals(52, deck.size());

        for (int i = 0; i < 10; i++) {
            deck.drawCard();
        }
        assertEquals(42, deck.size());
    }
}