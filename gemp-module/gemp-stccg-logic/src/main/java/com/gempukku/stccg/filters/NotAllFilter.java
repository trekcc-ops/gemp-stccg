package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class NotAllFilter implements CardFilter {

    @JsonProperty("filters")
    private final CardFilter[] _filters;

    public NotAllFilter(CardFilter... filters) {
        _filters = filters;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        CardFilter andFilter = new AndFilter(_filters);
        return !andFilter.accepts(game, physicalCard);
    }
}