package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;

import java.util.ArrayList;
import java.util.List;

public class AndFilterBlueprint implements FilterBlueprint {

    private final FilterBlueprint[] _filterBlueprints;

    public AndFilterBlueprint(FilterBlueprint... filterBlueprints) {
        _filterBlueprints = filterBlueprints;
    }

    public CardFilter getFilterable(DefaultGame cardGame, ActionContext actionContext) {
        List<Filterable> filterables = new ArrayList<>();
        for (FilterBlueprint blueprint : _filterBlueprints) {
            filterables.add(blueprint.getFilterable(cardGame, actionContext));
        }
        return Filters.and(filterables);
    }

}