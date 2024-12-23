package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.GameUtils;

import java.util.List;

public class ST1EPlayPhaseSegmentProcess extends ST1EGameProcess {

    public ST1EPlayPhaseSegmentProcess(ST1EGame game) {
        super(game);
    }

    @Override
    public void process() {
        Phase phase = _game.getCurrentPhase();
        String currentPlayerId = _game.getCurrentPlayerId();
        ST1EGame thisGame = _game; // To avoid conflict when decision calls "_game"
        final List<Action> playableActions = _game.getActionsEnvironment().getPhaseActions(currentPlayerId);
        if (!playableActions.isEmpty() || !_game.shouldAutoPass(phase)) {
            _game.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(_game.getCurrentPlayer(), "Play " + phase + " action or Pass",
                            playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            if ("revert".equalsIgnoreCase(result)) {
                                GameUtils.performRevert(thisGame, currentPlayerId);
                            } else {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    thisGame.getActionsEnvironment().addActionToStack(action);
                                } else {
                                    _consecutivePasses++;
                                }
                            }
                        }
                    });
        } else {
            _consecutivePasses++;
        }
    }

    @Override
    public GameProcess getNextProcess() {
        GameProcess result;
        if (_consecutivePasses > 0) {
            Phase phase = _game.getCurrentPhase();
            String message = "End of " + phase + " phase";
            _game.sendMessage(message);
            result = switch (phase) {
                case CARD_PLAY -> {
                    _game.getGameState().setCurrentPhase(Phase.EXECUTE_ORDERS);
                    message = "Start of " + Phase.EXECUTE_ORDERS + " phase";
                    _game.sendMessage("\n" + message);
                    yield new ST1EPlayPhaseSegmentProcess(_game);
                }
                case EXECUTE_ORDERS -> {
                    _game.getGameState().setCurrentPhase(Phase.END_OF_TURN);
                    yield new ST1EEndOfTurnProcess(_game);
                }
                case null, default -> throw new RuntimeException(
                        "End of play phase segment process reached without being in a valid play phase segment");
            };
        } else {
            result = new ST1EPlayPhaseSegmentProcess(_game);
        }
        return result;
    }

}