package com.gempukku.stccg.player;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

import java.util.Objects;

public class PlayerSourceFromMemory implements PlayerSource {

    private final String _memoryId;

    public PlayerSourceFromMemory(String memoryId) {
        _memoryId = memoryId;
    }

    @Override
    public boolean isPlayer(String playerName, DefaultGame cardGame, ActionContext actionContext) {
        return Objects.equals(actionContext.getValueFromMemory(_memoryId), playerName);
    }

    @Override
    public String getPlayerName(DefaultGame cardGame, ActionContext actionContext) {
        return actionContext.getValueFromMemory(_memoryId);
    }
}