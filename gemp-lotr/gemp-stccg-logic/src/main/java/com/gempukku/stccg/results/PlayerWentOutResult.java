package com.gempukku.stccg.results;

public class PlayerWentOutResult extends EffectResult {
    public PlayerWentOutResult(String playerId) {
        super(Type.PLAYER_WENT_OUT);
        _playerId = playerId;
    }
}
