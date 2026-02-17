package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_155_012_TenseNegotiations_Test extends AbstractAtTest {

    private MissionCard _mission;
    private PhysicalCard negotiations;
    private Collection<PersonnelCard> attemptingPersonnel;
    private PersonnelCard jace1;
    private PersonnelCard jace2;
    private PersonnelCard jace3;
    private PersonnelCard taylor;
    private PersonnelCard wallace;
    private PersonnelCard sarek;
    private void initializeGame(boolean ableToNullify, boolean ableToOvercome)
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        attemptingPersonnel = new ArrayList<>();
        _mission = builder.addMission("101_171", "Investigate Rogue Comet", P1);
        ShipCard runabout = builder.addShipInSpace("101_331", "Runabout", P1, _mission);
        if (ableToNullify) {
            sarek = builder.addCardAboardShipOrFacility("101_233", "Sarek", P1, runabout, PersonnelCard.class);
            attemptingPersonnel.add(sarek);
        }
        if (ableToOvercome) {
            jace1 = builder.addCardAboardShipOrFacility("112_207", "Jace Michaels", P1, runabout, PersonnelCard.class);
            attemptingPersonnel.add(jace1);
            jace2 = builder.addCardAboardShipOrFacility("112_207", "Jace Michaels", P1, runabout, PersonnelCard.class);
            attemptingPersonnel.add(jace2);
            jace3 = builder.addCardAboardShipOrFacility("112_207", "Jace Michaels", P1, runabout, PersonnelCard.class);
            attemptingPersonnel.add(jace3);
            taylor = builder.addCardAboardShipOrFacility("112_207", "Jace Michaels", P1, runabout, PersonnelCard.class);
            attemptingPersonnel.add(taylor);
        }
        wallace = builder.addCardAboardShipOrFacility("101_203", "Darian Wallace", P1, runabout, PersonnelCard.class);
        attemptingPersonnel.add(wallace);

        negotiations = builder.addSeedCardUnderMission("155_012", "Tense Negotiations", P2, _mission);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void nullifyDilemmaTest() throws Exception {
        initializeGame(true, false);
        assertEquals(2, attemptingPersonnel.size());
        attemptMission(P1, _mission);

        // Verify dilemma was nullified
        assertEquals(Zone.REMOVED, negotiations.getZone());
        assertTrue(cardWasNullified(negotiations));

        int personnelStopped = 0;
        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                personnelStopped++;
            }
        }

        assertEquals(0, personnelStopped);
    }

    @Test
    public void overcomeDilemmaTest() throws Exception {
        initializeGame(false, true);
        assertEquals(5, attemptingPersonnel.size());
        attemptMission(P1, _mission);

        // Verify P2 is allowed to select from personnel with Leadership or Diplomacy only
        assertTrue(selectableCardsAre(P2, List.of(jace1, jace2, jace3, taylor)));
        selectCard(P2, taylor);

        // Verify dilemma was overcome
        assertEquals(Zone.REMOVED, negotiations.getZone());
        assertTrue(taylor.isStopped());

        int personnelStopped = 0;
        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                personnelStopped++;
            }
        }

        assertEquals(1, personnelStopped);
    }

    @Test
    public void failDilemmaTest() throws Exception {
        initializeGame(false, false);
        assertEquals(1, attemptingPersonnel.size());
        attemptMission(P1, _mission);

        assertNotEquals(Zone.REMOVED, negotiations.getZone());
        assertTrue(wallace.isStopped());
    }

}