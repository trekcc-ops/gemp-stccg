package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.usage.UseOncePerTurnAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.modifiers.LimitCounter;

import java.util.Collections;
import java.util.List;

public class UsePerTurnLimitActionBlueprint implements SubActionBlueprint {

    private final ActionBlueprint _parentActionBlueprint;

    private final int _limitPerTurn;

    public UsePerTurnLimitActionBlueprint(ActionBlueprint parentActionBlueprint, int limitPerTurn) {
        _parentActionBlueprint = parentActionBlueprint;
        _limitPerTurn = limitPerTurn;
    }
    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions parentAction,
                                      GameTextContext actionContext) {
        Action usageLimitAction = new UseOncePerTurnAction(cardGame,
                actionContext.card(), _parentActionBlueprint, actionContext.yourName());
        return Collections.singletonList(usageLimitAction);
    }

    @Override
    public boolean isPlayableInFull(DefaultGame cardGame, GameTextContext actionContext) {
        GameState gameState = cardGame.getGameState();
        PhysicalCard thisCard = actionContext.card();
        LimitCounter counter = gameState.getUntilEndOfTurnLimitCounter(
                actionContext.yourName(), thisCard, _parentActionBlueprint);
        return counter.getUsedLimit() < _limitPerTurn;
    }

    @Override
    public boolean isPlayabilityCheckedForEffect() {
        return true;
    }
}