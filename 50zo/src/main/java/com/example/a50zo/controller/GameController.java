package com.example.a50zo.controller;

import com.example.a50zo.exceptions.EmptyDeckException;
import com.example.a50zo.exceptions.InvalidCardPlayException;
import com.example.a50zo.exceptions.PlayerEliminatedException;
import com.example.a50zo.model.*;
import com.example.a50zo.utils.CardImageLoader;
import com.example.a50zo.view.Alert;
import com.example.a50zo.view.WelcomeStage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Main game controller that manages game logic and UI updates.
 * Implements event handling for card plays and turn management.
 * Uses threads for machine player timing.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
public class GameController {

    @FXML
    private Label lblTableSum;

    @FXML
    private Label lblCurrentPlayer;

    @FXML
    private Label lblDeckSize;

    @FXML
    private ImageView imgTableCard;

    @FXML
    private HBox hboxPlayerHand;

    @FXML
    private VBox vboxMachine1;

    @FXML
    private VBox vboxMachine2;

    @FXML
    private VBox vboxMachine3;

    @FXML
    private Label lblMachine1;

    @FXML
    private Label lblMachine2;

    @FXML
    private Label lblMachine3;

    @FXML
    private Label lblMachine1Status;

    @FXML
    private Label lblMachine2Status;

    @FXML
    private Label lblMachine3Status;

    @FXML
    private Button btnNewGame;

