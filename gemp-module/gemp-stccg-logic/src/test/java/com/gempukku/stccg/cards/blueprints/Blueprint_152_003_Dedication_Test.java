package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.gamestate.MissionLocation;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_152_003_Dedication_Test extends AbstractAtTest {
    
    // Unit tests for card definition of Dedication to Duty

    @Test
    public void dedicationToDutyTest1() throws Exception {
        initializeQuickMissionAttempt("Investigate Rogue Comet");
        assertNotNull(_mission);

        ST1EPhysicalCard dedication =
                (ST1EPhysicalCard) _game.addCardToGame("152_003", P1);
        dedication.setZone(Zone.VOID);
        assertEquals("Dedication to Duty", dedication.getTitle());

        // Seed Dedication to Duty
        MissionLocation missionLocation = _mission.getLocationDeprecatedOnlyUseForTests(_game);
        seedCardsUnder(Collections.singleton(dedication), _mission);

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission);
        assertEquals(_outpost.getLocationDeprecatedOnlyUseForTests(_game), _mission.getLocationDeprecatedOnlyUseForTests(_game));
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard troi = (PersonnelCard) _game.addCardToGame("101_205", P1);
        ShipCard runabout =
                (ShipCard) _game.addCardToGame("101_331", P1);

        reportCardsToFacility(_outpost, troi, runabout);

        assertTrue(_outpost.hasCardInCrew(troi));
        assertFalse(_outpost.hasCardInCrew(runabout));
        assertEquals(_outpost, runabout.getDockedAtCard(_game));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        List<PersonnelCard> personnelBeaming = new ArrayList<>();
        personnelBeaming.add(troi);

        beamCards(P1, _outpost, personnelBeaming, runabout);
        for (PersonnelCard card : personnelBeaming) {
            assertTrue(runabout.hasCardInCrew(card));
            assertFalse(_outpost.hasCardInCrew(card));
        }

        undockShip(P1, runabout);
        assertFalse(runabout.isDocked());

        attemptMission(P1, runabout, _mission);
        assertTrue(troi.isStopped());

        playerDecided(P1, "0");
        assertFalse(runabout.hasCardInCrew(troi));
        assertEquals(Zone.DISCARD, troi.getZone());
        assertFalse(runabout.isStopped());
    }

    @Test
    public void dedicationToDutyTest2() throws Exception {
        initializeQuickMissionAttempt("Investigate Rogue Comet");
        assertNotNull(_mission);

        ST1EPhysicalCard dedication =
                (ST1EPhysicalCard) _game.addCardToGame("152_003", P1);
        dedication.setZone(Zone.VOID);
        assertEquals("Dedication to Duty", dedication.getTitle());

        // Seed Dedication to Duty
        MissionLocation missionLocation = _mission.getLocationDeprecatedOnlyUseForTests(_game);
        seedCardsUnder(Collections.singleton(dedication), _mission);

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission);
        assertEquals(_outpost.getLocationDeprecatedOnlyUseForTests(_game), _mission.getLocationDeprecatedOnlyUseForTests(_game));
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard troi = (PersonnelCard) _game.addCardToGame("101_205", P1);
        ShipCard runabout =
                (ShipCard) _game.addCardToGame("101_331", P1);

        reportCardsToFacility(_outpost, troi, runabout);

        assertTrue(_outpost.hasCardInCrew(troi));
        assertFalse(_outpost.hasCardInCrew(runabout));
        assertEquals(_outpost, runabout.getDockedAtCard(_game));
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        List<PersonnelCard> personnelBeaming = new ArrayList<>();
        personnelBeaming.add(troi);

        beamCards(P1, _outpost, personnelBeaming, runabout);
        for (PersonnelCard card : personnelBeaming) {
            assertTrue(runabout.hasCardInCrew(card));
            assertFalse(_outpost.hasCardInCrew(card));
        }

        undockShip(P1, runabout);
        assertFalse(runabout.isDocked());

        attemptMission(P1, runabout, _mission);
        assertTrue(troi.isStopped());
        int handSizeBefore = _game.getPlayer(P2).getCardsInHand().size();

        playerDecided(P1, "1");
        int handSizeAfter = _game.getPlayer(P2).getCardsInHand().size();

        assertTrue(runabout.hasCardInCrew(troi));
        assertEquals(handSizeBefore, handSizeAfter - 2);
        assertFalse(runabout.isStopped());
    }

}