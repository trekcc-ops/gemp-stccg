package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.actions.NoResponseActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;

public class RemoveCardFromGameActionResult extends NoResponseActionResult {

    @JsonProperty("targetCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private final Collection<PhysicalCard> _cardsRemoved;

    public RemoveCardFromGameActionResult(DefaultGame cardGame, RemoveCardFromGameAction action,
                                          Collection<PhysicalCard> cardsRemoved) {
        super(cardGame, ActionResultType.REMOVED_CARD_FROM_GAME, action.getPerformingPlayerId(), action);
        _cardsRemoved = cardsRemoved;
    }
}