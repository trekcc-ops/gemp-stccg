package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.SkillName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_155_060_Geordi_Test extends AbstractAtTest {

    @Test
    public void planetSkillsTest() throws DecisionResultInvalidException {
        initializeQuickMissionAttempt("Excavation");

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission.getLocation());
        assertEquals(_outpost.getLocation(), _mission.getLocation());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard geordi = new PersonnelCard(_game, 902, _game.getPlayer(P1), _cardLibrary.get("155_060"));
        PhysicalShipCard runabout =
                new PhysicalShipCard(_game, 906, _game.getPlayer(P1), _cardLibrary.get("101_331"));

        geordi.reportToFacility(_outpost);
        runabout.reportToFacility(_outpost);

        assertTrue(_outpost.getCrew().contains(geordi));
        assertFalse(_outpost.getCrew().contains(runabout));
        assertEquals(_outpost, runabout.getDockedAtCard());
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        beamCards(P1, _outpost, Collections.singleton(geordi), runabout);
        assertFalse(_outpost.getCrew().contains(geordi));
        assertTrue(runabout.getCrew().contains(geordi));
        assertEquals(MissionType.PLANET, geordi.getLocation().getMissionType());
        assertEquals(0, geordi.getSkillLevel(SkillName.NAVIGATION));
        assertEquals(0, geordi.getSkillLevel(SkillName.ASTROPHYSICS));
        assertEquals(0, geordi.getSkillLevel(SkillName.STELLAR_CARTOGRAPHY));
        assertEquals(1, geordi.getSkillLevel(SkillName.ENGINEER));
        assertEquals(1, geordi.getSkillLevel(SkillName.PHYSICS));
        assertEquals(1, geordi.getSkillLevel(SkillName.COMPUTER_SKILL));
    }

    @Test
    public void spaceSkillsTest() throws DecisionResultInvalidException {
        initializeQuickMissionAttempt("Investigate Rogue Comet");

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission.getLocation());
        assertEquals(_outpost.getLocation(), _mission.getLocation());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard geordi = new PersonnelCard(_game, 902, _game.getPlayer(P1), _cardLibrary.get("155_060"));
        PhysicalShipCard runabout =
                new PhysicalShipCard(_game, 906, _game.getPlayer(P1), _cardLibrary.get("101_331"));

        geordi.reportToFacility(_outpost);
        runabout.reportToFacility(_outpost);

        assertTrue(_outpost.getCrew().contains(geordi));
        assertFalse(_outpost.getCrew().contains(runabout));
        assertEquals(_outpost, runabout.getDockedAtCard());
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        beamCards(P1, _outpost, Collections.singleton(geordi), runabout);
        assertFalse(_outpost.getCrew().contains(geordi));
        assertTrue(runabout.getCrew().contains(geordi));
        assertEquals(MissionType.SPACE, geordi.getLocation().getMissionType());
        assertEquals(1, geordi.getSkillLevel(SkillName.NAVIGATION));
        assertEquals(1, geordi.getSkillLevel(SkillName.ASTROPHYSICS));
        assertEquals(1, geordi.getSkillLevel(SkillName.STELLAR_CARTOGRAPHY));
        assertEquals(0, geordi.getSkillLevel(SkillName.ENGINEER));
        assertEquals(0, geordi.getSkillLevel(SkillName.PHYSICS));
        assertEquals(0, geordi.getSkillLevel(SkillName.COMPUTER_SKILL));
    }


}