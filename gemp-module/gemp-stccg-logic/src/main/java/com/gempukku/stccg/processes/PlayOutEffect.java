package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.Effect;
import com.gempukku.stccg.actions.turn.SystemQueueAction;
import com.gempukku.stccg.game.DefaultGame;

import java.util.HashSet;

class PlayOutEffect extends SystemQueueAction {
    private final Effect _effect;
    private boolean _initialized;

    PlayOutEffect(Effect effect) {
        _effect = effect;
    }

    @Override
    public String getText(DefaultGame game) {
        return _effect.getText();
    }

    @Override
    public Action nextAction(DefaultGame cardGame) {
        if (!_initialized) {
            _initialized = true;
            appendEffect(new PlayOutRequiredBeforeResponsesEffect(this, new HashSet<>(), _effect));
            appendEffect(new PlayOutOptionalBeforeResponsesEffect(this, new HashSet<>(),
                    cardGame.getGameState().getPlayerOrder().getCounterClockwisePlayOrder(
                            cardGame.getGameState().getCurrentPlayerId(), true), 0, _effect));
            appendEffect(new PlayEffect(_effect));
        }

        return getNextAction();
    }
}