package com.gempukku.stccg.condition;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;

public class PhaseCondition implements Condition {
    private final Phase _phase;

    public PhaseCondition(Phase phase) {
        _phase = phase;
    }

    @Override
    public boolean isFulfilled(DefaultGame game) {
        return _phase == null || game.getGameState().getCurrentPhase() == _phase;
    }
}
