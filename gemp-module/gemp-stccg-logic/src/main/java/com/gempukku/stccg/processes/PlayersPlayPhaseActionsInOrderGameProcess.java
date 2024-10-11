package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.DefaultGame;

import java.util.List;

public class PlayersPlayPhaseActionsInOrderGameProcess extends GameProcess {
    private final ActionOrder _actionOrder;
    private int _consecutivePasses;
    private final GameProcess _followingGameProcess;

    private GameProcess _nextProcess;
    private final DefaultGame _game;

    public PlayersPlayPhaseActionsInOrderGameProcess(ActionOrder actionOrder, int consecutivePasses,
                                                     GameProcess followingGameProcess, DefaultGame game) {
        _actionOrder = actionOrder;
        _consecutivePasses = consecutivePasses;
        _followingGameProcess = followingGameProcess;
        _game = game;
    }

    @Override
    public void process() {
        Phase currentPhase = _game.getGameState().getCurrentPhase();
        String currentPhaseString = _game.getCurrentPhaseString();
        String playerId;
        if (_game.getGameState().isConsecutiveAction()) {
            playerId = _actionOrder.getLastPlayer();
            _game.getGameState().setConsecutiveAction(false);
        } else {
            playerId = _actionOrder.getNextPlayer();
        }

        final List<Action> playableActions = _game.getActionsEnvironment().getPhaseActions(playerId);
        if (playableActions.isEmpty() && _game.shouldAutoPass(currentPhase))
            playerPassed();
        else {
            _game.getUserFeedback().sendAwaitingDecision(playerId,
                    new CardActionSelectionDecision(
                            "Play " + currentPhaseString + " action or Pass", playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                _nextProcess = new PlayersPlayPhaseActionsInOrderGameProcess(
                                        _actionOrder, 0, _followingGameProcess, _game);
                                _game.getActionsEnvironment().addActionToStack(action);
                            } else {
                                playerPassed();
                            }
                        }
                    });
        }
    }

    private void playerPassed() {
        _consecutivePasses++;
        if (_consecutivePasses >= _actionOrder.getPlayerCount())
            _nextProcess = _followingGameProcess;
        else
            _nextProcess = new PlayersPlayPhaseActionsInOrderGameProcess(
                    _actionOrder, _consecutivePasses, _followingGameProcess, _game);
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
