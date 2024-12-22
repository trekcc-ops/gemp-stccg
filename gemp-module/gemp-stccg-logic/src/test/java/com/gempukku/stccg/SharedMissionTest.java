package com.gempukku.stccg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.common.filterable.Zone;
import com.gempukku.stccg.gamestate.MissionLocation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SharedMissionTest extends AbstractAtTest {

    @Test
    public void sharedMissionTest() throws DecisionResultInvalidException, JsonProcessingException {
        initializeSimple1EGameWithSharedMission(30);
        autoSeedMissions();
        assertNotEquals(Phase.SEED_MISSION, _game.getCurrentPhase());
        List<MissionLocation> locations = _game.getGameState().getSpacelineLocations();
        assertEquals(1, locations.size());
        MissionLocation onlyLocation = locations.getFirst();
        assertEquals(2, onlyLocation.getMissions().size());
        ObjectMapper mapper = new ObjectMapper();
        String serializedState = mapper.writeValueAsString(_game.getGameState());
        System.out.println(serializedState);
        assertEquals(Zone.SPACELINE, onlyLocation.getMissions().getLast().getZone());
    }
}