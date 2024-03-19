package com.gempukku.stccg.actions;

import com.gempukku.stccg.game.TribblesGame;

public class PlayerWentOutResult extends EffectResult {
    public PlayerWentOutResult(String playerId, TribblesGame game) {
        super(Type.PLAYER_WENT_OUT, game);
        _playerId = playerId;
    }
}
