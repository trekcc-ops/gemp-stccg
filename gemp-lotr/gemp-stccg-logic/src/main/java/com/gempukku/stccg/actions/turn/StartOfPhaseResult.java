package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;

public class StartOfPhaseResult extends EffectResult {
    private final Phase _phase;
    private final String _playerId;

    public StartOfPhaseResult(Phase phase, String playerId, DefaultGame game) {
        super(EffectResult.Type.START_OF_PHASE, game);
        _phase = phase;
        _playerId = playerId;
    }

    public String getPlayerId() {
        return _playerId;
    }

    public Phase getPhase() {
        return _phase;
    }
}
