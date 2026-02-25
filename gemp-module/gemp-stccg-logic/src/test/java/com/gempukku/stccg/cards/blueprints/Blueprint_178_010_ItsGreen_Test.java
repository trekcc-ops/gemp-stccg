package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class Blueprint_178_010_ItsGreen_Test extends AbstractAtTest {

    private PhysicalCard itsGreen;
    private MissionCard mission;
    private List<PersonnelCard> attemptingPersonnel;
    private ShipCard runabout;

    private void initializeGame(int crewSize, boolean includeEngineer) throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission(MissionType.SPACE, Affiliation.FEDERATION, P1);
        itsGreen = builder.addSeedCardUnderMission("178_010", "It's Green", P1, mission);
        attemptingPersonnel = new ArrayList<>();
        runabout = builder.addShipInSpace("101_331", "Runabout", P1, mission);

        for (int i = 0; i < crewSize; i++) {
            if (includeEngineer) {
                attemptingPersonnel.add(builder.addCardAboardShipOrFacility(
                        "101_220", "Linda Larson", P1, runabout, PersonnelCard.class));
            } else {
                attemptingPersonnel.add(builder.addCardAboardShipOrFacility(
                        "101_242", "Taitt", P1, runabout, PersonnelCard.class));
            }
        }
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void attemptWithTenEngineerTest() throws Exception {
        // If crew has size 10 and selection has ENGINEER, six personnel are stopped and dilemma is removed from game
        initializeGame(10, true);
        attemptMission(P1, mission);

        int stoppedPersonnelCount = 0;

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                stoppedPersonnelCount++;
            }
        }

        assertEquals(6, stoppedPersonnelCount);
        assertEquals(Zone.REMOVED, itsGreen.getZone());
    }

    @Test
    public void attemptWithNineEngineerTest() throws Exception {
        // If crew has size 9 and selection has ENGINEER, three personnel are stopped and dilemma is removed from game
        initializeGame(9, true);
        attemptMission(P1, mission);

        int stoppedPersonnelCount = 0;

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                stoppedPersonnelCount++;
            }
        }

        assertEquals(3, stoppedPersonnelCount);
        assertEquals(Zone.REMOVED, itsGreen.getZone());
    }

    @Test
    public void attemptWithTwoEngineerTest() throws Exception {
        // If crew has size 2 and selection has ENGINEER, they are both stopped and dilemma is removed from game
        initializeGame(2, true);
        attemptMission(P1, mission);

        int stoppedPersonnelCount = 0;

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                stoppedPersonnelCount++;
            }
        }

        assertEquals(2, stoppedPersonnelCount);

        // Ship not stopped because dilemma was not failed
        assertFalse(runabout.isStopped());

        assertEquals(Zone.REMOVED, itsGreen.getZone());
    }

    @Test
    public void attemptWithNoEngineerTest() throws Exception {
        // If there are no ENGINEERs, nobody is stopped and dilemma is overcome
        initializeGame(5, false);
        attemptMission(P1, mission);

        int stoppedPersonnelCount = 0;

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                stoppedPersonnelCount++;
            }
        }

        assertEquals(0, stoppedPersonnelCount);
        assertEquals(Zone.REMOVED, itsGreen.getZone());
    }


}