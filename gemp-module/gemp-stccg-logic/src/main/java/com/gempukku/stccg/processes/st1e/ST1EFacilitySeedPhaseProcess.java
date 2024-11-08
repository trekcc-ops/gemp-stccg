package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayerOrder;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;

import java.util.List;

public class ST1EFacilitySeedPhaseProcess extends ST1EGameProcess {

    private int _consecutivePasses;
    public ST1EFacilitySeedPhaseProcess(int consecutivePasses, ST1EGame game) {
        super(game.getCurrentPlayer(), game);
        _consecutivePasses = consecutivePasses;
    }

    @Override
    public void process() {
        String _currentPlayer = _game.getCurrentPlayerId();

        final List<Action> playableActions = _game.getActionsEnvironment().getPhaseActions(_currentPlayer);
        if (playableActions.isEmpty() && _game.shouldAutoPass(_game.getGameState().getCurrentPhase())) {
            _consecutivePasses++;
        } else {
            DefaultGame thisGame = _game;
            _game.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(_game.getPlayer(_currentPlayer), "Play " +
                            _game.getGameState().getCurrentPhase().getHumanReadable() + " action or Pass",
                            playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                _consecutivePasses = 0;
                                thisGame.getActionsEnvironment().addActionToStack(action);
                            } else {
                                _consecutivePasses++;
                            }
                        }
                    });
        }
    }

    @Override
    public GameProcess getNextProcess() {
        PlayerOrder playerOrder = _game.getGameState().getPlayerOrder();
        if (_consecutivePasses >= playerOrder.getPlayerCount()) {
            playerOrder.setCurrentPlayer(playerOrder.getFirstPlayer());
            return new ST1EStartOfPlayPhaseProcess(_game);
        } else {
            playerOrder.advancePlayer();
            return new ST1EFacilitySeedPhaseProcess(_consecutivePasses, _game);
        }
    }

    public int getConsecutivePasses() { return _consecutivePasses; }

}