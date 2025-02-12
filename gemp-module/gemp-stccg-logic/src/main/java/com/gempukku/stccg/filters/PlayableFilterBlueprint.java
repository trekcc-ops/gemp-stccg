package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.Filterable;

public class PlayableFilterBlueprint implements FilterBlueprint {

    public Filterable getFilterable(ActionContext actionContext) {
        return Filters.playable;
    }
}