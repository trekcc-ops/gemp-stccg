package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.game.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Affiliation;
import com.gempukku.stccg.common.filterable.MissionType;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_161_006_InvidiumLeak_Test extends AbstractAtTest {
    private MissionCard _mission;
    private PhysicalCard invidiumLeak;
    private ShipCard runabout;
    private List<PersonnelCard> attemptingPersonnel;

    private void initializeGame(boolean overcomeDilemma) throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission(MissionType.SPACE, Affiliation.FEDERATION, P1);
        invidiumLeak = builder.addSeedCardUnderMission("161_006", "Invidium Leak", P2, _mission);
        runabout = builder.addShipInSpace("101_331", "Runabout", P1, _mission);

        // Alexana Devos - MEDICAL + CUNNING=8
        // Data- no MEDICAL, CUNNING=12
        // Simon Tarses - MEDICAL, CUNNING=5
        attemptingPersonnel = new ArrayList<>();

        attemptingPersonnel.add(builder.addCardAboardShipOrFacility(
                "101_204", "Data", P1, runabout, PersonnelCard.class));
        attemptingPersonnel.add(builder.addCardAboardShipOrFacility(
                "101_236", "Simon Tarses", P1, runabout, PersonnelCard.class));

        if (overcomeDilemma) {
            attemptingPersonnel.add(builder.addCardAboardShipOrFacility(
                    "204_020", "Alexana Devos", P1, runabout, PersonnelCard.class));
        }
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }


    @Test
    public void failDilemmaTest() throws Exception {

        initializeGame(false);
        attemptMission(P1, _mission);


        for (PersonnelCard personnel : attemptingPersonnel) {
            assertTrue(personnel.isStopped());
        }
        assertTrue(runabout.isStopped());
        assertNotEquals(Zone.REMOVED, invidiumLeak.getZone());
    }

    @Test
    public void passDilemmaTest() throws Exception {

        initializeGame(true);
        attemptMission(P1, _mission);


        for (PersonnelCard personnel : attemptingPersonnel) {
            assertFalse(personnel.isStopped());
        }
        assertFalse(runabout.isStopped());
        assertEquals(Zone.REMOVED, invidiumLeak.getZone());
    }

}