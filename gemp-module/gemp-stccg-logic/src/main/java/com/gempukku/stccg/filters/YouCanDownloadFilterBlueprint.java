package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.Filterable;

public class YouCanDownloadFilterBlueprint implements FilterBlueprint {
    public Filterable getFilterable(ActionContext actionContext) {
        return Filters.cardsYouCanDownload(actionContext.getPerformingPlayer());
    }

}