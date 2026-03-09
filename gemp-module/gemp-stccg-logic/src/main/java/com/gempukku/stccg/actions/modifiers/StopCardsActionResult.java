package com.gempukku.stccg.actions.modifiers;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.actions.NoResponseActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.Collection;

public class StopCardsActionResult extends NoResponseActionResult {
    @JsonProperty("targetCardIds")
    @JsonIdentityReference(alwaysAsId=true)
    private final Collection<PhysicalCard> _stoppedCards = new ArrayList<>();

    public StopCardsActionResult(DefaultGame cardGame, String performingPlayerId,
                                 StopCardsAction action, Collection<PhysicalCard> stoppedCards) {
        super(cardGame, ActionResultType.CHANGED_AFFILIATION, performingPlayerId, action);
        _stoppedCards.addAll(stoppedCards);
    }
}