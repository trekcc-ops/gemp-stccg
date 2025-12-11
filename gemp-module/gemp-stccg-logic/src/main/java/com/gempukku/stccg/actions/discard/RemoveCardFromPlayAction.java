package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.FixedCardResolver;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RemoveCardFromPlayAction extends ActionyAction {

    private final FixedCardResolver _cardTarget;

    public RemoveCardFromPlayAction(DefaultGame cardGame, String performingPlayerName, PhysicalCard cardToRemove) {
        super(cardGame, performingPlayerName, ActionType.REMOVE_CARD_FROM_GAME);
        _cardTarget = new FixedCardResolver(cardToRemove);
        _cardTargets.add(_cardTarget);
    }

    @Override
    public boolean requirementsAreMet(DefaultGame cardGame) {
        return true;
    }

    @JsonProperty("targetCardId")
    private int targetCardId() {
        return _cardTarget.getCard().getCardId();
    }

    public void processEffect(DefaultGame cardGame) {
        Collection<PhysicalCard> removedCards = List.of(_cardTarget.getCard());
        Set<PhysicalCard> toRemoveFromZone = new HashSet<>(removedCards);
        cardGame.getGameState().removeCardsFromZoneWithoutSendingToClient(cardGame, toRemoveFromZone);
        for (PhysicalCard removedCard : removedCards) {
            cardGame.getGameState().addCardToRemovedPile(removedCard);
            if (removedCard instanceof ST1EPhysicalCard stCard && stCard.isStopped()) {
                stCard.unstop();
            }
        }
        setAsSuccessful();
    }
}