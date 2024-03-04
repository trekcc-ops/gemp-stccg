package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.game.PlayerOrder;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;

import java.util.List;

public class ST1EFacilitySeedPhaseProcess extends ST1EGameProcess {

    private PlayerOrder _playOrder;
    private int _consecutivePasses;
    public ST1EFacilitySeedPhaseProcess(int consecutivePasses, ST1EGame game) {
        super(game);
        _consecutivePasses = consecutivePasses;
    }

    @Override
    public void process() {
        _playOrder = _game.getGameState().getPlayerOrder();
        String _currentPlayer = _playOrder.getCurrentPlayer();
        _game.getGameState().sendMessage("DEBUG: Beginning facility seed phase process");

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
                                _consecutivePasses = 0;
                                _game.getActionsEnvironment().addActionToStack(action);
                            } else {
                                _consecutivePasses++;
                                _game.getGameState().sendMessage("DEBUG: Incrementing consecutivePasses");
                            }
                        }
                    });
        }
    }

    @Override
    public GameProcess getNextProcess() {
        if (_consecutivePasses >= _playOrder.getPlayerCount()) {
            _game.getGameState().sendMessage("DEBUG: Exiting facility seed phase process");
            _playOrder.setCurrentPlayer(_playOrder.getFirstPlayer());
            return new ST1EStartOfPlayPhaseProcess(new ST1EStartOfTurnGameProcess(_game), _game);
        } else {
            _playOrder.advancePlayer();
            return new ST1EFacilitySeedPhaseProcess(_consecutivePasses, _game);
        }
    }
}