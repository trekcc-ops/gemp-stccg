package com.gempukku.stccg;

import com.gempukku.stccg.cards.CardNotFoundException;
import com.gempukku.stccg.cards.physicalcard.*;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.gamestate.MissionLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SharedMissionTest extends AbstractAtTest {

    private MissionCard mission1;
    private MissionCard mission2;

    private void initializeGame() throws InvalidGameOperationException, CardNotFoundException {
        GameTestBuilder builder = new GameTestBuilder(_cardLibrary, formatLibrary, _players);
        mission1 = builder.addMissionToDeck("101_154", "Excavation", P1);
        mission2 = builder.addMissionToDeck("101_154", "Excavation", P2);
        _game = builder.getGame();
        builder.setPhase(Phase.SEED_MISSION);
        builder.startGame();
    }


    @Test
    public void sharedMissionTest() throws DecisionResultInvalidException, InvalidGameOperationException, CardNotFoundException {
        initializeGame();
        assertFalse(mission1.isInPlay());
        seedMission(mission1);
        assertTrue(mission1.isInPlay());

        seedMission(mission2);
        assertTrue(mission2.isInPlay());

        assertNotEquals(Phase.SEED_MISSION, _game.getCurrentPhase());
        List<MissionLocation> locations = _game.getGameState().getUnorderedMissionLocations();
        assertEquals(1, locations.size());
        MissionLocation onlyLocation = locations.getFirst();
        assertEquals(2, onlyLocation.getMissionCards().size());
        assertEquals(Zone.SPACELINE, onlyLocation.getMissionCards().getLast().getZone());
    }
}