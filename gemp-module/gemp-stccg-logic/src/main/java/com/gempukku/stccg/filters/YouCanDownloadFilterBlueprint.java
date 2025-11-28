package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.common.filterable.Filterable;
import com.gempukku.stccg.game.DefaultGame;

public class YouCanDownloadFilterBlueprint implements FilterBlueprint {
    public Filterable getFilterable(DefaultGame cardGame, ActionContext actionContext) {
        return Filters.cardsYouCanDownload(actionContext.getPerformingPlayerId());
    }

}