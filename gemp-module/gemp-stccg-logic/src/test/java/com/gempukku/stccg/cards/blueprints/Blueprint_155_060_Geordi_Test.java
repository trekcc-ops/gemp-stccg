package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
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
        assertEquals(_outpost.getLocationDeprecatedOnlyUseForTests(_game), _mission.getLocationDeprecatedOnlyUseForTests(_game));
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard geordi = (PersonnelCard) _game.addCardToGame("155_060", P1);
        ShipCard runabout =
                (ShipCard) _game.addCardToGame("101_331", P1);

        reportCardsToFacility(_outpost, geordi, runabout);

        assertTrue(_outpost.hasCardInCrew(geordi));
        assertFalse(_outpost.hasCardInCrew(runabout));
        assertEquals(_outpost, runabout.getDockedAtCard(_game));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        beamCards(P1, _outpost, Collections.singleton(geordi), runabout);
        assertFalse(_outpost.hasCardInCrew(geordi));
        assertTrue(runabout.hasCardInCrew(geordi));
        assertEquals(MissionType.PLANET, geordi.getLocationDeprecatedOnlyUseForTests(_game).getMissionType());
        assertEquals(0, geordi.getSkillLevel(_game, SkillName.NAVIGATION));
        assertEquals(0, geordi.getSkillLevel(_game, SkillName.ASTROPHYSICS));
        assertEquals(0, geordi.getSkillLevel(_game, SkillName.STELLAR_CARTOGRAPHY));
        assertEquals(1, geordi.getSkillLevel(_game, SkillName.ENGINEER));
        assertEquals(1, geordi.getSkillLevel(_game, SkillName.PHYSICS));
        assertEquals(1, geordi.getSkillLevel(_game, SkillName.COMPUTER_SKILL));
    }

    @Test
    public void spaceSkillsTest() throws DecisionResultInvalidException, InvalidGameLogicException,
            CardNotFoundException, InvalidGameOperationException, PlayerNotFoundException {
        initializeQuickMissionAttempt("Investigate Rogue Comet");

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission);
        assertEquals(_outpost.getLocationDeprecatedOnlyUseForTests(_game), _mission.getLocationDeprecatedOnlyUseForTests(_game));
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard geordi = (PersonnelCard) _game.addCardToGame("155_060", P1);
        ShipCard runabout =
                (ShipCard) _game.addCardToGame("101_331", P1);

        reportCardsToFacility(_outpost, geordi, runabout);

        assertTrue(_outpost.hasCardInCrew(geordi));
        assertFalse(_outpost.hasCardInCrew(runabout));
        assertEquals(_outpost, runabout.getDockedAtCard(_game));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        beamCards(P1, _outpost, Collections.singleton(geordi), runabout);
        assertFalse(_outpost.hasCardInCrew(geordi));
        assertTrue(runabout.hasCardInCrew(geordi));
        assertEquals(MissionType.SPACE, geordi.getLocationDeprecatedOnlyUseForTests(_game).getMissionType());
        assertEquals(1, geordi.getSkillLevel(_game, SkillName.NAVIGATION));
        assertEquals(1, geordi.getSkillLevel(_game, SkillName.ASTROPHYSICS));
        assertEquals(1, geordi.getSkillLevel(_game, SkillName.STELLAR_CARTOGRAPHY));
        assertEquals(0, geordi.getSkillLevel(_game, SkillName.ENGINEER));
        assertEquals(0, geordi.getSkillLevel(_game, SkillName.PHYSICS));
        assertEquals(0, geordi.getSkillLevel(_game, SkillName.COMPUTER_SKILL));
    }


}