package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.PlayerOrder;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.processes.GameProcess;

import java.util.List;

public class ST1EMissionSeedPhaseProcess extends ST1EGameProcess {
    private final PlayerOrder _playOrder;
    private int _consecutivePasses;
    private final ST1EGameProcess _followingGameProcess;

    public ST1EMissionSeedPhaseProcess(int consecutivePasses, ST1EGameProcess followingGameProcess, ST1EGame game) {
        super(game);
        _followingGameProcess = followingGameProcess;
        _consecutivePasses = consecutivePasses;
        _playOrder = game.getGameState().getPlayerOrder();
    }

    @Override
    public void process() {
        String _currentPlayer = _playOrder.getCurrentPlayer();

        final List<Action> playableActions = _game.getActionsEnvironment().getPhaseActions(_currentPlayer);
        ST1EGameState gameState = _game.getGameState();
        Phase currentPhase = gameState.getCurrentPhase();

        if (playableActions.isEmpty() && _game.shouldAutoPass(currentPhase)) {
            _consecutivePasses++;
        } else {
            String message = "Play " + currentPhase.getHumanReadable() + " action";
            _game.getUserFeedback().sendAwaitingDecision(_currentPlayer,
                    new CardActionSelectionDecision(message, playableActions, true, true) {
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
            return new ST1EMissionSeedPhaseProcess(_consecutivePasses, _followingGameProcess, _game);
        }
    }
}