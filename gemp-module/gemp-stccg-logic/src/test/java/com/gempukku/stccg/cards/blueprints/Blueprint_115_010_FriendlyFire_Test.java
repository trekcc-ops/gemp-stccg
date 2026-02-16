package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.MissionCard;
import com.gempukku.stccg.cards.physicalcard.PersonnelCard;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.cards.physicalcard.ShipCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.MissionLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_115_010_FriendlyFire_Test extends AbstractAtTest {

    private MissionCard _mission;
    private PhysicalCard friendly;
    private ShipCard runabout;
    private PersonnelCard taylor;
    private PersonnelCard taitt;
    private PersonnelCard taylor2;

    private void initializeGame(boolean passDilemma) throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        builder.addMission("101_154", "Excavation", P1);
        _mission = builder.addMission("101_171", "Investigate Rogue Comet", P1);
        friendly = builder.addSeedCardUnderMission("115_010", "Friendly Fire", P2, _mission);
        runabout = builder.addShipInSpace("101_331", "Runabout", P1, _mission);
        taylor = builder.addCardAboardShipOrFacility("112_215", "Taylor Moore", P1, runabout, PersonnelCard.class);
        taitt = builder.addCardAboardShipOrFacility(
                "101_242", "Taitt", P1, runabout, PersonnelCard.class);
        if (passDilemma) {
            taylor2 = builder.addCardAboardShipOrFacility("112_215", "Taylor Moore", P1, runabout, PersonnelCard.class);
        }
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void failDilemmaTest() throws DecisionResultInvalidException,
            CardNotFoundException, InvalidGameOperationException {

        initializeGame(false);
        MissionLocation missionLocation = (MissionLocation) _mission.getGameLocation(_game);

        assertFalse(friendly.isPlacedOnMission());
        assertEquals(1, missionLocation.getSeedCards().size());
        assertFalse(friendly.isInPlay());

        attemptMission(P1, _mission);
        assertTrue(friendly.isPlacedOnMission());
        assertEquals(0, missionLocation.getSeedCards().size());
        assertEquals(Zone.AT_LOCATION, friendly.getZone());
        assertTrue(friendly.isInPlay());
        assertEquals(friendly.getLocationId(), _mission.getLocationId());

        int stoppedPersonnel = 0;
        int killedPersonnel = 0;

        for (PersonnelCard personnel : List.of(taylor, taitt)) {
            if (personnel.isStopped()) {
                stoppedPersonnel++;
            }
            if (personnelWasKilled(personnel)) {
                killedPersonnel++;
            }
        }
        assertEquals(1, stoppedPersonnel);
        assertEquals(1, killedPersonnel);
    }

    @Test
    public void passDilemmaTest() throws DecisionResultInvalidException,
            CardNotFoundException, InvalidGameOperationException {

        initializeGame(true);
        MissionLocation missionLocation = (MissionLocation) _mission.getGameLocation(_game);

        assertFalse(friendly.isPlacedOnMission());
        assertEquals(1, missionLocation.getSeedCards().size());
        assertFalse(friendly.isInPlay());

        attemptMission(P1, _mission);
        assertFalse(friendly.isPlacedOnMission());
        assertEquals(0, missionLocation.getSeedCards().size());
        assertEquals(Zone.REMOVED, friendly.getZone());
        assertFalse(friendly.isInPlay());

        // Verify that mission can be re-attempted
        assertDoesNotThrow(() -> attemptMission(P1, _mission));
    }

    @Test
    public void longTermEffectsTest() throws DecisionResultInvalidException,
            CardNotFoundException, InvalidGameOperationException {

        initializeGame(false);
        attemptMission(P1, _mission);
        assertTrue(friendly.isPlacedOnMission());
        skipToNextTurnAndPhase(P1, Phase.EXECUTE_ORDERS);
        assertThrows(DecisionResultInvalidException.class, () -> attemptMission(P1, _mission));
    }

}