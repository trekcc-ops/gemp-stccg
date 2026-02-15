package com.gempukku.stccg.rules.st1e;

import com.gempukku.stccg.cards.physicalcard.CardWithCrew;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class PresentRule {

    public static boolean cardsArePresentWithEachOther(DefaultGame cardGame, PhysicalCard... cards) {
        for (PhysicalCard card1 : cards) {
            for (PhysicalCard card2 : cards) {
                if (!cardsArePresentWithEachOther(cardGame, card1, card2)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean cardsArePresentWithEachOther(DefaultGame cardGame, PhysicalCard card1, PhysicalCard card2) {
        // Cards are present with dilemmas while encountering them
        if (card1.isBeingEncounteredBy(cardGame, card2) || card2.isBeingEncounteredBy(cardGame, card1)) {
            return true;
        }
        // Cards in play are present with themselves
        if (card1 == card2) {
            return true;
        }
        // Cards not in play do not have presence
        if (!card1.isInPlay() || !card2.isInPlay()) {
            return false;
        }
        // Cards on a planet surface are present with each other
        if (card1.isOnPlanet(cardGame) && card2.isOnPlanet(cardGame) &&
                card1.getAttachedTo(cardGame) instanceof MissionCard &&
                card2.getAttachedTo(cardGame) instanceof MissionCard &&
                card1.getLocationId() == card2.getLocationId()
        ) {
            return true;
        }
        // Cards aboard the same ship or siteless facility are present with each other
        if (card1.getAttachedTo(cardGame) instanceof CardWithCrew cardWithCrew &&
                cardWithCrew == card2.getAttachedTo(cardGame)) {
            return true;
        }
        return false;
    }
}