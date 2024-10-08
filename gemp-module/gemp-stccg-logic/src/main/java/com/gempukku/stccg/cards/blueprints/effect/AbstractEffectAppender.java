package com.gempukku.stccg.cards.blueprints.effect;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.actions.Effect;

public abstract class AbstractEffectAppender implements EffectBlueprint {
    @Override
    public final void addEffectToAction(boolean cost, CostToEffectAction action, ActionContext actionContext) {
        if (cost)
            action.appendCost(createEffect(true, action, actionContext));
        else
            action.appendEffect(createEffect(false, action, actionContext));
    }

    protected abstract Effect createEffect(boolean cost, CostToEffectAction action,
                                           ActionContext actionContext);

    @Override
    public boolean isPlayableInFull(ActionContext actionContext) {
        return true;
    }
}