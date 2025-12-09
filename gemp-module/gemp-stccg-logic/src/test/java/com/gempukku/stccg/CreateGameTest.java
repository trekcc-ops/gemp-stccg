package com.gempukku.stccg;

import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.game.InvalidGameLogicException;
import com.gempukku.stccg.game.InvalidGameOperationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CreateGameTest extends AbstractAtTest {

    @Test
    public void introTwoPlayerGameTest() throws DecisionResultInvalidException, InvalidGameOperationException {
        initializeIntroductoryTwoPlayerGame();

        // Figure out which player is going first
        String player1 = _game.getGameState().getPlayerOrder().getFirstPlayer();
        String player2 = _game.getOpponent(player1);

        // The game should start in the mission seed phase, with only the first player having an awaiting decision
        assertNotNull(_game.getAwaitingDecision(player1));
        assertNull(_game.getAwaitingDecision(player2));
        assertEquals(Phase.SEED_MISSION, _game.getGameState().getCurrentPhase());

        selectFirstAction(player1); // This should seed the first mission, then seeding should advance to player2

        assertNotNull(_game.getAwaitingDecision(player2));
        assertNull(_game.getAwaitingDecision(player1));
        assertEquals(Phase.SEED_MISSION, _game.getGameState().getCurrentPhase());
        assertEquals(1, _game.getGameState().getSpacelineLocations().size());

        // Player2 will need to decide twice - once to select the mission, then once to decide where to seed it
        selectFirstAction(player2);
        assertNotNull(_game.getAwaitingDecision(player2));
        playerDecided(player2, "0");

        assertNotNull(_game.getAwaitingDecision(player1));
        assertNull(_game.getAwaitingDecision(player2));
        assertEquals(Phase.SEED_MISSION, _game.getGameState().getCurrentPhase());
        assertEquals(2, _game.getGameState().getSpacelineLocations().size());
    }

}