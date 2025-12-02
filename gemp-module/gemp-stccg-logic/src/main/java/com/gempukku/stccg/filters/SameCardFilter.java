package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class SameCardFilter implements CardFilter {

    @JsonProperty("cardId")
    private final int _cardId;

    public SameCardFilter(PhysicalCard card) {
        _cardId = card.getCardId();
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        return physicalCard.getCardId() == _cardId;
    }
}