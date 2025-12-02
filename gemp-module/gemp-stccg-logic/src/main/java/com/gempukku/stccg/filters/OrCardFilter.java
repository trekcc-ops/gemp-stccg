package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class OrCardFilter implements CardFilter {

    private final List<CardFilter> _filters = new LinkedList<>();

    public OrCardFilter(CardFilter... filters) {
        _filters.addAll(Arrays.asList(filters));
    }

    public OrCardFilter(Filterable... filterables) {
        CardFilter[] filters = Filters.convertToFilters(filterables);
        _filters.addAll(List.of(filters));
    }

    @Override
    public boolean accepts(DefaultGame game, PhysicalCard physicalCard) {
        for (CardFilter filter : _filters) {
            if (filter.accepts(game, physicalCard)) {
                return true;
            }
        }
        return false;
    }

}