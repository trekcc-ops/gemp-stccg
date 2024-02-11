package com.gempukku.stccg.effects.tribblepowers;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.TribblesActionContext;
import com.gempukku.stccg.effects.choose.ChooseAndDiscardCardsFromHandEffect;
import com.gempukku.stccg.effects.defaulteffect.ActivateTribblePowerEffect;

public class ActivateDiscardTribblePowerEffect extends ActivateTribblePowerEffect {
    public ActivateDiscardTribblePowerEffect(CostToEffectAction action, TribblesActionContext actionContext) {
        super(action, actionContext);
    }

    @Override
    protected FullEffectResult playEffectReturningResult() {
        new ChooseAndDiscardCardsFromHandEffect(_game, _action, _source.getOwnerName(),false,1).playEffect();
        _game.getActionsEnvironment().emitEffectResult(_result);
        return new FullEffectResult(true);
    }
}