package com.gempukku.stccg.player;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public interface PlayerSource {
    boolean isPlayer(String playerName, DefaultGame cardGame, ActionContext actionContext);
    String getPlayerName(DefaultGame cardGame, ActionContext actionContext);
}