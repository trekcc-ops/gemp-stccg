package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.StartOfTurnGameProcess;

import java.util.List;

@JsonTypeName("ST1EEndOfTurnProcess")
public class ST1EEndOfTurnProcess extends ST1EGameProcess {

    public ST1EEndOfTurnProcess() {
        super();
    }

    @Override
    public void process(DefaultGame cardGame) {
        String playerId = cardGame.getCurrentPlayerId();
        for (PhysicalCard card : Filters.filterActive(cardGame, Filters.ship))
            ((PhysicalShipCard) card).restoreRange();
        cardGame.getGameState().playerDrawsCard(playerId);
        cardGame.sendMessage(playerId + " drew their normal end-of-turn card draw");
        final List<TopLevelSelectableAction> playableActions =
                cardGame.getActionsEnvironment().getPhaseActions(playerId);
        Phase phase = cardGame.getCurrentPhase();
        if (!playableActions.isEmpty() || !cardGame.shouldAutoPass(phase)) {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(cardGame.getCurrentPlayer(),
                            "Play " + phase + " action or Pass", playableActions, cardGame) {
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
        }
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) {
        cardGame.getModifiersEnvironment().signalEndOfTurn(); // Remove "until end of turn" modifiers
        cardGame.getActionsEnvironment().signalEndOfTurn(); // Remove "until end of turn" permitted actions
        cardGame.getGameState().sendMessage(cardGame.getCurrentPlayerId() + " ended their turn");
        cardGame.getGameState().setCurrentPhase(Phase.BETWEEN_TURNS);
        String playerId = cardGame.getGameState().getCurrentPlayerId();
        ActionOrder actionOrder = cardGame.getGameState().getPlayerOrder().getClockwisePlayOrder(playerId, false);
        actionOrder.getNextPlayer();

        String nextPlayer = actionOrder.getNextPlayer();
        cardGame.getGameState().startPlayerTurn(nextPlayer);
        return new StartOfTurnGameProcess();
    }
}