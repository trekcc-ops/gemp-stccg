package com.gempukku.stccg.actions.scorepoints;

import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.game.TribblesGame;

public class PlayerWentOutResult extends ActionResult {
    public PlayerWentOutResult(String playerId) {
        super(Type.PLAYER_WENT_OUT, playerId);
    }
}