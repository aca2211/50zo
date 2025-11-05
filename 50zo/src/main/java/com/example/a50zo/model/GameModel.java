package com.example.a50zo.model;

import com.example.a50zo.exceptions.EmptyDeckException;
import com.example.a50zo.exceptions.InvalidCardPlayException;
import com.example.a50zo.exceptions.PlayerEliminatedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Main game model that manages the game state, players, deck, and game logic.
 * Implements the core rules of Cincuentazo.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
public class GameModel {
    private final Deck deck;
    private final List<Player> players;
    private final List<Card> tablePile;
    private int currentPlayerIndex;
    private int tableSum;
    private boolean gameOver;
    private Player winner;

    /**
     * Constructor for GameModel.
     *
     * @param numberOfMachinePlayers Number of machine players (1-3)
     */
    public GameModel(int numberOfMachinePlayers) {
        this.deck = new Deck();
        this.players = new ArrayList<>();
        this.tablePile = new ArrayList<>();
        this.currentPlayerIndex = 0;
        this.tableSum = 0;
        this.gameOver = false;

        initializePlayers(numberOfMachinePlayers);
    }

    /**
     * Initializes all players (1 human + machine players).
     *
     * @param numberOfMachinePlayers Number of machine players to create
     */
    private void initializePlayers(int numberOfMachinePlayers) {
        players.add(new HumanPlayer("You"));
        for (int i = 1; i <= numberOfMachinePlayers; i++) {
            players.add(new MachinePlayer("Machine " + i));
        }
    }

    /**
     * Initializes the game by dealing cards and setting up the table.
     *
     * @throws EmptyDeckException if the deck runs out during setup
     */
    public void initializeGame() throws EmptyDeckException {
        // Deal 4 cards to each player
        for (int i = 0; i < 4; i++) {
            for (Player player : players) {
                player.addCardToHand(deck.drawCard());
            }
        }

        // Place one card on the table
        Card initialCard = deck.drawCard();
        tablePile.add(initialCard);
        tableSum = initialCard.getBestValue(0);
    }

    /**
     * Plays a card for the current player.
     *
     * @param card The card to play
     * @throws InvalidCardPlayException if the card cannot be played
     */
    public void playCard(Card card) throws InvalidCardPlayException {
        Player currentPlayer = getCurrentPlayer();

        if (currentPlayer.isEliminated()) {
            throw new InvalidCardPlayException("Eliminated players cannot play");
        }

        if (!card.canBePlayed(tableSum)) {
            throw new InvalidCardPlayException("Playing this card would exceed 50");
        }

        currentPlayer.removeCardFromHand(card);
        tablePile.add(card);
        tableSum += card.getBestValue(tableSum - card.getBestValue(tableSum));
    }

    /**
     * Current player draws a card from the deck.
     * Replenishes deck from table pile if necessary.
     *
     * @throws EmptyDeckException if unable to draw a card
     */
    public void drawCard() throws EmptyDeckException {
        if (deck.isEmpty()) {
            if (tablePile.size() > 1) {
                deck.replenishFromTable(tablePile);
                // Keep only the top card on the table
                Card topCard = tablePile.get(tablePile.size() - 1);
                tablePile.clear();
                tablePile.add(topCard);
            } else {
                throw new EmptyDeckException("Cannot replenish deck");
            }
        }

        Card drawnCard = deck.drawCard();
        getCurrentPlayer().addCardToHand(drawnCard);
    }

    /**
     * Advances to the next player's turn.
     * Skips eliminated players and checks for game over condition.
     */
    public void nextTurn() {
        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        } while (getCurrentPlayer().isEliminated() && getActivePlayers().size() > 1);

        checkGameOver();
    }

    /**
     * Eliminates the current player if they have no valid moves.
     * Returns their cards to the deck.
     *
     * @throws PlayerEliminatedException when a player is eliminated
     */
    public void eliminateCurrentPlayer() throws PlayerEliminatedException {
        Player player = getCurrentPlayer();

        if (!player.hasValidMove(tableSum)) {
            player.eliminate();
            List<Card> cards = player.removeAllCards();
            deck.addCards(cards);

            checkGameOver();

            if (!gameOver) {
                nextTurn();
            }

            throw new PlayerEliminatedException(player.getName() + " has been eliminated!");
        }
    }

    /**
     * Checks if the game is over (only one player remains).
     */
    private void checkGameOver() {
        List<Player> activePlayers = getActivePlayers();
        if (activePlayers.size() == 1) {
            gameOver = true;
            winner = activePlayers.get(0);
        }
    }

    /**
     * Gets the list of active (non-eliminated) players.
     *
     * @return List of active players
     */
    public List<Player> getActivePlayers() {
        return players.stream()
                .filter(p -> !p.isEliminated())
                .toList();
    }

    /**
     * Gets the current player.
     *
     * @return The current player
     */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    /**
     * Gets all players in the game.
     *
     * @return List of all players
     */
    public List<Player> getPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * Gets the current table sum.
     *
     * @return The current sum
     */
    public int getTableSum() {
        return tableSum;
    }

    /**
     * Gets the top card on the table.
     *
     * @return The top card, or null if no cards on table
     */
    public Card getTopCard() {
        return tablePile.isEmpty() ? null : tablePile.get(tablePile.size() - 1);
    }

    /**
     * Gets the number of cards remaining in the deck.
     *
     * @return Deck size
     */
    public int getDeckSize() {
        return deck.size();
    }

    /**
     * Checks if the game is over.
     *
     * @return true if the game has ended
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Gets the winner of the game.
     *
     * @return The winning player, or null if game is not over
     */
    public Player getWinner() {
        return winner;
    }

    /**
     * Gets the human player.
     *
     * @return The human player
     */
    public HumanPlayer getHumanPlayer() {
        return (HumanPlayer) players.get(0);
    }
}
