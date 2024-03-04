package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.TribblesActionContext;


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