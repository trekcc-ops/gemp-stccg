package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_152_003_Dedication_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private MissionCard _mission;
    private PhysicalCard dedication;
    private PersonnelCard troi;
    private ShipCard runabout;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_171", "Investigate Rogue Comet", P1);
        outpost = builder.addFacility("101_104", P1); // Federation Outpost
        dedication = builder.addSeedCardUnderMission("152_003", "Dedication to Duty", P2, _mission);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        runabout = builder.addDockedShip("101_331", "Runabout", P1, outpost);
        troi = builder.addCardAboardShipOrFacility("101_205", "Deanna Troi", P1, runabout, PersonnelCard.class);
        builder.startGame();
    }

    @Test
    public void firstOptionTest() throws Exception {
        initializeGame();

        undockShip(P1, runabout);
        assertFalse(runabout.isDocked());

        attemptMission(P1, runabout, _mission);
        assertTrue(troi.isStopped());

        playerDecided(P1, "0");
        assertFalse(runabout.hasCardInCrew(troi));
        assertEquals(Zone.DISCARD, troi.getZone());
        assertFalse(runabout.isStopped());
    }

    @Test
    public void secondOptionTest() throws Exception {
        initializeGame();

        undockShip(P1, runabout);
        assertFalse(runabout.isDocked());

        attemptMission(P1, runabout, _mission);
        assertTrue(troi.isStopped());
        int handSizeBefore = _game.getPlayer(P2).getCardsInHand().size();

        // Player 2 draws two cards (one for each of Troi's skill dots)
        playerDecided(P1, "1");
        int handSizeAfter = _game.getPlayer(P2).getCardsInHand().size();

        assertTrue(runabout.hasCardInCrew(troi));
        assertEquals(handSizeBefore, handSizeAfter - 2);
        assertFalse(runabout.isStopped());
    }

}