package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.actions.missionattempt.AttemptMissionAction;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.SkillName;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_139_013_PickingUpThePieces_Test extends AbstractAtTest {

    private MissionCard _mission;
    private PhysicalCard pickingUp;
    private PersonnelCard troi;
    private PersonnelCard larson;
    private ShipCard runabout;
    private PersonnelCard taitt;
    private PersonnelCard marruu;
    private Collection<PersonnelCard> attemptingPersonnel = new ArrayList<>();

    private void initializeGame(int geologyOrComputerSkillPersonnel) throws InvalidGameOperationException, CardNotFoundException {
        attemptingPersonnel.clear();
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_154", "Excavation", P1);
        pickingUp = builder.addSeedCardUnderMission("139_013", "Picking Up the Pieces", P2, _mission);
        troi = builder.addCardOnPlanetSurface(
                "101_205", "Deanna Troi", P1, _mission, PersonnelCard.class);
        attemptingPersonnel.add(troi);
        if (geologyOrComputerSkillPersonnel >= 1) {
            taitt = builder.addCardOnPlanetSurface(
                    "101_242", "Taitt", P1, _mission, PersonnelCard.class);
            attemptingPersonnel.add(taitt);
        }
        if (geologyOrComputerSkillPersonnel >= 2) {
            marruu = builder.addCardOnPlanetSurface(
                    "163_031", "Marruu", P1, _mission, PersonnelCard.class);
            attemptingPersonnel.add(marruu);
            for (int i = 2; i < geologyOrComputerSkillPersonnel; i++) {
                PersonnelCard newCard = builder.addCardOnPlanetSurface(
                        "163_031", "Marruu", P1, _mission, PersonnelCard.class);
                attemptingPersonnel.add(newCard);
            }
        }
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void failDilemmaTest() throws Exception {
        initializeGame(0);
        assertEquals(1, attemptingPersonnel.size());
        AttemptMissionAction action = attemptMission(P1, _mission);
        assertTrue(troi.isStopped());
        assertNotEquals(Zone.REMOVED, pickingUp.getZone());
        assertTrue(action.wasFailed());
        assertTrue(_game.getActionsEnvironment().getActionStack().isEmpty());
    }

    @Test
    public void overcomeDilemmaTest() throws Exception {
        initializeGame(1);
        assertEquals(2, attemptingPersonnel.size());
        AttemptMissionAction action = attemptMission(P1, _mission);
        assertEquals(Zone.REMOVED, pickingUp.getZone());
        assertFalse(troi.isStopped());
        assertTrue(taitt.isStopped());
    }

    @Test
    public void overcomeWithTwoSkilledPersonnelTest() throws Exception {
        initializeGame(2);
        assertEquals(3, attemptingPersonnel.size());
        AttemptMissionAction action = attemptMission(P1, _mission);
        assertEquals(Zone.REMOVED, pickingUp.getZone());

        int stoppedCount = 0;
        int skilledCount = 0;

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                stoppedCount++;
            }
            if (personnel.hasSkill(SkillName.GEOLOGY, _game) || personnel.hasSkill(SkillName.COMPUTER_SKILL, _game)) {
                skilledCount++;
            }
        }

        assertEquals(1, stoppedCount);
        assertEquals(2, skilledCount);
    }

    @Test
    public void overcomeWithThreeSkilledPersonnelTest() throws Exception {
        initializeGame(3);
        AttemptMissionAction action = attemptMission(P1, _mission);
        assertEquals(Zone.REMOVED, pickingUp.getZone());
        assertEquals(4, attemptingPersonnel.size());

        int stoppedCount = 0;
        int skilledCount = 0;

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                stoppedCount++;
            }
            if (personnel.hasSkill(SkillName.GEOLOGY, _game) || personnel.hasSkill(SkillName.COMPUTER_SKILL, _game)) {
                skilledCount++;
            }
        }

        assertEquals(2, stoppedCount);
        assertEquals(3, skilledCount);
    }


}