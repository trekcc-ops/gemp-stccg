package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.actions.NoResponseActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ProxyAnonymousCard;
import com.gempukku.stccg.game.DefaultGame;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;

public class RemoveCardsFromPreseedStackActionResult extends NoResponseActionResult {

    private final Collection<PhysicalCard> _cardsRemoved;
    public RemoveCardsFromPreseedStackActionResult(DefaultGame cardGame, String performingPlayerId, Action action,
                                                   Collection<PhysicalCard> cardsBeingSeeded) {
        super(cardGame, ActionResultType.REMOVE_CARDS_FROM_PRESEED_STACK, performingPlayerId, action);
        _cardsRemoved = cardsBeingSeeded;
    }

    private RemoveCardsFromPreseedStackActionResult(int resultId, String performingPlayerId, Action action,
                                                    Collection<PhysicalCard> cardsBeingSeeded,
                                                    ZonedDateTime timestamp) {
        super(resultId, ActionResultType.REMOVE_CARDS_FROM_PRESEED_STACK, performingPlayerId, action, timestamp);
        _cardsRemoved = cardsBeingSeeded;
    }

    @JsonProperty("targetCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private Collection<PhysicalCard> getCardsAdded() {
        return _cardsRemoved;
    }

    @Override
    public RemoveCardsFromPreseedStackActionResult getResultForPlayer(String playerName) {
        if (_performingPlayerId.equals(playerName)) {
            return this;
        } else {
            Collection<PhysicalCard> anonymousCards = new ArrayList<>();
            for (PhysicalCard card : _cardsRemoved) {
                anonymousCards.add(new ProxyAnonymousCard(card.getOwnerName()));
            }
            return new RemoveCardsFromPreseedStackActionResult(_resultId, _performingPlayerId, _action,
                    anonymousCards, _timestamp);
        }
    }
}