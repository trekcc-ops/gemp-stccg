package com.gempukku.stccg.cards.blueprints.effect;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.PlayerNotFoundException;

import java.util.List;

public abstract class DelayedEffectBlueprint implements EffectBlueprint {

    @Override
    public final void addEffectToAction(boolean cost, CardPerformedAction action, ActionContext actionContext) {
        final SystemQueueAction sysAction = new SystemQueueAction(actionContext.getGame()) {
            @Override
            public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
                try {
                    // Need to insert them, but in the reverse order
                    final List<Action> actions = createActions(action, actionContext);
                    if (actions != null) {
                        final Action[] effectsArray = actions.toArray(new Action[0]);
                        for (int i = effectsArray.length - 1; i >= 0; i--)
                            if (cost)
                                action.insertCost(effectsArray[i]);
                            else
                                action.insertEffect(effectsArray[i]);
                    }
                } catch (InvalidCardDefinitionException exp) {
                    throw new InvalidGameLogicException(exp.getMessage());
                }
                Action nextAction = getNextAction();
                if (nextAction != null)
                    return nextAction;
                else {
                    setAsSuccessful();
                    return null;
                }
            }
        };

        if (cost) {
            action.appendCost(sysAction);
        } else {
            action.appendEffect(sysAction);
        }
    }


    abstract protected List<Action> createActions(CardPerformedAction action, ActionContext actionContext)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException;

    @Override
    public boolean isPlayableInFull(ActionContext actionContext) {
        return true;
    }
}