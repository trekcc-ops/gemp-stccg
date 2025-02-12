package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.player.PlayerOrder;
import org.junit.jupiter.api.Test;

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