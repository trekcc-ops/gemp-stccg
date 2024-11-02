package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.ActionsEnvironment;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

import java.util.List;
import java.util.Set;

class PlayOutEffectResults extends SystemQueueAction {
    private final Set<? extends EffectResult> _effectResults;
    private boolean _initialized;
    private final ActionsEnvironment _actionsEnvironment;

    PlayOutEffectResults(DefaultGame game, Set<? extends EffectResult> effectResults) {
        super(game);
        _effectResults = effectResults;
        _actionsEnvironment = _game.getActionsEnvironment();
    }

    @Override
    public Effect nextEffect() {
        if (!_initialized) {
            _initialized = true;
            List<Action> requiredResponses = _actionsEnvironment.getRequiredAfterTriggers(_effectResults);
            if (!requiredResponses.isEmpty())
                appendEffect(new PlayOutAllActionsIfEffectNotCancelledEffect(this, requiredResponses));

            GameState gameState = _game.getGameState();
            appendEffect(
                    new PlayOutOptionalAfterResponsesEffect(this,
                            gameState.getPlayerOrder().getCounterClockwisePlayOrder(
                                    gameState.getCurrentPlayerId(), true
                            ), 0, _effectResults
                    )
            );
        }
        return getNextEffect();
    }

}