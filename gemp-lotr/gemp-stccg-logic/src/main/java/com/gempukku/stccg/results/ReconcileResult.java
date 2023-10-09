package com.gempukku.stccg.results;

import com.gempukku.stccg.effects.EffectResult;

public class ReconcileResult extends EffectResult {
    private final String _playerId;

    public ReconcileResult(String playerId) {
        super(EffectResult.Type.RECONCILE);
        _playerId = playerId;
    }

    public String getPlayerId() {
        return _playerId;
    }
}
