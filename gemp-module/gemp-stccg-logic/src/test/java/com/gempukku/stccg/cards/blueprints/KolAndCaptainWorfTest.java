package com.gempukku.stccg.cards.blueprints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.List;

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
        _game.getGameState().seedFacilityAtLocationForTestingOnly(_game, outpost, mission);

        assertTrue(outpost.isInPlay());

        reportCardToFacility(kol, outpost);
        assertTrue(personnelAttributesAre(kol, List.of(6, 6, 5)));
        assertTrue(personnelAttributesAre(arridor, List.of(4, 8, 5)));

        reportCardToFacility(arridor, outpost);
        assertTrue(_game.getGameState().cardsArePresentWithEachOther(arridor, kol));

        assertTrue(personnelAttributesAre(kol, List.of(8, 8, 7)));
        assertTrue(personnelAttributesAre(arridor, List.of(4, 8, 5)));

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
        _game.getGameState().seedFacilityAtLocationForTestingOnly(_game, outpost1, mission);

        assertTrue(outpost1.isInPlay());

        reportCardToFacility(worf, outpost1);
        assertTrue(personnelAttributesAre(worf, List.of(8, 6, 10)));
        assertTrue(personnelAttributesAre(kehleyr1, List.of(7, 8, 7)));
        assertTrue(personnelAttributesAre(kehleyr2, List.of(8, 7, 7)));

        reportCardsToFacility(outpost1, kehleyr1, kehleyr2);
        assertTrue(_game.getGameState().getAllCardsInPlay().contains(kehleyr1));
        assertTrue(_game.getGameState().getAllCardsInPlay().contains(kehleyr2));
        assertTrue(_game.getGameState().cardsArePresentWithEachOther(kehleyr1, worf));

        assertTrue(personnelAttributesAre(worf, List.of(10, 8, 12)));
        assertTrue(personnelAttributesAre(kehleyr1, List.of(9, 10, 9)));
        assertTrue(personnelAttributesAre(kehleyr2, List.of(10, 9, 9)));
    }

}