    private GameModel gameModel;
    private CardImageLoader imageLoader;
    private List<ImageView> playerCardViews;
    private Thread machinePlayerThread;
    private volatile boolean isProcessingTurn = false;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        imageLoader = CardImageLoader.getInstance();
        playerCardViews = new ArrayList<>();
    }

    /**
     * Initializes a new game with the specified number of machine players.
     *
     * @param numberOfMachinePlayers Number of machine players (1-3)
     */
    public void initializeGame(int numberOfMachinePlayers) {
        gameModel = new GameModel(numberOfMachinePlayers);

        try {
            gameModel.initializeGame();
            setupMachinePlayers(numberOfMachinePlayers);
            updateUI();

            // Start machine turn if human is not first
            if (!(gameModel.getCurrentPlayer() instanceof HumanPlayer)) {
                startMachineTurn();
            }
        } catch (EmptyDeckException e) {
            Alert.showError("Error", "Game Initialization Failed", e.getMessage());
        }
    }

    /**
     * Sets up the machine player display areas based on count.
     *
     * @param count Number of machine players
     */
    private void setupMachinePlayers(int count) {
        vboxMachine1.setVisible(count >= 1);
        vboxMachine2.setVisible(count >= 2);
        vboxMachine3.setVisible(count >= 3);

        if (count >= 1) {
            lblMachine1.setText(gameModel.getPlayers().get(1).getName());
            updateMachineCards(vboxMachine1, 4);
        }
        if (count >= 2) {
            lblMachine2.setText(gameModel.getPlayers().get(2).getName());
            updateMachineCards(vboxMachine2, 4);
        }
        if (count >= 3) {
            lblMachine3.setText(gameModel.getPlayers().get(3).getName());
            updateMachineCards(vboxMachine3, 4);
        }
    }

    /**
     * Updates all UI elements to reflect current game state.
     */
    private void updateUI() {
        updateTableDisplay();
        updatePlayerHand();
        updateMachinePlayersDisplay();
        updateGameInfo();

        // NUEVO: Verificar si el jugador humano puede jugar
        if (gameModel.getCurrentPlayer() instanceof HumanPlayer && !isProcessingTurn) {
            checkHumanPlayerCanPlay();
        }
    }
    /**
     * Checks if the human player can make any valid move.
     * If not, eliminates the player automatically.
     */
    private void checkHumanPlayerCanPlay() {
        HumanPlayer humanPlayer = gameModel.getHumanPlayer();

        if (!humanPlayer.isEliminated() && !humanPlayer.hasValidMove(gameModel.getTableSum())) {
            // El jugador humano no tiene movimientos válidos
            Platform.runLater(() -> {
                Alert.showWarning(
                        "No Valid Moves!",
                        "You're Eliminated!",
                        "You have no cards that can be played without exceeding 50. You are eliminated from the game!"
                );

                try {
                    isProcessingTurn = true;
                    gameModel.eliminateCurrentPlayer();
                    updateUI();

                    if (gameModel.isGameOver()) {
                        handleGameOver();
                    } else {
                        gameModel.nextTurn();
                        updateUI();

                        // Si el siguiente es máquina, iniciar su turno
                        if (!(gameModel.getCurrentPlayer() instanceof HumanPlayer)) {
                            startMachineTurn();
                        }
                    }
                } catch (PlayerEliminatedException e) {
                    // Ya manejado arriba
                } finally {
                    isProcessingTurn = false;
                }
            });
        }
    }

    /**
     * Updates the table card and sum display.
     */
    private void updateTableDisplay() {
        Card topCard = gameModel.getTopCard();
        if (topCard != null) {
            imgTableCard.setImage(imageLoader.getCardImage(topCard.getImageFileName()));
        }
        lblTableSum.setText(String.valueOf(gameModel.getTableSum()));

        // Add visual effect to table sum
        int sum = gameModel.getTableSum();
        if (sum > 40) {
            lblTableSum.setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold;");
        } else if (sum > 30) {
            lblTableSum.setStyle("-fx-text-fill: #ff9944; -fx-font-weight: bold;");
        } else {
            lblTableSum.setStyle("-fx-text-fill: #44ff44; -fx-font-weight: bold;");
        }
    }

    /**
     * Updates the human player's hand display.
     */
    private void updatePlayerHand() {
        hboxPlayerHand.getChildren().clear();
        playerCardViews.clear();

        HumanPlayer humanPlayer = gameModel.getHumanPlayer();
        List<Card> hand = humanPlayer.getHand();

        for (Card card : hand) {
            ImageView cardView = createCardView(card);
            playerCardViews.add(cardView);
            hboxPlayerHand.getChildren().add(cardView);
        }
    }

    /**
     * Creates an interactive card view for the human player.
     *
     * @param card The card to display
     * @return ImageView with event handlers
     */
    private ImageView createCardView(Card card) {
        ImageView cardView = new ImageView(imageLoader.getCardImage(card.getImageFileName()));
        cardView.setFitWidth(100);
        cardView.setFitHeight(140);
        cardView.setPreserveRatio(true);

        // Add hover effect
        DropShadow shadow = new DropShadow();
        shadow.setRadius(15);

        boolean canPlay = card.canBePlayed(gameModel.getTableSum());

        cardView.setOnMouseEntered(e -> {
            if (!isProcessingTurn && gameModel.getCurrentPlayer() instanceof HumanPlayer) {
                cardView.setEffect(shadow);
                cardView.setTranslateY(-10);

                // Show if card can be played
                if (canPlay) {
                    shadow.setColor(Color.LIGHTGREEN);
                } else {
                    shadow.setColor(Color.LIGHTCORAL);
                }
            }
        });

        cardView.setOnMouseExited(e -> {
            cardView.setEffect(null);
            cardView.setTranslateY(0);
        });

        // Handle card click
        cardView.setOnMouseClicked(e -> handleCardPlay(card));

        // Visual indicator if card cannot be played
        if (!canPlay) {
            cardView.setOpacity(0.6);
        }

        return cardView;
    }


    /**
     * Handles when the human player clicks a card to play it.
     *
     * @param card The card to play
     */
    private void handleCardPlay(Card card) {
        if (isProcessingTurn) {
            return;
        }

        if (!(gameModel.getCurrentPlayer() instanceof HumanPlayer)) {
            Alert.showWarning("Not Your Turn", "Wait", "It's not your turn!");
            return;
        }

        // Verificar si el jugador puede jugar CUALQUIER carta
        if (!gameModel.getHumanPlayer().hasValidMove(gameModel.getTableSum())) {
            Alert.showWarning(
                    "No Valid Moves",
                    "You're Eliminated!",
                    "You cannot play any card. You will be eliminated!"
            );
            return;
        }

        // Verificar si ESTA carta específica puede jugarse
        if (!card.canBePlayed(gameModel.getTableSum())) {
            Alert.showWarning(
                    "Invalid Card",
                    "Cannot Play This Card",
                    "This card would make the sum exceed 50. Choose another card!"
            );
            return;
        }

        try {
            isProcessingTurn = true;

            // Guardar la carta antes de jugarla
            int oldSum = gameModel.getTableSum();

            gameModel.getHumanPlayer().playCard(card, gameModel.getTableSum());
            gameModel.playCard(card);

            int newSum = gameModel.getTableSum();

            // Mostrar información de la jugada
            System.out.println("Played: " + card.toString() + " | Old sum: " + oldSum + " → New sum: " + newSum);

            gameModel.drawCard();

            updateUI();

            if (gameModel.isGameOver()) {
                handleGameOver();
            } else {
                gameModel.nextTurn();
                updateUI();

                // Start machine turn if next player is machine
                if (!(gameModel.getCurrentPlayer() instanceof HumanPlayer)) {
                    startMachineTurn();
                }
            }

        } catch (InvalidCardPlayException e) {
            Alert.showWarning("Invalid Play", "Cannot play this card", e.getMessage());
        } catch (EmptyDeckException e) {
            Alert.showError("Error", "Deck Error", e.getMessage());
        } catch (PlayerEliminatedException e) {
            Alert.showInfo("Player Eliminated", "Eliminated", e.getMessage());
        } finally {
            isProcessingTurn = false;
        }
    }

    /**
     * Starts a machine player's turn in a separate thread.
     */
    private void startMachineTurn() {
        machinePlayerThread = new Thread(() -> {
            try {
                // Random delay between 2-4 seconds for card selection
                Random random = new Random();
                Thread.sleep(2000 + random.nextInt(2000));

                Platform.runLater(() -> {
                    try {
                        processMachineTurn();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        machinePlayerThread.setDaemon(true);
        machinePlayerThread.start();
    }

    /**
     * Processes a machine player's turn.
     */
    private void processMachineTurn() {
        if (isProcessingTurn || gameModel.isGameOver()) {
            return;
        }

        Player currentPlayer = gameModel.getCurrentPlayer();

        if (currentPlayer instanceof MachinePlayer) {
            try {
                isProcessingTurn = true;

                // Check if machine can play
                if (!currentPlayer.hasValidMove(gameModel.getTableSum())) {
                    gameModel.eliminateCurrentPlayer();
                    updateUI();

                    if (!gameModel.isGameOver()) {
                        continueToNextTurn();
                    } else {
                        handleGameOver();
                    }
                    return;
                }

                // Machine selects and plays card
                Card selectedCard = ((MachinePlayer) currentPlayer).selectCard(gameModel.getTableSum());
                gameModel.playCard(selectedCard);

                updateUI();

                // Delay before drawing card
                Thread drawThread = new Thread(() -> {
                    try {
                        Random random = new Random();
                        Thread.sleep(1000 + random.nextInt(1000));

                        Platform.runLater(() -> {
                            try {
                                gameModel.drawCard();
                                updateUI();

                                if (gameModel.isGameOver()) {
                                    handleGameOver();
                                } else {
                                    gameModel.nextTurn();
                                    updateUI();
                                    continueToNextTurn();
                                }
                            } catch (EmptyDeckException e) {
                                Alert.showError("Error", "Deck Error", e.getMessage());
                            } finally {
                                isProcessingTurn = false;
                            }
                        });

                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                drawThread.setDaemon(true);
                drawThread.start();

            } catch (InvalidCardPlayException e) {
                try {
                    gameModel.eliminateCurrentPlayer();
                    updateUI();

                    if (!gameModel.isGameOver()) {
                        continueToNextTurn();
                    } else {
                        handleGameOver();
                    }
                } catch (PlayerEliminatedException pe) {
                    Alert.showInfo("Player Eliminated", "Machine Eliminated", pe.getMessage());
                } finally {
                    isProcessingTurn = false;
                }
            } catch (PlayerEliminatedException e) {
                Alert.showInfo("Player Eliminated", "Machine Eliminated", e.getMessage());
                isProcessingTurn = false;

                if (!gameModel.isGameOver()) {
                    continueToNextTurn();
                } else {
                    handleGameOver();
                }
            }
        }
    }

    /**
     * Continues to the next player's turn.
     */
    private void continueToNextTurn() {
        if (gameModel.getCurrentPlayer() instanceof HumanPlayer) {
            isProcessingTurn = false;

            // Check if human player can play
            if (!gameModel.getHumanPlayer().hasValidMove(gameModel.getTableSum())) {
                try {
                    gameModel.eliminateCurrentPlayer();
                    Alert.showInfo("Eliminated", "You're Out!", "You have no valid moves and are eliminated!");
                    handleGameOver();
                } catch (PlayerEliminatedException e) {
                    Alert.showInfo("Eliminated", "You're Out!", e.getMessage());
                }
            }
        } else {
            startMachineTurn();
        }
    }

    /**
     * Updates the display for all machine players.
     */
    private void updateMachinePlayersDisplay() {
        List<Player> players = gameModel.getPlayers();

        if (players.size() > 1) {
            updateMachinePlayerDisplay(vboxMachine1, lblMachine1Status, players.get(1));
        }
        if (players.size() > 2) {
            updateMachinePlayerDisplay(vboxMachine2, lblMachine2Status, players.get(2));
        }
        if (players.size() > 3) {
            updateMachinePlayerDisplay(vboxMachine3, lblMachine3Status, players.get(3));
        }
    }

    /**
     * Updates display for a specific machine player.
     *
     * @param vbox Container for the machine player
     * @param statusLabel Status label
     * @param player The player to display
     */
    private void updateMachinePlayerDisplay(VBox vbox, Label statusLabel, Player player) {
        if (player.isEliminated()) {
            statusLabel.setText("ELIMINATED");
            statusLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold;");

            // Clear cards
            HBox cardBox = (HBox) vbox.getChildren().stream()
                    .filter(node -> node instanceof HBox)
                    .findFirst()
                    .orElse(null);
            if (cardBox != null) {
                cardBox.getChildren().clear();
            }
        } else {
            statusLabel.setText("Cards: " + player.getHandSize());
            statusLabel.setStyle("-fx-text-fill: #44ff44; -fx-font-weight: bold;");
            updateMachineCards(vbox, player.getHandSize());
        }
    }

    /**
     * Updates the card backs display for a machine player.
     *
     * @param vbox Container for the cards
     * @param cardCount Number of cards to display
     */
    private void updateMachineCards(VBox vbox, int cardCount) {
        HBox cardBox = (HBox) vbox.getChildren().stream()
                .filter(node -> node instanceof HBox)
                .findFirst()
                .orElse(null);

        if (cardBox == null) {
            cardBox = new HBox(5);
            cardBox.setAlignment(Pos.CENTER);
            vbox.getChildren().add(cardBox);
        }

        cardBox.getChildren().clear();

        for (int i = 0; i < cardCount; i++) {
            ImageView cardBack = new ImageView(imageLoader.getCardBackImage());
            cardBack.setFitWidth(60);
            cardBack.setFitHeight(84);
            cardBack.setPreserveRatio(true);
            cardBox.getChildren().add(cardBack);
        }
    }

    /**
     * Updates game information labels.
     */
    private void updateGameInfo() {
        Player currentPlayer = gameModel.getCurrentPlayer();
        lblCurrentPlayer.setText(currentPlayer.getName() + "'s Turn");

        if (currentPlayer instanceof HumanPlayer) {
            lblCurrentPlayer.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
        } else {
            lblCurrentPlayer.setStyle("-fx-text-fill: #FF9800; -fx-font-weight: bold;");
        }

        lblDeckSize.setText(String.valueOf(gameModel.getDeckSize()));
    }

    /**
     * Handles game over condition.
     */
    private void handleGameOver() {
        isProcessingTurn = true;
        Player winner = gameModel.getWinner();

        String message = winner instanceof HumanPlayer
                ? "Congratulations! You won!"
                : winner.getName() + " wins!";

        Alert.showInfo("Game Over", "Winner!", message);
        btnNewGame.setDisable(false);
    }

    /**
     * Handles new game button click.
     */
    @FXML
    private void handleNewGame() {
        if (Alert.showConfirmation("New Game", "Start Over", "Do you want to start a new game?")) {
            cleanup();
            try {
                WelcomeStage welcomeStage = WelcomeStage.getInstance();
                welcomeStage.show();

                Stage currentStage = (Stage) btnNewGame.getScene().getWindow();
                currentStage.close();
            } catch (IOException e) {
                Alert.showError("Error", "Failed to restart", e.getMessage());
            }
        }
    }

    /**
     * Cleans up resources and stops threads.
     */
    public void cleanup() {
        if (machinePlayerThread != null && machinePlayerThread.isAlive()) {
            machinePlayerThread.interrupt();
        }
    }
}
