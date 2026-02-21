package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class OrFilterBlueprint implements FilterBlueprint {

    private final Collection<FilterBlueprint> _filterBlueprints;

    public OrFilterBlueprint(Collection<FilterBlueprint> filterBlueprints) {
        _filterBlueprints = filterBlueprints;
    }

    public CardFilter getFilterable(DefaultGame cardGame, GameTextContext actionContext) {
        List<CardFilter> filterables = new LinkedList<>();
        for (FilterBlueprint filterBlueprint : _filterBlueprints)
            filterables.add(filterBlueprint.getFilterable(cardGame, actionContext));
        return new OrCardFilter(filterables);
    }

}