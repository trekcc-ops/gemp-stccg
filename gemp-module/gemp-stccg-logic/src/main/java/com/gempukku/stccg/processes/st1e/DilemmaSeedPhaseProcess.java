package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.playcard.AddSeedCardsAction;
import com.gempukku.stccg.actions.playcard.RemoveSeedCardsAction;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class DilemmaSeedPhaseProcess extends SimultaneousGameProcess {

    DilemmaSeedPhaseProcess(Collection<String> playersSelecting, ST1EGame game) {
        super(playersSelecting, game);
    }

    @Override
    public void process() {
        Collection<String> playerIds = _game.getPlayerIds();
        for (String playerId : playerIds) {
            if (_playersParticipating.contains(playerId))
                selectMissionToSeedUnder(playerId);
        }
    }

    abstract List<MissionCard> getAvailableMissions(Player player);

    protected void selectMissionToSeedUnder(String playerId) {
        if (getAvailableMissions(_game.getPlayer(playerId)).isEmpty()) {
            _playersParticipating.remove(playerId);
        } else {
            ST1EGameState gameState = _game.getGameState();
            List<Action> seedActions = new ArrayList<>();
            Player player = gameState.getPlayer(playerId);
            List<MissionCard> availableMissions = getAvailableMissions(player);
            for (MissionCard mission : availableMissions) {
                // TODO - These actions are red herrings and are never actually used
                if (!gameState.getHand(playerId).isEmpty()) {
                    Action seedCardsAction = new AddSeedCardsAction(player, mission);
                    seedActions.add(seedCardsAction);
                }
                Collection<PhysicalCard> cardsPreSeeded = mission.getLocation().getCardsPreSeeded(player);
                if (cardsPreSeeded != null && !cardsPreSeeded.isEmpty()) {
                    Action removeSeedCardsAction = new RemoveSeedCardsAction(player, mission);
                    seedActions.add(removeSeedCardsAction);
                }
            }

            _game.getUserFeedback().sendAwaitingDecision(
                    new CardActionSelectionDecision(_game.getPlayer(playerId), getDecisionText(_game.getPlayer(playerId)),
                            seedActions) {
                        @Override
                        public void decisionMade(String result) throws DecisionResultInvalidException {
                            Action action = getSelectedAction(result);
                            if (action == null) {
                                _playersParticipating.remove(playerId);
                            } else {
                                PhysicalCard topCard = action.getActionSource();
                                selectCardsToSeed(player, topCard);
                                if (action instanceof AddSeedCardsAction)
                                    selectCardsToSeed(player, topCard);
                                else if (action instanceof RemoveSeedCardsAction)
                                    selectCardsToRemove(player, topCard);
                                else gameState.sendMessage("Game error - invalid action selected");
                            }
                        }
                    });
        }
    }

    protected abstract String getDecisionText(Player player);

    private void selectCardsToSeed(Player player, PhysicalCard topCard) {
        Collection<PhysicalCard> availableCards = _game.getGameState().getHand(player.getPlayerId());
        _game.getUserFeedback().sendAwaitingDecision(
                new CardsSelectionDecision(player, "Select cards to seed under " + topCard.getTitle(), availableCards) {
            @Override
            public void decisionMade (String result) throws DecisionResultInvalidException {
                Collection<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                _game.getGameState().preSeedCardsUnder(selectedCards, topCard, player);
                selectMissionToSeedUnder(player.getPlayerId());
            }
        });
    }

    private void selectCardsToRemove(Player player, PhysicalCard topCard) {
        Collection<PhysicalCard> availableCards = topCard.getLocation().getCardsPreSeeded(player);
        _game.getUserFeedback().sendAwaitingDecision(
                new ArbitraryCardsSelectionDecision(player, "Select cards to remove from " + topCard.getTitle(),
                        availableCards) {
                    @Override
                    public void decisionMade (String result) throws DecisionResultInvalidException {
                        Collection<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                        for (PhysicalCard card : selectedCards) {
                            topCard.getLocation().removePreSeedCard(card, player);
                            _game.getGameState().removeCardFromZone(card);
                            _game.getGameState().addCardToZone(card, Zone.HAND);
                        }
                        selectMissionToSeedUnder(player.getPlayerId());
                    }
                });
    }
}