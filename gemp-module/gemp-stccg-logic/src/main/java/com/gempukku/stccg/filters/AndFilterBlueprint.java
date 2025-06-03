package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.Filterable;

import java.util.ArrayList;
import java.util.List;

public class AndFilterBlueprint implements FilterBlueprint {

    private final FilterBlueprint[] _filterBlueprints;

    public AndFilterBlueprint(FilterBlueprint... filterBlueprints) {
        _filterBlueprints = filterBlueprints;
    }

    public Filterable getFilterable(ActionContext actionContext) {
        List<Filterable> filterables = new ArrayList<>();
        for (FilterBlueprint blueprint : _filterBlueprints) {
            filterables.add(blueprint.getFilterable(actionContext));
        }
        return Filters.and(filterables);
    }

}