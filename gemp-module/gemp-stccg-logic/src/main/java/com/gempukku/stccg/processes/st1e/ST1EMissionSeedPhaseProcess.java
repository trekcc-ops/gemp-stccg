package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.GameProcess;

import java.beans.ConstructorProperties;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@JsonTypeName("ST1EMissionSeedPhaseProcess")
public class ST1EMissionSeedPhaseProcess extends ST1EGameProcess {

    public ST1EMissionSeedPhaseProcess() {
        super(0);
    }

    @ConstructorProperties({"consecutivePasses"})
    public ST1EMissionSeedPhaseProcess(int consecutivePasses) {
        super(consecutivePasses);
    }

    @Override
    public void process(DefaultGame cardGame) throws InvalidGameLogicException, PlayerNotFoundException {
        Player currentPlayer = cardGame.getCurrentPlayer();

        final List<TopLevelSelectableAction> playableActions =
                cardGame.getActionsEnvironment().getPhaseActions(cardGame, currentPlayer);
        ST1EGameState gameState = getST1EGame(cardGame).getGameState();
        Phase currentPhase = gameState.getCurrentPhase();

        if (playableActions.isEmpty() && cardGame.shouldAutoPass(currentPhase)) {
            _consecutivePasses++;
        } else {
            String message = "Play " + currentPhase + " action";
            cardGame.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(currentPlayer, message, playableActions, true, cardGame) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            try {
                                if ("revert".equalsIgnoreCase(result))
                                    cardGame.performRevert(currentPlayer);
                                Action action = getSelectedAction(result);
                                cardGame.getActionsEnvironment().addActionToStack(action);
                            } catch(InvalidGameLogicException exp) {
                                throw new DecisionResultInvalidException(exp.getMessage());
                            }
                        }
                    });
        }
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException {
        ST1EGame stGame = getST1EGame(cardGame);
        PlayerOrder playerOrder = cardGame.getGameState().getPlayerOrder();

        // Check if any missions are left to be seeded
        boolean areAllMissionsSeeded = true;
        for (Player player : cardGame.getPlayers()) {
            if (!cardGame.getGameState().getZoneCards(player, Zone.HAND).isEmpty())
                areAllMissionsSeeded = false;
        }

        if (areAllMissionsSeeded) {
            playerOrder.setCurrentPlayer(playerOrder.getFirstPlayer());
            ST1EGameState gameState = getST1EGame(cardGame).getGameState();
            cardGame.setCurrentPhase(Phase.SEED_DILEMMA);
            for (Player player : cardGame.getPlayers()) {
                List<PhysicalCard> remainingSeeds = new LinkedList<>(player.getCardsInGroup(Zone.SEED_DECK));
                for (PhysicalCard card : remainingSeeds) {
                    gameState.removeCardsFromZone(cardGame, player, List.of(card));
                    gameState.addCardToZone(card, Zone.HAND);
                }
            }
            cardGame.takeSnapshot("Start of dilemma seed phase");
            return new DilemmaSeedPhaseOpponentsMissionsProcess(stGame);
        } else {
            playerOrder.advancePlayer();
            return new ST1EMissionSeedPhaseProcess(_consecutivePasses);
        }
    }

}