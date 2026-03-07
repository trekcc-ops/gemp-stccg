package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.GameTestBuilder;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_155_011_Temptation_Test extends AbstractAtTest {

    private MissionCard _mission;
    private Collection<PersonnelCard> attemptingPersonnel;
    private PhysicalCard temptation;

    private void initializeGame(int honor, int treachery, int neither) throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        attemptingPersonnel = new ArrayList<>();
        _mission = builder.addMission(MissionType.PLANET, Affiliation.KLINGON, P1);
        temptation = builder.addSeedCardUnderMission("155_011", "Temptation", P2, _mission);

        // Gorath has Honor
        for (int i = 0; i < honor; i++) {
            attemptingPersonnel.add(builder.addCardOnPlanetSurface(
                    "101_260", "Gorath", P1, _mission, PersonnelCard.class));
        }

        // Zegov has Treachery
        for (int i = 0; i < treachery; i++) {
            attemptingPersonnel.add(builder.addCardOnPlanetSurface(
                    "155_090", "Zegov", P1, _mission, PersonnelCard.class));
        }

        // Kromm has neither
        for (int i = 0; i < neither; i++) {
            attemptingPersonnel.add(builder.addCardOnPlanetSurface(
                    "101_276", "Kromm", P1, _mission, PersonnelCard.class));
        }

        builder.setPhase(Phase.EXECUTE_ORDERS);
        _game = builder.startGame();
    }

    @Test
    public void failDilemmaTest() throws Exception {
        // If you don't have 3 personnel, dilemma is failed
        initializeGame(1,1,0);
        attemptMission(P1, _mission);

        for (PersonnelCard personnel : attemptingPersonnel) {
            assertTrue(personnel.isStopped());
        }

        assertNotEquals(Zone.REMOVED, temptation.getZone());
    }

    @Test
    public void passDilemmaWithStopsTest() throws Exception {
        // Nobody in Away Team has Honor or Treachery, so all 3 personnel selected are stopped
        initializeGame(0,0,5);
        attemptMission(P1, _mission);

        int stoppedPersonnel = 0;

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                stoppedPersonnel++;
            }
        }

        assertEquals(3, stoppedPersonnel);
        assertEquals(Zone.REMOVED, temptation.getZone());
    }

    @Test
    public void passDilemmaWithNoStopsTest() throws Exception {
        // Everybody in Away Team has Honor or Treachery, so nobody is stopped
        initializeGame(2,2,0);
        attemptMission(P1, _mission);

        int stoppedPersonnel = 0;

        for (PersonnelCard personnel : attemptingPersonnel) {
            if (personnel.isStopped()) {
                stoppedPersonnel++;
            }
        }

        assertEquals(0, stoppedPersonnel);
        assertEquals(Zone.REMOVED, temptation.getZone());
    }

}