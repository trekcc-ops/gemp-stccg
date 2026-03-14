package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.game.GameTestBuilder;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_111_002_ArmusStickySituation_Test extends AbstractAtTest {

    private MissionCard mission;
    private PhysicalCard armus;
    private PersonnelCard riker;
    private PersonnelCard secondPersonnel;

    private void initializeGame(boolean includeSecondPersonnel, boolean differentStrength, boolean requirementsMet)
            throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        mission = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P1);
        armus = builder.addSeedCardUnderMission("111_002", "Armus: Sticky Situation", P1, mission);

            // William T. Riker - Diplomacy, CUNNING 7, STRENGTH 7
        riker = builder.addCardOnPlanetSurface("101_250", "William T. Riker", P1, mission, PersonnelCard.class);

        if (includeSecondPersonnel) {
            if (differentStrength && requirementsMet) {
                // Devinoni Ral - Diplomacy, CUNNING 9, STRENGTH 3
                secondPersonnel = builder.addCardOnPlanetSurface("101_292", "Devinoni Ral", P1, mission, PersonnelCard.class);
            } else if (differentStrength && !requirementsMet) {
                // Jace Michaels - Diplomacy, CUNNING 7, STRENGTH 6
                secondPersonnel = builder.addCardOnPlanetSurface("112_207", "Jace Michaels", P1, mission, PersonnelCard.class);
            } else if (!differentStrength && requirementsMet) {
                // Spock - Diplomacy, CUNNING 10, STRENGTH 7
                secondPersonnel = builder.addCardOnPlanetSurface("106_018", "Spock", P1, mission, PersonnelCard.class);
            } else if (!differentStrength && !requirementsMet) {
                throw new RuntimeException("Not writing a test for this outcome");
            }
        } else {
            secondPersonnel = null;
        }

        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    // attempt with one personnel
    // attempt with two personnel of equal STRENGTH; Diplomacy + CUNNING>7
    // attempt with personnel of different STRENGTH

    @Test
    public void attemptWithOnePersonnelTest() throws Exception {
        // If only one personnel, they are automatically selected to be killed and the dilemma is re-seeded

        initializeGame(false, false, false);
        attemptMission(P1, mission);

        assertTrue(personnelWasKilledAndDiscarded(riker));
        assertNotEquals(Zone.REMOVED, armus.getZone());
    }

    @Test
    public void attemptWithEqualStrengthKillTest() throws Exception {
        // P2 selects the personnel with Diplomacy + CUNNING>7; they are killed, and dilemma is discarded

        initializeGame(true, false, true);
        attemptMission(P1, mission);
        selectCard(P2, secondPersonnel);

        assertTrue(personnelWasKilledAndDiscarded(secondPersonnel));
        assertEquals(Zone.REMOVED, armus.getZone());
    }

    @Test
    public void attemptWithEqualStrengthStopTest() throws Exception {
        // P2 selects the personnel with Diplomacy + CUNNING>7; they are stopped, and dilemma is discarded

        initializeGame(true, false, true);
        attemptMission(P1, mission);
        selectCard(P2, riker);

        assertFalse(personnelWasKilledAndDiscarded(secondPersonnel));
        assertTrue(riker.isStopped());
        assertEquals(Zone.REMOVED, armus.getZone());
    }

    @Test
    public void attemptWithDifferentStrengthTest() throws Exception {
        // If there's not a tie for highest STRENGTH, P2 doesn't select a card

        initializeGame(true, true, true);
        attemptMission(P1, mission);
        assertNull(_game.getAwaitingDecision(P2));
    }


}