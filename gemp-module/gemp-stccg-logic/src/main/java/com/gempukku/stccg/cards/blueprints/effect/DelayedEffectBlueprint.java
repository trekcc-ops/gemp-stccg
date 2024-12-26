package com.gempukku.stccg.cards.blueprints.effect;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.StackActionEffect;
import com.gempukku.stccg.actions.UnrespondableEffect;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;

import java.util.List;

public abstract class DelayedEffectBlueprint implements EffectBlueprint {

    @Override
    public final void addEffectToAction(boolean cost, Action action, ActionContext actionContext) {
        final SystemQueueAction sysAction = new SystemQueueAction(actionContext.getGame()) {
            @Override
            public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException {
                try {
                    // Need to insert them, but in the reverse order
                    final List<Action> actions = createActions(action, actionContext);
                    if (actions != null) {
                        final Action[] effectsArray = actions.toArray(new Action[0]);
                        for (int i = effectsArray.length - 1; i >= 0; i--)
                            if (cost)
                                action.insertCost(cardGame, effectsArray[i]);
                            else
                                action.insertEffect(cardGame, effectsArray[i]);
                    }
                } catch (InvalidCardDefinitionException exp) {
                    throw new InvalidGameLogicException(exp.getMessage());
                }
                return getNextAction();
            }
        };

        if (cost) {
            action.appendCost(new StackActionEffect(actionContext.getGame(), sysAction));
        } else {
            action.appendEffect(new StackActionEffect(actionContext.getGame(), sysAction));
        }
    }


    abstract protected List<Action> createActions(Action action, ActionContext actionContext)
            throws InvalidGameLogicException, InvalidCardDefinitionException;

    @Override
    public boolean isPlayableInFull(ActionContext actionContext) {
        return true;
    }
}