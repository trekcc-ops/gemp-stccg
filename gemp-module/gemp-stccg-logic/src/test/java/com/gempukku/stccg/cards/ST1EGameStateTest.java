package com.gempukku.stccg.cards;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import com.gempukku.stccg.gamestate.ST1EGameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ST1EGameStateTest extends AbstractAtTest {

    @Test
    public void seedFacilityTest() throws PlayerNotFoundException, InvalidGameLogicException {
        initializeSimple1EGame(30);
        Player player1 = _game.getPlayer(1);

        final MissionCard mission = new MissionCard(_game, 101, player1, _cardLibrary.get("101_174"));
        final FacilityCard outpost1 = new FacilityCard(_game, 102, player1, _cardLibrary.get("101_105"));

        assertFalse(outpost1.isInPlay());
        assertFalse(_userFeedback.hasNoPendingDecisions());

        ST1EGameState gameState = _game.getGameState();

        gameState.addMissionLocationToSpacelineForTestingOnly(_game, mission, 0);
        gameState.seedFacilityAtLocationForTestingOnly(_game, outpost1, mission.getLocationDeprecatedOnlyUseForTests());

        assertTrue(outpost1.isInPlay());

    }
}