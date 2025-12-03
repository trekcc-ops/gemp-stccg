package com.gempukku.stccg.player;

import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.game.DefaultGame;

public class PlayerSourceFromMemory implements PlayerSource {

    private final String _memoryId;

    public PlayerSourceFromMemory(String memoryId) {
        _memoryId = memoryId;
    }
    @Override
    public String getPlayerId(DefaultGame cardGame,  ActionContext actionContext) {
        return actionContext.getValueFromMemory(_memoryId);
    }
}