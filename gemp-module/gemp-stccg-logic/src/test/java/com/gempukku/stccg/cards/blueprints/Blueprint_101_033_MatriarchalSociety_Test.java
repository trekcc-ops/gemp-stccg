package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.GameTestBuilder;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_101_033_MatriarchalSociety_Test extends AbstractAtTest {

    private MissionCard _mission;
    private PhysicalCard matriarchal;
    private List<PersonnelCard> attemptingPersonnel;

    private void initializeGame(int femalesToBring)
            throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _mission = builder.addMission("101_154", "Excavation", P1);
        matriarchal = builder.addSeedCardUnderMission("101_033", "Matriarchal Society", P1, _mission);
        attemptingPersonnel = new ArrayList<>();

        for (int i = 0; i < femalesToBring; i++) {
            attemptingPersonnel.add(builder.addCardOnPlanetSurface(
                    "101_201", "Calloway", P1, _mission, PersonnelCard.class));
        }
        attemptingPersonnel.add(builder.addCardOnPlanetSurface(
                "101_204", "Data", P1, _mission, PersonnelCard.class));

        builder.setPhase(Phase.EXECUTE_ORDERS);
        _game = builder.startGame();
    }


    @Test
    public void failDilemmaTest() throws Exception {
        initializeGame(1);
        attemptMission(P1, _mission);

        for (PersonnelCard personnel : attemptingPersonnel) {
            assertTrue(personnel.isStopped());
        }

        assertNotEquals(Zone.REMOVED, matriarchal.getZone());
    }

    @Test
    public void passDilemmaTest() throws Exception {
        initializeGame(2);
        attemptMission(P1, _mission);

        for (PersonnelCard personnel : attemptingPersonnel) {
            assertFalse(personnel.isStopped());
        }

        assertEquals(Zone.REMOVED, matriarchal.getZone());
    }


}