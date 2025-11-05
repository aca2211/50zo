package com.example.a50zo.model;
import com.example.a50zo.exceptions.InvalidCardPlayException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a machine (AI) player in the game.
 * Automatically selects cards based on strategy.
 *
 * @author Cincuentazo Team
 * @version 1.0
 */
public class MachinePlayer extends Player {

    /**
     * Constructor for MachinePlayer.
     *
     * @param name The player's name
     */
    public MachinePlayer(String name) {
        super(name);
    }

    /**
     * Selects a card to play using AI strategy.
     * Strategy: Play the card that keeps the sum as close to 50 as possible
     * without exceeding it.
     *
     * @param currentSum The current sum on the table
     * @return The selected card
     * @throws InvalidCardPlayException if no valid card can be played
     */
    @Override
    public Card selectCard(int currentSum) throws InvalidCardPlayException {
        List<Card> validCards = hand.stream()
                .filter(card -> card.canBePlayed(currentSum))
                .collect(Collectors.toList());

        if (validCards.isEmpty()) {
            throw new InvalidCardPlayException(name + " has no valid moves");
        }

        // Strategy: Play the card that brings the sum closest to 50
        Card bestCard = validCards.get(0);
        int bestSum = currentSum + bestCard.getBestValue(currentSum);

        for (Card card : validCards) {
            int newSum = currentSum + card.getBestValue(currentSum);
            if (newSum > bestSum && newSum <= 50) {
                bestCard = card;
                bestSum = newSum;
            }
        }

        removeCardFromHand(bestCard);
        return bestCard;
    }
}