package com.example.a50zo.view;

import com.example.a50zo.controller.GameController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main game stage that displays the game interface.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
public class GameStage extends Stage {
    private GameController controller;

    /**
     * Constructor that initializes the game stage.
     *
     * @param numberOfMachinePlayers Number of machine players (1-3)
     * @throws IOException if FXML file cannot be loaded
     */
    public GameStage(int numberOfMachinePlayers) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/a50zo/fxml/game-view.fxml")
        );
        Parent root = loader.load();

        controller = loader.getController();
        controller.initializeGame(numberOfMachinePlayers);

        Scene scene = new Scene(root);
        setScene(scene);
        setTitle("Cincuentazo - Game");
        setResizable(false);

        // Handle window close
        setOnCloseRequest(event -> {
            if (Alert.showConfirmation(
                    "Exit Game",
                    "Are you sure?",
                    "Do you want to exit the game?")) {
                controller.cleanup();
            } else {
                event.consume();
            }
        });

        centerOnScreen();
    }

    /**
     * Gets the game controller.
     *
     * @return The GameController instance
     */
    public GameController getController() {
        return controller;
    }

    /**
     * Gets an instance of the game stage.
     *
     * @param numberOfMachinePlayers Number of machine players
     * @return A new GameStage instance
     * @throws IOException if FXML file cannot be loaded
     */
    public static GameStage getInstance(int numberOfMachinePlayers) throws IOException {
        return new GameStage(numberOfMachinePlayers);
    }
}
