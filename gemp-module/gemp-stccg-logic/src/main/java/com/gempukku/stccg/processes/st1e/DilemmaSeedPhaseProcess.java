package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.placecard.AddCardsToSeedCardStackAction;
import com.gempukku.stccg.actions.placecard.RemoveCardsFromSeedCardStackAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ActionSelectionDecision;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.decisions.DecisionContext;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class DilemmaSeedPhaseProcess extends SimultaneousGameProcess {

    DilemmaSeedPhaseProcess(Collection<String> playersSelecting) {
        super(playersSelecting);
    }

    @Override
    public void process(DefaultGame cardGame) throws InvalidGameLogicException {
        ST1EGame stGame = getST1EGame(cardGame);
        Collection<String> playerIds = cardGame.getPlayerIds();
        for (String playerId : playerIds) {
            if (_playersParticipating.contains(playerId))
                try {
                    selectMissionToSeedUnder(playerId, stGame);
                } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
                    cardGame.sendErrorMessage(exp);
                }
        }
    }

    abstract List<MissionLocation> getAvailableMissions(ST1EGame stGame, String playerId);

    protected void selectMissionToSeedUnder(String playerId, ST1EGame cardGame)
            throws InvalidGameLogicException, PlayerNotFoundException {
        if (getAvailableMissions(cardGame, playerId).isEmpty()) {
            _playersParticipating.remove(playerId);
        } else {
            ST1EGameState gameState = cardGame.getGameState();
            List<TopLevelSelectableAction> seedActions = new ArrayList<>();
            Player player = gameState.getPlayer(playerId);
            List<MissionLocation> availableMissions = getAvailableMissions(cardGame, playerId);
            for (MissionLocation mission : availableMissions) {
                if (!player.getCardsInGroup(Zone.SEED_DECK).isEmpty()) {
                    TopLevelSelectableAction seedCardsAction = new AddCardsToSeedCardStackAction(cardGame, player, mission);
                    seedActions.add(seedCardsAction);
                }
                if (mission.hasCardsPreSeededByPlayer(player)) {
                    TopLevelSelectableAction removeSeedCardsAction = new RemoveCardsFromSeedCardStackAction(cardGame, player, mission);
                    seedActions.add(removeSeedCardsAction);
                }
            }

            cardGame.getUserFeedback().sendAwaitingDecision(
                    new ActionSelectionDecision(cardGame.getPlayer(playerId),
                            DecisionContext.SELECT_MISSION_FOR_SEED_CARDS, seedActions, cardGame, false) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            TopLevelSelectableAction action = getSelectedAction(result);
                            if (action == null) {
                                _playersParticipating.remove(playerId);
                            } else {
                                if (action instanceof AddCardsToSeedCardStackAction seedCardsAction) {
                                    try {
                                        selectCardsToSeed(player, cardGame, seedCardsAction);
                                    } catch(InvalidGameLogicException exp) {
                                        throw new DecisionResultInvalidException(exp.getMessage());
                                    }
                                } else if (action instanceof RemoveCardsFromSeedCardStackAction seedCardsAction) {
                                    selectCardsToRemove(player, cardGame, seedCardsAction);
                                } else {
                                    throw new DecisionResultInvalidException("Game error - invalid action selected");
                                }
                            }
                        }
                    });
        }
    }

    private void selectCardsToSeed(Player player, ST1EGame cardGame, AddCardsToSeedCardStackAction seedCardsAction)
            throws InvalidGameLogicException {
        Collection<PhysicalCard> availableCards = player.getCardsInGroup(Zone.SEED_DECK);
        cardGame.getUserFeedback().sendAwaitingDecision(
                new CardsSelectionDecision(player, "Select cards to seed under " + seedCardsAction.getLocationName(cardGame),
                        availableCards, cardGame) {
                    @Override
                    public void decisionMade (String result) throws DecisionResultInvalidException {
                        try {
                            Collection<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                            seedCardsAction.setSeedCards(selectedCards);
                            seedCardsAction.processEffect(player, cardGame);
                            cardGame.getActionsEnvironment().logCompletedActionNotInStack(seedCardsAction);
                            cardGame.sendActionResultToClient();
                            selectMissionToSeedUnder(player.getPlayerId(), cardGame);
                        } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
                            throw new DecisionResultInvalidException(exp.getMessage());
                        }
                    }
                });
    }


    private void selectCardsToRemove(Player player, ST1EGame cardGame, RemoveCardsFromSeedCardStackAction removeAction) {
        Collection<PhysicalCard> availableCards;
        MissionLocation mission = removeAction.getLocation();
        availableCards = mission.getPreSeedCardsForPlayer(player);
        cardGame.getUserFeedback().sendAwaitingDecision(
                new ArbitraryCardsSelectionDecision(player, "Select cards to remove from " + mission.getLocationName(),
                        availableCards, cardGame) {
                    @Override
                    public void decisionMade (String result) throws DecisionResultInvalidException {
                        Collection<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                        try {
                            removeAction.setCardsToRemove(selectedCards);
                            removeAction.processEffect(player, cardGame);
                            cardGame.getActionsEnvironment().logCompletedActionNotInStack(removeAction);
                            cardGame.sendActionResultToClient();
                            selectMissionToSeedUnder(player.getPlayerId(), cardGame);
                        } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
                            cardGame.sendErrorMessage(exp);
                        }
                    }
                });
    }

}