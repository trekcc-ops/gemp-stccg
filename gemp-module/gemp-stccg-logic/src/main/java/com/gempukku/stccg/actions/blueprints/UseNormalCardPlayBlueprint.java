package com.gempukku.stccg.actions.blueprints;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionWithSubActions;
import com.gempukku.stccg.actions.usage.UseNormalCardPlayAction;
import com.gempukku.stccg.cards.ActionContext;
import com.gempukku.stccg.cards.InvalidCardDefinitionException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.List;

public class UseNormalCardPlayBlueprint implements SubActionBlueprint {
    @Override
    public List<Action> createActions(DefaultGame cardGame, ActionWithSubActions parentAction,
                                      ActionContext actionContext)
            throws InvalidGameLogicException, InvalidCardDefinitionException, PlayerNotFoundException {
        return List.of(new UseNormalCardPlayAction(cardGame, actionContext.getPerformingPlayerId()));
    }

    @Override
    public boolean isPlayableInFull(DefaultGame cardGame, ActionContext actionContext) {
        GameState gameState = cardGame.getGameState();
        return gameState.getNormalCardPlaysAvailable(actionContext.getPerformingPlayerId()) >= 1;
    }

    @Override
    public boolean isPlayabilityCheckedForEffect() {
        return true;
    }

}