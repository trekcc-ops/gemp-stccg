package com.gempukku.stccg;

import com.gempukku.stccg.common.DecisionResultInvalidException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlayerListAtTest extends AbstractAtTest {
    @Test
    public void testAllPlayers() {
        initializeSimple1EGame(30);

        final String[] allPlayers = _game.getAllPlayerIds();
        assertEquals(2, allPlayers.length);
        assertEquals(P1, allPlayers[0]);
        assertEquals(P2, allPlayers[1]);
    }
}