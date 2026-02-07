package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class OrCardFilter implements CardFilter {

    @JsonProperty("filters")
    private final List<CardFilter> _filters = new LinkedList<>();

    @JsonCreator
    public OrCardFilter(@JsonProperty("filters") List<CardFilter> filters) {
        _filters.addAll(filters);
    }

    public OrCardFilter(CardFilter... filters) {
        this(Arrays.asList(filters));
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