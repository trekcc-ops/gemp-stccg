package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.usage.UseOncePerGameAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.modifiers.LimitCounter;

import java.util.Collections;
import java.util.List;

public class UsePerGameLimitActionBlueprint implements SubActionBlueprint {

    private final ActionBlueprint _parentActionBlueprint;

    private final int _limitPerGame;

    public UsePerGameLimitActionBlueprint(ActionBlueprint parentActionBlueprint, int limitPerGame) {
        _parentActionBlueprint = parentActionBlueprint;
        _limitPerGame = limitPerGame;
    }
    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions parentAction,
                                      ActionContext actionContext) {
        Action usageLimitAction = new UseOncePerGameAction(cardGame,
                actionContext.card(), actionContext.getPerformingPlayerId(), _parentActionBlueprint);
        return Collections.singletonList(usageLimitAction);
    }

    @Override
    public boolean isPlayableInFull(DefaultGame cardGame, ActionContext actionContext) {
        GameState gameState = cardGame.getGameState();
        PhysicalCard thisCard = actionContext.card();
        LimitCounter counter = gameState.getUntilEndOfGameLimitCounter(actionContext.getPerformingPlayerId(),
                thisCard, _parentActionBlueprint);
        return counter.getUsedLimit() < _limitPerGame;
    }

    @Override
    public boolean isPlayabilityCheckedForEffect() {
        return true;
    }
}