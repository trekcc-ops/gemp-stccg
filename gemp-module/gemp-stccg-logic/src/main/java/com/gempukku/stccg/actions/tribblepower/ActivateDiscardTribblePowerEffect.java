package com.gempukku.stccg.actions.tribblepower;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.actions.choose.ChooseAndDiscardCardsFromHandEffect;


public class ActivateDiscardTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateDiscardTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        new ChooseAndDiscardCardsFromHandEffect(getGame(), _action, _source.getOwnerName(),false,1).playEffect();
        getGame().getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }
}