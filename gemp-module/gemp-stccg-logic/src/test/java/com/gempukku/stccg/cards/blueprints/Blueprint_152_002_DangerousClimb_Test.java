package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class Blueprint_152_002_DangerousClimb_Test extends AbstractAtTest {

    private PersonnelCard wilkins;
    private MissionCard _mission;
    private PhysicalCard dangerousClimb;
    private PersonnelCard wallace;
    private Collection<PersonnelCard> personnelAttempting = new ArrayList<>();

    private void initializeGame(boolean includeGeology)
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_154", "Excavation", P1);
        dangerousClimb = builder.addSeedCardUnderMission("152_002", "Dangerous Climb", P2, _mission);
        wilkins = builder.addCardOnPlanetSurface("109_111", "Richard Wilkins", P1, _mission, PersonnelCard.class);
        wallace = builder.addCardOnPlanetSurface("101_203", "Darian Wallace", P1, _mission, PersonnelCard.class);
        personnelAttempting.addAll(List.of(wilkins, wallace));

        if (includeGeology) {
            personnelAttempting.add(builder.addCardOnPlanetSurface(
                    "101_242", "Taitt", P1, _mission, PersonnelCard.class));
            personnelAttempting.add(builder.addCardOnPlanetSurface(
                    "101_242", "Taitt", P1, _mission, PersonnelCard.class));
        }

        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }


    @Test
    public void failDilemmaTest() throws Exception {
        // Wilkins and Wallace don't have the requirements, so one of them is killed
        initializeGame(false);
        assertEquals(2, personnelAttempting.size());
        attemptMission(P1, _mission);
        assertNotEquals(Zone.REMOVED, dangerousClimb.getZone());

        int stoppedPersonnel = 0;
        int killedPersonnel = 0;

        for (PersonnelCard personnelCard : personnelAttempting) {
            if (personnelWasKilled(personnelCard)) {
                killedPersonnel++;
            }
            if (personnelCard.isStopped()) {
                stoppedPersonnel++;
            }
        }

        assertEquals(1, stoppedPersonnel);
        assertEquals(1, killedPersonnel);
    }

    @Test
    public void passDilemmaTest() throws Exception {
        // Away Team has 2 Geology + CUNNING>20
        initializeGame(true);
        assertEquals(4, personnelAttempting.size());
        attemptMission(P1, _mission);
        assertEquals(Zone.REMOVED, dangerousClimb.getZone());

        int stoppedPersonnel = 0;
        int killedPersonnel = 0;

        for (PersonnelCard personnelCard : personnelAttempting) {
            if (personnelWasKilled(personnelCard)) {
                killedPersonnel++;
            }
            if (personnelCard.isStopped()) {
                stoppedPersonnel++;
            }
        }

        assertEquals(0, stoppedPersonnel);
        assertEquals(0, killedPersonnel);
    }

}