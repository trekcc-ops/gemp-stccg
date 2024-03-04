package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.game.PlayerOrder;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;

import java.util.List;

public class ST1EMissionSeedPhaseProcessOld extends ST1EGameProcess {
    private final PlayerOrder _playOrder;
    private int _consecutivePasses;
    private final ST1EGameProcess _followingGameProcess;

    public ST1EMissionSeedPhaseProcessOld(int consecutivePasses, ST1EGameProcess followingGameProcess, ST1EGame game) {
        super(game);
        _followingGameProcess = followingGameProcess;
        _consecutivePasses = consecutivePasses;
        _playOrder = game.getGameState().getPlayerOrder();
    }

    @Override
    public void process() {
        String _currentPlayer = _playOrder.getCurrentPlayer();

        final List<Action> playableActions = _game.getActionsEnvironment().getPhaseActions(_currentPlayer);
        if (playableActions.isEmpty() && _game.shouldAutoPass(_currentPlayer, _game.getGameState().getCurrentPhase())) {
            _consecutivePasses++;
        } else {
            _game.getUserFeedback().sendAwaitingDecision(_currentPlayer,
                    new CardActionSelectionDecision(1, "Play " +
                            _game.getGameState().getCurrentPhase().getHumanReadable() + " action or Pass", playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                _game.getActionsEnvironment().addActionToStack(action);
                                _consecutivePasses = 0;
                            } else {
                                _consecutivePasses++;
                            }
                        }
                    });
        }
    }

    @Override
    public GameProcess getNextProcess() {
        if (_consecutivePasses >= _playOrder.getPlayerCount()) {
            _playOrder.setCurrentPlayer(_playOrder.getFirstPlayer());
            return _followingGameProcess;
        } else {
            _playOrder.advancePlayer();
            return new ST1EMissionSeedPhaseProcessOld(_consecutivePasses, _followingGameProcess, _game);
        }
    }
}