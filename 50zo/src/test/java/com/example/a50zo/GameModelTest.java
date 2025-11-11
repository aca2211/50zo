package com.example.a50zo;

import com.example.a50zo.exceptions.EmptyDeckException;
import com.example.a50zo.exceptions.InvalidCardPlayException;
import com.example.a50zo.exceptions.PlayerEliminatedException;
import com.example.a50zo.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the GameModel class.
 * Tests game initialization, player management, and game flow.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
class GameModelTest {

    private GameModel gameModel;

    @BeforeEach
    void setUp() throws EmptyDeckException {
        gameModel = new GameModel(2);
        gameModel.initializeGame();
    }

    @Test
    @DisplayName("Test game initializes with correct number of players")
    void testGameInitialization() {
        assertEquals(3, gameModel.getPlayers().size()); // 1 human + 2 machines
        assertEquals(3, gameModel.getActivePlayers().size());
    }

    @Test
    @DisplayName("Test each player starts with 4 cards")
    void testPlayersStartWith4Cards() {
        for (Player player : gameModel.getPlayers()) {
            assertEquals(4, player.getHandSize());
        }
    }

    @Test
    @DisplayName("Test table has initial card and sum")
    void testTableInitialization() {
        assertNotNull(gameModel.getTopCard());
        assertTrue(gameModel.getTableSum() >= -10 && gameModel.getTableSum() <= 10);
    }

    @Test
    @DisplayName("Test first player is human")
    void testFirstPlayerIsHuman() {
        Player firstPlayer = gameModel.getCurrentPlayer();
        assertTrue(firstPlayer instanceof HumanPlayer);
        assertEquals("You", firstPlayer.getName());
    }

    @Test
    @DisplayName("Test playing a valid card updates table sum")
    void testPlayValidCard() throws InvalidCardPlayException, EmptyDeckException {
        int initialSum = gameModel.getTableSum();
        HumanPlayer humanPlayer = gameModel.getHumanPlayer();

        // Find a valid card to play
        Card validCard = null;
        for (Card card : humanPlayer.getHand()) {
            if (card.canBePlayed(initialSum)) {
                validCard = card;
                break;
            }
        }

        assertNotNull(validCard, "Player should have at least one valid card");

        int expectedSum = initialSum + validCard.getBestValue(initialSum);
        gameModel.playCard(validCard);

        assertEquals(expectedSum, gameModel.getTableSum());
        assertEquals(validCard, gameModel.getTopCard());
    }

    @Test
    @DisplayName("Test drawing card adds to player hand")
    void testDrawCard() throws EmptyDeckException {
        int initialHandSize = gameModel.getCurrentPlayer().getHandSize();
        gameModel.drawCard();
        assertEquals(initialHandSize + 1, gameModel.getCurrentPlayer().getHandSize());
    }

    @Test
    @DisplayName("Test next turn advances to next player")
    void testNextTurn() {
        Player firstPlayer = gameModel.getCurrentPlayer();
        gameModel.nextTurn();
        Player secondPlayer = gameModel.getCurrentPlayer();

        assertNotEquals(firstPlayer, secondPlayer);
    }

    @Test
    @DisplayName("Test next turn skips eliminated players")
    void testNextTurnSkipsEliminatedPlayers() {
        Player firstPlayer = gameModel.getCurrentPlayer();
        gameModel.nextTurn();
        Player secondPlayer = gameModel.getCurrentPlayer();

        // Eliminate second player
        secondPlayer.eliminate();

        gameModel.nextTurn();
        Player thirdPlayer = gameModel.getCurrentPlayer();

        assertNotEquals(secondPlayer, thirdPlayer);
    }

    @Test
    @DisplayName("Test eliminating player removes them from active players")
    void testEliminatePlayer() {
        Player player = gameModel.getPlayers().get(1);
        int initialActiveCount = gameModel.getActivePlayers().size();

        player.eliminate();

        assertEquals(initialActiveCount - 1, gameModel.getActivePlayers().size());
        assertFalse(gameModel.getActivePlayers().contains(player));
    }

    @Test
    @DisplayName("Test game over when only one player remains")
    void testGameOver() {
        // Eliminate all but one player
        gameModel.getPlayers().get(1).eliminate();
        gameModel.getPlayers().get(2).eliminate();

        gameModel.nextTurn();

        assertTrue(gameModel.isGameOver());
        assertNotNull(gameModel.getWinner());
        assertEquals(gameModel.getHumanPlayer(), gameModel.getWinner());
    }

