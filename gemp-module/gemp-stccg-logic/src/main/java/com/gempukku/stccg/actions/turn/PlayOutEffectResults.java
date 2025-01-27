package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionResult;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

import java.util.List;
import java.util.Set;

public class PlayOutEffectResults extends SystemQueueAction {
    private final Set<ActionResult> _actionResults;
    private boolean _initialized;

    public PlayOutEffectResults(DefaultGame game, Set<ActionResult> actionResults) {
        super(game);
        _actionResults = actionResults;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        if (!_initialized) {
            _initialized = true;
            List<TopLevelSelectableAction> requiredResponses =
                    cardGame.getActionsEnvironment().getRequiredAfterTriggers(_actionResults);
            if (!requiredResponses.isEmpty())
                appendEffect(
                        new PlayOutRequiredResponsesAction(cardGame, this, requiredResponses));

            GameState gameState = cardGame.getGameState();
            ActionOrder actionOrder = gameState.getPlayerOrder().getCounterClockwisePlayOrder(
                    gameState.getCurrentPlayerId(), true);
            appendEffect(
                    new PlayOutOptionalResponsesAction(cardGame, this, actionOrder, 0, _actionResults));
        }
        Action nextAction = getNextAction();
        if (nextAction == null)
            setAsSuccessful();
        return nextAction;
    }

}