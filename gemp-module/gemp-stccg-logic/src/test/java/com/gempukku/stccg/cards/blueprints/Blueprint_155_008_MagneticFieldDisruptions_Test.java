package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.game.GameTestBuilder;
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
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_155_008_MagneticFieldDisruptions_Test extends AbstractAtTest {

    private MissionCard _mission;
    private PhysicalCard disruptions;
    private Collection<PersonnelCard> attemptingPersonnel;

    private void initializeGame(boolean passFirstCondition, boolean passSecondCondition) throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission(MissionType.SPACE, Affiliation.FEDERATION, P1);
        disruptions = builder.addSeedCardUnderMission("155_008", "Magnetic Field Disruptions", P2, _mission);
        ShipCard runabout = builder.addShipInSpace("101_331", "Runabout", P1, _mission);
        attemptingPersonnel = new ArrayList<>();

        // Mot the Barber - neither needed skill; CUNNING = 4
        attemptingPersonnel.add(builder.addCardAboardShipOrFacility(
                "101_225", "Mot the Barber", P1, runabout, PersonnelCard.class));

        if (passFirstCondition) {
            // Wesley has needed skills; CUNNING = 8
            attemptingPersonnel.add(builder.addCardAboardShipOrFacility(
                    "101_249", "Wesley Crusher", P1, runabout, PersonnelCard.class));
        }

        if (passSecondCondition) {
            // add five copies of T'Lara with CUNNING = 8 each
            for (int i = 0; i < 5; i++) {
                attemptingPersonnel.add(builder.addCardAboardShipOrFacility(
                        "159_020", "T'Lara", P1, runabout, PersonnelCard.class));
            }
        }

        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void failFirstConditionTest() throws Exception {
        initializeGame(false, true);
        assertEquals(6, attemptingPersonnel.size());
        attemptMission(P1, _mission);

        int stoppedCount = 0;
        int killedCount = 0;

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                stoppedCount++;
            }
            if (personnelWasKilled(personnel)) {
                killedCount++;
            }
        }

        assertEquals(1, killedCount);
        assertEquals(5, stoppedCount);
        assertNotEquals(Zone.REMOVED, disruptions.getZone());
    }

    @Test
    public void failSecondConditionTest() throws Exception {
        initializeGame(true, false);
        assertEquals(2, attemptingPersonnel.size());
        attemptMission(P1, _mission);

        int stoppedCount = 0;
        int killedCount = 0;

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                stoppedCount++;
            }
            if (personnelWasKilled(personnel)) {
                killedCount++;
            }
        }

        assertEquals(0, killedCount);
        assertEquals(2, stoppedCount);
        assertNotEquals(Zone.REMOVED, disruptions.getZone());
    }

    @Test
    public void passDilemmaTest() throws Exception {
        initializeGame(true, true);
        assertEquals(7, attemptingPersonnel.size());
        attemptMission(P1, _mission);

        int stoppedCount = 0;
        int killedCount = 0;

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                stoppedCount++;
            }
            if (personnelWasKilled(personnel)) {
                killedCount++;
            }
        }

        assertEquals(0, killedCount);
        assertEquals(1, stoppedCount);
        assertEquals(Zone.REMOVED, disruptions.getZone());
    }

}