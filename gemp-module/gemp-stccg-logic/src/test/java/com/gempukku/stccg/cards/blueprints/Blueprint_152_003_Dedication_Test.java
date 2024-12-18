package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalShipCard;
import com.gempukku.stccg.cards.physicalcard.ST1EPhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_152_003_Dedication_Test extends AbstractAtTest {
    
    // Unit tests for card definition of Dedication to Duty

    @Test
    public void dedicationToDutyTest1() throws DecisionResultInvalidException {
        initializeQuickMissionAttempt("Investigate Rogue Comet");
        assertNotNull(_mission);

        ST1EPhysicalCard dedication =
                new ST1EPhysicalCard(_game, 901, _game.getPlayer(P1), _cardLibrary.get("152_003"));
        dedication.setZone(Zone.VOID);
        assertEquals("Dedication to Duty", dedication.getTitle());

        // Seed Dedication to Duty
        _game.getGameState().seedCardsUnder(Collections.singleton(dedication), _mission);

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission.getLocation());
        assertEquals(_outpost.getLocation(), _mission.getLocation());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard troi = new PersonnelCard(_game, 902, _game.getPlayer(P1), _cardLibrary.get("101_205"));
        PhysicalShipCard runabout =
                new PhysicalShipCard(_game, 906, _game.getPlayer(P1), _cardLibrary.get("101_331"));

        troi.reportToFacility(_outpost);
        runabout.reportToFacility(_outpost);

        assertTrue(_outpost.getCrew().contains(troi));
        assertFalse(_outpost.getCrew().contains(runabout));
        assertEquals(_outpost, runabout.getDockedAtCard());
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        List<PersonnelCard> personnelBeaming = new ArrayList<>();
        personnelBeaming.add(troi);

        beamCards(P1, _outpost, personnelBeaming, runabout);
        for (PersonnelCard card : personnelBeaming) {
            assertTrue(runabout.getCrew().contains(card));
            assertFalse(_outpost.getCrew().contains(card));
        }

        undockShip(P1, runabout);
        assertFalse(runabout.isDocked());

        attemptMission(P1, runabout, _mission);
        assertTrue(troi.isStopped());

        playerDecided(P1, "0");
        assertFalse(runabout.getCrew().contains(troi));
        assertEquals(Zone.DISCARD, troi.getZone());
        assertFalse(runabout.isStopped());
    }

    @Test
    public void dedicationToDutyTest2() throws DecisionResultInvalidException {
        initializeQuickMissionAttempt("Investigate Rogue Comet");
        assertNotNull(_mission);

        ST1EPhysicalCard dedication =
                new ST1EPhysicalCard(_game, 901, _game.getPlayer(P1), _cardLibrary.get("152_003"));
        dedication.setZone(Zone.VOID);
        assertEquals("Dedication to Duty", dedication.getTitle());

        // Seed Dedication to Duty
        _game.getGameState().seedCardsUnder(Collections.singleton(dedication), _mission);

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission.getLocation());
        assertEquals(_outpost.getLocation(), _mission.getLocation());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PersonnelCard troi = new PersonnelCard(_game, 902, _game.getPlayer(P1), _cardLibrary.get("101_205"));
        PhysicalShipCard runabout =
                new PhysicalShipCard(_game, 906, _game.getPlayer(P1), _cardLibrary.get("101_331"));

        troi.reportToFacility(_outpost);
        runabout.reportToFacility(_outpost);

        assertTrue(_outpost.getCrew().contains(troi));
        assertFalse(_outpost.getCrew().contains(runabout));
        assertEquals(_outpost, runabout.getDockedAtCard());
        skipCardPlay();
        assertEquals(Phase.EXECUTE_ORDERS, _game.getCurrentPhase());

        List<PersonnelCard> personnelBeaming = new ArrayList<>();
        personnelBeaming.add(troi);

        beamCards(P1, _outpost, personnelBeaming, runabout);
        for (PersonnelCard card : personnelBeaming) {
            assertTrue(runabout.getCrew().contains(card));
            assertFalse(_outpost.getCrew().contains(card));
        }

        undockShip(P1, runabout);
        assertFalse(runabout.isDocked());

        attemptMission(P1, runabout, _mission);
        assertTrue(troi.isStopped());
        int handSizeBefore = _game.getGameState().getHand(P2).size();

        playerDecided(P1, "1");
        int handSizeAfter = _game.getGameState().getHand(P2).size();

        assertTrue(runabout.getCrew().contains(troi));
        assertEquals(handSizeBefore, handSizeAfter - 2);
        assertFalse(runabout.isStopped());
    }

}