package com.gempukku.stccg.cards;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.game.Player;
import com.gempukku.stccg.game.PlayerNotFoundException;
import com.gempukku.stccg.gamestate.ST1EGameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ST1EGameStateTest extends AbstractAtTest {

    @Test
    public void seedFacilityTest() throws PlayerNotFoundException {
        initializeSimple1EGame(30);
        Player player1 = _game.getPlayer(1);

        final MissionCard mission = new MissionCard(_game, 101, player1, _cardLibrary.get("101_174"));
        final FacilityCard outpost1 = new FacilityCard(_game, 102, player1, _cardLibrary.get("101_105"));

        assertFalse(outpost1.isInPlay());
        assertFalse(_userFeedback.hasNoPendingDecisions());

        ST1EGameState gameState = _game.getGameState();

        gameState.addMissionLocationToSpaceline(mission, 0);
        gameState.seedFacilityAtLocation(outpost1, 0);

        assertTrue(outpost1.isInPlay());

    }
}