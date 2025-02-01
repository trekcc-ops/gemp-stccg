package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.PlayerOrder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PhaseSerializerTest extends AbstractAtTest {

    @Test
    public void serializerTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        Phase[] phases1 = Phase.values();

        String phaseString1 = mapper.writeValueAsString(phases1);
        Phase[] phases2 = mapper.readValue(phaseString1, Phase[].class);
        String phaseString2 = mapper.writeValueAsString(phases2);

        assertEquals(phaseString1, phaseString2);
    }

}