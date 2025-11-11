package com.example.a50zo.utils;

import javafx.scene.image.Image;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for loading and caching card images.
 * Implements singleton pattern for efficient memory usage.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
public class CardImageLoader {
    private static CardImageLoader instance;
    private final Map<String, Image> imageCache;
    private final Image cardBackImage;
    private static final String IMAGE_PATH = "/images/cards/";
    private static final String CARD_BACK = "back.png";

    /**
     * Private constructor for singleton pattern.
     */
    private CardImageLoader() {
        imageCache = new HashMap<>();
        cardBackImage = loadImage(CARD_BACK);
    }

    /**
     * Gets the singleton instance of CardImageLoader.
     *
     * @return The CardImageLoader instance
     */
    public static CardImageLoader getInstance() {
        if (instance == null) {
            instance = new CardImageLoader();
        }
        return instance;
    }

    /**
     * Loads a card image by file name.
     * Uses caching to avoid reloading the same image.
     *
     * @param fileName Name of the image file
     * @return The loaded Image object
     */
    private Image loadImage(String fileName) {
        try {
            String path = IMAGE_PATH + fileName;
            return new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.err.println("Error loading image: " + fileName);
            return null;
        }
    }

    /**
     * Gets the image for a specific card.
     *
     * @param fileName The card's image file name
     * @return The card image
     */
    public Image getCardImage(String fileName) {
        if (!imageCache.containsKey(fileName)) {
            imageCache.put(fileName, loadImage(fileName));
        }
        return imageCache.get(fileName);
    }

    /**
     * Gets the card back image.
     *
     * @return The card back image
     */
    public Image getCardBackImage() {
        return cardBackImage;
    }

    /**
     * Clears the image cache to free memory.
     */
    public void clearCache() {
        imageCache.clear();
    }
}