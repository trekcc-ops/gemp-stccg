package com.gempukku.stccg;

import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DoorwaySeedPhaseTest extends AbstractAtTest {

    @Test
    public void doorwayTest() throws DecisionResultInvalidException {
        initializeSimple1EGameWithDoorways(30);
        assertEquals(Phase.SEED_DOORWAY, _game.getCurrentPhase());

        selectCard(P1, _game.getGameState().getSeedDeck(P1).getFirst());
        selectCard(P2, _game.getGameState().getSeedDeck(P2).getFirst());

        assertEquals(Phase.SEED_MISSION, _game.getCurrentPhase());
    }

}