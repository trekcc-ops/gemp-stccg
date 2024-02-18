package com.gempukku.stccg.processes;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import com.gempukku.stccg.game.ST1EGame;

import java.util.List;

public class ST1ENormalCardPlayProcess extends ST1EGameProcess {
    private final String _playerId;

    ST1ENormalCardPlayProcess(String playerId, ST1EGame game) {
        super(game);
        _playerId = playerId;
    }
    @Override
    public void process() {
        _game.getGameState().sendMessage("DEBUG: Beginning normal card play process");
        _game.getGameState().setCurrentPhase(Phase.CARD_PLAY);
        final List<Action> playableActions = _game.getActionsEnvironment().getPhaseActions(_playerId);
        if (!playableActions.isEmpty() || !_game.shouldAutoPass(_playerId, _game.getGameState().getCurrentPhase())) {
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardActionSelectionDecision(1, "Play " +
                            _game.getGameState().getCurrentPhase().getHumanReadable() + " action or Pass", playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null)
                                _game.getActionsEnvironment().addActionToStack(action);
                        }
                    });
        }
    }

    @Override
    public GameProcess getNextProcess() {
        return new ST1EExecuteOrdersPhaseProcess(_playerId, _game);
    }
}
