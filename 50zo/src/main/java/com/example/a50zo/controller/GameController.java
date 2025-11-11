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

    @FXML
    private Button btnMainMenu;

    private GameModel gameModel;
    private CardImageLoader imageLoader;
    private List<ImageView> playerCardViews;
    private Thread machinePlayerThread;
    private volatile boolean isProcessingTurn = false;
    private volatile boolean humanEliminationChecked = false;
    private static final boolean DEBUG = true;

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

        // Deshabilitar botones SOLO al inicio del juego (no después)
        if (!gameModel.getHumanPlayer().isEliminated()) {
            btnNewGame.setDisable(true);
            btnMainMenu.setDisable(true);
        }

        // Resetear flag de eliminación
        humanEliminationChecked = false;

        try {
            gameModel.initializeGame();
            setupMachinePlayers(numberOfMachinePlayers);
            updateUI();

            // Verificar y comenzar el turno apropiado
            checkAndStartTurn();

        } catch (EmptyDeckException e) {
            Alert.showError("Error", "Game Initialization Failed", e.getMessage());
        }
    }

    /**
     * Verifica y comienza el turno del jugador actual
     */
    private void checkAndStartTurn() {
        if (gameModel.isGameOver()) {
            handleGameOver();
            return;
        }

        Player currentPlayer = gameModel.getCurrentPlayer();
        log("=== CHECK AND START TURN ===");
        log("Current player: " + currentPlayer.getName());
        log("Is human: " + (currentPlayer instanceof HumanPlayer));
        log("Has valid moves: " + currentPlayer.hasValidMove(gameModel.getTableSum()));

        if (currentPlayer instanceof HumanPlayer) {
            HumanPlayer humanPlayer = (HumanPlayer) currentPlayer;

            if (!humanPlayer.isEliminated() && !humanPlayer.hasValidMove(gameModel.getTableSum())) {
                log("⚠️ Human player has NO valid moves - eliminating");
                eliminateHumanPlayer();
            } else {
                log("✅ Human player can play - waiting for card selection");
                isProcessingTurn = false;
                humanEliminationChecked = false;
            }
        } else {
            log("🤖 Starting machine player turn");
            startMachineTurn();
        }
    }

    /**
     * Elimina al jugador humano cuando no tiene movimientos válidos
     */
    private void eliminateHumanPlayer() {
        if (humanEliminationChecked) {
            log("Human elimination already processed");
            return;
        }

        humanEliminationChecked = true;
        isProcessingTurn = true;

        // PRIMERO habilitar los botones
        btnNewGame.setDisable(false);
        btnMainMenu.setDisable(false);

        Platform.runLater(() -> {
            Alert.showWarning(
                    "No Valid Moves!",
                    "You're Eliminated!",
                    "You have no cards that can be played without exceeding 50.\nYou are eliminated from the game!"
            );

            try {
                gameModel.eliminateCurrentPlayer();
                updateUI();

                // CONFIRMAR que los botones siguen habilitados después de updateUI
                btnNewGame.setDisable(false);
                btnMainMenu.setDisable(false);

                if (gameModel.isGameOver()) {
                    handleGameOver();
                } else {
                    // Esperar un poco antes de continuar
                    Thread continueThread = new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                            Platform.runLater(() -> {
                                isProcessingTurn = false;

                                // ASEGURAR que los botones siguen habilitados
                                btnNewGame.setDisable(false);
                                btnMainMenu.setDisable(false);

                                checkAndStartTurn();
                            });
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    });
                    continueThread.setDaemon(true);
                    continueThread.start();
                }
            } catch (PlayerEliminatedException e) {
                log("Human player eliminated: " + e.getMessage());

                // ASEGURAR botones habilitados incluso en excepción
                btnNewGame.setDisable(false);
                btnMainMenu.setDisable(false);
            }
        });
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

        if (humanPlayer.isEliminated()) {
            Label eliminatedLabel = new Label("❌ YOU ARE ELIMINATED ❌");
            eliminatedLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-size: 24px; -fx-font-weight: bold;");
            hboxPlayerHand.getChildren().add(eliminatedLabel);
            return;
        }

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
            if (!isProcessingTurn && gameModel.getCurrentPlayer() instanceof HumanPlayer
                    && !gameModel.getHumanPlayer().isEliminated()) {
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
            cardView.setOpacity(0.5);
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
            log("Turn is being processed, ignoring click");
            return;
        }

        if (!(gameModel.getCurrentPlayer() instanceof HumanPlayer)) {
            Alert.showWarning("Not Your Turn", "Wait", "It's not your turn!");
            return;
        }

        if (gameModel.getHumanPlayer().isEliminated()) {
            Alert.showWarning("Eliminated", "Cannot Play", "You have been eliminated from the game!");
            return;
        }

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
            int oldSum = gameModel.getTableSum();

            gameModel.getHumanPlayer().playCard(card, gameModel.getTableSum());
            gameModel.playCard(card);

            int newSum = gameModel.getTableSum();
            log("Human played: " + card + " | " + oldSum + " → " + newSum);

            gameModel.drawCard();
            gameModel.nextTurn();

            updateUI();

            if (gameModel.isGameOver()) {
                handleGameOver();
            } else {
                // Continuar al siguiente turno después de un delay
                Thread continueThread = new Thread(() -> {
                    try {
                        Thread.sleep(800);
                        Platform.runLater(() -> {
                            isProcessingTurn = false;
                            checkAndStartTurn();
                        });
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
                continueThread.setDaemon(true);
                continueThread.start();
            }

        } catch (InvalidCardPlayException e) {
            Alert.showWarning("Invalid Play", "Cannot play this card", e.getMessage());
            isProcessingTurn = false;
        } catch (EmptyDeckException e) {
            Alert.showError("Error", "Deck Error", e.getMessage());
            isProcessingTurn = false;
        }
    }

    /**
     * Starts a machine player's turn in a separate thread.
     */
    private void startMachineTurn() {
        isProcessingTurn = true;

        machinePlayerThread = new Thread(() -> {
            try {
                Random random = new Random();
                Thread.sleep(2000 + random.nextInt(2000));

                Platform.runLater(() -> processMachineTurn());

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
        if (gameModel.isGameOver()) {
            handleGameOver();
            return;
        }

        Player currentPlayer = gameModel.getCurrentPlayer();
        log("Machine turn: " + currentPlayer.getName());

        if (!(currentPlayer instanceof MachinePlayer)) {
            log("ERROR: Expected machine player");
            isProcessingTurn = false;
            return;
        }

        try {
            if (!currentPlayer.hasValidMove(gameModel.getTableSum())) {
                log("Machine has no valid moves - eliminating");

                Platform.runLater(() -> {
                    Alert.showInfo(
                            "Player Eliminated",
                            currentPlayer.getName() + " Eliminated",
                            currentPlayer.getName() + " has no valid moves!"
                    );
                });

                gameModel.eliminateCurrentPlayer();
                updateUI();

                Thread.sleep(1500);

                if (!gameModel.isGameOver()) {
                    Platform.runLater(() -> {
                        isProcessingTurn = false;
                        checkAndStartTurn();
                    });
                } else {
                    Platform.runLater(() -> handleGameOver());
                }
                return;
            }

            Card selectedCard = ((MachinePlayer) currentPlayer).selectCard(gameModel.getTableSum());
            int oldSum = gameModel.getTableSum();
            gameModel.playCard(selectedCard);
            int newSum = gameModel.getTableSum();

            log("Machine played: " + selectedCard + " | " + oldSum + " → " + newSum);

            Platform.runLater(() -> updateUI());

            Random random = new Random();
            Thread.sleep(1000 + random.nextInt(1000));

            gameModel.drawCard();
            gameModel.nextTurn();

            Platform.runLater(() -> updateUI());

            if (gameModel.isGameOver()) {
                Platform.runLater(() -> handleGameOver());
            } else {
                Platform.runLater(() -> {
                    isProcessingTurn = false;
                    checkAndStartTurn();
                });
            }

        } catch (InvalidCardPlayException e) {
            log("Machine play failed: " + e.getMessage());
            try {
                gameModel.eliminateCurrentPlayer();
                Platform.runLater(() -> updateUI());

                if (!gameModel.isGameOver()) {
                    Platform.runLater(() -> {
                        isProcessingTurn = false;
                        checkAndStartTurn();
                    });
                } else {
                    Platform.runLater(() -> handleGameOver());
                }
            } catch (PlayerEliminatedException pe) {
                log("Machine eliminated: " + pe.getMessage());
            }
        } catch (PlayerEliminatedException e) {
            log("Machine eliminated: " + e.getMessage());
            Platform.runLater(() -> {
                if (!gameModel.isGameOver()) {
                    isProcessingTurn = false;
                    checkAndStartTurn();
                } else {
                    handleGameOver();
                }
            });
        } catch (Exception e) {
            log("ERROR: " + e.getMessage());
            e.printStackTrace();
            isProcessingTurn = false;
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
     */
    private void updateMachinePlayerDisplay(VBox vbox, Label statusLabel, Player player) {
        if (player.isEliminated()) {
            statusLabel.setText("ELIMINATED");
            statusLabel.setStyle("-fx-text-fill: #ff4444; -fx-font-weight: bold;");

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
                ? "🎉 Congratulations! You won!"
                : "😔 " + winner.getName() + " wins!";

        Alert.showInfo("Game Over", "Winner!", message);

        // Habilitar ambos botones al final del juego
        btnNewGame.setDisable(false);
        btnMainMenu.setDisable(false);
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
     * Handles main menu button click.
     */
    @FXML
    private void handleMainMenu() {
        if (Alert.showConfirmation("Main Menu", "Return to Menu", "Do you want to return to the main menu?")) {
            cleanup();
            try {
                WelcomeStage welcomeStage = WelcomeStage.getInstance();
                welcomeStage.show();

                Stage currentStage = (Stage) btnMainMenu.getScene().getWindow();
                currentStage.close();
            } catch (IOException e) {
                Alert.showError("Error", "Failed to return to menu", e.getMessage());
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

    /**
     * Logs debug messages
     */
    private void log(String message) {
        if (DEBUG) {
            System.out.println("[GAME] " + message);
        }
    }
}