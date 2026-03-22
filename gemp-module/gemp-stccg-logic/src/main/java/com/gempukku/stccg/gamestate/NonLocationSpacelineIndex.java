package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Quadrant;

public class NonLocationSpacelineIndex implements SpacelineIndex {

    @JsonProperty("cardId")
    private final int _cardId;

    private final Quadrant _quadrant;

    @JsonCreator
    public NonLocationSpacelineIndex(
            @JsonProperty("cardId")
            int cardId,
            @JsonProperty("quadrant")
            Quadrant quadrant) {
        _cardId = cardId;
        _quadrant = quadrant;
    }

    public NonLocationSpacelineIndex(PhysicalCard card, Quadrant quadrant) {
        _cardId = card.getCardId();
        _quadrant = quadrant;
    }

    @Override
    public Quadrant getQuadrant() {
        return _quadrant;
    }
}