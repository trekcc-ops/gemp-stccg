package com.gempukku.lotro.results;

import com.gempukku.lotro.effects.EffectResult;

public class PlayerWentOutResult extends EffectResult {
    public PlayerWentOutResult(String playerId) {
        super(Type.PLAYER_WENT_OUT);
        _playerId = playerId;
    }
}
