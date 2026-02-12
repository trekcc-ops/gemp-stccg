package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_109_010_Maglock_Test extends AbstractAtTest {
    
    // Unit tests for card definition of Maglock
    private FacilityCard outpost;
    private MissionCard _mission;
    private PhysicalCard _maglock;
    private PersonnelCard troi;
    private PersonnelCard data;
    private ShipCard runabout;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_171", "Investigate Rogue Comet", P1);
        outpost = builder.addOutpost(Affiliation.FEDERATION, P1); // Federation Outpost
        _maglock = builder.addSeedCardUnderMission("109_010", "Maglock", P2, _mission);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        runabout = builder.addDockedShip("101_331", "Runabout", P1, outpost);
        data = builder.addCardAboardShipOrFacility("101_204", "Data", P1, runabout, PersonnelCard.class);
        troi = builder.addCardAboardShipOrFacility("101_205", "Deanna Troi", P1, runabout, PersonnelCard.class);
        builder.addCardAboardShipOrFacility("101_202", "Christopher Hobson", P1, runabout, PersonnelCard.class);
        builder.addCardAboardShipOrFacility("101_215", "Jean-Luc Picard", P1, runabout, PersonnelCard.class);
        builder.startGame();
    }


    @Test
    public void maglockFailedTest() throws DecisionResultInvalidException,
            CardNotFoundException, InvalidGameOperationException {

        initializeGame();

        // leave Data at the outpost to not meet the 3 OFFICER with STRENGTH >5 requirement
        beamCards(P1, runabout, List.of(data), outpost);
        assertFalse(runabout.hasCardInCrew(data));

        undockShip(P1, runabout);
        assertFalse(runabout.isDocked());

        attemptMission(P1, runabout, _mission);
        for (PersonnelCard personnel : runabout.getAttemptingPersonnel(_game)) {
            assertTrue(personnel.isStopped());
        }
        assertTrue(runabout.isStopped());
        assertFalse(_mission.getLocationDeprecatedOnlyUseForTests(_game).isCompleted());
        assertTrue(_mission.getLocationDeprecatedOnlyUseForTests(_game).getSeedCards().contains(_maglock));
    }

    @Test
    public void maglockPassedTest()
            throws DecisionResultInvalidException, CardNotFoundException, InvalidGameOperationException {

        initializeGame();

        // Leave Troi at the outpost; still meet requirement
        beamCards(P1, runabout, List.of(troi), outpost);
        assertFalse(runabout.hasCardInCrew(troi));

        undockShip(P1, runabout);
        assertFalse(runabout.isDocked());

        attemptMission(P1, runabout, _mission);
        for (PersonnelCard personnel : runabout.getAttemptingPersonnel(_game)) {
            assertFalse(personnel.isStopped());
        }
        assertFalse(runabout.isStopped());
        assertTrue(_mission.getLocationDeprecatedOnlyUseForTests(_game).isCompleted());
        assertFalse(_mission.getLocationDeprecatedOnlyUseForTests(_game).getSeedCards().contains(_maglock));
        assertEquals(Zone.REMOVED, _maglock.getZone());
    }

}