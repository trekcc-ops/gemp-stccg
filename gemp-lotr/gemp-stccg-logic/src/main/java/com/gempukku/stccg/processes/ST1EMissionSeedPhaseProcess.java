package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.game.PlayerOrder;
import com.gempukku.stccg.game.ST1EGame;

import java.util.List;

public class ST1EMissionSeedPhaseProcess implements GameProcess<ST1EGame> {
    private PlayerOrder _playOrder;
    private int _consecutivePasses;
    private final GameProcess<ST1EGame> _followingGameProcess;

    public ST1EMissionSeedPhaseProcess(int consecutivePasses, GameProcess<ST1EGame> followingGameProcess) {
        _followingGameProcess = followingGameProcess;
        _consecutivePasses = consecutivePasses;
    }

    @Override
    public void process(ST1EGame game) {
        _playOrder = game.getGameState().getPlayerOrder();
        String _currentPlayer = _playOrder.getCurrentPlayer();

        final List<Action> playableActions = game.getActionsEnvironment().getPhaseActions(_currentPlayer);
        if (playableActions.size() == 0 && game.shouldAutoPass(_currentPlayer, game.getGameState().getCurrentPhase())) {
            _consecutivePasses++;
        } else {
            game.getUserFeedback().sendAwaitingDecision(_currentPlayer,
                    new CardActionSelectionDecision(1, "Play " +
                            game.getGameState().getCurrentPhase().getHumanReadable() + " action or Pass", playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                game.getActionsEnvironment().addActionToStack(action);
                                _consecutivePasses = 0;
                            } else {
                                _consecutivePasses++;
                            }
                        }
                    });
        }
    }

    @Override
    public GameProcess<ST1EGame> getNextProcess() {
        if (_consecutivePasses >= _playOrder.getPlayerCount()) {
            _playOrder.setCurrentPlayer(_playOrder.getFirstPlayer());
            return _followingGameProcess;
        } else {
            _playOrder.advancePlayer();
            return new ST1EMissionSeedPhaseProcess(_consecutivePasses, _followingGameProcess);
        }
    }
}