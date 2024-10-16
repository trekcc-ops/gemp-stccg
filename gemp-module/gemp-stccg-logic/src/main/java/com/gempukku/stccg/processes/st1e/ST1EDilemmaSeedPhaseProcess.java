package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.actions.Action;
import com.gempukku.stccg.actions.playcard.SeedMissionCardsAction;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import com.gempukku.stccg.decisions.CardsSelectionDecision;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.gamestate.ST1ELocation;
import com.gempukku.stccg.processes.GameProcess;

import java.util.*;

public class ST1EDilemmaSeedPhaseProcess extends ST1EGameProcess {
    private final Set<String> _playersDone;
    private final Collection<String> _playersSelecting = new HashSet<>();

    ST1EDilemmaSeedPhaseProcess(Set<String> playersDone, ST1EGame game) {
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

    @Override
    public GameProcess getNextProcess() {
        Set<String> players = _game.getPlayerIds();
        if (players.size() == _playersDone.size()) return new ST1EStartOfFacilitySeedPhaseProcess(_game);
        else return new ST1EDilemmaSeedPhaseProcess(_playersDone, _game);
    }

    private void selectMissionToSeedUnder(String playerId) {
        ST1EGameState gameState = _game.getGameState();
        List<Action> seedActions = new ArrayList<>();
        for (ST1ELocation location : gameState.getSpacelineLocations()) {
            try {
                MissionCard mission = location.getMissionForPlayer(playerId);
                // TODO - SeedMissionCardsAction is a red herring; this action is never used
                Action seedCardsAction = new SeedMissionCardsAction(gameState.getPlayer(playerId), mission);
                seedActions.add(seedCardsAction);
            } catch (InvalidGameLogicException exp) {
                gameState.sendMessage(exp.getMessage());
            }
        }

        _game.getUserFeedback().sendAwaitingDecision(playerId,
                new CardActionSelectionDecision("Select a mission to seed cards under", seedActions) {
                    @Override
                    public void decisionMade(String result) throws DecisionResultInvalidException {
                        Action action = getSelectedAction(result);
                        if (action != null) {
                            PhysicalCard topCard = action.getActionSource();
                            selectCardsToSeed(gameState.getPlayer(playerId), topCard);
                        } else {
                            _playersDone.add(playerId);
                        }
                    }
                });
    }

    private void selectCardsToSeed(Player player, PhysicalCard topCard) {
        Collection<PhysicalCard> availableCards = _game.getGameState().getHand(player.getPlayerId());
        _game.getUserFeedback().sendAwaitingDecision(player.getPlayerId(),
                new CardsSelectionDecision(1, "Select cards to seed under " + topCard.getTitle(), availableCards,
                0, availableCards.size()) {
            @Override
            public void decisionMade (String result) throws DecisionResultInvalidException {
                Set<PhysicalCard> selectedCards = getSelectedCardsByResponse(result);
                _game.getGameState().seedCardsUnder(selectedCards, topCard);
                selectMissionToSeedUnder(player.getPlayerId());
            }
        });
    }
}