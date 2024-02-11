package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.game.PlayerOrder;
import com.gempukku.stccg.game.ST1EGame;

import java.util.List;

public class ST1EFacilitySeedPhaseProcess implements GameProcess<ST1EGame> {

    private PlayerOrder _playOrder;
    private int _consecutivePasses;
    private ST1EGame _game;
    public ST1EFacilitySeedPhaseProcess(int consecutivePasses) {
        _consecutivePasses = consecutivePasses;
    }

    @Override
    public void process(ST1EGame game) {
        _game = game;
        _playOrder = game.getGameState().getPlayerOrder();
        String _currentPlayer = _playOrder.getCurrentPlayer();
        _game.getGameState().sendMessage("DEBUG: Beginning facility seed phase process");

        final List<Action> playableActions = game.getActionsEnvironment().getPhaseActions(_currentPlayer);
        if (playableActions.isEmpty() && game.shouldAutoPass(_currentPlayer, game.getGameState().getCurrentPhase())) {
            _consecutivePasses++;
        } else {
            game.getUserFeedback().sendAwaitingDecision(_currentPlayer,
                    new CardActionSelectionDecision(1, "Play " +
                            game.getGameState().getCurrentPhase().getHumanReadable() + " action or Pass", playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                _consecutivePasses = 0;
                                game.getActionsEnvironment().addActionToStack(action);
                            } else {
                                _consecutivePasses++;
                                _game.getGameState().sendMessage("DEBUG: Incrementing consecutivePasses");
                            }
                        }
                    });
        }
    }

    @Override
    public GameProcess<ST1EGame> getNextProcess() {
        if (_consecutivePasses >= _playOrder.getPlayerCount()) {
            _game.getGameState().sendMessage("DEBUG: Exiting facility seed phase process");
            _playOrder.setCurrentPlayer(_playOrder.getFirstPlayer());
            return new ST1EStartOfPlayPhaseProcess(_playOrder.getFirstPlayer());
        } else {
            _playOrder.advancePlayer();
            return new ST1EFacilitySeedPhaseProcess(_consecutivePasses);
        }
    }
}