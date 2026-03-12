package com.gempukku.stccg.actions.placecard;

import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.ActionResultType;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class PlaceCardInPointAreaResult extends ActionResult {

    @JsonProperty("targetCardId")
    @JsonIdentityReference(alwaysAsId=true)
    private final PhysicalCard _cardPlaced;

    public PlaceCardInPointAreaResult(DefaultGame cardGame, PlaceCardInPointAreaAction action,
                                      PhysicalCard cardPlaced) {
        super(cardGame, ActionResultType.PLACED_CARD_IN_POINT_AREA, action);
        _cardPlaced = cardPlaced;
    }
}