package com.gempukku.stccg.actions.discard;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.List;

public class DiscardUtils {
    public static void cardsToChangeZones(DefaultGame game, Collection<PhysicalCard> movingCards, Collection<PhysicalCard> discardedCards, Collection<PhysicalCard> toMoveToDiscard) {
        for (PhysicalCard card : movingCards) {
            cardsToChangeZones(game, movingCards, card, discardedCards, toMoveToDiscard);
        }
    }

    private static void cardsToChangeZones(DefaultGame game, Collection<PhysicalCard> movingCards, PhysicalCard card, Collection<PhysicalCard> discardedCards, Collection<PhysicalCard> toMoveToDiscard) {
        List<PhysicalCard> attachedCards = game.getGameState().getAttachedCards(card);
        for (PhysicalCard attachedCard : attachedCards) {
            if (!movingCards.contains(attachedCard)) {
                discardedCards.add(attachedCard);
                toMoveToDiscard.add(attachedCard);
                cardsToChangeZones(game, movingCards, attachedCard, discardedCards, toMoveToDiscard);
            }
        }
        toMoveToDiscard.addAll(card.getStackedCards(game));
    }
}