package com.example.a50zo;

import com.example.a50zo.view.WelcomeStage;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Main application class for Cincuentazo game.
 * Initializes and launches the JavaFX application.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
public class Main extends Application {

    /**
     * The main entry point for the JavaFX application.
     * Creates and displays the welcome stage.
     *
     * @param primaryStage The primary stage provided by JavaFX
     * @throws IOException if the welcome stage cannot be loaded
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        WelcomeStage welcomeStage = WelcomeStage.getInstance();
        welcomeStage.show();
    }

    /**
     * Main method that launches the JavaFX application.
     *
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Called when the application is stopped.
     * Performs cleanup operations.
     */
    @Override
    public void stop() {
        System.out.println("Cincuentazo application closing...");
    }
}