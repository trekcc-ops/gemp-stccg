package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.StartOfTurnGameProcess;

import java.beans.ConstructorProperties;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

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
                cardGame.getActionsEnvironment().getPhaseActions(currentPlayer.getPlayerId());
        if (playableActions.isEmpty() && cardGame.shouldAutoPass(cardGame.getGameState().getCurrentPhase())) {
            _consecutivePasses++;
        } else {
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(currentPlayer, "Play " +
                            cardGame.getGameState().getCurrentPhase() + " action or Pass",
                            playableActions, cardGame) {
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
    public GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException {
        ST1EGame stGame = getST1EGame(cardGame);
        PlayerOrder playerOrder = cardGame.getGameState().getPlayerOrder();
        if (_consecutivePasses >= playerOrder.getPlayerCount()) {
            playerOrder.setCurrentPlayer(playerOrder.getFirstPlayer());

            Collection<Player> players = cardGame.getPlayers();

            ST1EGameState gameState = stGame.getGameState();
            cardGame.takeSnapshot("Start of play phase");

            for (Player player : players) {
                Iterable<PhysicalCard> remainingSeedCards = new LinkedList<>(player.getCardsInHand());
                for (PhysicalCard card : remainingSeedCards) {
                    gameState.removeCardFromZone(card);
                    gameState.addCardToZone(card, Zone.REMOVED);
                }
            }

            for (Player player : players) {
                player.shuffleDrawDeck(cardGame);
                for (int i = 0; i < cardGame.getFormat().getHandSize(); i++) {
                    gameState.playerDrawsCard(player);
                }
            }
            gameState.sendMessage("Players drew starting hands");
            return new StartOfTurnGameProcess();
        } else {
            playerOrder.advancePlayer();
            return new ST1EFacilitySeedPhaseProcess(_consecutivePasses);
        }
    }

}