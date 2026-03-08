package com.gempukku.stccg.actions.discard;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.actions.NoResponseActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class NullifyCardResult extends NoResponseActionResult {

    @JsonProperty("targetCardId")
    private final PhysicalCard _cardNullified;

    public NullifyCardResult(DefaultGame cardGame, NullifyCardAction action) {
        super(cardGame, ActionResultType.NULLIFY, action.getPerformingPlayerId(), action);
        _cardNullified = action.getNullifiedCard();
    }
}