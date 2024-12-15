package com.gempukku.stccg.cards.blueprints;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Blueprint_156_010_Surprise_Party_Test extends AbstractAtTest {

    @Test
    public void surprisePartyTest() throws DecisionResultInvalidException {
        initializeSimple1EGame(40, "156_010"); // Give both players decks full of Surprise Party
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());
        assertEquals(7, _game.getGameState().getHand(P1).size());
        assertEquals(7, _game.getGameState().getHand(P2).size());

        PhysicalCard party = _game.getGameState().getHand(P1).getFirst();
        assertEquals("Surprise Party", party.getTitle());

        playCard(P1, party);
        assertEquals(6, _game.getGameState().getHand(P1).size());
        assertNotNull(_userFeedback.getAwaitingDecision(P2));

        chooseOnlyAction(P2);
        assertEquals(9, _game.getGameState().getHand(P2).size()); // Verify that P2 drew 2 cards

        skipCardPlay();
        skipExecuteOrders();
        assertEquals(7, _game.getGameState().getHand(P1).size()); // Verify that P1 took their normal card draw

        assertNotNull(_userFeedback.getAwaitingDecision(P1)); // Verify that P1 has an optional action
        chooseOnlyAction(P1);
        assertEquals(8, _game.getGameState().getHand(P1).size()); // Verify that P1 took another card draw
    }

}