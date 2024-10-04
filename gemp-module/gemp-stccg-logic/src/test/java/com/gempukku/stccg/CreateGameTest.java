package com.gempukku.stccg;

import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CreateGameTest extends AbstractAtTest {

    @Test
    public void introTwoPlayerGameTest() throws DecisionResultInvalidException {
        initializeIntroductoryTwoPlayerGame();

        // Figure out which player is going first
        String player1 = _game.getGameState().getPlayerOrder().getFirstPlayer();
        String player2 = _game.getOpponent(player1);

        // The game should start in the mission seed phase, with only the first player having an awaiting decision
        assertNotNull(_userFeedback.getAwaitingDecision(player1));
        assertNull(_userFeedback.getAwaitingDecision(player2));
        assertEquals(Phase.SEED_MISSION, _game.getGameState().getCurrentPhase());

        playerDecided(player1, "0"); // This should seed the first mission, then seeding should advance to player2

        assertNotNull(_userFeedback.getAwaitingDecision(player2));
        assertNull(_userFeedback.getAwaitingDecision(player1));
        assertEquals(Phase.SEED_MISSION, _game.getGameState().getCurrentPhase());
        assertEquals(1, _game.getGameState().getSpacelineLocations().size());

        // Player2 will need to decide twice - once to select the mission, then once to decide where to seed it
        playerDecided(player2, "0");
        assertNotNull(_userFeedback.getAwaitingDecision(player2));
        playerDecided(player2, "0");

        assertNotNull(_userFeedback.getAwaitingDecision(player1));
        assertNull(_userFeedback.getAwaitingDecision(player2));
        assertEquals(Phase.SEED_MISSION, _game.getGameState().getCurrentPhase());
        assertEquals(2, _game.getGameState().getSpacelineLocations().size());
    }

}