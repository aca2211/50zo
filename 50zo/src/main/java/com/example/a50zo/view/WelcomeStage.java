package com.example.a50zo.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Stage for the welcome screen where players select game settings.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
public class WelcomeStage extends Stage {

    /**
     * Constructor that initializes the welcome stage.
     *
     * @throws IOException if FXML file cannot be loaded
     */
    public WelcomeStage() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/example/a50zo/fxml/welcome-view.fxml")
        );
        Parent root = loader.load();

        Scene scene = new Scene(root);
        setScene(scene);
        setTitle("Cincuentazo - Welcome");
        setResizable(false);

        // Center the stage on screen
        centerOnScreen();
    }

    /**
     * Gets an instance of the welcome stage.
     *
     * @return A new WelcomeStage instance
     * @throws IOException if FXML file cannot be loaded
     */
    public static WelcomeStage getInstance() throws IOException {
        return new WelcomeStage();
    }
}
