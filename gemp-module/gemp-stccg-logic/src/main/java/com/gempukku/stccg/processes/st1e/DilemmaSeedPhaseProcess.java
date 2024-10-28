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

import java.util.*;

public abstract class DilemmaSeedPhaseProcess extends ST1EGameProcess {
    protected final Set<String> _playersDone;
    protected final Collection<String> _playersSelecting = new HashSet<>();
    DilemmaSeedPhaseProcess(Set<String> playersDone, ST1EGame game) {
        super(game);
        _playersDone = playersDone;
        for (String playerId : game.getPlayerIds()) {
            if (!playersDone.contains(playerId))
                _playersSelecting.add(playerId);
        }
    }

    @Override
    public void process() {
        for (String playerId : _playersSelecting) selectMissionToSeedUnder(playerId);
    }

    abstract List<MissionCard> getAvailableMissions(Player player);

    protected void selectMissionToSeedUnder(String playerId) {
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
            if (mission.getCardsPreSeeded(player) != null && !mission.getCardsPreSeeded(player).isEmpty()) {
                Action removeSeedCardsAction = new RemoveSeedCardsAction(player, mission);
                seedActions.add(removeSeedCardsAction);
            }
        }

        _game.getUserFeedback().sendAwaitingDecision(playerId,
                new CardActionSelectionDecision("Select a mission to seed cards under", seedActions) {
                    @Override
                    public void decisionMade(String result) throws DecisionResultInvalidException {
                        Action action = getSelectedAction(result);
                        if (action == null) {
                            _playersDone.add(playerId);
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

    private void selectCardsToSeed(Player player, PhysicalCard topCard) {
        Collection<PhysicalCard> availableCards = _game.getGameState().getHand(player.getPlayerId());
        _game.getUserFeedback().sendAwaitingDecision(player.getPlayerId(),
                new CardsSelectionDecision("Select cards to seed under " + topCard.getTitle(), availableCards) {
            @Override
            public void decisionMade (String result) throws DecisionResultInvalidException {
                Collection<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                _game.getGameState().preSeedCardsUnder(selectedCards, topCard, player);
                selectMissionToSeedUnder(player.getPlayerId());
            }
        });
    }

    private void selectCardsToRemove(Player player, PhysicalCard topCard) {
        Collection<PhysicalCard> availableCards = topCard.getCardsPreSeeded(player);
        _game.getUserFeedback().sendAwaitingDecision(player.getPlayerId(),
                new ArbitraryCardsSelectionDecision("Select cards to remove from " + topCard.getTitle(),
                        availableCards) {
                    @Override
                    public void decisionMade (String result) throws DecisionResultInvalidException {
                        Collection<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                        for (PhysicalCard card : selectedCards) {
                            topCard.removePreSeedCard(card, player);
                            _game.getGameState().removeCardFromZone(card);
                            _game.getGameState().addCardToZone(card, Zone.HAND);
                        }
                        selectMissionToSeedUnder(player.getPlayerId());
                    }
                });
    }
}