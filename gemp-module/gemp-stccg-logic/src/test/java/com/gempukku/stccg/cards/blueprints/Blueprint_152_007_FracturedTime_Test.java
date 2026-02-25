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
import com.gempukku.stccg.game.GameRandomizer;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class Blueprint_152_007_FracturedTime_Test extends AbstractAtTest {

    private PhysicalCard fracturedTime;
    private MissionCard mission;
    private List<PersonnelCard> attemptingPersonnel;

    private void initializeGame(GameRandomizer randomizer) throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players, randomizer);
        _game = builder.getGame();
        mission = builder.addMission(MissionType.SPACE, Affiliation.FEDERATION, P1);
        fracturedTime = builder.addSeedCardUnderMission("152_007", "Fractured Time", P1, mission);
        attemptingPersonnel = new ArrayList<>();
        ShipCard runabout = builder.addShipInSpace("101_331", "Runabout", P1, mission);

        for (int i = 0; i < 20; i++) {
            attemptingPersonnel.add(builder.addCardAboardShipOrFacility(
                    "101_220", "Linda Larson", P1, runabout, PersonnelCard.class));
        }
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void removeCardsFromGameTest() throws Exception {
        GameRandomizer randomizer = Mockito.mock(GameRandomizer.class);
        initializeGame(randomizer);
        Mockito.when(randomizer.getRandomItemsFromList(attemptingPersonnel, 9))
                .thenReturn(attemptingPersonnel.subList(0,9));

        attemptMission(P1, mission);

        for (PersonnelCard removedPersonnel : attemptingPersonnel.subList(0,9)) {
            assertNotEquals(Zone.REMOVED, removedPersonnel.getZone());
        }
        for (PersonnelCard removedPersonnel : attemptingPersonnel.subList(9,20)) {
            assertEquals(Zone.REMOVED, removedPersonnel.getZone());
        }
        assertEquals(Zone.REMOVED, fracturedTime.getZone());
    }

}