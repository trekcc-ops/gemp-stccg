package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionType;
import com.gempukku.stccg.actions.ActionyAction;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.blueprints.SubActionBlueprint;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class AddSubactionEffectsAction extends SystemQueueAction {

    private final boolean _isCost;
    private final CardPerformedAction _parentAction;
    private final ActionContext _actionContext;
    private final SubActionBlueprint _blueprint;

    public AddSubactionEffectsAction(ActionContext actionContext, boolean isCost, CardPerformedAction parentAction,
                                     SubActionBlueprint blueprint) {
        super(actionContext.getGame());
        _isCost = isCost;
        _parentAction = parentAction;
        _actionContext = actionContext;
        _blueprint = blueprint;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        try {
            // Need to insert them, but in the reverse order
            final List<Action> actions = _blueprint.createActions(_parentAction, _actionContext);
            if (actions != null) {
                final Action[] effectsArray = actions.toArray(new Action[0]);
                for (int i = effectsArray.length - 1; i >= 0; i--)
                    if (_isCost)
                        _parentAction.insertCost(effectsArray[i]);
                    else
                        _parentAction.insertEffect(effectsArray[i]);
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

}