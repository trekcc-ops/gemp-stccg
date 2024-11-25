package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.TribblesActionContext;


public class ActivateReverseTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateReverseTribblePowerEffect(Action action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        getGame().getGameState().getPlayerOrder().reversePlayerOrder();
        getGame().getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }
}