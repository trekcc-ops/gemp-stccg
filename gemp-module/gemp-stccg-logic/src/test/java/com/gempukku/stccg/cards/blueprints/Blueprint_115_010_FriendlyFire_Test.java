package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.GameTestBuilder;
import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.MissionLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("SpellCheckingInspection")
public class Blueprint_115_010_FriendlyFire_Test extends AbstractAtTest {

    private FacilityCard outpost;
    private PersonnelCard picard;
    private MissionCard _mission;
    private PhysicalCard friendly;
    private PersonnelCard troi;
    private PersonnelCard hobson;
    private PersonnelCard data;
    private ShipCard runabout;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        _game = builder.getGame();
        _mission = builder.addMission("101_171", "Investigate Rogue Comet", P1);
        outpost = builder.addFacility("101_104", P1); // Federation Outpost
        friendly = builder.addSeedCardUnderMission("115_010", "Friendly Fire", P2, _mission);
        runabout = builder.addDockedShip("101_331", "Runabout", P1, outpost);
        data = builder.addCardAboardShipOrFacility("101_204", "Data", P1, runabout, PersonnelCard.class);
        troi = builder.addCardAboardShipOrFacility("101_205", "Deanna Troi", P1, runabout, PersonnelCard.class);
        hobson = builder.addCardAboardShipOrFacility("101_202", "Christopher Hobson", P1, runabout, PersonnelCard.class);
        picard = builder.addCardAboardShipOrFacility("101_215", "Jean-Luc Picard", P1, runabout, PersonnelCard.class);
        builder.setPhase(Phase.EXECUTE_ORDERS);
        builder.startGame();
    }

    @Test
    public void failDilemmaTest() throws DecisionResultInvalidException,
            CardNotFoundException, InvalidGameOperationException {

        initializeGame();
        MissionLocation missionLocation = (MissionLocation) _mission.getGameLocation(_game);

        undockShip(P1, runabout);
        assertFalse(friendly.isPlacedOnMission());
        assertEquals(1, missionLocation.getSeedCards().size());
        assertFalse(friendly.isInPlay());

        attemptMission(P1, runabout, _mission);
        assertTrue(friendly.isPlacedOnMission());
        assertEquals(0, missionLocation.getSeedCards().size());
        assertTrue(friendly.isInPlay());

    }

}