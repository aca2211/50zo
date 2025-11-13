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
        log("Player index: " + gameModel.getPlayers().indexOf(currentPlayer));
        log("Is human: " + (currentPlayer instanceof HumanPlayer));
        log("Is eliminated: " + currentPlayer.isEliminated());
        log("Table sum: " + gameModel.getTableSum());

        // Si el jugador actual está eliminado, avanzar al siguiente
        if (currentPlayer.isEliminated()) {
            log("⚠️ Current player is eliminated, advancing turn");
            gameModel.nextTurn();
            checkAndStartTurn();
            return;
        }

        // Verificar movimientos válidos
        boolean hasValidMoves = currentPlayer.hasValidMove(gameModel.getTableSum());
        log("Has valid moves: " + hasValidMoves);

        if (!hasValidMoves) {
            log("Current hand:");
            for (Card card : currentPlayer.getHand()) {
                int newSum = gameModel.getTableSum() + card.getBestValue(gameModel.getTableSum());
                log("  - " + card + " would result in: " + newSum + " (can play: " + card.canBePlayed(gameModel.getTableSum()) + ")");
            }
        }

        if (currentPlayer instanceof HumanPlayer) {
            HumanPlayer humanPlayer = (HumanPlayer) currentPlayer;

            if (!hasValidMoves) {
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

        try {
            // Eliminar jugador
            gameModel.eliminateCurrentPlayer();
            log("Human player eliminated successfully");

            // Avanzar turno
            if (!gameModel.isGameOver()) {
                gameModel.nextTurn();
                log("Turn advanced to: " + gameModel.getCurrentPlayer().getName());
            }

        } catch (PlayerEliminatedException e) {
            log("PlayerEliminatedException: " + e.getMessage());
        }

        // Actualizar UI
        Platform.runLater(() -> updateUI());

        // Mostrar alerta SIN BLOQUEAR
        Thread alertThread = new Thread(() -> {
            Platform.runLater(() -> {
                Alert.showWarning(
                        "No Valid Moves!",
                        "You're Eliminated!",
                        "You have no cards that can be played without exceeding 50.\nYou are eliminated from the game!"
                );
            });
        });
        alertThread.setDaemon(true);
        alertThread.start();

        if (gameModel.isGameOver()) {
            log("Game Over - Final winner check");
            Platform.runLater(() -> {
                updateUI(); // Actualizar UI final
                btnNewGame.setDisable(false);
                btnMainMenu.setDisable(false);
                handleGameOver();
            });
        } else {
            // El juego continúa con las máquinas
            log("=== GAME CONTINUES ===");
            log("Active players: " + gameModel.getActivePlayers().size());
            for (Player p : gameModel.getActivePlayers()) {
                log("  - " + p.getName() + " (Hand: " + p.getHandSize() + " cards)");
            }
            log("Next player: " + gameModel.getCurrentPlayer().getName());

            // Esperar y continuar en un thread separado
            Thread continueThread = new Thread(() -> {
                try {
                    Thread.sleep(2500); // Dar tiempo para que se vea la alerta

                    Platform.runLater(() -> {
                        log("Resuming game after human elimination");
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
        cardView.setFitWidth(85);  // Reducido de 100
        cardView.setFitHeight(119); // Reducido de 140
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
            int handSizeBefore = gameModel.getHumanPlayer().getHandSize();

            log("=== HUMAN PLAYING CARD ===");
            log("Hand size before: " + handSizeBefore);
            log("Card to play: " + card);

            // Jugar carta
            gameModel.getHumanPlayer().playCard(card, gameModel.getTableSum());
            gameModel.playCard(card);

            int newSum = gameModel.getTableSum();
            log("Human played: " + card + " | " + oldSum + " → " + newSum);
            log("Hand size after play: " + gameModel.getHumanPlayer().getHandSize());

            // Tomar carta del mazo
            gameModel.drawCard();
            int handSizeAfter = gameModel.getHumanPlayer().getHandSize();
            log("Hand size after draw: " + handSizeAfter);

            if (handSizeAfter != 4) {
                log("⚠️ WARNING: Hand size is " + handSizeAfter + " instead of 4!");
            }

            // Avanzar turno
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
        log("=== PROCESS MACHINE TURN CALLED ===");
        log("isProcessingTurn: " + isProcessingTurn);
        log("gameModel.isGameOver(): " + gameModel.isGameOver());

        if (gameModel.isGameOver()) {
            log("Game is over, calling handleGameOver");
            handleGameOver();
            return;
        }

        Player currentPlayer = gameModel.getCurrentPlayer();
        log("Machine turn: " + currentPlayer.getName());
        log("Current player eliminated: " + currentPlayer.isEliminated());
        log("Current player hand size: " + currentPlayer.getHandSize());

        if (!(currentPlayer instanceof MachinePlayer)) {
            log("ERROR: Expected machine player but got: " + currentPlayer.getClass().getName());
            isProcessingTurn = false;
            return;
        }

        try {
            if (!currentPlayer.hasValidMove(gameModel.getTableSum())) {
                log("Machine has no valid moves - eliminating");

                String playerName = currentPlayer.getName();

                // Eliminar jugador
                gameModel.eliminateCurrentPlayer();

                // Avanzar al siguiente turno DESPUÉS de eliminar
                if (!gameModel.isGameOver()) {
                    gameModel.nextTurn();
                }

                Platform.runLater(() -> updateUI());

                // Mostrar alerta
                Platform.runLater(() -> {
                    Alert.showInfo(
                            "Player Eliminated",
                            playerName + " Eliminated",
                            playerName + " has no valid moves and is eliminated!"
                    );
                });

                Thread.sleep(1500);

                // Continuar con el siguiente jugador
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

            // Seleccionar y jugar carta
            Card selectedCard = ((MachinePlayer) currentPlayer).selectCard(gameModel.getTableSum());
            int oldSum = gameModel.getTableSum();
            gameModel.playCard(selectedCard);
            int newSum = gameModel.getTableSum();

            log("Machine played: " + selectedCard + " | " + oldSum + " → " + newSum);
            Platform.runLater(() -> updateUI());

            // Delay antes de tomar carta
            Random random = new Random();
            Thread.sleep(1000 + random.nextInt(1000));

            // Tomar carta del mazo
            gameModel.drawCard();
            log("Machine drew a card. Hand size: " + currentPlayer.getHandSize());

            // Avanzar turno
            gameModel.nextTurn();
            Platform.runLater(() -> updateUI());

            // Verificar game over o continuar
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
                String playerName = currentPlayer.getName();

                gameModel.eliminateCurrentPlayer();

                if (!gameModel.isGameOver()) {
                    gameModel.nextTurn();
                }

                Platform.runLater(() -> updateUI());

                Platform.runLater(() -> {
                    Alert.showInfo(
                            "Player Eliminated",
                            playerName + " Eliminated",
                            playerName + " could not make a valid play!"
                    );
                });

                Thread.sleep(1500);

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
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        } catch (PlayerEliminatedException e) {
            log("Machine eliminated via exception: " + e.getMessage());
            Platform.runLater(()->updateUI());
            try {
                Thread.sleep(1500);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }

            Platform.runLater(() -> {
                if (!gameModel.isGameOver()) {
                    isProcessingTurn = false;
                    checkAndStartTurn();
                } else {
                    updateUI();
                    handleGameOver();
                }
            });
        } catch (EmptyDeckException e) {
            log("ERROR: Empty deck - " + e.getMessage());
            Alert.showError("Error", "Deck Error", "Could not draw card: " + e.getMessage());
            isProcessingTurn = false;
        } catch (Exception e) {
            log("ERROR: " + e.getMessage());
            e.printStackTrace();
            isProcessingTurn = false;

            Platform.runLater(() -> {
                if (!gameModel.isGameOver()) {
                    checkAndStartTurn();
                }
            });
        }
    }

    /**
     * Updates the display for all machine players.
     */
    private void updateMachinePlayersDisplay() {
        List<Player> players = gameModel.getPlayers();

        if (players.size() == 1) {
            updateMachinePlayerDisplay(vboxMachine1,lblMachine1Status, players.get(0));
        }
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
            cardBox = new HBox(4);  // Reducido de 5
            cardBox.setAlignment(Pos.CENTER);
            vbox.getChildren().add(cardBox);
        }

        cardBox.getChildren().clear();

        for (int i = 0; i < cardCount; i++) {
            ImageView cardBack = new ImageView(imageLoader.getCardBackImage());
            cardBack.setFitWidth(50);  // Reducido de 60
            cardBack.setFitHeight(70); // Reducido de 84
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
        log("=== HANDLE GAME OVER CALLED ===");
        log("isProcessingTurn: " + isProcessingTurn);
        log("gameModel.isGameOver(): " + gameModel.isGameOver());

        isProcessingTurn = true;
        Player winner = gameModel.getWinner();

        if (winner == null) {
            log("ERROR: Game over but no winner found!");
            log("Active players: " + gameModel.getActivePlayers().size());
            for (Player p : gameModel.getActivePlayers()) {
                log("  - " + p.getName() + " (eliminated: " + p.isEliminated() + ")");
            }
            return;
        }

        log("Winner: " + winner.getName());
        log("Winner type: " + winner.getClass().getSimpleName());

        // IMPORTANTE: Actualizar UI antes de mostrar alerta para reflejar último estado
        updateUI();

        String title;
        String header;
        String message;

        if (winner instanceof HumanPlayer) {
            title = "🎉 VICTORY! 🎉";
            header = "Congratulations!";
            message = "You won the game! You're the last player standing!\n\nWell played! 🏆";
        } else {
            title = "Game Over";
            header = winner.getName() + " Wins!";
            message = winner.getName() + " is the winner!\n\nBetter luck next time! 🎮";
        }

        // Mostrar alerta en un thread separado para no bloquear UI
        Thread alertThread = new Thread(() -> {
            Platform.runLater(() -> {
                Alert.showInfo(title, header, message);
            });
        });
        alertThread.setDaemon(true);
        alertThread.start();

        // Habilitar ambos botones al final del juego
        btnNewGame.setDisable(false);
        btnMainMenu.setDisable(false);

        log("Buttons enabled - Game Over complete");
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