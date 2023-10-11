package com.gempukku.stccg.results;

import com.gempukku.stccg.common.filterable.Phase;

public class EndOfPhaseResult extends EffectResult {
    private final Phase _phase;

    public EndOfPhaseResult(Phase phase) {
        super(EffectResult.Type.END_OF_PHASE);
        _phase = phase;
    }

    public Phase getPhase() {
        return _phase;
    }
}
