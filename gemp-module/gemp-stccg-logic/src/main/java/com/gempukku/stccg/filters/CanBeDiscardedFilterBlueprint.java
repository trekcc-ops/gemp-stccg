package com.gempukku.stccg.filters;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.Filterable;

public class CanBeDiscardedFilterBlueprint implements FilterBlueprint {
    public Filterable getFilterable(ActionContext actionContext) {
        return Filters.canBeDiscarded(actionContext.getPerformingPlayerId(), actionContext.getSource());
    }
}