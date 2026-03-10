package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.actions.NoResponseActionResult;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class DilemmaPlacedOnCardResult extends NoResponseActionResult {

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _cardPlaced;

    @JsonProperty("cardPlacedOnId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _parentCard;

    public DilemmaPlacedOnCardResult(DefaultGame cardGame, PhysicalCard cardPlaced, Action action, PhysicalCard parentCard) {
        super(cardGame, ActionResultType.DILEMMA_PLACED_ON_CARD, action.getPerformingPlayerId(), action);
        _cardPlaced = cardPlaced;
        _parentCard = parentCard;
    }

}