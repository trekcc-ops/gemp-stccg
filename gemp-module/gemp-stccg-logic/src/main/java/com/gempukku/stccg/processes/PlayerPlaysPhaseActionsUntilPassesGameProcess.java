package com.gempukku.stccg.processes;

import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.actions.Action;

import java.util.List;

public class PlayerPlaysPhaseActionsUntilPassesGameProcess extends GameProcess {
    private final String _playerId;
    private final GameProcess _followingGameProcess;

    private GameProcess _nextProcess;
    private final DefaultGame _game;

    public PlayerPlaysPhaseActionsUntilPassesGameProcess(String playerId, GameProcess followingGameProcess,
                                                         DefaultGame game) {
        _playerId = playerId;
        _followingGameProcess = followingGameProcess;
        _game = game;
    }

    @Override
    public void process() {
        final List<Action> playableActions = _game.getActionsEnvironment().getPhaseActions(_playerId);

        if (playableActions.isEmpty() && _game.shouldAutoPass(_game.getGameState().getCurrentPhase())) {
            playerPassed();
        } else {
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardActionSelectionDecision(
                            "Play " + _game.getCurrentPhaseString() + " action or Pass", playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                _nextProcess = new PlayerPlaysPhaseActionsUntilPassesGameProcess(
                                        _playerId, _followingGameProcess, _game);
                                _game.getActionsEnvironment().addActionToStack(action);
                            } else
                                playerPassed();
                        }
                    });
        }
    }

    private void playerPassed() {
        _nextProcess = _followingGameProcess;
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
