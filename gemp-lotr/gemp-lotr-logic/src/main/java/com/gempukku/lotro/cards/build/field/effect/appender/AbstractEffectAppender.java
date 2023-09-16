package com.gempukku.lotro.cards.build.field.effect.appender;

import com.gempukku.lotro.cards.build.DefaultActionContext;
import com.gempukku.lotro.actions.lotronly.CostToEffectAction;
import com.gempukku.lotro.effects.Effect;
import com.gempukku.lotro.game.DefaultGame;

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
