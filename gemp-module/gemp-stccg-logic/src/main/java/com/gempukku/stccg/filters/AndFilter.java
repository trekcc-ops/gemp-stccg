package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.List;

public class AndFilter implements CardFilter {

    @JsonProperty("filters")
    private final Iterable<CardFilter> _filters;

    public AndFilter(CardFilter... filters) {
        _filters = List.of(filters);
    }

    public AndFilter(Collection<CardFilter> filters) {
        _filters = filters;
    }

    public AndFilter(Filterable... filterables) {
        _filters = List.of(Filters.convertToFilters(filterables));
    }


    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        boolean result = true;
        for (CardFilter filter : _filters) {
            if (!filter.accepts(game, physicalCard)) {
                result = false;
            }
        }
        return result;
    }

}