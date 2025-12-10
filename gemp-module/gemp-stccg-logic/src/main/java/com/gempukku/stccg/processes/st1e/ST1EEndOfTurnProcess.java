package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.draw.DrawSingleCardAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.game.ActionOrder;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.StartOfTurnGameProcess;

import java.util.List;

@JsonTypeName("ST1EEndOfTurnProcess")
public class ST1EEndOfTurnProcess extends ST1EGameProcess {

    public ST1EEndOfTurnProcess() {
        super();
    }

    @Override
    public void process(DefaultGame cardGame) throws PlayerNotFoundException {
        Player player = cardGame.getCurrentPlayer();
        for (PhysicalCard card : Filters.filterCardsInPlay(cardGame, Filters.ship))
            ((ShipCard) card).restoreRange();
        DrawSingleCardAction drawAction = new DrawSingleCardAction(cardGame, player);
        drawAction.processEffect(cardGame);
        cardGame.getActionsEnvironment().logCompletedActionNotInStack(drawAction);
        cardGame.sendActionResultToClient();
        final List<TopLevelSelectableAction> playableActions =
                cardGame.getActionsEnvironment().getPhaseActions(cardGame, player);
        Phase phase = cardGame.getCurrentPhase();
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
        }
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) throws PlayerNotFoundException {
        GameState gameState = cardGame.getGameState();
        gameState.signalEndOfTurn();
        cardGame.setCurrentPhase(Phase.BETWEEN_TURNS);
        Player currentPlayer = cardGame.getCurrentPlayer();
        ActionOrder actionOrder =
                gameState.getPlayerOrder().getClockwisePlayOrder(currentPlayer, false);
        actionOrder.getNextPlayer();

        String nextPlayerId = actionOrder.getNextPlayer();
        Player nextPlayer = cardGame.getPlayer(nextPlayerId);
        cardGame.getGameState().startPlayerTurn(nextPlayer);
        return new StartOfTurnGameProcess();
    }
}