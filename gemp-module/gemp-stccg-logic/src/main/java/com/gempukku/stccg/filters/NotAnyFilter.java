package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

public class NotAnyFilter implements CardFilter {

    @JsonProperty("filters")
    private final CardFilter[] _filters;

    public NotAnyFilter(CardFilter... filters) {
        _filters = filters;
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        CardFilter orFilter = new OrCardFilter(_filters);
        return !orFilter.accepts(game, physicalCard);
    }
}