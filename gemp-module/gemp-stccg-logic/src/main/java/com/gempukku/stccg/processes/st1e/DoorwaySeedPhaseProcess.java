package com.gempukku.stccg.processes.st1e;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.ST1EGameState;
import com.gempukku.stccg.processes.GameProcess;

import java.util.*;

public class DoorwaySeedPhaseProcess extends SimultaneousGameProcess {

    public DoorwaySeedPhaseProcess(ST1EGame game) {
        super(game.getPlayerIds(), game);
    }

    public DoorwaySeedPhaseProcess(Collection<String> playersSelecting, ST1EGame game) {
        super(playersSelecting, game);
    }

    @Override
    public void process(DefaultGame cardGame) {
        List<String> playerIds = new LinkedList<>(_game.getPlayerIds());
        for (String playerId : playerIds) {
            Collection<PhysicalCard> doorwaySeeds = new LinkedList<>();
            // TODO - Doorway seeding is a bit more complicated for cards like Bajoran Wormhole, or non-seedable doors
            for (PhysicalCard seedCard : _game.getGameState().getSeedDeck(playerId)) {
                if (seedCard.getCardType() == CardType.DOORWAY)
                    doorwaySeeds.add(seedCard);
            }
            if (!doorwaySeeds.isEmpty()) {
                String message = "Select cards to seed during doorway phase";
                _game.getUserFeedback().sendAwaitingDecision(
                        new ArbitraryCardsSelectionDecision(_game.getPlayer(playerId), message, doorwaySeeds) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                List<PhysicalCard> cards = getSelectedCardsByResponse(result);
                                for (PhysicalCard card : cards) {
                                    _game.getActionsEnvironment().addActionToStack(card.createSeedCardAction());
                                }
                            }
                        });
            }
        }
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) {
        ST1EGameState _gameState = _game.getGameState();
        _gameState.setCurrentPhase(Phase.SEED_MISSION);
        for (String player : _game.getPlayerIds()) {
            List<PhysicalCard> missionSeeds = new LinkedList<>(_gameState.getZoneCards(player, Zone.MISSIONS_PILE));
            if (!_game.getFormat().isNoShuffle())
                Collections.shuffle(missionSeeds);
            for (PhysicalCard card : missionSeeds) {
                _gameState.removeCardsFromZone(player, Collections.singleton(card));
                _gameState.addCardToZone(card, Zone.HAND);
            }
        }
        return new ST1EMissionSeedPhaseProcess(_game);
    }
}