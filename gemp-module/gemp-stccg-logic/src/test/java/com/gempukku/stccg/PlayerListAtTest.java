package com.gempukku.stccg;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerListAtTest extends AbstractAtTest {
    @Test
    public void testAllPlayers() throws Exception {
        initializeSimple1EGame(30);

        final String[] allPlayers = _game.getAllPlayerIds();
        assertEquals(2, allPlayers.length);
        assertEquals(P1, allPlayers[0]);
        assertEquals(P2, allPlayers[1]);
    }
}