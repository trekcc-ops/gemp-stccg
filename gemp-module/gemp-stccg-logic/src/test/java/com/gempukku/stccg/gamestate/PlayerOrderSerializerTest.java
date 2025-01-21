package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.PlayerOrder;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerOrderSerializerTest extends AbstractAtTest {

    @Test
    public void serializerTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        List<String> players = List.of("player1", "player2", "player3");
        PlayerOrder playerOrder1 = new PlayerOrder(players);
        String playerOrderString1 = mapper.writeValueAsString(playerOrder1);

        PlayerOrder playerOrder2 = mapper.readValue(playerOrderString1, PlayerOrder.class);
        String playerOrderString2 = mapper.writeValueAsString(playerOrder2);

        assertEquals(playerOrderString1, playerOrderString2);
    }

}