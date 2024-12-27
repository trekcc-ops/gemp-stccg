package com.gempukku.stccg.actions.turn;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.SubAction;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

import java.util.List;
import java.util.Set;

public class PlayOutEffectResults extends SystemQueueAction {
    private final Set<EffectResult> _effectResults;
    private boolean _initialized;

    public PlayOutEffectResults(DefaultGame game, Set<EffectResult> effectResults) {
        super(game);
        _effectResults = effectResults;
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        if (!_initialized) {
            _initialized = true;
            List<Action> requiredResponses = cardGame.getActionsEnvironment().getRequiredAfterTriggers(_effectResults);
            if (!requiredResponses.isEmpty())
                appendAction(
                        new PlayOutAllSubActionsIfActionNotCancelledAction(cardGame, this, requiredResponses));

            GameState gameState = cardGame.getGameState();
            ActionOrder actionOrder = gameState.getPlayerOrder().getCounterClockwisePlayOrder(
                    gameState.getCurrentPlayerId(), true);
            appendAction(new PlayOutOptionalAfterResponsesAction(
                    cardGame, this, actionOrder, 0, _effectResults));
        }
        return getNextAction();
    }

}