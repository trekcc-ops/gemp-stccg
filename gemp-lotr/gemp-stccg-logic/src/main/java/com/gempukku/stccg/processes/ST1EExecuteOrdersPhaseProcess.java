package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.game.ST1EGame;

import java.util.List;

public class ST1EExecuteOrdersPhaseProcess implements GameProcess<ST1EGame> {
    private final String _playerId;
    private GameProcess<ST1EGame> _nextProcess;

    ST1EExecuteOrdersPhaseProcess(String playerId) {
        _playerId = playerId;
    }
    @Override
    public void process(ST1EGame game) {
        game.getGameState().sendMessage("DEBUG: Execute orders phase.");
        game.getGameState().setCurrentPhase(Phase.EXECUTE_ORDERS);
        final List<Action> playableActions = game.getActionsEnvironment().getPhaseActions(_playerId);
        if (!playableActions.isEmpty() || !game.shouldAutoPass(_playerId, game.getGameState().getCurrentPhase())) {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardActionSelectionDecision(1, "Play " +
                            game.getGameState().getCurrentPhase().getHumanReadable() + " action or Pass", playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                _nextProcess = new ST1EExecuteOrdersPhaseProcess(_playerId);
                                game.getActionsEnvironment().addActionToStack(action);
                            } else
                                _nextProcess = new ST1EEndOfTurnProcess(_playerId);
                        }
                    });
        }
    }

    @Override
    public GameProcess<ST1EGame> getNextProcess() { return _nextProcess; }
}
