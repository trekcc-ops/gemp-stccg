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

public class AddCardsToPreseedStackActionResult extends NoResponseActionResult {

    private final Collection<PhysicalCard> _cardsAdded;
    public AddCardsToPreseedStackActionResult(DefaultGame cardGame, String performingPlayerId, Action action,
                                              Collection<PhysicalCard> cardsBeingSeeded) {
        super(cardGame, ActionResultType.ADDED_PRESEEDS, performingPlayerId, action);
        _cardsAdded = cardsBeingSeeded;
    }

    private AddCardsToPreseedStackActionResult(int resultId, String performingPlayerId, Action action,
                                               Collection<PhysicalCard> cardsBeingSeeded,
                                               ZonedDateTime timestamp) {
        super(resultId, ActionResultType.ADDED_PRESEEDS, performingPlayerId, action, timestamp);
        _cardsAdded = cardsBeingSeeded;
    }

    @JsonProperty("targetCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private Collection<PhysicalCard> getCardsAdded() {
        return _cardsAdded;
    }

    @Override
    public AddCardsToPreseedStackActionResult getResultForPlayer(String playerName) {
        if (_performingPlayerId.equals(playerName)) {
            return this;
        } else {
            Collection<PhysicalCard> anonymousCards = new ArrayList<>();
            for (PhysicalCard card : _cardsAdded) {
                anonymousCards.add(new ProxyAnonymousCard(card.getOwnerName()));
            }
            return new AddCardsToPreseedStackActionResult(_resultId, _performingPlayerId, _action,
                    anonymousCards, _timestamp);
        }
    }
}