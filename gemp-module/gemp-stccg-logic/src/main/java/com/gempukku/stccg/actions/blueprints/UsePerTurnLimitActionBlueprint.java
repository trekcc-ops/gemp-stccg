package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.usage.UseOncePerTurnAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.modifiers.LimitCounter;

public class UsePerTurnLimitActionBlueprint implements SubActionBlueprint {

    private final ActionBlueprint _parentActionBlueprint;

    private final int _limitPerTurn;

    public UsePerTurnLimitActionBlueprint(ActionBlueprint parentActionBlueprint, int limitPerTurn) {
        _parentActionBlueprint = parentActionBlueprint;
        _limitPerTurn = limitPerTurn;
    }
    public Action createAction(DefaultGame cardGame, GameTextContext context) {
        return new UseOncePerTurnAction(cardGame, context.card(), _parentActionBlueprint, context.yourName());
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