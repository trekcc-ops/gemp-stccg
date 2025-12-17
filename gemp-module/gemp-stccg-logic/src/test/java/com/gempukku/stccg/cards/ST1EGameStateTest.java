package com.gempukku.stccg.cards;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.gamestate.ST1EGameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ST1EGameStateTest extends AbstractAtTest {

    @Test
    public void seedFacilityTest() throws Exception {
        initializeSimple1EGame(30);
        ST1EGameState gameState = _game.getGameState();

        final MissionCard mission = new MissionCard(101, P1, _cardLibrary.get("101_174"));
        final FacilityCard outpost1 = new FacilityCard(102, P1, _cardLibrary.get("101_105"));

        assertFalse(outpost1.isInPlay());
        assertFalse(gameState.hasNoPendingDecisions());


        gameState.addMissionLocationToSpacelineForTestingOnly(_game, mission, 0);
        gameState.seedFacilityAtLocationForTestingOnly(_game, outpost1, mission);

        assertTrue(outpost1.isInPlay());

    }
}