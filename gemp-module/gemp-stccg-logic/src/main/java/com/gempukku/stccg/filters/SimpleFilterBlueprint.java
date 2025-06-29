package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.Filterable;

import java.util.ArrayList;
import java.util.List;

public class SimpleFilterBlueprint implements FilterBlueprint {

    private final Filterable _filterable;

    public SimpleFilterBlueprint(Filterable filterable) {
        _filterable = filterable;
    }

    public Filterable getFilterable(ActionContext actionContext) {
        return _filterable;
    }

}