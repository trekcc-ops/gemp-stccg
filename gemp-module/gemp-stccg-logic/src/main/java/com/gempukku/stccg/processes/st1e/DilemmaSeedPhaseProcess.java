package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
import com.gempukku.stccg.actions.placecard.AddCardToSeedCardStack;
import com.gempukku.stccg.actions.placecard.RemoveCardFromPreSeedStack;
import com.gempukku.stccg.actions.playcard.AddSeedCardsAction;
import com.gempukku.stccg.actions.playcard.RemoveSeedCardsAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.game.*;
import com.gempukku.stccg.gamestate.MissionLocation;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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

    abstract List<MissionCard> getAvailableMissions(ST1EGame stGame, String playerId);

    protected void selectMissionToSeedUnder(String playerId, ST1EGame cardGame)
            throws InvalidGameLogicException, PlayerNotFoundException {
        if (getAvailableMissions(cardGame, playerId).isEmpty()) {
            _playersParticipating.remove(playerId);
        } else {
            ST1EGameState gameState = cardGame.getGameState();
            List<TopLevelSelectableAction> seedActions = new ArrayList<>();
            Player player = gameState.getPlayer(playerId);
            List<MissionCard> availableMissions = getAvailableMissions(cardGame, playerId);
            for (MissionCard mission : availableMissions) {
                // TODO - These actions are red herrings and are never actually used
                if (!player.getCardsInGroup(Zone.SEED_DECK).isEmpty()) {
                    TopLevelSelectableAction seedCardsAction = new AddSeedCardsAction(player, mission);
                    seedActions.add(seedCardsAction);
                }
                if (mission.getGameLocation() instanceof MissionLocation missionLocation &&
                        missionLocation.hasCardsPreSeededByPlayer(player)) {
                    TopLevelSelectableAction removeSeedCardsAction = new RemoveSeedCardsAction(player, mission);
                    seedActions.add(removeSeedCardsAction);
                }
            }

            cardGame.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(cardGame.getPlayer(playerId),
                            getDecisionText(cardGame, cardGame.getPlayer(playerId)), seedActions, cardGame) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            TopLevelSelectableAction action = getSelectedAction(result);
                            if (action == null) {
                                _playersParticipating.remove(playerId);
                            } else {
                                try {
                                    int cardId = action.getCardIdForActionSelection();
                                    PhysicalCard topCard = cardGame.getCardFromCardId(cardId);
                                    selectCardsToSeed(player, cardGame, topCard);
                                    if (action instanceof AddSeedCardsAction)
                                        selectCardsToSeed(player, cardGame, topCard);
                                    else if (action instanceof RemoveSeedCardsAction &&
                                            topCard.getGameLocation() instanceof MissionLocation mission)
                                        selectCardsToRemove(player, cardGame, topCard, mission);
                                    else {
                                        throw new DecisionResultInvalidException("Game error - invalid action selected");
                                    }
                                } catch(CardNotFoundException exp) {
                                    throw new DecisionResultInvalidException(exp.getMessage());
                                }
                            }
                        }
                    });
        }
    }

    protected abstract String getDecisionText(DefaultGame cardGame, Player player);

    private void selectCardsToSeed(Player player, ST1EGame cardGame, PhysicalCard topCard) {
        Collection<PhysicalCard> availableCards = player.getCardsInGroup(Zone.SEED_DECK);
        cardGame.getUserFeedback().sendAwaitingDecision(
                new CardsSelectionDecision(player, "Select cards to seed under " + topCard.getTitle(),
                        availableCards, cardGame) {
                    @Override
                    public void decisionMade (String result) throws DecisionResultInvalidException {
                        try {
                            Collection<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                            if (topCard.getGameLocation() instanceof MissionLocation mission) {
                                for (PhysicalCard card : selectedCards) {
                                    AddCardToSeedCardStack removeAction =
                                            new AddCardToSeedCardStack(cardGame, player, card, mission);
                                    removeAction.processEffect(card.getOwner(), cardGame);
                                    cardGame.getActionsEnvironment().logCompletedActionNotInStack(removeAction);
                                    cardGame.sendActionResultToClient();
                                }
                            } else {
                                throw new InvalidGameLogicException("Tried to seed cards under a non-mission card");
                            }
                            selectMissionToSeedUnder(player.getPlayerId(), cardGame);
                        } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
                            throw new DecisionResultInvalidException(exp.getMessage());
                        }
                    }
                });
    }


    private void selectCardsToRemove(Player player, ST1EGame cardGame, PhysicalCard topCard, MissionLocation mission) {
        Collection<PhysicalCard> availableCards;
        availableCards = mission.getPreSeedCardsForPlayer(player);
        cardGame.getUserFeedback().sendAwaitingDecision(
                new ArbitraryCardsSelectionDecision(player, "Select cards to remove from " + topCard.getTitle(),
                        availableCards, cardGame) {
                    @Override
                    public void decisionMade (String result) throws DecisionResultInvalidException {
                        Collection<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                        try {
                            for (PhysicalCard card : selectedCards) {
                                RemoveCardFromPreSeedStack removeAction =
                                        new RemoveCardFromPreSeedStack(cardGame, player, card, mission);
                                removeAction.processEffect(card.getOwner(), cardGame);
                                cardGame.getActionsEnvironment().logCompletedActionNotInStack(removeAction);
                                cardGame.sendActionResultToClient();
                            }
                            selectMissionToSeedUnder(player.getPlayerId(), cardGame);
                        } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
                            cardGame.sendErrorMessage(exp);
                        }
                    }
                });
    }

}