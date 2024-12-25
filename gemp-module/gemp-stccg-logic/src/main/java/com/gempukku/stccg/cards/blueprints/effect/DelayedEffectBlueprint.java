package com.gempukku.stccg.cards.blueprints.effect;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.Collections;
import java.util.List;

public abstract class DelayedEffectBlueprint implements EffectBlueprint {

    @Override
    public final void addEffectToAction(boolean cost, Action action, ActionContext actionContext) {
        final UnrespondableEffect effect = new UnrespondableEffect(actionContext) {
            @Override
            protected void doPlayEffect() throws InvalidCardDefinitionException, InvalidGameLogicException {
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

        };

        if (cost) {
            action.appendCost(effect);
        } else {
            action.appendEffect(effect);
        }
    }

    protected Effect createEffect(Action action, ActionContext context) throws InvalidGameLogicException, InvalidCardDefinitionException {
        throw new UnsupportedOperationException("One of createEffect or createEffects has to be overwritten");
    }

    protected List<? extends Effect> createEffects(boolean cost, Action action, ActionContext actionContext)
            throws InvalidCardDefinitionException, InvalidGameLogicException {
        final Effect effect = createEffect(action, actionContext);
        if (effect == null)
            return null;
        return Collections.singletonList(effect);
    }

    @Override
    public boolean isPlayableInFull(ActionContext actionContext) {
        return true;
    }
}