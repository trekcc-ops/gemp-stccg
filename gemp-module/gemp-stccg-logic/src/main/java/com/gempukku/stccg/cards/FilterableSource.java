package com.gempukku.stccg.cards;

import com.gempukku.stccg.common.filterable.Filterable;

public interface FilterableSource {
    Filterable getFilterable(ActionContext actionContext);
}
