package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalReportableCard1E;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_101_060_Test extends AbstractAtTest {
    
    // Unit tests for card definition of Medical Kit

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    public void medicalKitTest() throws InvalidGameLogicException {
        initializeSimple1EGame(30);
        Player player1 = _game.getPlayer(1);

        final MissionCard mission = new MissionCard(_game, 101, player1, _cardLibrary.get("101_174"));
        final PhysicalReportableCard1E medicalKit =
                new PhysicalReportableCard1E(_game, 102, player1, _cardLibrary.get("101_060"));
        final PersonnelCard picard =
                new PersonnelCard(_game, 103, player1, _cardLibrary.get("101_215")); // OFFICER class, no MEDICAL
        final PersonnelCard taris =
                new PersonnelCard(_game, 104, player1, _cardLibrary.get("105_085")); // OFFICER class + MEDICAL

        // Federation Outpost
        final FacilityCard outpost = new FacilityCard(_game, 104, player1, _cardLibrary.get("101_104"));

        assertFalse(outpost.isInPlay());
        assertEquals("Jean-Luc Picard", picard.getTitle());
        assertEquals("Taris", taris.getTitle());
        assertEquals("Medical Kit", medicalKit.getTitle());

        _game.getGameState().addMissionLocationToSpaceline(mission, 0);
        _game.getGameState().seedFacilityAtLocation(outpost, 0);

        assertTrue(outpost.isInPlay());

        picard.reportToFacility(outpost);
        taris.reportToFacility(outpost);

        assertEquals(0, picard.getSkillLevel(SkillName.MEDICAL));
        assertEquals(1, taris.getSkillLevel(SkillName.MEDICAL));

        medicalKit.reportToFacility(outpost);
        assertTrue(medicalKit.isPresentWith(picard));
        assertTrue(medicalKit.isPresentWith(taris));

        assertEquals(1, picard.getSkillLevel(SkillName.MEDICAL));
        assertEquals(2, taris.getSkillLevel(SkillName.MEDICAL));
    }
}