package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import com.gempukku.stccg.processes.GameProcess;
import com.gempukku.stccg.processes.st1e.ST1EEndOfTurnProcess;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GameProcessSerializerTest extends AbstractAtTest {

    private final ObjectMapper _mapper = new ObjectMapper();

    @Test
    public void serializerTest() throws JsonProcessingException, DecisionResultInvalidException, InvalidGameOperationException {
        initializeQuickMissionAttempt("Investigate Rogue Comet");

        // ST1EFacilitySeedPhaseProcess
        copyProcessAndAssertEqual(_game.getGameState().getCurrentProcess());

        copyProcessAndAssertEqual(new ST1EEndOfTurnProcess());
    }

    private void copyProcessAndAssertEqual(GameProcess process1) throws JsonProcessingException {
        String processString1 = _mapper.writeValueAsString(process1);
        GameProcess process2 = _mapper.readValue(processString1, GameProcess.class);
        String processString2 = _mapper.writeValueAsString(process2);

        assertEquals(processString1, processString2);
        assertEquals(process1.getClass(), process2.getClass());
    }

}