package com.gempukku.stccg.actions.scorepoints;

import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.game.TribblesGame;

public class PlayerWentOutResult extends EffectResult {
    public PlayerWentOutResult(String playerId, TribblesGame game) {
        super(Type.PLAYER_WENT_OUT);
        _playerId = playerId;
    }
}