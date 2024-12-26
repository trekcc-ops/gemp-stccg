package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerOrder;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.game.GameUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ST1EMissionSeedPhaseProcess extends ST1EGameProcess {

    public ST1EMissionSeedPhaseProcess(ST1EGame game) {
        this(0, game);
    }

    public ST1EMissionSeedPhaseProcess(int consecutivePasses, ST1EGame game) {
        super(consecutivePasses, game);
    }

    @Override
    public void process(DefaultGame cardGame) {
        String _currentPlayer = _game.getCurrentPlayerId();

        final List<Action> playableActions = _game.getActionsEnvironment().getPhaseActions(_currentPlayer);
        ST1EGameState gameState = _game.getGameState();
        Phase currentPhase = gameState.getCurrentPhase();

        if (playableActions.isEmpty() && _game.shouldAutoPass(currentPhase)) {
            _consecutivePasses++;
        } else {
            DefaultGame thisGame = _game;
            String message = "Play " + currentPhase + " action";
            Player player = _game.getCurrentPlayer();
            _game.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(player, message, playableActions, true) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            if ("revert".equalsIgnoreCase(result))
                                GameUtils.performRevert(thisGame, _currentPlayer);
                            Action action = getSelectedAction(result);
                            thisGame.getActionsEnvironment().addActionToStack(action);
                        }
                    });
        }
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) {
        PlayerOrder playerOrder = _game.getGameState().getPlayerOrder();

        // Check if any missions are left to be seeded
        boolean areAllMissionsSeeded = true;
        for (Player player : _game.getPlayers()) {
            if (!_game.getGameState().getZoneCards(player.getPlayerId(), Zone.HAND).isEmpty())
                areAllMissionsSeeded = false;
        }

        if (areAllMissionsSeeded) {
            playerOrder.setCurrentPlayer(playerOrder.getFirstPlayer());
            ST1EGameState gameState = _game.getGameState();
            gameState.setCurrentPhase(Phase.SEED_DILEMMA);
            for (String player : _game.getPlayerIds()) {
                List<PhysicalCard> remainingSeeds = new LinkedList<>(gameState.getSeedDeck(player));
                for (PhysicalCard card : remainingSeeds) {
                    gameState.removeCardsFromZone(player, Collections.singleton(card));
                    gameState.addCardToZone(card, Zone.HAND);
                }
            }
            _game.takeSnapshot("Start of dilemma seed phase");
            return new DilemmaSeedPhaseOpponentsMissionsProcess(_game);
        } else {
            playerOrder.advancePlayer();
            return new ST1EMissionSeedPhaseProcess(_consecutivePasses, _game);
        }
    }

}