package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.cards.DefaultActionContext;
import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.effects.Effect;
import com.gempukku.stccg.game.DefaultGame;

public abstract class AbstractEffectAppender<AbstractGame extends DefaultGame> implements EffectAppender<AbstractGame> {
    @Override
    public final void appendEffect(boolean cost, CostToEffectAction action, DefaultActionContext<AbstractGame> actionContext) {
        if (cost)
            action.appendCost(createEffect(true, action, actionContext));
        else
            action.appendEffect(createEffect(false, action, actionContext));
    }

    protected abstract Effect createEffect(boolean cost, CostToEffectAction action,
                                           DefaultActionContext<AbstractGame> actionContext);

    @Override
    public boolean isPlayableInFull(DefaultActionContext<AbstractGame> actionContext) {
        return true;
    }
}
