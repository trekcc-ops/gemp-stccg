package com.gempukku.stccg.player;

import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

public interface PlayerSource {
    boolean isPlayer(String playerName, DefaultGame cardGame, GameTextContext actionContext);
    String getPlayerName(DefaultGame cardGame, GameTextContext actionContext);
}