package com.gempukku.stccg.rules;

import com.gempukku.stccg.AbstractAtTest;
import com.gempukku.stccg.cards.physicalcard.PhysicalCard;
import com.gempukku.stccg.common.DecisionResultInvalidException;
import com.gempukku.stccg.common.filterable.Phase;
import com.gempukku.stccg.decisions.CardActionSelectionDecision;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NormalCardPlayTest extends AbstractAtTest {

    @Test
    public void normalCardPlayTest() throws DecisionResultInvalidException {
        initializeQuickMissionAttempt("Excavation");

        // Seed Federation Outpost
        seedFacility(P1, _outpost, _mission.getLocation());
        assertEquals(_outpost.getLocation(), _mission.getLocation());
        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());

        PhysicalCard tarses1 = _game.getGameState().getHand(P1).get(1);
        assertEquals("Simon Tarses", tarses1.getTitle());
        reportCard(P1, tarses1, _outpost);
        assertTrue(_outpost.getCrew().contains(tarses1));

        assertEquals(Phase.CARD_PLAY, _game.getCurrentPhase());
        assertTrue(_userFeedback.getAwaitingDecision(P1) instanceof CardActionSelectionDecision);

        if (_userFeedback.getAwaitingDecision(P1) instanceof CardActionSelectionDecision decision)
            assertTrue(decision.getActions().isEmpty());
    }

}