package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KolAndCaptainWorfTest extends AbstractAtTest {

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    public void kolTest() throws JsonProcessingException, CardNotFoundException, InvalidGameLogicException, PlayerNotFoundException {
        initializeSimple1EGame(30);

        final MissionCard mission = (MissionCard) newCardForGame("101_174", P1);
        final FacilityCard outpost = (FacilityCard) newCardForGame("101_105", P1);
        final PersonnelCard kol = (PersonnelCard) newCardForGame("155_072", P1);
        final PersonnelCard arridor = (PersonnelCard) newCardForGame("155_069", P1);

        assertFalse(outpost.isInPlay());
        assertEquals("Kol", kol.getTitle());
        assertEquals("Dr. Arridor", arridor.getTitle());
        assertNotNull(mission);

        _game.getGameState().addMissionLocationToSpacelineForTestingOnly(_game, mission, 0);
        _game.getGameState().seedFacilityAtLocationForTestingOnly(_game, outpost, mission.getGameLocation());

        assertTrue(outpost.isInPlay());

        kol.reportToFacilityForTestingOnly(outpost);
        assertEquals(Integer.valueOf(6), kol.getAttribute(CardAttribute.INTEGRITY));
        assertEquals(Integer.valueOf(6), kol.getAttribute(CardAttribute.CUNNING));
        assertEquals(Integer.valueOf(5), kol.getAttribute(CardAttribute.STRENGTH));

        assertEquals(Integer.valueOf(4), arridor.getAttribute(CardAttribute.INTEGRITY));
        assertEquals(Integer.valueOf(8), arridor.getAttribute(CardAttribute.CUNNING));
        assertEquals(Integer.valueOf(5), arridor.getAttribute(CardAttribute.STRENGTH));

        arridor.reportToFacilityForTestingOnly(outpost);
        assertTrue(kol.isPresentWith(arridor));

        assertEquals(Integer.valueOf(8), kol.getAttribute(CardAttribute.INTEGRITY));
        assertEquals(Integer.valueOf(8), kol.getAttribute(CardAttribute.CUNNING));
        assertEquals(Integer.valueOf(7), kol.getAttribute(CardAttribute.STRENGTH));

        assertEquals(Integer.valueOf(4), arridor.getAttribute(CardAttribute.INTEGRITY));
        assertEquals(Integer.valueOf(8), arridor.getAttribute(CardAttribute.CUNNING));
        assertEquals(Integer.valueOf(5), arridor.getAttribute(CardAttribute.STRENGTH));

        String gameStateString = _game.getGameState().serializeComplete();
    }

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    public void captainWorfTest() throws CardNotFoundException, InvalidGameLogicException, PlayerNotFoundException {
        initializeSimple1EGame(30);

        final MissionCard mission = (MissionCard) newCardForGame("101_174", P1);
        final FacilityCard outpost1 = (FacilityCard) newCardForGame("101_105", P1);
        final PersonnelCard worf = (PersonnelCard) newCardForGame("155_079", P1);
        final PersonnelCard kehleyr1 = (PersonnelCard) newCardForGame("155_080", P1);
        final PersonnelCard kehleyr2 = (PersonnelCard) newCardForGame("101_217", P2);

        assertFalse(outpost1.isInPlay());
        assertEquals("Captain Worf", worf.getTitle()); // 8-6-10
        assertEquals("Commander K'Ehleyr", kehleyr1.getTitle()); // 7-8-7
        assertEquals("K'Ehleyr", kehleyr2.getTitle()); // 8-7-7
        assertNotNull(mission);

        _game.getGameState().addMissionLocationToSpacelineForTestingOnly(_game, mission, 0);
        _game.getGameState().seedFacilityAtLocationForTestingOnly(_game, outpost1, mission.getGameLocation());

        assertTrue(outpost1.isInPlay());

        worf.reportToFacilityForTestingOnly(outpost1);
        assertEquals(Integer.valueOf(8), worf.getAttribute(CardAttribute.INTEGRITY));
        assertEquals(Integer.valueOf(6), worf.getAttribute(CardAttribute.CUNNING));
        assertEquals(Integer.valueOf(10), worf.getAttribute(CardAttribute.STRENGTH));

        assertEquals(Integer.valueOf(7), kehleyr1.getAttribute(CardAttribute.INTEGRITY));
        assertEquals(Integer.valueOf(8), kehleyr1.getAttribute(CardAttribute.CUNNING));
        assertEquals(Integer.valueOf(7), kehleyr1.getAttribute(CardAttribute.STRENGTH));

        assertEquals(Integer.valueOf(8), kehleyr2.getAttribute(CardAttribute.INTEGRITY));
        assertEquals(Integer.valueOf(7), kehleyr2.getAttribute(CardAttribute.CUNNING));
        assertEquals(Integer.valueOf(7), kehleyr2.getAttribute(CardAttribute.STRENGTH));


        kehleyr1.reportToFacilityForTestingOnly(outpost1);
        kehleyr2.reportToFacilityForTestingOnly(outpost1);
        assertTrue(_game.getGameState().getAllCardsInPlay().contains(kehleyr1));
        assertTrue(_game.getGameState().getAllCardsInPlay().contains(kehleyr2));

        assertTrue(kehleyr1.isPresentWith(worf));

        assertEquals(Integer.valueOf(10), worf.getAttribute(CardAttribute.INTEGRITY));
        assertEquals(Integer.valueOf(8), worf.getAttribute(CardAttribute.CUNNING));
        assertEquals(Integer.valueOf(12), worf.getAttribute(CardAttribute.STRENGTH));

        assertEquals(Integer.valueOf(9), kehleyr1.getAttribute(CardAttribute.INTEGRITY));
        assertEquals(Integer.valueOf(10), kehleyr1.getAttribute(CardAttribute.CUNNING));
        assertEquals(Integer.valueOf(9), kehleyr1.getAttribute(CardAttribute.STRENGTH));

        assertEquals(Integer.valueOf(10), kehleyr2.getAttribute(CardAttribute.INTEGRITY));
        assertEquals(Integer.valueOf(9), kehleyr2.getAttribute(CardAttribute.CUNNING));
        assertEquals(Integer.valueOf(9), kehleyr2.getAttribute(CardAttribute.STRENGTH));
    }

}