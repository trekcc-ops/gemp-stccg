package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.CardPerformedAction;
import com.gempukku.stccg.actions.blueprints.SubActionBlueprint;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class AddSubactionEffectsAction extends SystemQueueAction {

    private final boolean _isCost;
    private final CardPerformedAction _parentAction;
    private final SubActionBlueprint _blueprint;

    public AddSubactionEffectsAction(DefaultGame cardGame, ActionContext actionContext, boolean isCost,
                                     CardPerformedAction parentAction, SubActionBlueprint blueprint) {
        super(cardGame, actionContext, parentAction.getPerformingPlayerId());
        _isCost = isCost;
        _parentAction = parentAction;
        _blueprint = blueprint;
    }


    @Override
    protected void processEffect(DefaultGame cardGame) {
        try {
            final List<Action> actions = _blueprint.createActions(cardGame, _parentAction, _actionContext);
            if (actions != null) {
                if (_isCost)
                    _parentAction.insertCosts(actions);
                else
                    _parentAction.insertActions(actions);
            }
        } catch (InvalidCardDefinitionException | InvalidGameLogicException | PlayerNotFoundException exp) {
            cardGame.sendErrorMessage(exp);
            setAsFailed();
        }
        setAsSuccessful();
    }

    public boolean requirementsAreMet(DefaultGame cardGame) {
        if (_blueprint.isPlayabilityCheckedForEffect()) {
            return _blueprint.isPlayableInFull(cardGame, _actionContext);
        } else {
            return true;
        }
    }
}