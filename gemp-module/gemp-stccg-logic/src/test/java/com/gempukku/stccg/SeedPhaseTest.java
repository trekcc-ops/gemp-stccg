package com.gempukku.stccg;

import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.AwaitingDecisionType;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.CardType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.filters.Filters;
import com.gempukku.stccg.gamestate.ST1ELocation;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SeedPhaseTest extends AbstractAtTest {

    @Test
    public void autoSeedTest() throws DecisionResultInvalidException {
        initializeIntroductoryTwoPlayerGame();

        // Figure out which player is going first
        String player1 = _game.getGameState().getPlayerOrder().getFirstPlayer();
        String player2 = _game.getOpponent(player1);

        // Both players keep picking option #1 until all missions are seeded
        while (_game.getGameState().getCurrentPhase() == Phase.SEED_MISSION) {
            if (_userFeedback.getAwaitingDecision(player1) != null) {
                playerDecided(player1, "0");
            }
            if (_userFeedback.getAwaitingDecision(player2) != null) {
                playerDecided(player2, "0");
            }
        }

        // There should now be 12 missions seeded
        assertEquals(12, _game.getGameState().getSpacelineLocations().size());
        for (ST1ELocation location : _game.getGameState().getSpacelineLocations()) {
            System.out.println((location.getLocationZoneIndex()+1) + " - " + location.getLocationName());
        }

        // Both players seed facility at random eligible location
        while (_game.getGameState().getCurrentPhase() == Phase.SEED_FACILITY) {
            if (_userFeedback.getAwaitingDecision(player1) != null) {
                if (_userFeedback.getAwaitingDecision(player1).getDecisionType() == AwaitingDecisionType.CARD_SELECTION) {
                    List<String> cardIdList = new java.util.ArrayList<>(Arrays.stream(_userFeedback.getAwaitingDecision(player1).getDecisionParameters().get("cardId")).toList());
                    Collections.shuffle(cardIdList);
                    playerDecided(player1, cardIdList.getFirst());
                }
                else
                    playerDecided(player1, "0");
            }
            if (_userFeedback.getAwaitingDecision(player2) != null) {
                if (_userFeedback.getAwaitingDecision(player2).getDecisionType() == AwaitingDecisionType.CARD_SELECTION) {
                    List<String> cardIdList = new java.util.ArrayList<>(Arrays.stream(_userFeedback.getAwaitingDecision(player2).getDecisionParameters().get("cardId")).toList());
                    Collections.shuffle(cardIdList);
                    playerDecided(player2, cardIdList.getFirst());
                }
                else
                    playerDecided(player2, "0");
            }
        }

        // Verify that both facilities were seeded
        assertEquals(2, Filters.filterActive(_game, CardType.FACILITY).size());
        for (PhysicalCard card : Filters.filterActive(_game, CardType.FACILITY)) {
            System.out.println(card.getTitle() + " seeded at " + card.getLocation().getLocationName());
        }

        // Verify that the seed phase is over and both players have drawn starting hands
        assertEquals(Phase.CARD_PLAY, _game.getGameState().getCurrentPhase());
        assertEquals(7, _game.getGameState().getHand(player1).size());
        assertEquals(7, _game.getGameState().getHand(player2).size());
    }

}