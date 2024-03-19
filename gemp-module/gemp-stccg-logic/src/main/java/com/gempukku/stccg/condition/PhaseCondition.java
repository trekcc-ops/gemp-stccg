package com.gempukku.stccg.condition;

import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.DefaultGame;

public class PhaseCondition implements Condition {
    private final Phase _phase;
    private final DefaultGame _game;

    public PhaseCondition(DefaultGame game, Phase phase) {
        _game = game;
        _phase = phase;
    }

    @Override
    public boolean isFulfilled() {
        return _phase == null || _game.getGameState().getCurrentPhase() == _phase;
    }
}
