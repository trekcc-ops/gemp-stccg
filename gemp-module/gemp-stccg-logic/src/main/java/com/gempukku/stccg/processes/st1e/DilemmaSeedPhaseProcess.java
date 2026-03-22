package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.placecard.AddCardsToPreseedStackAction;
import com.gempukku.stccg.actions.placecard.RemoveCardsFromPreseedCardStackAction;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.*;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.*;

public abstract class DilemmaSeedPhaseProcess extends SimultaneousGameProcess {

    protected final DilemmaSeedPhaseType _type;

    DilemmaSeedPhaseProcess(Collection<String> playersSelecting, DilemmaSeedPhaseType type) {
        super(playersSelecting);
        _type = type;
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
            List<Action> seedActions = new ArrayList<>();
            Player player = gameState.getPlayer(playerId);
            List<MissionLocation> availableMissions = getAvailableMissions(cardGame, playerId);
            for (MissionLocation mission : availableMissions) {
                Collection<PhysicalCard> seedableCards = getCardsThatCanBeSeededUnderMission(player, cardGame, mission);
                if (!seedableCards.isEmpty()) {
                    Action seedCardsAction = new AddCardsToPreseedStackAction(cardGame, player, mission);
                    seedActions.add(seedCardsAction);
                }
                if (mission.hasCardsPreSeededByPlayer(player)) {
                    Action removeSeedCardsAction = new RemoveCardsFromPreseedCardStackAction(cardGame, player, mission);
                    seedActions.add(removeSeedCardsAction);
                }
            }

            DecisionContext decisionContext = switch(_type) {
                case YOUR_MISSION -> DecisionContext.SELECT_YOUR_MISSION_FOR_SEED_CARDS;
                case OPPONENT_MISSION -> DecisionContext.SELECT_OPPONENT_MISSION_FOR_SEED_CARDS;
                case SHARED_MISSION -> DecisionContext.SELECT_SHARED_MISSION_FOR_SEED_CARDS;
            };

            cardGame.sendAwaitingDecision(
                    new ActionSelectionDecision(cardGame.getPlayer(playerId),
                            decisionContext, seedActions, cardGame, false) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action == null) {
                                _playersParticipating.remove(playerId);
                            } else {
                                if (action instanceof AddCardsToPreseedStackAction seedCardsAction) {
                                    try {
                                        selectCardsToSeed(player, cardGame, seedCardsAction);
                                    } catch(InvalidGameLogicException exp) {
                                        throw new DecisionResultInvalidException(exp.getMessage());
                                    }
                                } else if (action instanceof RemoveCardsFromPreseedCardStackAction seedCardsAction) {
                                    selectCardsToRemove(player, cardGame, seedCardsAction);
                                } else {
                                    throw new DecisionResultInvalidException("Game error - invalid action selected");
                                }
                            }
                        }
                    });
        }
    }

    private Collection<PhysicalCard> getCardsThatCanBeSeededUnderMission(Player player, ST1EGame cardGame,
                                                                         MissionLocation location) {
        Collection<PhysicalCard> availableCards = new ArrayList<>();
        if (cardGame.getFormat().misSeedsAllowed()) {
            availableCards.addAll(player.getSeedDeckCards());
        } else {
            Collection<PhysicalCard> existingPreSeeds = new ArrayList<>();
            if (location != null) {
                existingPreSeeds.addAll(location.getPreSeedCardsForPlayer(player));
            }
            for (PhysicalCard card : player.getCardsInGroup(Zone.SEED_DECK_FOR_DILEMMA_PHASE)) {
                boolean addToAvailable = true;
                if (location == null) {
                    addToAvailable = false;
                } else if (card.getCardType() != CardType.DILEMMA && card.getCardType() != CardType.ARTIFACT) {
                    addToAvailable = false;
                } else {
                    if (card.getBlueprint().getMissionType() == MissionType.PLANET && !location.isPlanet()) {
                        addToAvailable = false;
                    } else if (card.getBlueprint().getMissionType() == MissionType.SPACE && !location.isSpace()) {
                        addToAvailable = false;
                    } else {
                        for (PhysicalCard seededCard : existingPreSeeds) {
                            if (seededCard.isCopyOf(card)) {
                                addToAvailable = false;
                            }
                        }
                    }
                }
                if (addToAvailable) {
                    availableCards.add(card);
                }
            }
        }
        return availableCards;
    }

    private void selectCardsToSeed(Player player, ST1EGame cardGame, AddCardsToPreseedStackAction seedCardsAction)
            throws InvalidGameLogicException {
        MissionLocation location = seedCardsAction.getLocation(cardGame);
        Collection<PhysicalCard> availableCards = getCardsThatCanBeSeededUnderMission(player, cardGame, location);

        Map<PhysicalCard, List<PhysicalCard>> validCombinations = new HashMap<>();
        for (PhysicalCard card : availableCards) {
            List<PhysicalCard> combos = new ArrayList<>();
            for (PhysicalCard comboCard : availableCards) {
                if (!comboCard.isCopyOf(card)) {
                    combos.add(comboCard);
                }
            }
            validCombinations.put(card, combos);
        }

        String message = "Select cards to seed under " + seedCardsAction.getLocationName(cardGame);
        AwaitingDecision decision = new ArbitraryCardsSelectionDecision(player.getPlayerId(), message,
                availableCards, validCombinations, 0, availableCards.size(), cardGame) {
            @Override
            public void decisionMade(String result) throws DecisionResultInvalidException {
                try {
                    Collection<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                    seedCardsAction.setSeedCards(selectedCards);
                    seedCardsAction.processEffect(cardGame);
                    cardGame.getActionsEnvironment().logCompletedActionNotInStack(seedCardsAction);
                    cardGame.sendActionResultToClient();
                    selectMissionToSeedUnder(player.getPlayerId(), cardGame);
                } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
                    throw new DecisionResultInvalidException(exp.getMessage());
                }
            }
        };
        cardGame.sendAwaitingDecision(decision);
    }


    private void selectCardsToRemove(Player player, ST1EGame cardGame, RemoveCardsFromPreseedCardStackAction removeAction) {
        Collection<PhysicalCard> availableCards;
        MissionLocation mission = removeAction.getLocation();
        availableCards = mission.getPreSeedCardsForPlayer(player);
        cardGame.sendAwaitingDecision(
                new ArbitraryCardsSelectionDecision(player, "Select cards to remove from " + mission.getLocationName(),
                        availableCards, cardGame) {
                    @Override
                    public void decisionMade (String result) throws DecisionResultInvalidException {
                        Collection<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                        try {
                            removeAction.setCardsToRemove(selectedCards);
                            removeAction.processEffect(cardGame);
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