package com.gempukku.stccg.results;

import com.gempukku.stccg.effects.EffectResult;

public class PlayerWentOutResult extends EffectResult {
    public PlayerWentOutResult(String playerId) {
        super(Type.PLAYER_WENT_OUT);
        _playerId = playerId;
    }
}
