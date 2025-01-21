package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.PlayerOrder;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.StartOfTurnGameProcess;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class ST1EFacilitySeedPhaseProcess extends ST1EGameProcess {

    public ST1EFacilitySeedPhaseProcess(int consecutivePasses, ST1EGame game) {
        super(game);
        _consecutivePasses = consecutivePasses;
    }

    @Override
    public void process(DefaultGame cardGame) {
        String _currentPlayer = _game.getCurrentPlayerId();

        final List<TopLevelSelectableAction> playableActions =
                _game.getActionsEnvironment().getPhaseActions(_currentPlayer);
        if (playableActions.isEmpty() && _game.shouldAutoPass(_game.getGameState().getCurrentPhase())) {
            _consecutivePasses++;
        } else {
            DefaultGame thisGame = _game;
            _game.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(_game.getPlayer(_currentPlayer), "Play " +
                            _game.getGameState().getCurrentPhase() + " action or Pass",
                            playableActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action != null) {
                                _consecutivePasses = 0;
                                thisGame.getActionsEnvironment().addActionToStack(action);
                            } else {
                                _consecutivePasses++;
                            }
                        }
                    });
        }
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) {
        PlayerOrder playerOrder = _game.getGameState().getPlayerOrder();
        if (_consecutivePasses >= playerOrder.getPlayerCount()) {
            playerOrder.setCurrentPlayer(playerOrder.getFirstPlayer());

            Set<String> playerIds = _game.getPlayerIds();

            ST1EGameState gameState = _game.getGameState();
            _game.takeSnapshot("Start of play phase");

            for (String playerId : playerIds) {
                Iterable<PhysicalCard> remainingSeedCards = new LinkedList<>(gameState.getHand(playerId));
                for (PhysicalCard card : remainingSeedCards) {
                    gameState.removeCardFromZone(card);
                    gameState.addCardToZone(card, Zone.REMOVED);
                }
            }

            for (String playerId : playerIds) {
                gameState.shuffleDeck(playerId);
                for (int i = 0; i < _game.getFormat().getHandSize(); i++) {
                    gameState.playerDrawsCard(playerId);
                }
            }
            gameState.sendMessage("Players drew starting hands");
            return new StartOfTurnGameProcess();
        } else {
            playerOrder.advancePlayer();
            return new ST1EFacilitySeedPhaseProcess(_consecutivePasses, _game);
        }
    }

}