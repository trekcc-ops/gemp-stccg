package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.discard.RemoveCardFromPlayAction;
import com.gempukku.stccg.actions.draw.DrawMultipleCardsUnrespondableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.StartOfTurnGameProcess;

import java.beans.ConstructorProperties;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

@JsonTypeName("ST1EFacilitySeedPhaseProcess")
public class ST1EFacilitySeedPhaseProcess extends ST1EGameProcess {

    @ConstructorProperties({"consecutivePasses"})
    public ST1EFacilitySeedPhaseProcess(int consecutivePasses) {
        super();
        _consecutivePasses = consecutivePasses;
    }

    @Override
    public void process(DefaultGame cardGame) throws PlayerNotFoundException {
        Player currentPlayer = cardGame.getCurrentPlayer();

        final List<TopLevelSelectableAction> playableActions =
                cardGame.getActionsEnvironment().getPhaseActions(cardGame, currentPlayer);
        if (playableActions.isEmpty() && cardGame.shouldAutoPass(cardGame.getGameState().getCurrentPhase(),currentPlayer.getPlayerId() )) {
            _consecutivePasses++;
        } else {
            cardGame.sendAwaitingDecision(
                    new ActionSelectionDecision(currentPlayer, DecisionContext.SELECT_PHASE_ACTION,
                            playableActions, cardGame, false) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                _consecutivePasses = 0;
                                cardGame.getActionsEnvironment().addActionToStack(action);
                            } else {
                                _consecutivePasses++;
                            }
                        }
                    });
        }
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        PlayerOrder playerOrder = cardGame.getGameState().getPlayerOrder();
        if (_consecutivePasses >= playerOrder.getPlayerCount()) {
            playerOrder.setCurrentPlayer(playerOrder.getFirstPlayer());

            Collection<Player> players = cardGame.getPlayers();

            for (Player player : players) {
                Iterable<PhysicalCard> remainingSeedCards = new LinkedList<>(player.getCardsInGroup(Zone.SEED_DECK));
                for (PhysicalCard card : remainingSeedCards) {
                    RemoveCardFromPlayAction removeAction =
                            new RemoveCardFromPlayAction(cardGame, card.getOwnerName(), card);
                    removeAction.processEffect(cardGame);
                    cardGame.getActionsEnvironment().logCompletedActionNotInStack(removeAction);
                    cardGame.sendActionResultToClient();
                }
            }

            for (Player player : players) {
                player.shuffleDrawDeck(cardGame);
                int cardsToDraw = cardGame.getFormat().getHandSize();
                DrawMultipleCardsUnrespondableAction drawAction =
                        new DrawMultipleCardsUnrespondableAction(cardGame, player, cardsToDraw);
                drawAction.processEffect(cardGame);
                cardGame.getActionsEnvironment().logCompletedActionNotInStack(drawAction);
                cardGame.sendActionResultToClient();
            }
            String firstPlayerId = playerOrder.getFirstPlayer();
            cardGame.getGameState().startPlayerTurn(cardGame.getPlayer(firstPlayerId));
            return new StartOfTurnGameProcess();
        } else {
            playerOrder.advancePlayer();
            return new ST1EFacilitySeedPhaseProcess(_consecutivePasses);
        }
    }

}