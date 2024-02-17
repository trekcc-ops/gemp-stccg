package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.game.ST1EGame;

import java.util.List;

public class ST1ENormalCardPlayProcess implements GameProcess<ST1EGame> {
    private String _playerId;

    ST1ENormalCardPlayProcess(String playerId) {
        _playerId = playerId;
    }
    @Override
    public void process(ST1EGame game) {
        game.getGameState().sendMessage("DEBUG: Beginning normal card play process");
        game.getGameState().setCurrentPhase(Phase.CARD_PLAY);
        final List<Action> playableActions = game.getActionsEnvironment().getPhaseActions(_playerId);
        if (!playableActions.isEmpty() || !game.shouldAutoPass(_playerId, game.getGameState().getCurrentPhase())) {
            game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardActionSelectionDecision(1, "Play " +
                            game.getGameState().getCurrentPhase().getHumanReadable() + " action or Pass", playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null)
                                game.getActionsEnvironment().addActionToStack(action);
                        }
                    });
        }
    }

    @Override
    public GameProcess getNextProcess() {
        return new ST1EExecuteOrdersPhaseProcess(_playerId);
    }
}
