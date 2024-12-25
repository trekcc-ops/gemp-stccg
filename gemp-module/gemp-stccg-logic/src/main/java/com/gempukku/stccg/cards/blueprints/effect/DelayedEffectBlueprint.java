package com.gempukku.stccg.cards.blueprints.effect;

import com.gempukku.stccg.actions.*;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.LinkedList;
import java.util.List;

public abstract class DelayedEffectBlueprint implements EffectBlueprint {

    @Override
    public final void addEffectToAction(boolean cost, Action action, ActionContext actionContext) {
        final UnrespondableEffect effect = new UnrespondableEffect(actionContext) {
            @Override
            protected void doPlayEffect() throws InvalidCardDefinitionException, InvalidGameLogicException {
                // Need to insert them, but in the reverse order
                final List<Action> actions = createActions(action, actionContext);
                if (actions != null) {
                    final Action[] effectsArray = actions.toArray(new Action[0]);
                    for (int i = effectsArray.length - 1; i >= 0; i--)
                        if (cost)
                            action.insertCost(new StackActionEffect(actionContext.getGame(), effectsArray[i]));
                        else
                            action.insertEffect(new StackActionEffect(actionContext.getGame(), effectsArray[i]));
                }
            }

        };

        if (cost) {
            action.appendCost(effect);
        } else {
            action.appendEffect(effect);
        }
    }

    abstract protected List<Action> createActions(Action action, ActionContext actionContext)
            throws InvalidGameLogicException, InvalidCardDefinitionException;

    @Override
    public boolean isPlayableInFull(ActionContext actionContext) {
        return true;
    }
}