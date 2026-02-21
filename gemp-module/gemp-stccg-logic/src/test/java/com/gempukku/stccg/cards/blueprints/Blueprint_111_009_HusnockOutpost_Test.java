package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.cardgroup.PhysicalCardGroup;
import com.gempukku.stccg.cards.physicalcard.FacilityCard;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class Blueprint_111_009_HusnockOutpost_Test extends AbstractAtTest {

    @Test
    public void seedTest() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        builder.addMission("219_022", "Evade Sensors", P1);
        PhysicalCard outpost = builder.addSeedDeckCard("111_009", "Husnock Outpost", P1);
        PhysicalCard husnockShip1 = builder.addSeedDeckCard("101_353", "Husnock Ship", P1);
        PhysicalCard husnockShip2 = builder.addSeedDeckCard("101_353", "Husnock Ship", P1);
        builder.setPhase(Phase.SEED_FACILITY);
        builder.startGame();
        seedCard(P1, outpost);
        assertTrue(outpost.isInPlay());

        useGameText(P1, outpost);
        assertTrue(getSelectableCards(P1).containsAll(List.of(husnockShip1, husnockShip2)));
        selectCard(P1, husnockShip1);

        // Verify that Husnock Ship can't be seeded twice
        assertThrows(DecisionResultInvalidException.class, () -> useGameText(P1, outpost));
    }

    @Test
    public void twoOutpostsTest() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        MissionCard mission1 = builder.addMission("219_022", "Evade Sensors", P1);
        MissionCard mission2 = builder.addMission("155_049", "Visit Tranquil Colony", P1);
        PhysicalCard outpost1 = builder.addSeedDeckCard("111_009", "Husnock Outpost", P1);
        PhysicalCard outpost2 = builder.addSeedDeckCard("111_009", "Husnock Outpost", P1);
        PhysicalCard husnockShip1 = builder.addSeedDeckCard("101_353", "Husnock Ship", P1);
        PhysicalCard husnockShip2 = builder.addSeedDeckCard("101_353", "Husnock Ship", P1);
        builder.setPhase(Phase.SEED_FACILITY);
        builder.startGame();
        PhysicalCardGroup seedDeck = _game.getGameState().getCardGroup(P1, Zone.SEED_DECK);
        seedCard(P1, outpost1);
        selectCard(P1, mission1);
        assertTrue(outpost1.isInPlay());
        assertFalse(seedDeck.contains(outpost1));

        seedCard(P1, outpost2);
        assertTrue(outpost2.isInPlay());
        assertFalse(seedDeck.contains(outpost2));

        useGameText(P1, outpost1);
        assertTrue(getSelectableCards(P1).containsAll(List.of(husnockShip1, husnockShip2)));
        selectCard(P1, husnockShip1);
        assertTrue(husnockShip1.isInPlay());
        assertFalse(seedDeck.contains(husnockShip1));
        assertFalse(husnockShip2.isInPlay());
        assertTrue(seedDeck.contains(husnockShip2));

        // Verify that
        useGameText(P1, outpost2);
        assertTrue(husnockShip2.isInPlay());
    }

    @Test
    public void shieldsTest() throws Exception {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        MissionCard mission = builder.addMission("219_022", "Evade Sensors", P1);
        FacilityCard outpost = builder.addFacility("111_009", P1, mission);
        ShipCard husnockShip1 = builder.addDockedShip("101_353", "Husnock Ship", P1, outpost);
        builder.setPhase(Phase.SEED_FACILITY);
        builder.startGame();

        assertEquals(22, husnockShip1.getShields(_game));

    }


}