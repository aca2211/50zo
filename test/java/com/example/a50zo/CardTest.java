package com.example.a50zo;

import com.example.a50zo.model.Card;
import com.example.a50zo.model.Rank;
import com.example.a50zo.model.Suit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Card class.
 * Tests card value calculations and play validation.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
class CardTest {

    private Card aceCard;
    private Card kingCard;
    private Card nineCard;
    private Card fiveCard;

    @BeforeEach
    void setUp() {
        aceCard = new Card(Rank.ACE, Suit.HEARTS);
        kingCard = new Card(Rank.KING, Suit.SPADES);
        nineCard = new Card(Rank.NINE, Suit.DIAMONDS);
        fiveCard = new Card(Rank.FIVE, Suit.CLUBS);
    }

    @Test
    @DisplayName("Test Ace chooses best value (10) when sum allows")
    void testAceBestValueHigh() {
        int currentSum = 30;
        assertEquals(10, aceCard.getBestValue(currentSum));
    }

    @Test
    @DisplayName("Test Ace chooses best value (1) when 10 would exceed 50")
    void testAceBestValueLow() {
        int currentSum = 45;
        assertEquals(1, aceCard.getBestValue(currentSum));
    }

    @Test
    @DisplayName("Test King subtracts 10")
    void testKingValue() {
        int currentSum = 30;
        assertEquals(-10, kingCard.getBestValue(currentSum));
    }

    @Test
    @DisplayName("Test Nine adds zero")
    void testNineValue() {
        int currentSum = 25;
        assertEquals(0, nineCard.getBestValue(currentSum));
    }

    @Test
    @DisplayName("Test number card adds its value")
    void testNumberCardValue() {
        int currentSum = 20;
        assertEquals(5, fiveCard.getBestValue(currentSum));
    }

    @Test
    @DisplayName("Test card can be played when sum stays at or below 50")
    void testCanBePlayed() {
        assertTrue(fiveCard.canBePlayed(45));
        assertTrue(kingCard.canBePlayed(50));
        assertTrue(nineCard.canBePlayed(50));
    }

    @Test
    @DisplayName("Test card cannot be played when sum exceeds 50")
    void testCannotBePlayed() {
        assertFalse(fiveCard.canBePlayed(46));
        Card tenCard = new Card(Rank.TEN, Suit.HEARTS);
        assertFalse(tenCard.canBePlayed(45));
    }

    @Test
    @DisplayName("Test Ace can always be played (chooses 1 if needed)")
    void testAceCanAlwaysBePlayed() {
        assertTrue(aceCard.canBePlayed(50));
        assertTrue(aceCard.canBePlayed(49));
        assertTrue(aceCard.canBePlayed(5));
    }

    @Test
    @DisplayName("Test face cards can be played even with high sum")
    void testFaceCardsCanBePlayed() {
        assertTrue(kingCard.canBePlayed(50));
        Card queenCard = new Card(Rank.QUEEN, Suit.HEARTS);
        assertTrue(queenCard.canBePlayed(48));
    }

    @Test
    @DisplayName("Test card image file name format")
    void testImageFileName() {
        assertEquals("AH.png", aceCard.getImageFileName());
        assertEquals("KS.png", kingCard.getImageFileName());
        assertEquals("9D.png", nineCard.getImageFileName());
        assertEquals("5C.png", fiveCard.getImageFileName());
    }

    @Test
    @DisplayName("Test card toString format")
    void testToString() {
        assertEquals("AH", aceCard.toString());
        assertEquals("KS", kingCard.toString());
        assertEquals("9D", nineCard.toString());
        assertEquals("5C", fiveCard.toString());
    }

    @Test
    @DisplayName("Test card rank and suit getters")
    void testGetters() {
        assertEquals(Rank.ACE, aceCard.getRank());
        assertEquals(Suit.HEARTS, aceCard.getSuit());
        assertEquals(Rank.KING, kingCard.getRank());
        assertEquals(Suit.SPADES, kingCard.getSuit());
    }
}
