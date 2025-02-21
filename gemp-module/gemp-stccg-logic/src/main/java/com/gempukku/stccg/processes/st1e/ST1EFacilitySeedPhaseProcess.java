package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.draw.DrawMultipleCardsUnrespondableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.StartOfTurnGameProcess;

import java.beans.ConstructorProperties;
import java.util.Collection;
import java.util.Collections;
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
        if (playableActions.isEmpty() && cardGame.shouldAutoPass(cardGame.getGameState().getCurrentPhase())) {
            _consecutivePasses++;
        } else {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(currentPlayer, "Play " +
                            cardGame.getGameState().getCurrentPhase() + " action or Pass",
                            playableActions, cardGame) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            try {
                                Action action = getSelectedAction(result);
                                if (action != null) {
                                    _consecutivePasses = 0;
                                    cardGame.getActionsEnvironment().addActionToStack(action);
                                } else {
                                    _consecutivePasses++;
                                }
                            } catch(InvalidGameLogicException exp) {
                                throw new DecisionResultInvalidException(exp.getMessage());
                            }
                        }
                    });
        }
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        ST1EGame stGame = getST1EGame(cardGame);
        PlayerOrder playerOrder = cardGame.getGameState().getPlayerOrder();
        if (_consecutivePasses >= playerOrder.getPlayerCount()) {
            playerOrder.setCurrentPlayer(playerOrder.getFirstPlayer());

            Collection<Player> players = cardGame.getPlayers();

            ST1EGameState gameState = stGame.getGameState();

            for (Player player : players) {
                Iterable<PhysicalCard> remainingSeedCards = new LinkedList<>(player.getCardsInHand());
                for (PhysicalCard card : remainingSeedCards) {
                    gameState.removeCardsFromZone(cardGame, card.getOwner(), Collections.singleton(card));
                    gameState.addCardToZone(card, Zone.REMOVED, true);
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
            return new StartOfTurnGameProcess();
        } else {
            playerOrder.advancePlayer();
            return new ST1EFacilitySeedPhaseProcess(_consecutivePasses);
        }
    }

}