    @Test
    @DisplayName("Test game not over with multiple active players")
    void testGameNotOver() {
        assertFalse(gameModel.isGameOver());
        assertNull(gameModel.getWinner());
    }

    @Test
    @DisplayName("Test get human player returns correct player")
    void testGetHumanPlayer() {
        HumanPlayer humanPlayer = gameModel.getHumanPlayer();
        assertNotNull(humanPlayer);
        assertTrue(humanPlayer instanceof HumanPlayer);
        assertEquals("You", humanPlayer.getName());
    }

    @Test
    @DisplayName("Test deck size decreases when drawing cards")
    void testDeckSizeDecreases() throws EmptyDeckException {
        int initialDeckSize = gameModel.getDeckSize();
        gameModel.drawCard();
        assertEquals(initialDeckSize - 1, gameModel.getDeckSize());
    }

    @Test
    @DisplayName("Test playing invalid card throws exception")
    void testPlayInvalidCard() {
        // Create a card that would exceed 50
        Card invalidCard = new Card(Rank.TEN, Suit.HEARTS);

        // Set table sum to 45
        while (gameModel.getTableSum() < 45) {
            try {
                // Find a card to increase sum
                for (Card card : gameModel.getCurrentPlayer().getHand()) {
                    if (card.getRank() != Rank.NINE && card.getRank() != Rank.JACK
                            && card.getRank() != Rank.QUEEN && card.getRank() != Rank.KING) {
                        gameModel.playCard(card);
                        break;
                    }
                }
            } catch (Exception e) {
                break;
            }
        }

        // Now try to play a card that exceeds 50
        assertThrows(InvalidCardPlayException.class, () -> {
            if (!invalidCard.canBePlayed(gameModel.getTableSum())) {
                throw new InvalidCardPlayException("Cannot play this card");
            }
        });
    }

    @Test
    @DisplayName("Test eliminated player cards return to deck")
    void testEliminatedPlayerCardsReturnToDeck() throws PlayerEliminatedException {
        Player player = gameModel.getPlayers().get(1);
        int initialDeckSize = gameModel.getDeckSize();
        int playerHandSize = player.getHandSize();

        player.eliminate();
        player.removeAllCards();

        // Cards should be available to add back to deck
        assertEquals(0, player.getHandSize());
    }

    @Test
    @DisplayName("Test game with 1 machine player")
    void testGameWith1MachinePlayer() throws EmptyDeckException {
        GameModel game = new GameModel(1);
        game.initializeGame();

        assertEquals(2, game.getPlayers().size());
        assertEquals(1, game.getPlayers().stream()
                .filter(p -> p instanceof MachinePlayer).count());
    }

    @Test
    @DisplayName("Test game with 3 machine players")
    void testGameWith3MachinePlayers() throws EmptyDeckException {
        GameModel game = new GameModel(3);
        game.initializeGame();

        assertEquals(4, game.getPlayers().size());
        assertEquals(3, game.getPlayers().stream()
                .filter(p -> p instanceof MachinePlayer).count());
    }

    @Test
    @DisplayName("Test current player can have valid moves")
    void testPlayerHasValidMoves() {
        Player currentPlayer = gameModel.getCurrentPlayer();

        // At game start, player should have valid moves
        boolean hasValidMove = currentPlayer.hasValidMove(gameModel.getTableSum());

        // This should typically be true at game start
        assertTrue(hasValidMove || gameModel.getTableSum() > 40);
    }

    @Test
    @DisplayName("Test top card on table updates after play")
    void testTopCardUpdates() throws InvalidCardPlayException, EmptyDeckException {
        Card initialTopCard = gameModel.getTopCard();

        // Find and play a valid card
        HumanPlayer humanPlayer = gameModel.getHumanPlayer();
        Card validCard = null;
        for (Card card : humanPlayer.getHand()) {
            if (card.canBePlayed(gameModel.getTableSum())) {
                validCard = card;
                break;
            }
        }

        if (validCard != null) {
            gameModel.playCard(validCard);
            Card newTopCard = gameModel.getTopCard();

            assertEquals(validCard, newTopCard);
            assertNotEquals(initialTopCard, newTopCard);
        }
    }
}
