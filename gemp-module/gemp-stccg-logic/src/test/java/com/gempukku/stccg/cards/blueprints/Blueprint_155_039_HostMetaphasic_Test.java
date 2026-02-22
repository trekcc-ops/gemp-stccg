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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Blueprint_155_039_HostMetaphasic_Test extends AbstractAtTest {

    MissionCard hostTest;
    PhysicalCard metaphasic;
    ShipCard runabout1;

    private void initializeGame(String hostTestOwnerName) throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        hostTest = builder.addMission("155_039", "Host Metaphasic Shielding Test", hostTestOwnerName);
        MissionCard mission2 = builder.addMission(MissionType.PLANET, Affiliation.FEDERATION, P1);
        metaphasic = builder.addDrawDeckCard("101_083", "Metaphasic Shields", P1);
        runabout1 = builder.addShipInSpace("101_331", "Runabout", P1, hostTest);
        ShipCard runabout2 = builder.addShipInSpace("101_331", "Runabout", P1, mission2);
        ShipCard opposingRunabout = builder.addShipInSpace("101_331", "Runabout", P2, hostTest);
        builder.addCardAboardShipOrFacility("106_018", "Spock", P1, runabout1, PersonnelCard.class);
        builder.addCardAboardShipOrFacility("101_212", "Geordi La Forge", P1, runabout1, PersonnelCard.class);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void solveYourMissionTest() throws Exception {
        initializeGame(P1);
        attemptMission(P1, hostTest);
        assertTrue(hostTest.isCompleted(_game));
        useGameText(P1, hostTest);
        assertTrue(metaphasic.isInPlay());
        assertEquals(runabout1, metaphasic.getAtopCard());
    }

    @Test
    public void solveOpponentsMissionTest() throws Exception {
        initializeGame(P2);
        attemptMission(P1, hostTest);
        assertTrue(hostTest.isCompleted(_game));
        useGameText(P1, hostTest);
        assertTrue(metaphasic.isInPlay());
        assertEquals(runabout1, metaphasic.getAtopCard());
    }


}