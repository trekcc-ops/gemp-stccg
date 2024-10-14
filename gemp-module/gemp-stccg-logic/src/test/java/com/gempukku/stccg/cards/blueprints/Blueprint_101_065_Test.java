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

public class Blueprint_101_065_Test extends AbstractAtTest {
    
    // Unit tests for card definition of Tricorder

    @Test
    @SuppressWarnings("SpellCheckingInspection")
    public void tricorderTest() {
        initializeSimple1EGame(30);
        Player player1 = _game.getPlayer(1);

        final MissionCard mission = new MissionCard(_game, 101, player1, _cardLibrary.get("101_174"));
        final PhysicalReportableCard1E tricorder = 
                new PhysicalReportableCard1E(_game, 102, player1, _cardLibrary.get("101_065"));
        final PersonnelCard geordi =
                new PersonnelCard(_game, 103, player1, _cardLibrary.get("101_212")); // ENGINEER class, no SCIENCE
        final PersonnelCard tamal =
                new PersonnelCard(_game, 104, player1, _cardLibrary.get("172_031")); // ENGINEER class + SCIENCE
        final PersonnelCard deanna =
                new PersonnelCard(_game, 105, player1, _cardLibrary.get("101_205")); // no ENGINEER or SCIENCE

        // Federation Outpost
        final FacilityCard outpost = new FacilityCard(_game, 104, player1, _cardLibrary.get("101_104"));

        assertFalse(outpost.isInPlay());
        assertEquals("Geordi La Forge", geordi.getTitle());
        assertEquals("Tamal", tamal.getTitle());
        assertEquals("Tricorder", tricorder.getTitle());
        assertEquals("Deanna Troi", deanna.getTitle());

        try {
            _game.getGameState().addToSpaceline(mission, 0, false);
        } catch(InvalidGameLogicException exp) {
            System.out.println(exp.getMessage());
        }
        _game.getGameState().seedFacilityAtLocation(outpost, 0);

        assertTrue(outpost.isInPlay());

        geordi.reportToFacility(outpost);
        tamal.reportToFacility(outpost);
        deanna.reportToFacility(outpost);

        assertFalse(geordi.hasSkill(SkillName.SCIENCE));
        assertFalse(deanna.hasSkill(SkillName.SCIENCE));
        assertEquals(0, geordi.getSkillLevel(SkillName.SCIENCE));
        assertEquals(1, tamal.getSkillLevel(SkillName.SCIENCE));

        tricorder.reportToFacility(outpost);
        assertTrue(tricorder.isPresentWith(geordi));
        assertTrue(tricorder.isPresentWith(tamal));
        assertTrue(tricorder.isPresentWith(deanna));

        assertTrue(geordi.hasSkill(SkillName.SCIENCE));
        assertFalse(deanna.hasSkill(SkillName.SCIENCE));
        assertEquals(1, geordi.getSkillLevel(SkillName.SCIENCE));
        assertEquals(2, tamal.getSkillLevel(SkillName.SCIENCE));
    }
}