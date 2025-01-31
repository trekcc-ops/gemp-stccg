package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class AndFilter implements CardFilter {

    private final List<CardFilter> _filters;

    public AndFilter(CardFilter... filters) {
        _filters = Arrays.asList(filters);
    }

    public AndFilter(List<CardFilter> filters) {
        _filters = filters;
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