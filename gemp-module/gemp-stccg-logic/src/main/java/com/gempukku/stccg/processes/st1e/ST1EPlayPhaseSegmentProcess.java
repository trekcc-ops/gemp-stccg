package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.GameUtils;

import java.util.List;

public class ST1EPlayPhaseSegmentProcess extends ST1EGameProcess {
    private final String _playerId;
    private ST1EGameProcess _nextProcess;

    ST1EPlayPhaseSegmentProcess(String playerId, ST1EGame game) {
        super(game);
        _playerId = playerId;
    }

    ST1EPlayPhaseSegmentProcess(ST1EGame game) {
        super(game);
        _playerId = game.getCurrentPlayerId();
    }

    @Override
    public void process() {
        final List<Action> playableActions = _game.getActionsEnvironment().getPhaseActions(_playerId);
        if (!playableActions.isEmpty() || !_game.shouldAutoPass(_game.getGameState().getCurrentPhase())) {
            _game.getUserFeedback().sendAwaitingDecision(_playerId,
                    new CardActionSelectionDecision(1, "Play " +
                            _game.getCurrentPhaseString() + " action or Pass", playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            if ("revert".equalsIgnoreCase(result)) {
                                GameUtils.performRevert(_game, _playerId);
                            } else {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    // TODO SNAPSHOT
                                    // Take game snapshot before top-level action performed
//                                    String snapshotSourceCardInfo = action.getActionSource() != null ?
//                                            (": " + action.getActionSource().getCardLink()) : "";
//                                    _game.takeSnapshot(_playerId + ": " + action.getText() +
//                                            snapshotSourceCardInfo);

                                    _nextProcess = new ST1EPlayPhaseSegmentProcess(_playerId, _game);
                                    _game.getActionsEnvironment().addActionToStack(action);
                                } else {
                                    _nextProcess = new ST1EEndOfPlayPhaseSegmentProcess(_game);
                                }
                            }
                        }
                    });
        }
    }

    @Override
    public GameProcess getNextProcess() { return _nextProcess; }

}