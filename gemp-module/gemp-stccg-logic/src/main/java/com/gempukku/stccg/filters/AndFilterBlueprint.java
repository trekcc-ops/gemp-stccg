package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AndFilterBlueprint implements FilterBlueprint {

    private final FilterBlueprint[] _filterBlueprints;

    public AndFilterBlueprint(FilterBlueprint... filterBlueprints) {
        _filterBlueprints = filterBlueprints;
    }

    public AndFilterBlueprint(Collection<FilterBlueprint> filterBlueprints) {
        _filterBlueprints = filterBlueprints.toArray(new FilterBlueprint[0]);
    }

    public CardFilter getFilterable(DefaultGame cardGame, GameTextContext actionContext) {
        List<Filterable> filterables = new ArrayList<>();
        for (FilterBlueprint blueprint : _filterBlueprints) {
            filterables.add(blueprint.getFilterable(cardGame, actionContext));
        }
        return Filters.and(filterables);
    }

}