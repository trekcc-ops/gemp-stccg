package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.blueprints.SubActionBlueprint;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;

import java.util.List;

public class AddCostSubAction extends SystemQueueAction {

    private final ActionWithSubActions _parentAction;
    private final SubActionBlueprint _blueprint;

    public AddCostSubAction(DefaultGame cardGame, GameTextContext actionContext,
                            ActionWithSubActions parentAction, SubActionBlueprint blueprint) {
        super(cardGame, actionContext, parentAction.getPerformingPlayerId());
        _parentAction = parentAction;
        _blueprint = blueprint;
    }


    @Override
    protected void processEffect(DefaultGame cardGame) {
        final Action actionToAdd = _blueprint.createAction(cardGame, _parentAction, _actionContext);
        if (actionToAdd != null) {
            _parentAction.insertCosts(List.of(actionToAdd));
            setAsSuccessful();
        } else {
            setAsFailed();
        }
    }

    public boolean requirementsAreMet(DefaultGame cardGame) {
        if (_blueprint.isPlayabilityCheckedForEffect()) {
            return _blueprint.isPlayableInFull(cardGame, _actionContext);
        } else {
            return true;
        }
    }

    public SubActionBlueprint getSubAction() {
        return _blueprint;
    }
}