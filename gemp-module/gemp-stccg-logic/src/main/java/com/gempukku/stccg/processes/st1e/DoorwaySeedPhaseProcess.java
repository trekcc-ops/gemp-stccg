package com.gempukku.stccg.processes.st1e;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.decisions.ArbitraryCardsSelectionDecision;
import com.gempukku.stccg.game.DefaultGame;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.ST1EGame;
import com.gempukku.stccg.gamestate.GameState;
import com.gempukku.stccg.processes.GameProcess;

import java.beans.ConstructorProperties;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@JsonTypeName("DoorwaySeedPhaseProcess")
public class DoorwaySeedPhaseProcess extends SimultaneousGameProcess {

    @ConstructorProperties({"playersParticipating"})
    public DoorwaySeedPhaseProcess(Collection<String> playersSelecting) {
        super(playersSelecting);
    }

    @Override
    public void process(DefaultGame cardGame) throws InvalidGameLogicException {
        ST1EGame stGame = getST1EGame(cardGame);
        List<String> playerIds = new LinkedList<>(cardGame.getPlayerIds());
        for (String playerId : playerIds) {
            Collection<PhysicalCard> doorwaySeeds = new LinkedList<>();
            // TODO - Doorway seeding is a bit more complicated for cards like Bajoran Wormhole, or non-seedable doors
            for (PhysicalCard seedCard : stGame.getGameState().getSeedDeck(playerId)) {
                if (seedCard.getCardType() == CardType.DOORWAY)
                    doorwaySeeds.add(seedCard);
            }
            if (!doorwaySeeds.isEmpty()) {
                String message = "Select cards to seed during doorway phase";
                cardGame.getUserFeedback().sendAwaitingDecision(
                        new ArbitraryCardsSelectionDecision(cardGame.getPlayer(playerId), message, doorwaySeeds,
                                cardGame) {
                            @Override
                            public void decisionMade(String result) throws DecisionResultInvalidException {
                                List<PhysicalCard> cards = getSelectedCardsByResponse(result);
                                for (PhysicalCard card : cards) {
                                    cardGame.getActionsEnvironment().addActionToStack(card.createSeedCardAction());
                                }
                            }
                        });
            }
        }
    }

    @Override
    public GameProcess getNextProcess(DefaultGame cardGame) throws InvalidGameLogicException {
        GameState cardGameState = cardGame.getGameState();
        cardGameState.setCurrentPhase(Phase.SEED_MISSION);
        for (String player : cardGame.getPlayerIds()) {
            List<PhysicalCard> missionSeeds = new LinkedList<>(cardGameState.getZoneCards(player, Zone.MISSIONS_PILE));
            if (!cardGame.getFormat().isNoShuffle())
                Collections.shuffle(missionSeeds);
            for (PhysicalCard card : missionSeeds) {
                cardGameState.removeCardsFromZone(player, Collections.singleton(card));
                cardGameState.addCardToZone(card, Zone.HAND);
            }
        }
        ST1EGame stGame = getST1EGame(cardGame);
        return new ST1EMissionSeedPhaseProcess();
    }
}