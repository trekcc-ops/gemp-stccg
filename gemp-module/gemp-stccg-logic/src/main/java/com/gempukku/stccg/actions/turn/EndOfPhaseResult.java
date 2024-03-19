package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;

public class EndOfPhaseResult extends EffectResult {
    private final Phase _phase;

    public EndOfPhaseResult(Phase phase, DefaultGame game) {
        super(EffectResult.Type.END_OF_PHASE, game);
        _phase = phase;
    }

    public Phase getPhase() {
        return _phase;
    }
}
