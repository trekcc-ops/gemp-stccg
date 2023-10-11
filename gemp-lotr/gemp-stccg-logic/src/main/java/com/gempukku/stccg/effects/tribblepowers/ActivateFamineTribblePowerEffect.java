package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.effects.defaulteffect.ActivateTribblePowerEffect;

public class ActivateFamineTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateFamineTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        _game.getGameState().setNextTribbleInSequence(1);
        _game.getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }
}