package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.Filterable;

public class YourCardsPresentWithThisCardFilterBlueprint implements FilterBlueprint {

    public Filterable getFilterable(ActionContext actionContext) {
        return Filters.yourCardsPresentWith(actionContext.getPerformingPlayer(), actionContext.getSource());
    }

}