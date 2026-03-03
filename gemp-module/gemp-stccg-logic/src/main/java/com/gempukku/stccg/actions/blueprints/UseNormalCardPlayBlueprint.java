package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.usage.UseNormalCardPlayAction;
import com.gempukku.stccg.cards.GameTextContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

public class UseNormalCardPlayBlueprint implements SubActionBlueprint {
    @Override
    public Action createAction(DefaultGame cardGame, ActionWithSubActions parentAction,
                                      GameTextContext actionContext) {
        return new UseNormalCardPlayAction(cardGame, actionContext.yourName());
    }

    @Override
    public boolean isPlayableInFull(DefaultGame cardGame, GameTextContext actionContext) {
        GameState gameState = cardGame.getGameState();
        return gameState.getNormalCardPlaysAvailable(actionContext.yourName()) >= 1;
    }

    @Override
    public boolean isPlayabilityCheckedForEffect() {
        return true;
    }

}