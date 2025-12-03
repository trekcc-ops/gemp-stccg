package com.gempukku.stccg.player;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class YouPlayerSource implements PlayerSource {
    @Override
    public String getPlayerId(DefaultGame cardGame,  ActionContext actionContext) {
        return actionContext.getPerformingPlayerId();
    }
}