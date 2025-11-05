package com.example.a50zo.controller;


import com.example.a50zo.view.Alert;
import com.example.a50zo.view.GameStage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the welcome screen.
 * Handles player selection and game initialization.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
public class WelcomeController {

    @FXML
    private RadioButton radio1Player;

    @FXML
    private RadioButton radio2Players;

    @FXML
    private RadioButton radio3Players;

    @FXML
    private Button btnStartGame;

    @FXML
    private ToggleGroup playerGroup;

    /**
     * Initializes the controller.
     * Sets up radio button group and default selection.
     */
    @FXML
    public void initialize() {
        playerGroup = new ToggleGroup();
        radio1Player.setToggleGroup(playerGroup);
        radio2Players.setToggleGroup(playerGroup);
        radio3Players.setToggleGroup(playerGroup);

        // Default selection
        radio2Players.setSelected(true);
    }

    /**
     * Handles the start game button click.
     * Creates and shows the game stage.
     *
     * @param event The action event
     */
    @FXML
    private void handleStartGame(ActionEvent event) {
        int numberOfPlayers = getSelectedNumberOfPlayers();

        try {
            GameStage gameStage = GameStage.getInstance(numberOfPlayers);
            gameStage.show();

            // Close welcome stage
            Stage currentStage = (Stage) btnStartGame.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            Alert.showError(
                    "Error",
                    "Failed to start game",
                    "Could not load game interface: " + e.getMessage()
            );
        }
    }

    /**
     * Gets the number of machine players selected.
     *
     * @return Number of machine players (1-3)
     */
    private int getSelectedNumberOfPlayers() {
        if (radio1Player.isSelected()) {
            return 1;
        } else if (radio2Players.isSelected()) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * Handles the exit button click.
     *
     * @param event The action event
     */
    @FXML
    private void handleExit(ActionEvent event) {
        if (Alert.showConfirmation(
                "Exit",
                "Are you sure?",
                "Do you want to exit the application?")) {
            System.exit(0);
        }
    }
}