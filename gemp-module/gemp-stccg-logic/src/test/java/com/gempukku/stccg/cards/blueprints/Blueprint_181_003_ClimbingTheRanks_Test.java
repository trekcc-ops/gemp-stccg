package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.filterable.*;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_181_003_ClimbingTheRanks_Test extends AbstractAtTest {

    private MissionCard mission;
    private PhysicalCard climbing;
    private List<PersonnelCard> attemptingPersonnel;
    private List<PersonnelCard> targetPersonnel;

    private void initializeGame(boolean eligibleTargets, boolean requirementsMet) throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission(MissionType.SPACE, Affiliation.FEDERATION, P1);
        climbing = builder.addSeedCardUnderMission("181_003", "Climbing the Ranks", P1, mission);
        ShipCard runabout = builder.addShipInSpace("101_331", "Runabout", P1, mission);

        /*
            If eligibleTargets = TRUE:
        // Giusti 7/6/4 OFFICER + no Leadership - include 3 (total STR 12) to fail, 4 to pass
            If eligibleTargets = FALSE:
        Henreid 6/6/7 OFFICER + Leadership - include 1 (total STR 7) to fail, 2 to pass

            In both scenarios: (total STR 14)
        // Henreid 6/6/7 OFFICER + Leadership
        // K'Ehleyr 8/7/7 no OFFICER + no Leadership
         */
        attemptingPersonnel = new ArrayList<>();
        targetPersonnel = new ArrayList<>();

        if (eligibleTargets) {
            for (int i = 0; i < ((requirementsMet) ? 4 : 3); i++) {
                PersonnelCard giusti = builder.addCardAboardShipOrFacility(
                        "101_213", "Giusti", P1, runabout, PersonnelCard.class);
                attemptingPersonnel.add(giusti);
                targetPersonnel.add(giusti);
            }
        } else {
            for (int i = 0; i < ((requirementsMet) ? 2 : 1); i++) {
                attemptingPersonnel.add(builder.addCardAboardShipOrFacility(
                        "126_052", "Henreid", P1, runabout, PersonnelCard.class));
            }
        }
        attemptingPersonnel.add(builder.addCardAboardShipOrFacility(
                "126_052", "Henreid", P1, runabout, PersonnelCard.class));
        attemptingPersonnel.add(builder.addCardAboardShipOrFacility(
                "101_217", "K'Ehleyr", P1, runabout, PersonnelCard.class));

        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void passWithEligibleTargetsTest() throws Exception {
        initializeGame(true, true);
        attemptMission(P1, mission);

        assertTrue(selectableCardsAre(P2, targetPersonnel));
        PersonnelCard target = targetPersonnel.getFirst();
        selectCard(P2, target);

        assertTrue(personnelWasKilledAndDiscarded(target));
        assertEquals(Zone.REMOVED, climbing.getZone());
    }

    @Test
    public void failWithEligibleTargetsTest() throws Exception {
        initializeGame(true, false);
        attemptMission(P1, mission);

        assertTrue(selectableCardsAre(P2, targetPersonnel));
        PersonnelCard target = targetPersonnel.getFirst();
        selectCard(P2, target);

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel == target) {
                assertTrue(personnelWasKilledAndDiscarded(personnel));
            } else {
                assertTrue(personnel.isStopped());
            }
        }

        assertNotEquals(Zone.REMOVED, climbing.getZone());
    }

    @Test
    public void passWithNoTargetsTest() throws Exception {
        initializeGame(false, true);
        attemptMission(P1, mission);

        for (PersonnelCard personnel : attemptingPersonnel) {
            assertFalse(personnelWasKilledAndDiscarded(personnel));
            assertFalse(personnel.isStopped());
        }

        assertEquals(Zone.REMOVED, climbing.getZone());
    }

    @Test
    public void failWithNoTargetsTest() throws Exception {
        initializeGame(false, false);
        attemptMission(P1, mission);

        for (PersonnelCard personnel : attemptingPersonnel) {
            assertFalse(personnelWasKilledAndDiscarded(personnel));
            assertTrue(personnel.isStopped());
        }

        assertNotEquals(Zone.REMOVED, climbing.getZone());
    }


}