package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayOrder;

import java.util.List;

public class PlayersPlayPhaseActionsInOrderGameProcess implements GameProcess {
    private final PlayOrder _playOrder;
    private int _consecutivePasses;
    private final GameProcess _followingGameProcess;

    private GameProcess _nextProcess;

    public PlayersPlayPhaseActionsInOrderGameProcess(PlayOrder playOrder, int consecutivePasses, GameProcess followingGameProcess) {
        _playOrder = playOrder;
        _consecutivePasses = consecutivePasses;
        _followingGameProcess = followingGameProcess;
    }

    @Override
    public void process(final DefaultGame game) {
        String playerId;
        if (game.getGameState().isConsecutiveAction()) {
            playerId = _playOrder.getLastPlayer();
            game.getGameState().setConsecutiveAction(false);
        } else {
            playerId = _playOrder.getNextPlayer();
        }

        final List<Action> playableActions = game.getActionsEnvironment().getPhaseActions(playerId);
        if (playableActions.size() == 0 && game.shouldAutoPass(playerId, game.getGameState().getCurrentPhase())) {
            playerPassed();
        } else {
            game.getUserFeedback().sendAwaitingDecision(playerId,
                    new CardActionSelectionDecision(game, 1, "Play " + game.getGameState().getCurrentPhase().getHumanReadable() + " action or Pass", playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                _nextProcess = new PlayersPlayPhaseActionsInOrderGameProcess(_playOrder, 0, _followingGameProcess);
                                game.getActionsEnvironment().addActionToStack(action);
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
            _nextProcess = new PlayersPlayPhaseActionsInOrderGameProcess(_playOrder, _consecutivePasses, _followingGameProcess);
    }

    @Override
    public GameProcess getNextProcess() {
        return _nextProcess;
    }
}
