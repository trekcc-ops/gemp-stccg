package com.gempukku.stccg.at;

import com.gempukku.stccg.decisions.DecisionResultInvalidException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlayerListAtTest extends AbstractAtTest {
    @Test
    public void testAllPlayers() throws DecisionResultInvalidException {
        initializeSimplestGame();
        skipMulligans();

        final String[] allPlayers = _game.getAllPlayerIds();
        assertEquals(2, allPlayers.length);
        assertEquals(P1, allPlayers[0]);
        assertEquals(P2, allPlayers[1]);
    }
}
