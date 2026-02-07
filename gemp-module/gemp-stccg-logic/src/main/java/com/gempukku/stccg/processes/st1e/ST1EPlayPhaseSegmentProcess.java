package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.processes.GameProcess;

import java.util.List;

@JsonTypeName("ST1EPlayPhaseSegmentProcess")
public class ST1EPlayPhaseSegmentProcess extends ST1EGameProcess {

    public ST1EPlayPhaseSegmentProcess() {
        super();
    }

    @Override
    public void process(DefaultGame cardGame) throws PlayerNotFoundException {
        Phase phase = cardGame.getCurrentPhase();
        Player currentPlayer = cardGame.getCurrentPlayer();
        final List<TopLevelSelectableAction> playableActions =
                cardGame.getActionsEnvironment().getPhaseActions(cardGame, currentPlayer);
        if (!playableActions.isEmpty() || !cardGame.shouldAutoPass(phase)) {
            cardGame.sendAwaitingDecision(
                    new ActionSelectionDecision(cardGame.getCurrentPlayer(), DecisionContext.SELECT_PHASE_ACTION,
                            playableActions, cardGame, false) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                cardGame.getActionsEnvironment().addActionToStack(action);
                            } else {
                                _consecutivePasses++;
                            }
                        }
                    });
        } else {
            _consecutivePasses++;
        }
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException {
        GameProcess result;
        if (_consecutivePasses > 0) {
            Phase phase = cardGame.getCurrentPhase();
            result = switch (phase) {
                case CARD_PLAY -> {
                    cardGame.setCurrentPhase(Phase.EXECUTE_ORDERS);
                    yield new ST1EPlayPhaseSegmentProcess();
                }
                case EXECUTE_ORDERS -> {
                    cardGame.setCurrentPhase(Phase.END_OF_TURN);
                    yield new ST1EEndOfTurnProcess();
                }
                case null, default -> throw new InvalidGameLogicException(
                        "End of play phase segment process reached without being in a valid play phase segment");
            };
        } else {
            result = new ST1EPlayPhaseSegmentProcess();
        }
        return result;
    }

}