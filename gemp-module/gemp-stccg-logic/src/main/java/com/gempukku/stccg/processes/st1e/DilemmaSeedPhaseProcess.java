package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.TopLevelSelectableAction;
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
import com.gempukku.stccg.gamestate.ST1EGameState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
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
                if (!player.getCardsInHand().isEmpty()) {
                    TopLevelSelectableAction seedCardsAction = new AddSeedCardsAction(player, mission);
                    seedActions.add(seedCardsAction);
                }
                Collection<PhysicalCard> cardsPreSeeded = mission.getLocation().getCardsPreSeeded(player);
                if (cardsPreSeeded != null && !cardsPreSeeded.isEmpty()) {
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
                                    else if (action instanceof RemoveSeedCardsAction)
                                        selectCardsToRemove(player, cardGame, topCard);
                                    else cardGame.sendMessage("Game error - invalid action selected");
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
        Collection<PhysicalCard> availableCards = player.getCardsInHand();
        cardGame.getUserFeedback().sendAwaitingDecision(
                new CardsSelectionDecision(player, "Select cards to seed under " + topCard.getTitle(),
                        availableCards, cardGame) {
                    @Override
                    public void decisionMade (String result) throws DecisionResultInvalidException {
                        try {
                            Collection<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                            cardGame.getGameState().preSeedCardsUnder(selectedCards, topCard, player);
                            selectMissionToSeedUnder(player.getPlayerId(), cardGame);
                        } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
                            throw new DecisionResultInvalidException(exp.getMessage());
                        }
                    }
                });
    }


    private void selectCardsToRemove(Player player, ST1EGame cardGame, PhysicalCard topCard) {
        Collection<PhysicalCard> availableCards;
        try {
            availableCards = topCard.getLocation().getCardsPreSeeded(player);
        } catch(InvalidGameLogicException exp) {
            availableCards = new LinkedList<>();
        }
        cardGame.getUserFeedback().sendAwaitingDecision(
                new ArbitraryCardsSelectionDecision(player, "Select cards to remove from " + topCard.getTitle(),
                        availableCards, cardGame) {
                    @Override
                    public void decisionMade (String result) throws DecisionResultInvalidException {
                        Collection<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                        try {
                            for (PhysicalCard card : selectedCards) {
                                topCard.getLocation().removePreSeedCard(card, player);
                                cardGame.getGameState().removeCardFromZone(card);
                                cardGame.getGameState().addCardToZone(card, Zone.HAND);
                            }
                            selectMissionToSeedUnder(player.getPlayerId(), cardGame);
                        } catch(InvalidGameLogicException | PlayerNotFoundException exp) {
                            cardGame.sendErrorMessage(exp);
                        }
                    }
                });
    }

}