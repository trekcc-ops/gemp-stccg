package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class OrCardFilter implements CardFilter {

    @JsonProperty("filters")
    private final List<CardFilter> _filters = new LinkedList<>();

    public OrCardFilter(CardFilter... filters) {
        _filters.addAll(Arrays.asList(filters));
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