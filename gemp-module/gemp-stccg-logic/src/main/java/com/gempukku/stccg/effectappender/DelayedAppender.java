package com.gempukku.stccg.effectappender;

import com.gempukku.stccg.actions.CostToEffectAction;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;

import java.util.Collections;
import java.util.List;

public abstract class DelayedAppender implements EffectAppender {
    protected String _text;

    @Override
    public final void appendEffect(boolean cost, CostToEffectAction action, ActionContext actionContext) {
        final UnrespondableEffect effect = new UnrespondableEffect(actionContext) {
            @Override
            protected void doPlayEffect() {
                // Need to insert them, but in the reverse order
                final List<? extends Effect> effects = createEffects(cost, action, actionContext);
                if (effects != null) {
                    final Effect[] effectsArray = effects.toArray(new Effect[0]);
                    for (int i = effectsArray.length - 1; i >= 0; i--)
                        if (cost)
                            action.insertCost(effectsArray[i]);
                        else
                            action.insertEffect(effectsArray[i]);
                }
            }

            @Override
            public String getText() {
                return _text;
            }
        };

        if (cost) {
            action.appendCost(effect);
        } else {
            action.appendEffect(effect);
        }
    }

    protected Effect createEffect(boolean cost, CostToEffectAction action, ActionContext context) {
        throw new UnsupportedOperationException("One of createEffect or createEffects has to be overwritten");
    }

    protected List<? extends Effect> createEffects(boolean cost, CostToEffectAction action, ActionContext actionContext) {
        final Effect effect = createEffect(cost, action, actionContext);
        if (effect == null)
            return null;
        return Collections.singletonList(effect);
    }

    @Override
    public boolean isPlayableInFull(ActionContext actionContext) {
        return true;
    }
}
