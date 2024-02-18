package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayOrder;

import java.util.List;

public class PlayersPlayPhaseActionsInOrderGameProcess extends GameProcess {
    private final PlayOrder _playOrder;
    private int _consecutivePasses;
    private final GameProcess _followingGameProcess;

    private GameProcess _nextProcess;
    private DefaultGame _game;

    public PlayersPlayPhaseActionsInOrderGameProcess(PlayOrder playOrder, int consecutivePasses, GameProcess followingGameProcess, DefaultGame game) {
        _playOrder = playOrder;
        _consecutivePasses = consecutivePasses;
        _followingGameProcess = followingGameProcess;
        _game = game;
    }

    @Override
    public void process() {
        String playerId;
        if (_game.getGameState().isConsecutiveAction()) {
            playerId = _playOrder.getLastPlayer();
            _game.getGameState().setConsecutiveAction(false);
        } else {
            playerId = _playOrder.getNextPlayer();
        }

        final List<Action> playableActions = _game.getActionsEnvironment().getPhaseActions(playerId);
        if (playableActions.isEmpty() && _game.shouldAutoPass(playerId, _game.getGameState().getCurrentPhase())) {
            playerPassed();
        } else {
            _game.getUserFeedback().sendAwaitingDecision(playerId,
                    new CardActionSelectionDecision(1, "Play " + _game.getGameState().getCurrentPhase().getHumanReadable() + " action or Pass", playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                _nextProcess = new PlayersPlayPhaseActionsInOrderGameProcess(
                                        _playOrder, 0, _followingGameProcess, _game);
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
        if (_consecutivePasses >= _playOrder.getPlayerCount())
            _nextProcess = _followingGameProcess;
        else
            _nextProcess = new PlayersPlayPhaseActionsInOrderGameProcess(
                    _playOrder, _consecutivePasses, _followingGameProcess, _game);
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
