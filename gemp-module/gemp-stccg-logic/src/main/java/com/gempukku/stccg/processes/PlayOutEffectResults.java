package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.EffectResult;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;

import java.util.List;
import java.util.Set;

class PlayOutEffectResults extends SystemQueueAction {
    private final Set<? extends EffectResult> _effectResults;
    private boolean _initialized;

    PlayOutEffectResults(DefaultGame game, Set<? extends EffectResult> effectResults) {
        super();
        _effectResults = effectResults;
    }

    @Override
    public Effect nextEffect(DefaultGame cardGame) {
        if (!_initialized) {
            _initialized = true;
            List<Action> requiredResponses = cardGame.getActionsEnvironment().getRequiredAfterTriggers(_effectResults);
            if (!requiredResponses.isEmpty())
                appendEffect(new PlayOutAllActionsIfEffectNotCancelledEffect(cardGame, this, requiredResponses));

            GameState gameState = cardGame.getGameState();
            ActionOrder actionOrder = gameState.getPlayerOrder()
                    .getCounterClockwisePlayOrder(gameState.getCurrentPlayerId(), true);
            Effect effect = new PlayOutOptionalAfterResponsesEffect(
                    cardGame, this, actionOrder, 0, _effectResults);
            appendEffect(effect);
        }
        return getNextEffect();
    }

}