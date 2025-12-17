package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.EquipmentCard;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.gamestate.ST1EGameState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_101_057_FedPADD_Test extends AbstractAtTest {
    
    // Unit tests for card definition of Federation PADD

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    public void fedPADDTest() throws Exception {
        initializeSimple1EGame(30);
        ST1EGameState gameState = _game.getGameState();

        MissionCard mission = (MissionCard) newCardForGame("101_174", P1);
        EquipmentCard padd = (EquipmentCard) newCardForGame("101_057", P1);
        PersonnelCard picard = (PersonnelCard) newCardForGame("101_215", P1);

        assertNotNull(picard);
        assertNotNull(padd);

        // Federation Outpost
        FacilityCard outpost = (FacilityCard) newCardForGame("101_104", P1);

        assertFalse(outpost.isInPlay());
        assertEquals("Jean-Luc Picard", picard.getTitle());
        assertEquals("Federation PADD", padd.getTitle());
        assertNotNull(mission);

        gameState.addMissionLocationToSpacelineForTestingOnly(_game, mission, 0);
        _game.getGameState().seedFacilityAtLocationForTestingOnly(_game, outpost, mission);

        assertTrue(outpost.isInPlay());

        reportCardsToFacility(outpost, picard);
        assertEquals(8, picard.getCunning(_game));

        reportCardsToFacility(outpost, padd);
        assertTrue(gameState.cardsArePresentWithEachOther(picard, padd));
        assertEquals(10, picard.getCunning(_game));

    }
}