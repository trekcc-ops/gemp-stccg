package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.common.filterable.CardAttribute;
import com.gempukku.stccg.game.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CardBlueprintTest extends AbstractAtTest {

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    public void kolTest() throws JsonProcessingException {
        initializeSimple1EGame(30);
        Player player1 = _game.getPlayer(1);

        final MissionCard mission = new MissionCard(_game, 103, player1, _cardLibrary.get("101_174"));
        final FacilityCard outpost = new FacilityCard(_game, 104, player1, _cardLibrary.get("101_105"));
        final PersonnelCard kol = new PersonnelCard(_game, 101, player1, _cardLibrary.get("155_072"));
        final PersonnelCard arridor = new PersonnelCard(_game, 102, player1, _cardLibrary.get("155_069"));

        assertFalse(outpost.isInPlay());
        assertEquals("Kol", kol.getTitle());
        assertEquals("Dr. Arridor", arridor.getTitle());

        _game.getGameState().addMissionLocationToSpaceline(mission, 0);
        _game.getGameState().seedFacilityAtLocation(outpost, 0);

        assertTrue(outpost.isInPlay());

        kol.reportToFacility(outpost);
        assertEquals(Integer.valueOf(6), kol.getAttribute(CardAttribute.INTEGRITY));
        assertEquals(Integer.valueOf(6), kol.getAttribute(CardAttribute.CUNNING));
        assertEquals(Integer.valueOf(5), kol.getAttribute(CardAttribute.STRENGTH));

        assertEquals(Integer.valueOf(4), arridor.getAttribute(CardAttribute.INTEGRITY));
        assertEquals(Integer.valueOf(8), arridor.getAttribute(CardAttribute.CUNNING));
        assertEquals(Integer.valueOf(5), arridor.getAttribute(CardAttribute.STRENGTH));

        arridor.reportToFacility(outpost);
        assertTrue(kol.isPresentWith(arridor));

        assertEquals(Integer.valueOf(8), kol.getAttribute(CardAttribute.INTEGRITY));
        assertEquals(Integer.valueOf(8), kol.getAttribute(CardAttribute.CUNNING));
        assertEquals(Integer.valueOf(7), kol.getAttribute(CardAttribute.STRENGTH));

        assertEquals(Integer.valueOf(4), arridor.getAttribute(CardAttribute.INTEGRITY));
        assertEquals(Integer.valueOf(8), arridor.getAttribute(CardAttribute.CUNNING));
        assertEquals(Integer.valueOf(5), arridor.getAttribute(CardAttribute.STRENGTH));

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(_game.getGameState()).replace(",",",\n"));
    }

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    public void captainWorfTest() {
        initializeSimple1EGame(30);
        Player player1 = _game.getPlayer(1);
        Player player2 = _game.getPlayer(2);

        final MissionCard mission = new MissionCard(_game, 101, player1, _cardLibrary.get("101_174"));
        final FacilityCard outpost1 = new FacilityCard(_game, 102, player1, _cardLibrary.get("101_105"));
        final PersonnelCard worf = new PersonnelCard(_game, 104, player1, _cardLibrary.get("155_079"));
        final PersonnelCard kehleyr1 = new PersonnelCard(_game, 105, player1, _cardLibrary.get("155_080"));
        final PersonnelCard kehleyr2 = new PersonnelCard(_game, 106, player2, _cardLibrary.get("101_217"));

        assertFalse(outpost1.isInPlay());
        assertEquals("Captain Worf", worf.getTitle()); // 8-6-10
        assertEquals("Commander K'Ehleyr", kehleyr1.getTitle()); // 7-8-7
        assertEquals("K'Ehleyr", kehleyr2.getTitle()); // 8-7-7

        _game.getGameState().addMissionLocationToSpaceline(mission, 0);
        _game.getGameState().seedFacilityAtLocation(outpost1, 0);

        assertTrue(outpost1.isInPlay());

        worf.reportToFacility(outpost1);
        assertEquals(Integer.valueOf(8), worf.getAttribute(CardAttribute.INTEGRITY));
        assertEquals(Integer.valueOf(6), worf.getAttribute(CardAttribute.CUNNING));
        assertEquals(Integer.valueOf(10), worf.getAttribute(CardAttribute.STRENGTH));

        assertEquals(Integer.valueOf(7), kehleyr1.getAttribute(CardAttribute.INTEGRITY));
        assertEquals(Integer.valueOf(8), kehleyr1.getAttribute(CardAttribute.CUNNING));
        assertEquals(Integer.valueOf(7), kehleyr1.getAttribute(CardAttribute.STRENGTH));

        assertEquals(Integer.valueOf(8), kehleyr2.getAttribute(CardAttribute.INTEGRITY));
        assertEquals(Integer.valueOf(7), kehleyr2.getAttribute(CardAttribute.CUNNING));
        assertEquals(Integer.valueOf(7), kehleyr2.getAttribute(CardAttribute.STRENGTH));


        kehleyr1.reportToFacility(outpost1);
        kehleyr2.reportToFacility(outpost1);
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