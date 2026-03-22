package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.usage.UseOncePerGameAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.modifiers.LimitCounter;

public class UsePerGameLimitActionBlueprint implements SubActionBlueprint {

    private final ActionBlueprint _parentActionBlueprint;

    private final int _limitPerGame;

    public UsePerGameLimitActionBlueprint(ActionBlueprint parentActionBlueprint, int limitPerGame) {
        _parentActionBlueprint = parentActionBlueprint;
        _limitPerGame = limitPerGame;
    }
    public Action createAction(DefaultGame cardGame, GameTextContext actionContext) {
        return new UseOncePerGameAction(
                cardGame, actionContext.card(), actionContext.yourName(), _parentActionBlueprint);
    }

    @Override
    public boolean isPlayableInFull(DefaultGame cardGame, GameTextContext actionContext) {
        GameState gameState = cardGame.getGameState();
        PhysicalCard thisCard = actionContext.card();
        LimitCounter counter = gameState.getUntilEndOfGameLimitCounter(actionContext.yourName(),
                thisCard, _parentActionBlueprint);
        return counter.getUsedLimit() < _limitPerGame;
    }

    @Override
    public boolean isPlayabilityCheckedForEffect() {
        return true;
    }
}