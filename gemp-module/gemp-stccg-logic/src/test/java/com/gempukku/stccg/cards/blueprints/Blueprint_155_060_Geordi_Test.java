package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_155_060_Geordi_Test extends AbstractAtTest {

    @Test
    public void planetSkillsTest() throws DecisionResultInvalidException, InvalidGameLogicException,
            CardNotFoundException, InvalidGameOperationException, PlayerNotFoundException {
        initializeQuickMissionAttempt("Excavation");

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission);
        assertEquals(_outpost.getLocationDeprecatedOnlyUseForTests(), _mission.getLocationDeprecatedOnlyUseForTests());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard geordi = (PersonnelCard) _game.addCardToGame("155_060", _cardLibrary, P1);
        PhysicalShipCard runabout =
                (PhysicalShipCard) _game.addCardToGame("101_331", _cardLibrary, P1);

        geordi.reportToFacilityForTestingOnly(_outpost);
        runabout.reportToFacilityForTestingOnly(_outpost);

        assertTrue(_outpost.getCrew().contains(geordi));
        assertFalse(_outpost.getCrew().contains(runabout));
        assertEquals(_outpost, runabout.getDockedAtCard(_game));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        beamCards(P1, _outpost, Collections.singleton(geordi), runabout);
        assertFalse(_outpost.getCrew().contains(geordi));
        assertTrue(runabout.getCrew().contains(geordi));
        assertEquals(MissionType.PLANET, geordi.getLocationDeprecatedOnlyUseForTests().getMissionType());
        assertEquals(0, geordi.getSkillLevel(SkillName.NAVIGATION));
        assertEquals(0, geordi.getSkillLevel(SkillName.ASTROPHYSICS));
        assertEquals(0, geordi.getSkillLevel(SkillName.STELLAR_CARTOGRAPHY));
        assertEquals(1, geordi.getSkillLevel(SkillName.ENGINEER));
        assertEquals(1, geordi.getSkillLevel(SkillName.PHYSICS));
        assertEquals(1, geordi.getSkillLevel(SkillName.COMPUTER_SKILL));
    }

    @Test
    public void spaceSkillsTest() throws DecisionResultInvalidException, InvalidGameLogicException,
            CardNotFoundException, InvalidGameOperationException, PlayerNotFoundException {
        initializeQuickMissionAttempt("Investigate Rogue Comet");

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission);
        assertEquals(_outpost.getLocationDeprecatedOnlyUseForTests(), _mission.getLocationDeprecatedOnlyUseForTests());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard geordi = (PersonnelCard) _game.addCardToGame("155_060", _cardLibrary, P1);
        PhysicalShipCard runabout =
                (PhysicalShipCard) _game.addCardToGame("101_331", _cardLibrary, P1);

        geordi.reportToFacilityForTestingOnly(_outpost);
        runabout.reportToFacilityForTestingOnly(_outpost);

        assertTrue(_outpost.getCrew().contains(geordi));
        assertFalse(_outpost.getCrew().contains(runabout));
        assertEquals(_outpost, runabout.getDockedAtCard(_game));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        beamCards(P1, _outpost, Collections.singleton(geordi), runabout);
        assertFalse(_outpost.getCrew().contains(geordi));
        assertTrue(runabout.getCrew().contains(geordi));
        assertEquals(MissionType.SPACE, geordi.getLocationDeprecatedOnlyUseForTests().getMissionType());
        assertEquals(1, geordi.getSkillLevel(SkillName.NAVIGATION));
        assertEquals(1, geordi.getSkillLevel(SkillName.ASTROPHYSICS));
        assertEquals(1, geordi.getSkillLevel(SkillName.STELLAR_CARTOGRAPHY));
        assertEquals(0, geordi.getSkillLevel(SkillName.ENGINEER));
        assertEquals(0, geordi.getSkillLevel(SkillName.PHYSICS));
        assertEquals(0, geordi.getSkillLevel(SkillName.COMPUTER_SKILL));
    }


}