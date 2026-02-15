package com.gempukku.stccg.filters;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AndFilter implements CardFilter {

    @JsonProperty("filters")
    private final Collection<CardFilter> _filters;

    public AndFilter(CardFilter... filters) {
        this(List.of(filters));
    }

    @JsonCreator
    public AndFilter(@JsonProperty("filters") Collection<CardFilter> filters) {
        _filters = new ArrayList<>();
        for (CardFilter filter : filters) {
            if (filter instanceof AndFilter andFilter) {
                _filters.addAll(andFilter.getFilters());
            } else {
                _filters.add(filter);
            }
        }
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

    @JsonIgnore
    public Collection<CardFilter> getFilters() { return _filters; }

    @JsonIgnore
    public void appendCardFilter(CardFilter... filters) {
        for (CardFilter filter : filters) {
            if (filter != Filters.any && !_filters.contains(filter)) {
                if (filter instanceof AndFilter andFilter) {
                    appendCardFilter(andFilter.getFilters().toArray(new CardFilter[0]));
                } else {
                    _filters.add(filter);
                }
            }
        }
    }

}