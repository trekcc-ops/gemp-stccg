package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.game.ST1EGame;

import java.util.List;

public class ST1EExecuteOrdersPhaseProcess extends ST1EGameProcess {
    private final String _playerId;
    private ST1EGameProcess _nextProcess;

    ST1EExecuteOrdersPhaseProcess(String playerId, ST1EGame game) {
        super(game);
        _playerId = playerId;
    }
    @Override
    public void process() {
        _game.getGameState().sendMessage("DEBUG: Execute orders phase.");
        _game.getGameState().setCurrentPhase(Phase.EXECUTE_ORDERS);
        final List<Action> playableActions = _game.getActionsEnvironment().getPhaseActions(_playerId);
        if (!playableActions.isEmpty() || !_game.shouldAutoPass(_playerId, _game.getGameState().getCurrentPhase())) {
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardActionSelectionDecision(1, "Play " +
                            _game.getGameState().getCurrentPhase().getHumanReadable() + " action or Pass", playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                _nextProcess = new ST1EExecuteOrdersPhaseProcess(_playerId, _game);
                                _game.getActionsEnvironment().addActionToStack(action);
                            } else
                                _nextProcess = new ST1EEndOfTurnProcess(_playerId, _game);
                        }
                    });
        }
    }

    @Override
    public GameProcess getNextProcess() { return _nextProcess; }
}
