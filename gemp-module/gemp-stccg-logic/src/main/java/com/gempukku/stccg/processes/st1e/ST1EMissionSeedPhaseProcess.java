package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.player.PlayerOrder;
import com.gempukku.stccg.processes.GameProcess;

import java.beans.ConstructorProperties;
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

        if (playableActions.isEmpty() && cardGame.shouldAutoPass(currentPhase, currentPlayer.getPlayerId())) {
            _consecutivePasses++;
        } else {
            cardGame.sendAwaitingDecision(
                    new ActionSelectionDecision(currentPlayer, DecisionContext.SELECT_PHASE_ACTION,
                            playableActions, cardGame, true) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            cardGame.getActionsEnvironment().addActionToStack(action);
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
            if (!cardGame.getGameState().getZoneCards(player, Zone.MISSIONS_PILE).isEmpty())
                areAllMissionsSeeded = false;
        }

        if (areAllMissionsSeeded) {
            playerOrder.setCurrentPlayer(playerOrder.getFirstPlayer());
            cardGame.setCurrentPhase(Phase.SEED_DILEMMA);
            cardGame.sendActionResultToClient();
            return new DilemmaSeedPhaseOpponentsMissionsProcess(stGame);
        } else {
            playerOrder.advancePlayer();
            return new ST1EMissionSeedPhaseProcess(_consecutivePasses);
        }
    }

}