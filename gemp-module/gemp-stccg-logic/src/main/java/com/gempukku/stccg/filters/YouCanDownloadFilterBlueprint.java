package com.gempukku.stccg.filters;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

public class YouCanDownloadFilterBlueprint implements FilterBlueprint {
    public CardFilter getFilterable(DefaultGame cardGame, GameTextContext actionContext) {
        return Filters.cardsYouCanDownload(actionContext.yourName());
    }

}