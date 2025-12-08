package com.gempukku.stccg.gamestate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.player.Player;
import com.gempukku.stccg.player.PlayerNotFoundException;
import org.junit.jupiter.api.Test;

public class PlayerSerializerTest extends AbstractAtTest {

    @Test
    public void test() throws PlayerNotFoundException, JsonProcessingException {
        initializeSimple1EGame(50);
        Player player1 = _game.getPlayer(P1);
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = mapper.writeValueAsString(player1);
        System.out.println(jsonString);
        Player playerCopy = mapper.readValue(jsonString, Player.class);
    }
}