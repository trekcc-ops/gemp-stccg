package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.Filterable;

public interface FilterBlueprint {
    Filterable getFilterable(ActionContext actionContext);
}