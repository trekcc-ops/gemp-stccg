package com.gempukku.stccg.cards;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ST1EGameStateTest extends AbstractAtTest {

    @Test
    public void seedFacilityTest() {
        initializeSimple1EGame(30);
        Player player1 = _game.getPlayer(1);

        final MissionCard mission = new MissionCard(_game, 101, player1, _cardLibrary.get("101_174"));
        final FacilityCard outpost1 = new FacilityCard(_game, 102, player1, _cardLibrary.get("101_105"));

        assertFalse(outpost1.isInPlay());

        try {
            _game.getGameState().addToSpaceline(mission, 0, false);
        } catch(InvalidGameLogicException exp) {
            System.out.println(exp.getMessage());
        }
        _game.getGameState().seedFacilityAtLocation(outpost1, 0);

        assertTrue(outpost1.isInPlay());

    }
}