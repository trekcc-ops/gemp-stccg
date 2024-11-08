package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.GameUtils;

import java.util.List;

public class ST1EPlayPhaseSegmentProcess extends ST1EGameProcess {

    ST1EPlayPhaseSegmentProcess(ST1EGame game) {
        super(game.getCurrentPlayer(), game);
    }

    @Override
    public void process() {
        String currentPlayerId = _game.getCurrentPlayerId();
        ST1EGame thisGame = _game; // To avoid conflict when decision calls "_game"
        final List<Action> playableActions = _game.getActionsEnvironment().getPhaseActions(currentPlayerId);
        if (!playableActions.isEmpty() || !_game.shouldAutoPass(_game.getGameState().getCurrentPhase())) {
            _game.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(_game.getCurrentPlayer(), "Play " +
                            _game.getCurrentPhaseString() + " action or Pass", playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            if ("revert".equalsIgnoreCase(result)) {
                                GameUtils.performRevert(thisGame, currentPlayerId);
                            } else {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    thisGame.getActionsEnvironment().addActionToStack(action);
                                } else {
                                    _playersParticipating.remove(currentPlayerId);
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public GameProcess getNextProcess() {
        return (_playersParticipating.isEmpty()) ? new ST1EEndOfPlayPhaseSegmentProcess(_game) :
                new ST1EPlayPhaseSegmentProcess(_game);

    }

}