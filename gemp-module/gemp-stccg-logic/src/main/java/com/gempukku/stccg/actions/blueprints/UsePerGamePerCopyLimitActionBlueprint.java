package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.usage.UseOncePerGamePerCopyAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.modifiers.LimitCounter;

import java.util.Collections;
import java.util.List;

public class UsePerGamePerCopyLimitActionBlueprint implements SubActionBlueprint {

    private final ActionBlueprint _parentActionBlueprint;

    private final int _limitPerGame;

    public UsePerGamePerCopyLimitActionBlueprint(ActionBlueprint parentActionBlueprint, int limitPerGame) {
        _parentActionBlueprint = parentActionBlueprint;
        _limitPerGame = limitPerGame;
    }
    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions parentAction,
                                      GameTextContext actionContext) {
        Action usageLimitAction = new UseOncePerGamePerCopyAction(cardGame,
                actionContext.card(), actionContext.yourName(), _parentActionBlueprint);
        return Collections.singletonList(usageLimitAction);
    }

    @Override
    public boolean isPlayableInFull(DefaultGame cardGame, GameTextContext actionContext) {
        GameState gameState = cardGame.getGameState();
        PhysicalCard thisCard = actionContext.card();
        LimitCounter counter = gameState.getPerGamePerCopyLimitCounter(actionContext.yourName(),
                thisCard, _parentActionBlueprint);
        return counter.getUsedLimit() < _limitPerGame;
    }

    @Override
    public boolean isPlayabilityCheckedForEffect() {
        return true;
    }
}