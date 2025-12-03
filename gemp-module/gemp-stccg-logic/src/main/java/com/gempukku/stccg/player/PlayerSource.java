package com.gempukku.stccg.player;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public interface PlayerSource {
    String getPlayerId(DefaultGame cardGame, ActionContext actionContext);
}