package com.gempukku.stccg.cards;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.game.Player;
import org.junit.Test;

import static org.junit.Assert.*;

public class ST1EGameStateTest extends AbstractAtTest {

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    public void seedFacilityTest()
            throws DecisionResultInvalidException, CardNotFoundException {
        initializeSimple1EGame(30);
        Player player1 = _game.getPlayer(1);

        final MissionCard mission = new MissionCard(_game, 101, player1, _cardLibrary.get("101_174"));
        final FacilityCard outpost1 = new FacilityCard(_game, 102, player1, _cardLibrary.get("101_105"));

        assertFalse(outpost1.isInPlay());

        _game.getGameState().addToSpaceline(mission, 0, false);
        _game.getGameState().seedFacilityAtLocation(outpost1, 0);

        assertTrue(outpost1.isInPlay());

    }
}
