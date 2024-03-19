package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.TribblesActionContext;


public class ActivateReverseTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateReverseTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        _game.getGameState().getPlayerOrder().reversePlayerOrder();
        _game.getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }
}